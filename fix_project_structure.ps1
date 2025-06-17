# 1. Admin ve User klasörlerindeki tüm dosyaları orijinal konumlarına geri kopyala
# 2. Referansları düzelt
# 3. Admin ve User klasörlerini tamamen temizle

# ----------------------------------------------------------------
# 1. Tüm dosyaları ana klasörlere kopyala
# ----------------------------------------------------------------

# Admin Controller dosyaları
$adminControllerDir = "src\main\java\com\example\selfworkout\controller\admin"
if (Test-Path $adminControllerDir) {
    $files = Get-ChildItem -Path $adminControllerDir -Filter "*.java"
    foreach ($file in $files) {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        $updatedContent = $content -replace "package com\.example\.selfworkout\.controller\.admin;", "package com.example.selfworkout.controller;"
        $updatedContent = $updatedContent -replace "import com\.example\.selfworkout\.controller\.admin\.", "import com.example.selfworkout.controller."
        $updatedContent = $updatedContent -replace "import com\.example\.selfworkout\.controller\.user\.", "import com.example.selfworkout.controller."
        
        $targetPath = "src\main\java\com\example\selfworkout\controller\" + $file.Name
        $updatedContent | Out-File $targetPath -Encoding UTF8
        Write-Host "Admin controller kopyalandı: $($file.Name)"
    }
}

# User Controller dosyaları
$userControllerDir = "src\main\java\com\example\selfworkout\controller\user"
if (Test-Path $userControllerDir) {
    $files = Get-ChildItem -Path $userControllerDir -Filter "*.java"
    foreach ($file in $files) {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        $updatedContent = $content -replace "package com\.example\.selfworkout\.controller\.user;", "package com.example.selfworkout.controller;"
        $updatedContent = $updatedContent -replace "import com\.example\.selfworkout\.controller\.user\.", "import com.example.selfworkout.controller."
        $updatedContent = $updatedContent -replace "import com\.example\.selfworkout\.controller\.admin\.", "import com.example.selfworkout.controller."
        
        $targetPath = "src\main\java\com\example\selfworkout\controller\" + $file.Name
        $updatedContent | Out-File $targetPath -Encoding UTF8
        Write-Host "User controller kopyalandı: $($file.Name)"
    }
}

# Admin FXML dosyaları
$adminFxmlDir = "src\main\resources\com\example\selfworkout\admin"
if (Test-Path $adminFxmlDir) {
    $files = Get-ChildItem -Path $adminFxmlDir -Filter "*.fxml"
    foreach ($file in $files) {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        $updatedContent = $content -replace 'controller="com\.example\.selfworkout\.controller\.admin\.', 'controller="com.example.selfworkout.controller.'
        $updatedContent = $updatedContent -replace 'controller="com\.example\.selfworkout\.controller\.user\.', 'controller="com.example.selfworkout.controller.'
        
        $targetPath = "src\main\resources\com\example\selfworkout\" + $file.Name
        $updatedContent | Out-File $targetPath -Encoding UTF8
        Write-Host "Admin FXML kopyalandı: $($file.Name)"
    }
}

# User FXML dosyaları
$userFxmlDir = "src\main\resources\com\example\selfworkout\user"
if (Test-Path $userFxmlDir) {
    $files = Get-ChildItem -Path $userFxmlDir -Filter "*.fxml"
    foreach ($file in $files) {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        $updatedContent = $content -replace 'controller="com\.example\.selfworkout\.controller\.user\.', 'controller="com.example.selfworkout.controller.'
        $updatedContent = $updatedContent -replace 'controller="com\.example\.selfworkout\.controller\.admin\.', 'controller="com.example.selfworkout.controller.'
        
        $targetPath = "src\main\resources\com\example\selfworkout\" + $file.Name
        $updatedContent | Out-File $targetPath -Encoding UTF8
        Write-Host "User FXML kopyalandı: $($file.Name)"
    }
}

# ----------------------------------------------------------------
# 2. Ana klasörlerdeki referansları düzelt
# ----------------------------------------------------------------

# Controller dosyalarındaki admin/user referanslarını düzelt
$controllerDir = "src\main\java\com\example\selfworkout\controller"
$controllerFiles = Get-ChildItem -Path $controllerDir -Filter "*.java" -Recurse
foreach ($file in $controllerFiles) {
    # Ana controller klasöründe olup admin/user alt klasörlerinde olmayanları işle
    if (-not $file.FullName.Contains("\admin\") -and -not $file.FullName.Contains("\user\")) {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        $updatedContent = $content -replace "import com\.example\.selfworkout\.controller\.admin\.", "import com.example.selfworkout.controller."
        $updatedContent = $updatedContent -replace "import com\.example\.selfworkout\.controller\.user\.", "import com.example.selfworkout.controller."
        $updatedContent = $updatedContent -replace "instanceof com\.example\.selfworkout\.controller\.admin\.", "instanceof com.example.selfworkout.controller."
        $updatedContent = $updatedContent -replace "instanceof com\.example\.selfworkout\.controller\.user\.", "instanceof com.example.selfworkout.controller."
        $updatedContent = $updatedContent -replace "\(com\.example\.selfworkout\.controller\.admin\.", "(com.example.selfworkout.controller."
        $updatedContent = $updatedContent -replace "\(com\.example\.selfworkout\.controller\.user\.", "(com.example.selfworkout.controller."
        
        $updatedContent | Out-File $file.FullName -Encoding UTF8
        Write-Host "Controller referansları güncellendi: $($file.Name)"
    }
}

# FXML dosyalarındaki admin/user referanslarını düzelt
$fxmlDir = "src\main\resources\com\example\selfworkout"
$fxmlFiles = Get-ChildItem -Path $fxmlDir -Filter "*.fxml" -Recurse
foreach ($file in $fxmlFiles) {
    # Ana dizindeki FXML dosyalarını işle
    if (-not $file.FullName.Contains("\admin\") -and -not $file.FullName.Contains("\user\")) {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        $updatedContent = $content -replace 'controller="com\.example\.selfworkout\.controller\.admin\.', 'controller="com.example.selfworkout.controller.'
        $updatedContent = $updatedContent -replace 'controller="com\.example\.selfworkout\.controller\.user\.', 'controller="com.example.selfworkout.controller.'
        
        $updatedContent | Out-File $file.FullName -Encoding UTF8
        Write-Host "FXML referansları güncellendi: $($file.Name)"
    }
}

# SceneManager.java dosyasını düzelt
$sceneManagerFile = "src\main\java\com\example\selfworkout\util\SceneManager.java"
if (Test-Path $sceneManagerFile) {
    $content = Get-Content $sceneManagerFile -Raw -Encoding UTF8
    
    # FXML yollarını düzelt
    $updatedContent = $content -replace '"/com/example/selfworkout/admin/', '"/com/example/selfworkout/'
    $updatedContent = $updatedContent -replace '"/com/example/selfworkout/user/', '"/com/example/selfworkout/'
    
    # Controller cast ifadelerini düzelt
    $updatedContent = $updatedContent -replace "com\.example\.selfworkout\.controller\.admin\.", "com.example.selfworkout.controller."
    $updatedContent = $updatedContent -replace "com\.example\.selfworkout\.controller\.user\.", "com.example.selfworkout.controller."
    
    $updatedContent | Out-File $sceneManagerFile -Encoding UTF8
    Write-Host "SceneManager güncellendi"
}

# ----------------------------------------------------------------
# 3. Admin ve User klasörlerini tamamen sil
# ----------------------------------------------------------------

# Admin controller klasörünü sil
if (Test-Path $adminControllerDir) {
    Remove-Item -Recurse -Force $adminControllerDir
    Write-Host "Admin controller klasörü silindi"
}

# User controller klasörünü sil
if (Test-Path $userControllerDir) {
    Remove-Item -Recurse -Force $userControllerDir
    Write-Host "User controller klasörü silindi"
}

# Admin FXML klasörünü sil
if (Test-Path $adminFxmlDir) {
    Remove-Item -Recurse -Force $adminFxmlDir
    Write-Host "Admin FXML klasörü silindi"
}

# User FXML klasörünü sil
if (Test-Path $userFxmlDir) {
    Remove-Item -Recurse -Force $userFxmlDir
    Write-Host "User FXML klasörü silindi"
}

Write-Host "İşlem tamamlandı. Proje yapısı eski haline getirildi." 