# Controller referanslarını güncelleyen PowerShell script

# Ana işlem
$srcDir = "src"
$resourcesDir = "$srcDir\main\resources"
$javaDir = "$srcDir\main\java"

# AdminDashboardController FXML referanslarını güncelle
$fxmlFiles = Get-ChildItem -Path $resourcesDir -Filter "*.fxml" -Recurse
foreach ($file in $fxmlFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $oldPattern = "fx:controller=`"com.example.selfworkout.controller.AdminDashboardController`""
    $newPattern = "fx:controller=`"com.example.selfworkout.controller.admin.AdminDashboardController`""
    
    if ($content -match $oldPattern) {
        $updatedContent = $content -replace $oldPattern, $newPattern
        Set-Content -Path $file.FullName -Value $updatedContent -Encoding UTF8
        Write-Host "✅ Güncellendi: $($file.FullName)" -ForegroundColor Green
    }
}

# UserDashboardController FXML referanslarını güncelle
foreach ($file in $fxmlFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $oldPattern = "fx:controller=`"com.example.selfworkout.controller.UserDashboardController`""
    $newPattern = "fx:controller=`"com.example.selfworkout.controller.user.UserDashboardController`""
    
    if ($content -match $oldPattern) {
        $updatedContent = $content -replace $oldPattern, $newPattern
        Set-Content -Path $file.FullName -Value $updatedContent -Encoding UTF8
        Write-Host "✅ Güncellendi: $($file.FullName)" -ForegroundColor Green
    }
}

# AdminDashboardController Java import referanslarını güncelle
$javaFiles = Get-ChildItem -Path $javaDir -Filter "*.java" -Recurse
foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $oldPattern = "import com.example.selfworkout.controller.AdminDashboardController;"
    $newPattern = "import com.example.selfworkout.controller.admin.AdminDashboardController;"
    
    if ($content -match $oldPattern) {
        $updatedContent = $content -replace $oldPattern, $newPattern
        Set-Content -Path $file.FullName -Value $updatedContent -Encoding UTF8
        Write-Host "✅ Güncellendi: $($file.FullName)" -ForegroundColor Green
    }
}

# UserDashboardController Java import referanslarını güncelle
foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $oldPattern = "import com.example.selfworkout.controller.UserDashboardController;"
    $newPattern = "import com.example.selfworkout.controller.user.UserDashboardController;"
    
    if ($content -match $oldPattern) {
        $updatedContent = $content -replace $oldPattern, $newPattern
        Set-Content -Path $file.FullName -Value $updatedContent -Encoding UTF8
        Write-Host "✅ Güncellendi: $($file.FullName)" -ForegroundColor Green
    }
}

# AdminDashboardController instanceof referanslarını güncelle
foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $oldPattern = "instanceof com.example.selfworkout.controller.AdminDashboardController"
    $newPattern = "instanceof com.example.selfworkout.controller.admin.AdminDashboardController"
    
    if ($content -match $oldPattern) {
        $updatedContent = $content -replace $oldPattern, $newPattern
        Set-Content -Path $file.FullName -Value $updatedContent -Encoding UTF8
        Write-Host "✅ Güncellendi: $($file.FullName)" -ForegroundColor Green
    }
}

# UserDashboardController instanceof referanslarını güncelle
foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $oldPattern = "instanceof com.example.selfworkout.controller.UserDashboardController"
    $newPattern = "instanceof com.example.selfworkout.controller.user.UserDashboardController"
    
    if ($content -match $oldPattern) {
        $updatedContent = $content -replace $oldPattern, $newPattern
        Set-Content -Path $file.FullName -Value $updatedContent -Encoding UTF8
        Write-Host "✅ Güncellendi: $($file.FullName)" -ForegroundColor Green
    }
}

Write-Host "✅ Tüm referanslar güncellendi!" -ForegroundColor Green 