# 💪 SelfWorkout — Antrenman Takip Sistemi

Kişisel antrenman planlama ve ilerleme takibi için geliştirilmiş masaüstü uygulaması.
Kullanıcılar egzersiz kütüphanesinden rutin oluşturur, antrenmanlarını canlı olarak
kaydeder ve vücut ölçümleriyle gelişimlerini izler. Yöneticiler egzersiz, ekipman ve
kas grubu kataloğunu yönetir, sistem raporlarını görüntüler.

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-17-1f8ac0?style=flat-square&logo=openjdk&logoColor=white)
![MSSQL](https://img.shields.io/badge/SQL%20Server-Express-CC2927?style=flat-square&logo=microsoftsqlserver&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white)
![Durum](https://img.shields.io/badge/durum-geliştirme%20aşamasında-orange?style=flat-square)

> **Durum:** Geliştirme aşamasında. Proje derleniyor ve temel akışlar uygulanmış
> durumda. Otomatik test altyapısı yoktur (`test/` altındaki sınıflar elle çalıştırılan
> doğrulama betikleridir, JUnit testi değildir).

---

## Özellikler

### 👤 Kullanıcı

- **Egzersiz kütüphanesi** — kas grubu ve ekipmana göre egzersizleri inceleme
- **Rutin oluşturma** — egzersizleri sıralayıp kendi antrenman programını kurma
- **Canlı antrenman takibi** — set, tekrar ve ağırlık kaydı
- **Vücut ölçümleri** — kilo ve ölçü geçmişi tutma
- **İlerleme grafikleri** — zaman içindeki gelişimi görüntüleme
- **Favori egzersizler** — sık kullanılanları işaretleme
- **Profil yönetimi**

### ⚙️ Yönetici

- **Egzersiz yönetimi** — katalog oluşturma, kas grubu ve ekipman eşleştirme
- **Kas grubu ve ekipman yönetimi**
- **Kullanıcı yönetimi** — hesap listeleme, rol atama
- **Sistem raporları** — kullanım istatistikleri
- **Etkinlik günlüğü** — sistemdeki işlemlerin kaydı

---

## Mimari

Katmanlı mimari; her katman yalnızca bir alttakini tanır:

```
  FXML görünümleri  (20 ekran: admin/ + user/)
          │
     Controller     (19 sınıf)
          │
       Service      (17 sınıf — iş kuralları, doğrulama, raporlama)
          │
        DAO         (17 sınıf — SQL erişimi)
          │
       Model        (18 sınıf — veri nesneleri)
          │
    MSSQL Server    (17 tablo)
```

**Öne çıkan altyapı sınıfları**

| Sınıf | Görevi |
|---|---|
| `DatabaseConnection` | Thread-safe singleton bağlantı yönetimi |
| `ServiceManager` | Servis örneklerinin merkezî dağıtımı |
| `SceneManager` | Ekranlar arası geçiş |
| `ThemeManager` | Tema değiştirme (3 CSS teması) |
| `ValidationService` | Ortak girdi doğrulama |
| `ImportExportService` | Veri içe/dışa aktarma |

---

## Kurulum

### Gereksinimler

- JDK 17+
- Microsoft SQL Server (Express yeterli)
- Maven (wrapper dahil — `mvnw`)

### 1. Veritabanını oluştur

`src/main/sql/selfworkout.sql` betiği şemayı ve başlangıç verisini kurar:

```bash
sqlcmd -S localhost\SQLEXPRESS -U sa -i src/main/sql/selfworkout.sql
```

Ayrıntılı SQL Server kurulumu için [MSSQL_Database_Setup.md](MSSQL_Database_Setup.md).

### 2. Bağlantı bilgilerini ayarla

Kimlik bilgileri **kaynak koda yazılmaz**, ortam değişkenlerinden okunur:

| Değişken | Zorunlu | Varsayılan | Açıklama |
|---|---|---|---|
| `DB_URL` | hayır | `jdbc:sqlserver://localhost\SQLEXPRESS;databaseName=selfworkout;...` | JDBC bağlantı adresi |
| `DB_USERNAME` | hayır | `sa` | SQL Server kullanıcısı |
| `DB_PASSWORD` | **evet** | — | Kullanıcının parolası |

PowerShell'de:

```powershell
$env:DB_PASSWORD="sql_server_parolaniz"
```

Bash'te:

```bash
export DB_PASSWORD="sql_server_parolaniz"
```

`DB_PASSWORD` tanımlı değilse uygulama açıklayıcı bir hata ile durur.
Varsayılan bağlantı ayarları `src/main/resources/database.properties` içindedir;
ortam değişkeni tanımlıysa oradaki değerin yerine geçer.

### 3. Çalıştır

```bash
./mvnw javafx:run
```

---

## Veri modeli

17 tablo. Çekirdek ilişkiler:

- **`Users` ↔ `Roles`** — rol bazlı erişim (yönetici / kullanıcı)
- **`Exercises`** — `MuscleGroups` ve `Equipments` ile çoka-çok (`ExerciseMuscles`, `ExerciseEquipments`), ayrıca `ExerciseTags` ile etiketlenir
- **`ExerciseRoutines` → `RoutineExercises`** — kullanıcının oluşturduğu antrenman şablonları
- **`DailyWorkouts` → `WorkoutExercises`** — planlanan günlük antrenmanlar
- **`UserWorkouts` → `UserWorkoutExercises`** — gerçekleşen antrenman kayıtları
- **`BodyStats`** — kullanıcının ölçüm geçmişi
- **`FavoriteExercises`**, **`Logs`** — favoriler ve sistem etkinlik kaydı

---

## Proje yapısı

```
src/main/
├── java/com/example/selfworkout/
│   ├── controller/      19 controller (admin/ ve user/ alt paketleri dahil)
│   ├── service/         17 servis — iş mantığı
│   ├── dao/             17 DAO — veritabanı erişimi
│   ├── model/           18 veri sınıfı
│   ├── util/            bağlantı, sahne, tema, uyarı yardımcıları
│   └── test/            elle çalıştırılan doğrulama betikleri
├── resources/
│   ├── com/example/selfworkout/
│   │   ├── admin/       yönetici ekranları (FXML)
│   │   ├── user/        kullanıcı ekranları (FXML)
│   │   └── css/         3 tema
│   └── database.properties
└── sql/selfworkout.sql  şema ve başlangıç verisi
```

---

## Gelecek çalışmalar

### Kısa vade

- [ ] **Gerçek test altyapısı** — `test/` altındaki elle çalıştırılan betiklerin JUnit testlerine dönüştürülmesi (JUnit bağımlılığı `pom.xml`'de zaten mevcut)
- [ ] **Bağlantı havuzu** — `database.properties` içinde havuz ayarları tanımlı ama kod tek bir bağlantıyı yeniden kullanıyor; HikariCP'ye geçilmeli
- [ ] **Görsel varlık optimizasyonu** — `images/foto.png` 4269×2400 ve 24 MB; giriş ekranı için gereğinden büyük
- [ ] **Yinelenen controller'ların birleştirilmesi** — `ExerciseManagementController` ve `ExerciseManagementContentController` gibi çiftler

### Orta vade

- [ ] **Parola özetleme (hashing)** — kullanıcı parolalarının BCrypt ile saklanması
- [ ] **Şema göç (migration) yönetimi** — Flyway ile sürümlenebilir şema
- [ ] **Hata yönetiminin merkezîleştirilmesi** — `AlertUtil` üzerinden tutarlı kullanıcı bildirimi
- [ ] **Raporların dışa aktarımı** — PDF ve Excel çıktısı

### Uzun vade

- [ ] **Antrenman şablonu paylaşımı** — kullanıcılar arası rutin paylaşımı
- [ ] **Grafik zenginleştirme** — hacim, sıklık ve kişisel rekor takibi
- [ ] **Çoklu dil desteği**
- [ ] **Mobil eşlik uygulaması**

---

## Lisans

Belirtilmemiş.
