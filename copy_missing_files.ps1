# Eksik admin controller dosyalarını belirle ve kopyala
$adminControllers = @(
    "EquipmentManagementContentController.java",
    "ExerciseManagementContentController.java", 
    "MuscleGroupManagementContentController.java",
    "SystemReportsContentController.java",
    "SystemReportsController.java",
    "UserManagementContentController.java"
)

# Eksik user controller dosyalarını belirle ve kopyala
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

# Eksik admin FXML dosyalarını belirle ve kopyala
$adminFxml = @(
    "admin-dashboard.fxml",
    "dashboard-overview-simple.fxml",
    "equipment-management-content.fxml",
    "exercise-management-content.fxml",
    "muscle-group-management-content.fxml",
    "system-reports-content.fxml",
    "system-reports.fxml",
    "user-management-content.fxml"
)

# Dosyaları kontrol et ve eksikleri kopyala
foreach ($file in $adminControllers) {
    $sourcePath = "src\main\java\com\example\selfworkout\controller\$file"
    $targetPath = "src\main\java\com\example\selfworkout\controller\admin\$file"
    
    if ((Test-Path $sourcePath) -and (-not (Test-Path $targetPath))) {
        # Dosyayı oku
        $content = Get-Content $sourcePath -Raw
        
        # package ifadesini güncelle
        $updatedContent = $content -replace "package com\.example\.selfworkout\.controller;", "package com.example.selfworkout.controller.admin;"
        
        # Yeni içeriği hedef dosyaya yaz
        $updatedContent | Out-File $targetPath -Encoding utf8
        
        Write-Host "Eksik dosya kopyalandı: $file -> admin\"
    }
}

foreach ($file in $userControllers) {
    $sourcePath = "src\main\java\com\example\selfworkout\controller\$file"
    $targetPath = "src\main\java\com\example\selfworkout\controller\user\$file"
    
    if ((Test-Path $sourcePath) -and (-not (Test-Path $targetPath))) {
        # Dosyayı oku
        $content = Get-Content $sourcePath -Raw
        
        # package ifadesini güncelle
        $updatedContent = $content -replace "package com\.example\.selfworkout\.controller;", "package com.example.selfworkout.controller.user;"
        
        # Yeni içeriği hedef dosyaya yaz
        $updatedContent | Out-File $targetPath -Encoding utf8
        
        Write-Host "Eksik dosya kopyalandı: $file -> user\"
    }
}

foreach ($file in $adminFxml) {
    $sourcePath = "src\main\resources\com\example\selfworkout\$file"
    $targetPath = "src\main\resources\com\example\selfworkout\admin\$file"
    
    if ((Test-Path $sourcePath) -and (-not (Test-Path $targetPath))) {
        Copy-Item $sourcePath $targetPath
        Write-Host "Eksik FXML kopyalandı: $file -> admin\"
    }
}

Write-Host "Eksik dosyalar kopyalandı." 