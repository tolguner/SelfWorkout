-- Yeni veritabanı oluştur
CREATE DATABASE selfworkout;
USE selfworkout;

-- 1. Roller tablosu
CREATE TABLE Roles (
    id INT PRIMARY KEY IDENTITY(1,1),
    role_name NVARCHAR(20) NOT NULL UNIQUE -- 'admin', 'sporcu'
);

-- 2. Kullanıcılar tablosu
CREATE TABLE Users (
    id INT PRIMARY KEY IDENTITY(1,1),
    role_id INT NOT NULL,
    username NVARCHAR(50) NOT NULL UNIQUE,
    email NVARCHAR(100) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    name NVARCHAR(100),
    surname NVARCHAR(100),
    birthdate DATE,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (role_id) REFERENCES Roles(id)
);

-- 3. Kas Grupları
CREATE TABLE MuscleGroups (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255)
);

-- 4. Ekipmanlar
CREATE TABLE Equipments (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255)
);

-- 5. Egzersizler
CREATE TABLE Exercises (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255),
    difficulty_level NVARCHAR(20) CHECK (difficulty_level IN ('kolay', 'orta', 'zor')),
    instructions NVARCHAR(MAX),
    created_by INT NOT NULL, -- admin user id
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (created_by) REFERENCES Users(id)
);

-- 6. Egzersiz-Kas ilişkisi
CREATE TABLE ExerciseMuscles (
    id INT PRIMARY KEY IDENTITY(1,1),
    exercise_id INT,
    muscle_id INT,
    FOREIGN KEY (exercise_id) REFERENCES Exercises(id),
    FOREIGN KEY (muscle_id) REFERENCES MuscleGroups(id)
);

-- 7. Egzersiz-Ekipman ilişkisi
CREATE TABLE ExerciseEquipments (
    id INT PRIMARY KEY IDENTITY(1,1),
    exercise_id INT,
    equipment_id INT,
    FOREIGN KEY (exercise_id) REFERENCES Exercises(id),
    FOREIGN KEY (equipment_id) REFERENCES Equipments(id)
);

-- 8. Etiketler (arama için)
CREATE TABLE ExerciseTags (
    id INT PRIMARY KEY IDENTITY(1,1),
    exercise_id INT,
    tag NVARCHAR(100),
    FOREIGN KEY (exercise_id) REFERENCES Exercises(id)
);

-- 9. Rutinler
CREATE TABLE ExerciseRoutines (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    name NVARCHAR(100),
    description NVARCHAR(255),
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(id)
);

-- 10. Rutin içindeki egzersizler
CREATE TABLE RoutineExercises (
    id INT PRIMARY KEY IDENTITY(1,1),
    routine_id INT NOT NULL,
    exercise_id INT NOT NULL,
    exercise_order INT DEFAULT 1,
    set_count INT DEFAULT 3,
    reps_per_set NVARCHAR(50),
    weight_per_set NVARCHAR(50),
    FOREIGN KEY (routine_id) REFERENCES ExerciseRoutines(id),
    FOREIGN KEY (exercise_id) REFERENCES Exercises(id)
);

-- 11. Günlük planlanmış egzersizler
CREATE TABLE DailyWorkouts (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    workout_date DATE NOT NULL,
    notes NVARCHAR(255),
    status NVARCHAR(20) DEFAULT 'planned' CHECK (status IN ('planned', 'active', 'completed', 'cancelled')),
    FOREIGN KEY (user_id) REFERENCES Users(id)
);

-- 12. Günlük egzersiz detayları
CREATE TABLE WorkoutExercises (
    id INT PRIMARY KEY IDENTITY(1,1),
    daily_workout_id INT NOT NULL,
    exercise_id INT NOT NULL,
    exercise_order INT DEFAULT 1,
    set_count INT,
    reps_per_set NVARCHAR(50),
    weight_per_set NVARCHAR(50),
    FOREIGN KEY (daily_workout_id) REFERENCES DailyWorkouts(id),
    FOREIGN KEY (exercise_id) REFERENCES Exercises(id)
);

-- 13. Boy/Kilo ve BMI
CREATE TABLE BodyStats (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    record_date DATE DEFAULT CAST(GETDATE() AS DATE),
    height DECIMAL(5,2), -- cm cinsinden
    weight DECIMAL(5,2), -- kg cinsinden
    body_fat DECIMAL(5,2), -- vücut yağ oranı %
    muscle_mass DECIMAL(5,2), -- kas kütlesi kg
    notes NVARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES Users(id)
);

-- 14. Kullanıcının yaptığı antrenmanlar
CREATE TABLE UserWorkouts (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    routine_id INT NULL,
    exercise_id INT NULL, -- tek egzersiz antrenmanları için
    workout_date DATE DEFAULT CAST(GETDATE() AS DATE),
    workout_type NVARCHAR(20) CHECK (workout_type IN ('daily', 'routine')),
    notes NVARCHAR(255),
    status NVARCHAR(20) DEFAULT 'planned' CHECK (status IN ('planned', 'active', 'completed', 'cancelled')),
    duration_minutes INT,
    started_at DATETIME2,
    completed_at DATETIME2,
    FOREIGN KEY (user_id) REFERENCES Users(id),
    FOREIGN KEY (routine_id) REFERENCES ExerciseRoutines(id),
    FOREIGN KEY (exercise_id) REFERENCES Exercises(id)
);

-- 15. Gerçekleşmiş egzersiz kayıtları
CREATE TABLE UserWorkoutExercises (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_workout_id INT NOT NULL,
    exercise_id INT NOT NULL,
    set_number INT,
    reps INT,
    weight DECIMAL(5,2),
    notes NVARCHAR(255),
    FOREIGN KEY (user_workout_id) REFERENCES UserWorkouts(id),
    FOREIGN KEY (exercise_id) REFERENCES Exercises(id)
);

-- 16. Loglar
CREATE TABLE Logs (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT,
    action NVARCHAR(255),
    description NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(id)
);

-- 17. Favori egzersizler
CREATE TABLE FavoriteExercises (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    exercise_id INT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(id),
    FOREIGN KEY (exercise_id) REFERENCES Exercises(id)
);

-- Rolleri ekle
INSERT INTO Roles (role_name) VALUES ('admin');
INSERT INTO Roles (role_name) VALUES ('sporcu');

-- Admin kullanıcısını ekle
INSERT INTO Users (role_id, username, email, password, name, surname, created_at)
VALUES (1, 'admin', 'admin@selfworkout.com', 'admin123', 'System', 'Administrator', GETDATE());

-- Admin kullanıcısını kontrol et
SELECT 
    u.id,
    u.username,
    u.email,
    u.name,
    u.surname,
    r.role_name,
    u.created_at
FROM Users u
INNER JOIN Roles r ON u.role_id = r.id
WHERE u.username = 'admin';

-- Kas grupları ekle (eğer yoksa)
IF NOT EXISTS (SELECT 1 FROM MuscleGroups WHERE name = 'Göğüs')
BEGIN
INSERT INTO MuscleGroups (name, description) VALUES 
('Göğüs', 'Pektoralis kasları'),
('Sırt', 'Latissimus dorsi ve trapez kasları'),
('Omuz', 'Deltoid kasları'),
('Biceps', 'Kol ön kasları'),
('Triceps', 'Kol arka kasları'),
('Bacak', 'Quadriceps ve hamstring kasları'),
('Karın', 'Abdominal kasları'),
('Kalça', 'Glutes kasları');
END

-- Ekipmanlar ekle (eğer yoksa)
IF NOT EXISTS (SELECT 1 FROM Equipments WHERE name = 'Halter')
BEGIN
INSERT INTO Equipments (name, description) VALUES 
('Halter', 'Serbest ağırlık'),
('Dumbbell', 'El halterleri'),
('Barbell', 'Olimpik bar'),
('Kablo Makinesi', 'Kablo sistemi'),
('Smith Machine', 'Guided barbell'),
('Treadmill', 'Koşu bandı'),
('Bisiklet', 'Stationary bike'),
('Vücut Ağırlığı', 'Ekipman gerektirmez');
END

-- Örnek egzersiz ekleme (eğer yoksa)
IF NOT EXISTS (SELECT 1 FROM Exercises WHERE name = 'Bench Press')
BEGIN
    INSERT INTO Exercises (name, description, instructions, difficulty_level, created_by)
    VALUES ('Bench Press', 'Göğüs geliştirme egzersizi', 'Sırt üstü yatarak barbell kaldırma', 'orta', 1);
    
    -- Egzersiz-kas grubu ilişkisi
    INSERT INTO ExerciseMuscles (exercise_id, muscle_id)
    VALUES (1, 1); -- Bench Press -> Göğüs
    
    -- Egzersiz-ekipman ilişkisi  
    INSERT INTO ExerciseEquipments (exercise_id, equipment_id)
    VALUES (1, 3); -- Bench Press -> Barbell
END

-- Sonuçları göster
SELECT 'Kas Grupları:' as Tablo;
SELECT * FROM MuscleGroups;

SELECT 'Ekipmanlar:' as Tablo;
SELECT * FROM Equipments;

SELECT 'Egzersizler:' as Tablo;
SELECT * FROM Exercises;

-- Sayıları göster
SELECT COUNT(*) as kas_grubu_sayisi FROM MuscleGroups;
SELECT COUNT(*) as ekipman_sayisi FROM Equipments;
SELECT COUNT(*) as egzersiz_sayisi FROM Exercises;


-- Tüm tabloları listele
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_TYPE = 'BASE TABLE' 
ORDER BY TABLE_NAME;

-- Kullanıcı sayısını kontrol et
SELECT 
    r.role_name,
    COUNT(u.id) as user_count
FROM Roles r
LEFT JOIN Users u ON r.id = u.role_id
GROUP BY r.id, r.role_name;

-- Admin kullanıcısının detaylarını göster
SELECT 
    u.id,
    u.username,
    u.email,
    u.name + ' ' + u.surname as full_name,
    r.role_name,
    u.created_at
FROM Users u
INNER JOIN Roles r ON u.role_id = r.id
WHERE r.role_name = 'admin';

