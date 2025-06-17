# UserDashboardController ve AdminDashboardController içindeki FXML yollarını kontrol et ve düzelt
$userControllerFile = "src\main\java\com\example\selfworkout\controller\user\UserDashboardController.java"

if (Test-Path $userControllerFile) {
    $content = Get-Content $userControllerFile -Raw
    
    # FXML yollarını düzelt
    $updatedContent = $content -replace 'getResource\("/com/example/selfworkout/([^"]+\.fxml)"\)', 'getResource("/com/example/selfworkout/user/$1")'
    $updatedContent = $updatedContent -replace 'getResource\("/com/example/selfworkout/login.fxml"\)', 'getResource("/com/example/selfworkout/login.fxml")'
    $updatedContent = $updatedContent -replace 'getResource\("/com/example/selfworkout/register.fxml"\)', 'getResource("/com/example/selfworkout/register.fxml")'
    $updatedContent = $updatedContent -replace 'getResource\("/com/example/selfworkout/admin/([^"]+\.fxml)"\)', 'getResource("/com/example/selfworkout/admin/$1")'
    
    # Controller referanslarını düzelt
    $updatedContent = $updatedContent -replace 'instanceof com.example.selfworkout.controller.BaseController', 'instanceof com.example.selfworkout.controller.BaseController'
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $userControllerFile -Encoding utf8
    Write-Host "UserDashboardController güncellendi"
}

$adminControllerFile = "src\main\java\com\example\selfworkout\controller\admin\AdminDashboardController.java"

if (Test-Path $adminControllerFile) {
    $content = Get-Content $adminControllerFile -Raw
    
    # FXML yollarını düzelt
    $updatedContent = $content -replace 'getResource\("/com/example/selfworkout/([^"]+\.fxml)"\)', 'getResource("/com/example/selfworkout/admin/$1")'
    $updatedContent = $updatedContent -replace 'getResource\("/com/example/selfworkout/login.fxml"\)', 'getResource("/com/example/selfworkout/login.fxml")'
    $updatedContent = $updatedContent -replace 'getResource\("/com/example/selfworkout/register.fxml"\)', 'getResource("/com/example/selfworkout/register.fxml")'
    $updatedContent = $updatedContent -replace 'getResource\("/com/example/selfworkout/user/([^"]+\.fxml)"\)', 'getResource("/com/example/selfworkout/user/$1")'
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $adminControllerFile -Encoding utf8
    Write-Host "AdminDashboardController güncellendi"
}

# SceneManager.java içindeki referansları düzelt
$sceneManagerFile = "src\main\java\com\example\selfworkout\util\SceneManager.java"

if (Test-Path $sceneManagerFile) {
    $content = Get-Content $sceneManagerFile -Raw
    
    # Controller cast ifadelerini düzelt
    $updatedContent = $content -replace '(.*\(\s*)com\.example\.selfworkout\.controller\.([A-Z][a-zA-Z0-9]+Controller)(\s*\).*)', '$1com.example.selfworkout.controller.admin.$2$3'
    $updatedContent = $updatedContent -replace '(.*\(\s*)com\.example\.selfworkout\.controller\.(UserDashboard|UserProfile|WorkoutTracking|WorkoutStart|ActivityLog|Progress|ExerciseLibrary)Controller(\s*\).*)', '$1com.example.selfworkout.controller.user.$2Controller$3'
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $sceneManagerFile -Encoding utf8
    Write-Host "SceneManager güncellendi"
}

# LoginController ve RegisterController içindeki referansları kontrol et
$loginControllerFile = "src\main\java\com\example\selfworkout\controller\LoginController.java"
$registerControllerFile = "src\main\java\com\example\selfworkout\controller\RegisterController.java"

if (Test-Path $loginControllerFile) {
    $content = Get-Content $loginControllerFile -Raw
    
    # Admin ve User controller referanslarını düzelt
    $updatedContent = $content -replace 'com\.example\.selfworkout\.controller\.AdminDashboardController', 'com.example.selfworkout.controller.ActivityLogContentController.AdminDashboardController'
    $updatedContent = $updatedContent -replace 'com\.example\.selfworkout\.controller\.UserDashboardController', 'com.example.selfworkout.controller.user.UserDashboardController'
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $loginControllerFile -Encoding utf8
    Write-Host "LoginController güncellendi"
}

if (Test-Path $registerControllerFile) {
    $content = Get-Content $registerControllerFile -Raw
    
    # Admin ve User controller referanslarını düzelt
    $updatedContent = $content -replace 'com\.example\.selfworkout\.controller\.AdminDashboardController', 'com.example.selfworkout.controller.ActivityLogContentController.AdminDashboardController'
    $updatedContent = $updatedContent -replace 'com\.example\.selfworkout\.controller\.UserDashboardController', 'com.example.selfworkout.controller.user.UserDashboardController'
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $registerControllerFile -Encoding utf8
    Write-Host "RegisterController güncellendi"
}

Write-Host "Referanslar güncellendi" 