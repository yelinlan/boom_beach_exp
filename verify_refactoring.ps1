# 验证重构后的代码结构
Write-Host "=== 验证重构后的代码结构 ===" -ForegroundColor Green

$baseDir = "src/main/java/com/yll"

# 检查模型层
Write-Host "`n检查模型层..." -ForegroundColor Yellow
$modelFiles = @("model/GameData.java", "model/PlayerConfig.java", "model/UpgradeResult.java")
foreach ($file in $modelFiles) {
    if (Test-Path "$baseDir/$file") {
        Write-Host "  [OK] $file" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] $file" -ForegroundColor Red
    }
}

# 检查仓储层
Write-Host "`n检查数据访问层..." -ForegroundColor Yellow
$repoFiles = @("repository/DataRepository.java", "repository/FileDataRepository.java")
foreach ($file in $repoFiles) {
    if (Test-Path "$baseDir/$file") {
        Write-Host "  [OK] $file" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] $file" -ForegroundColor Red
    }
}

# 检查服务层
Write-Host "`n检查业务服务层..." -ForegroundColor Yellow
$serviceFiles = @("service/WebDataService.java", "service/ConfigService.java", "service/UpgradeCalculationService.java")
foreach ($file in $serviceFiles) {
    if (Test-Path "$baseDir/$file") {
        Write-Host "  [OK] $file" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] $file" -ForegroundColor Red
    }
}

# 检查解析器层
Write-Host "`n检查解析器层..." -ForegroundColor Yellow
if (Test-Path "$baseDir/parser/TimeParser.java") {
    Write-Host "  [OK] parser/TimeParser.java" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] parser/TimeParser.java" -ForegroundColor Red
}

# 检查主入口
Write-Host "`n检查主程序入口..." -ForegroundColor Yellow
if (Test-Path "$baseDir/Main.java") {
    Write-Host "  [OK] Main.java" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Main.java" -ForegroundColor Red
}

# 检查配置文件
Write-Host "`n检查配置文件..." -ForegroundColor Yellow
if (Test-Path "src/main/resources/log4j2.xml") {
    Write-Host "  [OK] log4j2.xml" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] log4j2.xml" -ForegroundColor Red
}

Write-Host "`n=== 验证完成 ===" -ForegroundColor Green
Write-Host "按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
