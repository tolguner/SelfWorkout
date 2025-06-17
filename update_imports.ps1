# Admin Controller'ların import ifadelerini düzenle
$adminControllerFiles = Get-ChildItem -Path "src\main\java\com\example\selfworkout\controller\admin\*.java"

foreach ($file in $adminControllerFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # BaseController ve diğer ana controller import'larını güncelle
    $updatedContent = $content -replace "import com.example.selfworkout.controller.BaseController;", "import com.example.selfworkout.controller.BaseController;"
    $updatedContent = $updatedContent -replace "import com.example.selfworkout.controller.([A-Za-z]+Controller);", "import com.example.selfworkout.controller.admin.`$1;"
    
    # User controller'larını güncelle
    $updatedContent = $updatedContent -replace "import com.example.selfworkout.controller.(UserDashboard|UserProfile|WorkoutTracking|WorkoutStart|ActivityLog|Progress|ExerciseLibrary)Controller;", "import com.example.selfworkout.controller.user.`$1Controller;"
    
    # instanceof kontrollerini güncelle
    $updatedContent = $updatedContent -replace "controller instanceof ([A-Za-z]+Controller)", "controller instanceof com.example.selfworkout.controller.admin.`$1"
    $updatedContent = $updatedContent -replace "controller instanceof (UserDashboard|UserProfile|WorkoutTracking|WorkoutStart|ActivityLog|Progress|ExerciseLibrary)Controller", "controller instanceof com.example.selfworkout.controller.user.`$1Controller"
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $file.FullName -Encoding utf8
    Write-Host "Admin controller güncellendi: $($file.Name)"
}

# User Controller'ların import ifadelerini düzenle
$userControllerFiles = Get-ChildItem -Path "src\main\java\com\example\selfworkout\controller\user\*.java"

foreach ($file in $userControllerFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # BaseController ve diğer ana controller import'larını güncelle
    $updatedContent = $content -replace "import com.example.selfworkout.controller.BaseController;", "import com.example.selfworkout.controller.BaseController;"
    $updatedContent = $updatedContent -replace "import com.example.selfworkout.controller.([A-Za-z]+Controller);", "import com.example.selfworkout.controller.user.`$1;"
    
    # Admin controller'larını güncelle
    $updatedContent = $updatedContent -replace "import com.example.selfworkout.controller.(Admin|SystemReports|ExerciseManagement|MuscleGroupManagement|EquipmentManagement|UserManagement|DashboardOverviewSimple)Controller;", "import com.example.selfworkout.controller.admin.`$1Controller;"
    
    # instanceof kontrollerini güncelle
    $updatedContent = $updatedContent -replace "controller instanceof ([A-Za-z]+Controller)", "controller instanceof com.example.selfworkout.controller.user.`$1"
    $updatedContent = $updatedContent -replace "controller instanceof (Admin|SystemReports|ExerciseManagement|MuscleGroupManagement|EquipmentManagement|UserManagement|DashboardOverviewSimple)Controller", "controller instanceof com.example.selfworkout.controller.admin.`$1Controller"
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $file.FullName -Encoding utf8
    Write-Host "User controller güncellendi: $($file.Name)"
}

# FXML dosyalarını güncelle - Admin
$adminFxmlFiles = Get-ChildItem -Path "src\main\resources\com\example\selfworkout\admin\*.fxml"

foreach ($file in $adminFxmlFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Controller referanslarını güncelle
    $updatedContent = $content -replace 'controller="com.example.selfworkout.controller.([A-Za-z]+Controller)"', 'controller="com.example.selfworkout.controller.admin.$1"'
    
    # User controller referanslarını güncelle
    $updatedContent = $updatedContent -replace 'controller="com.example.selfworkout.controller.(UserDashboard|UserProfile|WorkoutTracking|WorkoutStart|ActivityLog|Progress|ExerciseLibrary)Controller"', 'controller="com.example.selfworkout.controller.user.$1Controller"'
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $file.FullName -Encoding utf8
    Write-Host "Admin FXML güncellendi: $($file.Name)"
}

# FXML dosyalarını güncelle - User
$userFxmlFiles = Get-ChildItem -Path "src\main\resources\com\example\selfworkout\user\*.fxml"

foreach ($file in $userFxmlFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Controller referanslarını güncelle
    $updatedContent = $content -replace 'controller="com.example.selfworkout.controller.([A-Za-z]+Controller)"', 'controller="com.example.selfworkout.controller.user.$1"'
    
    # Admin controller referanslarını güncelle
    $updatedContent = $updatedContent -replace 'controller="com.example.selfworkout.controller.(Admin|SystemReports|ExerciseManagement|MuscleGroupManagement|EquipmentManagement|UserManagement|DashboardOverviewSimple)Controller"', 'controller="com.example.selfworkout.controller.admin.$1Controller"'
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $file.FullName -Encoding utf8
    Write-Host "User FXML güncellendi: $($file.Name)"
}

# SceneManager'ı güncelle
$sceneManagerFile = "src\main\java\com\example\selfworkout\util\SceneManager.java"
if (Test-Path $sceneManagerFile) {
    $content = Get-Content $sceneManagerFile -Raw
    
    # FXML yollarını güncelle - Admin
    $updatedContent = $content -replace '"/com/example/selfworkout/(admin-dashboard|dashboard-overview-simple|equipment-management-content|exercise-management-content|exercise-management|muscle-group-management-content|system-reports-content|system-reports|user-management-content).fxml"', '"/com/example/selfworkout/admin/$1.fxml"'
    
    # FXML yollarını güncelle - User
    $updatedContent = $updatedContent -replace '"/com/example/selfworkout/(activity-log-content|body-stats-content|exercise-library-content|progress-content|user-dashboard-content|user-profile-content|workout-start-content|workout-tracking-content|main-dashboard).fxml"', '"/com/example/selfworkout/user/$1.fxml"'
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $sceneManagerFile -Encoding utf8
    Write-Host "SceneManager güncellendi"
}

Write-Host "Tüm import ve referans güncellemeleri tamamlandı." 