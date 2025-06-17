# Taşınan Controller dosyalarını eski konumlarına taşı ve package bildirimlerini düzelt

# Admin Controller dosyalarını geri taşı
$adminControllerFiles = Get-ChildItem -Path "src\main\java\com\example\selfworkout\controller\admin\*.java"
foreach ($file in $adminControllerFiles) {
    $content = Get-Content $file.FullName -Raw
    $updatedContent = $content -replace "package com\.example\.selfworkout\.controller\.admin;", "package com.example.selfworkout.controller;"
    
    # İmport ifadelerini düzelt
    $updatedContent = $updatedContent -replace "import com\.example\.selfworkout\.controller\.admin\.", "import com.example.selfworkout.controller."
    $updatedContent = $updatedContent -replace "import com\.example\.selfworkout\.controller\.user\.", "import com.example.selfworkout.controller."
    
    # Güncellenen içeriği ana controller klasörüne yaz
    $targetFile = "src\main\java\com\example\selfworkout\controller\" + $file.Name
    $updatedContent | Out-File $targetFile -Encoding utf8
    Write-Host "Admin controller geri taşındı: $($file.Name)"
}

# User Controller dosyalarını geri taşı
$userControllerFiles = Get-ChildItem -Path "src\main\java\com\example\selfworkout\controller\user\*.java"
foreach ($file in $userControllerFiles) {
    $content = Get-Content $file.FullName -Raw
    $updatedContent = $content -replace "package com\.example\.selfworkout\.controller\.user;", "package com.example.selfworkout.controller;"
    
    # İmport ifadelerini düzelt
    $updatedContent = $updatedContent -replace "import com\.example\.selfworkout\.controller\.user\.", "import com.example.selfworkout.controller."
    $updatedContent = $updatedContent -replace "import com\.example\.selfworkout\.controller\.admin\.", "import com.example.selfworkout.controller."
    
    # Güncellenen içeriği ana controller klasörüne yaz
    $targetFile = "src\main\java\com\example\selfworkout\controller\" + $file.Name
    $updatedContent | Out-File $targetFile -Encoding utf8
    Write-Host "User controller geri taşındı: $($file.Name)"
}

# Admin FXML dosyalarını geri taşı
$adminFxmlFiles = Get-ChildItem -Path "src\main\resources\com\example\selfworkout\admin\*.fxml"
foreach ($file in $adminFxmlFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Controller referanslarını güncelle
    $updatedContent = $content -replace 'controller="com\.example\.selfworkout\.controller\.admin\.', 'controller="com.example.selfworkout.controller.'
    $updatedContent = $updatedContent -replace 'controller="com\.example\.selfworkout\.controller\.user\.', 'controller="com.example.selfworkout.controller.'
    
    # Güncellenen içeriği ana resources klasörüne yaz
    $targetFile = "src\main\resources\com\example\selfworkout\" + $file.Name
    $updatedContent | Out-File $targetFile -Encoding utf8
    Write-Host "Admin FXML geri taşındı: $($file.Name)"
}

# User FXML dosyalarını geri taşı
$userFxmlFiles = Get-ChildItem -Path "src\main\resources\com\example\selfworkout\user\*.fxml"
foreach ($file in $userFxmlFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Controller referanslarını güncelle
    $updatedContent = $content -replace 'controller="com\.example\.selfworkout\.controller\.user\.', 'controller="com.example.selfworkout.controller.'
    $updatedContent = $updatedContent -replace 'controller="com\.example\.selfworkout\.controller\.admin\.', 'controller="com.example.selfworkout.controller.'
    
    # Güncellenen içeriği ana resources klasörüne yaz
    $targetFile = "src\main\resources\com\example\selfworkout\" + $file.Name
    $updatedContent | Out-File $targetFile -Encoding utf8
    Write-Host "User FXML geri taşındı: $($file.Name)"
}

# SceneManager'ı güncelle
$sceneManagerFile = "src\main\java\com\example\selfworkout\util\SceneManager.java"
if (Test-Path $sceneManagerFile) {
    $content = Get-Content $sceneManagerFile -Raw
    
    # FXML yollarını güncelle
    $updatedContent = $content -replace '"/com/example/selfworkout/admin/([^"]+)\.fxml"', '"/com/example/selfworkout/$1.fxml"'
    $updatedContent = $updatedContent -replace '"/com/example/selfworkout/user/([^"]+)\.fxml"', '"/com/example/selfworkout/$1.fxml"'
    
    # Controller referanslarını güncelle
    $updatedContent = $updatedContent -replace 'com\.example\.selfworkout\.controller\.admin\.', 'com.example.selfworkout.controller.'
    $updatedContent = $updatedContent -replace 'com\.example\.selfworkout\.controller\.user\.', 'com.example.selfworkout.controller.'
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $sceneManagerFile -Encoding utf8
    Write-Host "SceneManager güncellendi"
}

# LoginController ve RegisterController güncelle
$loginControllerFile = "src\main\java\com\example\selfworkout\controller\LoginController.java" 
if (Test-Path $loginControllerFile) {
    $content = Get-Content $loginControllerFile -Raw
    
    # Controller referanslarını güncelle
    $updatedContent = $content -replace 'com\.example\.selfworkout\.controller\.admin\.', 'com.example.selfworkout.controller.'
    $updatedContent = $updatedContent -replace 'com\.example\.selfworkout\.controller\.user\.', 'com.example.selfworkout.controller.'
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $loginControllerFile -Encoding utf8
    Write-Host "LoginController güncellendi"
}

$registerControllerFile = "src\main\java\com\example\selfworkout\controller\RegisterController.java"
if (Test-Path $registerControllerFile) {
    $content = Get-Content $registerControllerFile -Raw
    
    # Controller referanslarını güncelle
    $updatedContent = $content -replace 'com\.example\.selfworkout\.controller\.admin\.', 'com.example.selfworkout.controller.'
    $updatedContent = $updatedContent -replace 'com\.example\.selfworkout\.controller\.user\.', 'com.example.selfworkout.controller.'
    
    # Güncellenen içeriği dosyaya yaz
    $updatedContent | Out-File $registerControllerFile -Encoding utf8
    Write-Host "RegisterController güncellendi"
}

Write-Host "Tüm dosyalar orijinal konumlarına geri taşındı ve referanslar düzeltildi." 