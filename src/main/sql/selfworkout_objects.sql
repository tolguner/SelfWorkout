/* =====================================================================
   SelfWorkout - Veritabani nesneleri
   ---------------------------------------------------------------------
   View, fonksiyon, trigger ve saklanan yordamlar.
   Once selfworkout.sql calistirilmali (semayi o kurar).

   Betik tekrar tekrar calistirilabilir; her nesne once varsa dusurulur.
   ===================================================================== */

USE selfworkout;
GO

/* =====================================================================
   1. GORUNUMLER (VIEW)
   ===================================================================== */

-- Tamamlanan her antrenmanin ozeti: sure, egzersiz sayisi, set sayisi
-- ve toplam hacim (tekrar x agirlik).
DROP VIEW IF EXISTS vw_UserWorkoutSummary;
GO
CREATE VIEW vw_UserWorkoutSummary AS
SELECT
    uw.id                                   AS user_workout_id,
    uw.user_id,
    u.username,
    uw.workout_date,
    uw.workout_type,
    uw.status,
    uw.duration_minutes,
    COUNT(DISTINCT uwe.exercise_id)         AS exercise_count,
    COUNT(uwe.id)                           AS total_sets,
    CAST(ISNULL(SUM(uwe.reps * uwe.weight), 0) AS DECIMAL(12, 2)) AS total_volume
FROM UserWorkouts uw
INNER JOIN Users u ON u.id = uw.user_id
LEFT JOIN UserWorkoutExercises uwe ON uwe.user_workout_id = uw.id
GROUP BY
    uw.id, uw.user_id, u.username, uw.workout_date,
    uw.workout_type, uw.status, uw.duration_minutes;
GO

-- Egzersiz katalogu: her egzersizin hedefledigi kas gruplari ve
-- gerektirdigi ekipmanlar tek satirda toplanir.
DROP VIEW IF EXISTS vw_ExerciseCatalog;
GO
CREATE VIEW vw_ExerciseCatalog AS
SELECT
    e.id                AS exercise_id,
    e.name              AS exercise_name,
    e.difficulty_level,
    e.description,
    -- STRING_AGG kullaniliyor (SQL Server 2017+). FOR XML PATH yontemi
    -- QUOTED_IDENTIFIER ayarina bagimli oldugu icin tercih edilmedi.
    (SELECT STRING_AGG(mg.name, ', ') WITHIN GROUP (ORDER BY mg.name)
       FROM ExerciseMuscles em
       INNER JOIN MuscleGroups mg ON mg.id = em.muscle_id
      WHERE em.exercise_id = e.id) AS muscle_groups,
    (SELECT STRING_AGG(eq.name, ', ') WITHIN GROUP (ORDER BY eq.name)
       FROM ExerciseEquipments ee
       INNER JOIN Equipments eq ON eq.id = ee.equipment_id
      WHERE ee.exercise_id = e.id) AS equipments
FROM Exercises e;
GO

-- Kullanicinin aylik ilerlemesi: antrenman sayisi, toplam hacim,
-- ortalama sure. Grafik ekranlarini beslemek icin.
DROP VIEW IF EXISTS vw_UserMonthlyProgress;
GO
CREATE VIEW vw_UserMonthlyProgress AS
SELECT
    uw.user_id,
    YEAR(uw.workout_date)                   AS yil,
    MONTH(uw.workout_date)                  AS ay,
    COUNT(DISTINCT uw.id)                   AS workout_count,
    CAST(ISNULL(SUM(uwe.reps * uwe.weight), 0) AS DECIMAL(12, 2)) AS total_volume,
    AVG(CAST(uw.duration_minutes AS DECIMAL(10, 2))) AS avg_duration_minutes
FROM UserWorkouts uw
LEFT JOIN UserWorkoutExercises uwe ON uwe.user_workout_id = uw.id
WHERE uw.status = 'completed'
GROUP BY uw.user_id, YEAR(uw.workout_date), MONTH(uw.workout_date);
GO

-- En cok kullanilan egzersizler. Yonetici raporlari icin.
DROP VIEW IF EXISTS vw_PopularExercises;
GO
CREATE VIEW vw_PopularExercises AS
SELECT
    e.id                AS exercise_id,
    e.name              AS exercise_name,
    e.difficulty_level,
    (SELECT COUNT(*) FROM UserWorkoutExercises uwe WHERE uwe.exercise_id = e.id) AS usage_count,
    (SELECT COUNT(*) FROM FavoriteExercises fe  WHERE fe.exercise_id  = e.id) AS favorite_count,
    (SELECT COUNT(*) FROM RoutineExercises re   WHERE re.exercise_id  = e.id) AS routine_count
FROM Exercises e;
GO

-- Vucut olcumleri ve bir onceki olcume gore degisim.
DROP VIEW IF EXISTS vw_BodyStatsTrend;
GO
CREATE VIEW vw_BodyStatsTrend AS
SELECT
    bs.id,
    bs.user_id,
    bs.record_date,
    bs.weight,
    bs.body_fat,
    bs.muscle_mass,
    bs.weight     - LAG(bs.weight)     OVER (PARTITION BY bs.user_id ORDER BY bs.record_date) AS weight_change,
    bs.body_fat   - LAG(bs.body_fat)   OVER (PARTITION BY bs.user_id ORDER BY bs.record_date) AS body_fat_change,
    bs.muscle_mass - LAG(bs.muscle_mass) OVER (PARTITION BY bs.user_id ORDER BY bs.record_date) AS muscle_mass_change
FROM BodyStats bs;
GO


/* =====================================================================
   2. FONKSIYONLAR (UDF)
   ===================================================================== */

-- Bir antrenmanin toplam hacmi (tekrar x agirlik).
DROP FUNCTION IF EXISTS fn_WorkoutVolume;
GO
CREATE FUNCTION fn_WorkoutVolume (@user_workout_id INT)
RETURNS DECIMAL(12, 2)
AS
BEGIN
    DECLARE @volume DECIMAL(12, 2);

    SELECT @volume = ISNULL(SUM(reps * weight), 0)
    FROM UserWorkoutExercises
    WHERE user_workout_id = @user_workout_id;

    RETURN ISNULL(@volume, 0);
END;
GO

-- Epley formuluyle tahmini tek tekrar maksimumu (1RM):
--   1RM = agirlik x (1 + tekrar / 30)
-- Kullanicinin o egzersizdeki en iyi setinden hesaplanir.
DROP FUNCTION IF EXISTS fn_EstimatedOneRepMax;
GO
CREATE FUNCTION fn_EstimatedOneRepMax (@user_id INT, @exercise_id INT)
RETURNS DECIMAL(6, 2)
AS
BEGIN
    DECLARE @one_rm DECIMAL(6, 2);

    SELECT TOP 1 @one_rm = CAST(uwe.weight * (1 + uwe.reps / 30.0) AS DECIMAL(6, 2))
    FROM UserWorkoutExercises uwe
    INNER JOIN UserWorkouts uw ON uw.id = uwe.user_workout_id
    WHERE uw.user_id = @user_id
      AND uwe.exercise_id = @exercise_id
      AND uwe.weight IS NOT NULL
      AND uwe.reps IS NOT NULL
    ORDER BY uwe.weight * (1 + uwe.reps / 30.0) DESC;

    RETURN ISNULL(@one_rm, 0);
END;
GO

-- Kullanicinin GUNCEL antrenman serisi (gun sayisi).
-- Ardisik gunler tek seri sayilir ve en son seri dondurulur.
-- Dikkat: son antrenman bugun degilse bu deger gecmisteki son seriyi verir;
-- "bugun itibariyla seri devam ediyor mu" sorusu icin son antrenman tarihi
-- ayrica kontrol edilmelidir. En uzun seri icin fn_LongestWorkoutStreak.
DROP FUNCTION IF EXISTS fn_WorkoutStreak;
GO
CREATE FUNCTION fn_WorkoutStreak (@user_id INT)
RETURNS INT
AS
BEGIN
    DECLARE @streak INT;

    ;WITH gunler AS (
        SELECT DISTINCT workout_date
        FROM UserWorkouts
        WHERE user_id = @user_id AND status = 'completed'
    ),
    gruplar AS (
        -- Ardisik gunlerde (tarih - sira) sabit kalir; bu sabit grubu verir.
        SELECT
            workout_date,
            DATEADD(DAY, -ROW_NUMBER() OVER (ORDER BY workout_date), workout_date) AS grup
        FROM gunler
    )
    SELECT TOP 1 @streak = COUNT(*)
    FROM gruplar
    GROUP BY grup
    ORDER BY MAX(workout_date) DESC;

    RETURN ISNULL(@streak, 0);
END;
GO

-- Kullanicinin simdiye kadarki EN UZUN kesintisiz antrenman serisi.
DROP FUNCTION IF EXISTS fn_LongestWorkoutStreak;
GO
CREATE FUNCTION fn_LongestWorkoutStreak (@user_id INT)
RETURNS INT
AS
BEGIN
    DECLARE @streak INT;

    ;WITH gunler AS (
        SELECT DISTINCT workout_date
        FROM UserWorkouts
        WHERE user_id = @user_id AND status = 'completed'
    ),
    gruplar AS (
        SELECT
            workout_date,
            DATEADD(DAY, -ROW_NUMBER() OVER (ORDER BY workout_date), workout_date) AS grup
        FROM gunler
    )
    SELECT TOP 1 @streak = COUNT(*)
    FROM gruplar
    GROUP BY grup
    ORDER BY COUNT(*) DESC;

    RETURN ISNULL(@streak, 0);
END;
GO

-- Kullanicinin en son olcumune gore vucut kitle indeksi.
DROP FUNCTION IF EXISTS fn_LatestBMI;
GO
CREATE FUNCTION fn_LatestBMI (@user_id INT)
RETURNS DECIMAL(5, 2)
AS
BEGIN
    DECLARE @bmi DECIMAL(5, 2);

    SELECT TOP 1
        @bmi = CASE
                   WHEN height IS NULL OR height <= 0 OR weight IS NULL THEN NULL
                   ELSE CAST(weight / POWER(height / 100.0, 2) AS DECIMAL(5, 2))
               END
    FROM BodyStats
    WHERE user_id = @user_id
    ORDER BY record_date DESC, id DESC;

    RETURN @bmi;
END;
GO


/* =====================================================================
   3. TETIKLEYICILER (TRIGGER)
   ===================================================================== */

-- Antrenman 'completed' durumuna gectiginde tamamlanma zamanini ve
-- sureyi doldurur, ayrica Logs tablosuna kayit dusurur.
DROP TRIGGER IF EXISTS trg_UserWorkoutCompleted;
GO
CREATE TRIGGER trg_UserWorkoutCompleted
ON UserWorkouts
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT UPDATE(status)
        RETURN;

    -- Yeni tamamlanan antrenmanlar (onceki durumu 'completed' olmayanlar)
    UPDATE uw
    SET completed_at = ISNULL(uw.completed_at, SYSDATETIME()),
        duration_minutes = ISNULL(
            uw.duration_minutes,
            CASE
                WHEN uw.started_at IS NOT NULL
                THEN DATEDIFF(MINUTE, uw.started_at, ISNULL(uw.completed_at, SYSDATETIME()))
                ELSE NULL
            END)
    FROM UserWorkouts uw
    INNER JOIN inserted i ON i.id = uw.id
    INNER JOIN deleted  d ON d.id = uw.id
    WHERE i.status = 'completed' AND d.status <> 'completed';

    INSERT INTO Logs (user_id, action, description)
    SELECT
        i.user_id,
        N'WORKOUT_COMPLETED',
        N'Antrenman tamamlandi. Antrenman no: ' + CAST(i.id AS NVARCHAR(20))
            + N', tarih: ' + CONVERT(NVARCHAR(10), i.workout_date, 104)
    FROM inserted i
    INNER JOIN deleted d ON d.id = i.id
    WHERE i.status = 'completed' AND d.status <> 'completed';
END;
GO

-- Ayni egzersizin bir kullanici tarafindan iki kez favorilenmesini engeller.
DROP TRIGGER IF EXISTS trg_PreventDuplicateFavorite;
GO
CREATE TRIGGER trg_PreventDuplicateFavorite
ON FavoriteExercises
INSTEAD OF INSERT
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO FavoriteExercises (user_id, exercise_id)
    SELECT i.user_id, i.exercise_id
    FROM inserted i
    WHERE NOT EXISTS (
        SELECT 1 FROM FavoriteExercises f
        WHERE f.user_id = i.user_id AND f.exercise_id = i.exercise_id
    );
END;
GO

-- Vucut olcumlerinde mantik disi degerleri reddeder.
DROP TRIGGER IF EXISTS trg_ValidateBodyStats;
GO
CREATE TRIGGER trg_ValidateBodyStats
ON BodyStats
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1 FROM inserted
        WHERE (weight      IS NOT NULL AND (weight      <= 0   OR weight      > 500))
           OR (height      IS NOT NULL AND (height      <= 0   OR height      > 300))
           OR (body_fat    IS NOT NULL AND (body_fat    < 0    OR body_fat    > 100))
           OR (muscle_mass IS NOT NULL AND (muscle_mass < 0    OR muscle_mass > 200))
    )
    BEGIN
        THROW 50001, N'Gecersiz vucut olcumu. Kilo 0-500 kg, boy 0-300 cm, yag orani 0-100%, kas kutlesi 0-200 kg araliginda olmalidir.', 1;
    END
END;
GO


/* =====================================================================
   4. SAKLANAN YORDAMLAR (STORED PROCEDURE)
   ===================================================================== */

-- Rutinden yeni bir antrenman baslatir ve olusan kaydin id'sini dondurur.
DROP PROCEDURE IF EXISTS sp_StartWorkout;
GO
CREATE PROCEDURE sp_StartWorkout
    @user_id       INT,
    @routine_id    INT = NULL,
    @exercise_id   INT = NULL,
    @user_workout_id INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    IF @routine_id IS NULL AND @exercise_id IS NULL
        THROW 50002, N'Antrenman baslatmak icin rutin veya egzersiz belirtilmelidir.', 1;

    INSERT INTO UserWorkouts (user_id, routine_id, exercise_id, workout_date,
                              workout_type, status, started_at)
    VALUES (@user_id, @routine_id, @exercise_id, CAST(GETDATE() AS DATE),
            CASE WHEN @routine_id IS NOT NULL THEN 'routine' ELSE 'daily' END,
            'active', SYSDATETIME());

    SET @user_workout_id = SCOPE_IDENTITY();

    INSERT INTO Logs (user_id, action, description)
    VALUES (@user_id, N'WORKOUT_STARTED',
            N'Antrenman baslatildi. Antrenman no: ' + CAST(@user_workout_id AS NVARCHAR(20)));
END;
GO

-- Antrenmani tamamlanmis olarak isaretler.
-- Sure ve gunluk kaydi trg_UserWorkoutCompleted tarafindan doldurulur.
DROP PROCEDURE IF EXISTS sp_CompleteWorkout;
GO
CREATE PROCEDURE sp_CompleteWorkout
    @user_workout_id INT
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM UserWorkouts WHERE id = @user_workout_id)
        THROW 50003, N'Antrenman bulunamadi.', 1;

    UPDATE UserWorkouts
    SET status = 'completed'
    WHERE id = @user_workout_id AND status <> 'completed';
END;
GO

-- Kullanici panosu icin ozet istatistikler.
DROP PROCEDURE IF EXISTS sp_UserDashboardStats;
GO
CREATE PROCEDURE sp_UserDashboardStats
    @user_id INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        (SELECT COUNT(*) FROM UserWorkouts
          WHERE user_id = @user_id AND status = 'completed')        AS tamamlanan_antrenman,
        (SELECT COUNT(*) FROM ExerciseRoutines
          WHERE user_id = @user_id)                                 AS rutin_sayisi,
        (SELECT COUNT(*) FROM FavoriteExercises
          WHERE user_id = @user_id)                                 AS favori_sayisi,
        dbo.fn_WorkoutStreak(@user_id)                              AS guncel_seri,
        dbo.fn_LongestWorkoutStreak(@user_id)                       AS en_uzun_seri,
        dbo.fn_LatestBMI(@user_id)                                  AS vucut_kitle_indeksi,
        (SELECT CAST(ISNULL(SUM(uwe.reps * uwe.weight), 0) AS DECIMAL(12, 2))
           FROM UserWorkoutExercises uwe
           INNER JOIN UserWorkouts uw ON uw.id = uwe.user_workout_id
          WHERE uw.user_id = @user_id AND uw.status = 'completed')  AS toplam_hacim;
END;
GO
