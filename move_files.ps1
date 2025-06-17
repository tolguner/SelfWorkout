# Admin Controller dosyalarını taşı
$adminControllers = @(
    "AdminDashboardController.java",
    "DashboardOverviewSimpleController.java",
    "EquipmentManagementContentController.java",
    "ExerciseManagementContentController.java",
    "ExerciseManagementController.java",
    "MuscleGroupManagementContentController.java",
    "SystemReportsContentController.java",
    "SystemReportsController.java",
    "UserManagementContentController.java"
)

# Kullanıcı (Sporcu) Controller dosyalarını taşı
$userControllers = @(
    "ActivityLogContentController.java",
    "ProgressContentController.java",
    "UserDashboardContentController.java",
    "UserDashboardController.java",
    "UserProfileContentController.java",
    "WorkoutStartContentController.java",
    "WorkoutTrackingContentController.java",
    "ExerciseLibraryContentController.java"
)

# Admin FXML dosyalarını taşı
$adminFxml = @(
    "admin-dashboard.fxml",
    "dashboard-overview-simple.fxml",
    "equipment-management-content.fxml",
    "exercise-management-content.fxml",
    "exercise-management.fxml",
    "muscle-group-management-content.fxml",
    "system-reports-content.fxml",
    "system-reports.fxml",
    "user-management-content.fxml"
)

# Kullanıcı (Sporcu) FXML dosyalarını taşı
$userFxml = @(
    "activity-log-content.fxml",
    "body-stats-content.fxml",
    "exercise-library-content.fxml",
    "progress-content.fxml",
    "user-dashboard-content.fxml",
    "user-profile-content.fxml",
    "workout-start-content.fxml",
    "workout-tracking-content.fxml",
    "main-dashboard.fxml"
)

# Dosyaları kopyala ve paket bildirimini güncelle
foreach ($file in $adminControllers) {
    $sourcePath = "src\main\java\com\example\selfworkout\controller\$file"
    $targetPath = "src\main\java\com\example\selfworkout\controller\admin\$file"
    
    if (Test-Path $sourcePath) {
        # Dosyayı oku
        $content = Get-Content $sourcePath -Raw
        
        # package ifadesini güncelle
        $updatedContent = $content -replace "package com\.example\.selfworkout\.controller;", "package com.example.selfworkout.controller.admin;"
        
        # Yeni içeriği hedef dosyaya yaz
        $updatedContent | Out-File $targetPath -Encoding utf8
        
        Write-Host "Kopyalandı ve güncellendi: $file -> admin\"
    } else {
        Write-Host "Dosya bulunamadı: $file"
    }
}

foreach ($file in $userControllers) {
    $sourcePath = "src\main\java\com\example\selfworkout\controller\$file"
    $targetPath = "src\main\java\com\example\selfworkout\controller\user\$file"
    
    if (Test-Path $sourcePath) {
        # Dosyayı oku
        $content = Get-Content $sourcePath -Raw
        
        # package ifadesini güncelle
        $updatedContent = $content -replace "package com\.example\.selfworkout\.controller;", "package com.example.selfworkout.controller.user;"
        
        # import ifadelerini güncelle
        $updatedContent = $updatedContent -replace "import com\.example\.selfworkout\.controller\.", "import com.example.selfworkout.controller."
        
        # Yeni içeriği hedef dosyaya yaz
        $updatedContent | Out-File $targetPath -Encoding utf8
        
        Write-Host "Kopyalandı ve güncellendi: $file -> user\"
    } else {
        Write-Host "Dosya bulunamadı: $file"
    }
}

# FXML dosyalarını kopyala
foreach ($file in $adminFxml) {
    $sourcePath = "src\main\resources\com\example\selfworkout\$file"
    $targetPath = "src\main\resources\com\example\selfworkout\admin\$file"
    
    if (Test-Path $sourcePath) {
        Copy-Item $sourcePath $targetPath
        Write-Host "Kopyalandı: $file -> admin\"
    } else {
        Write-Host "Dosya bulunamadı: $file"
    }
}

foreach ($file in $userFxml) {
    $sourcePath = "src\main\resources\com\example\selfworkout\$file"
    $targetPath = "src\main\resources\com\example\selfworkout\user\$file"
    
    if (Test-Path $sourcePath) {
        Copy-Item $sourcePath $targetPath
        Write-Host "Kopyalandı: $file -> user\"
    } else {
        Write-Host "Dosya bulunamadı: $file"
    }
}

Write-Host "Tüm dosyalar kopyalandı ve güncellendi." 