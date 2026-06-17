@echo off
chcp 65001 > nul
echo === 海岛奇兵数据分析工具 - 快速启动 ===
echo.

REM 检查Java是否安装
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Java，请先安装JDK 17或更高版本
    pause
    exit /b 1
)

echo [1/3] 检查Java环境...
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr "version"') do (
    echo   Java版本: %%i
)

REM 检查是否有Maven
where mvn >nul 2>&1
if not errorlevel 1 (
    echo.
    echo [2/3] 使用Maven构建...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo [错误] Maven构建失败
        pause
        exit /b 1
    )
    
    echo.
    echo [3/3] 运行程序...
    java -jar target\boom_beach_exp-1.0-SNAPSHOT.jar
) else (
    echo.
    echo [提示] 未检测到Maven，尝试手动编译...
    echo.
    
    REM 创建输出目录
    if not exist "classes" mkdir classes
    
    echo [2/3] 编译Java文件...
    javac -d classes -encoding UTF-8 src\main\java\com\yll\model\*.java src\main\java\com\yll\parser\*.java src\main\java\com\yll\repository\*.java src\main\java\com\yll\service\*.java src\main\java\com\yll\Main.java 2>nul
    
    if errorlevel 1 (
        echo [错误] 编译失败
        echo.
        echo 建议使用Maven构建项目：
        echo   mvn clean package
        pause
        exit /b 1
    )
    
    echo [3/3] 运行程序...
    java -cp classes com.yll.Main
)

echo.
echo === 程序已退出 ===
pause
