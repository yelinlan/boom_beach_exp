@echo off
chcp 65001 > nul
echo ========================================
echo   海岛奇兵数据分析工具 - 重构版本
echo ========================================
echo.

REM 设置类路径
set LIB_DIR=%USERPROFILE%\.m2\repository
set CLASSPATH=classes

REM 添加所有依赖到类路径
if exist "%LIB_DIR%\cn\hutool\hutool-all\5.8.20\hutool-all-5.8.20.jar" (
    set CLASSPATH=%CLASSPATH%;%LIB_DIR%\cn\hutool\hutool-all\5.8.20\hutool-all-5.8.20.jar
)

if exist "%LIB_DIR%\org\jsoup\jsoup\1.10.2\jsoup-1.10.2.jar" (
    set CLASSPATH=%CLASSPATH%;%LIB_DIR%\org\jsoup\jsoup\1.10.2\jsoup-1.10.2.jar
)

if exist "%LIB_DIR%\org\projectlombok\lombok\1.18.34\lombok-1.18.34.jar" (
    set CLASSPATH=%CLASSPATH%;%LIB_DIR%\org\projectlombok\lombok\1.18.34\lombok-1.18.34.jar
)

if exist "%LIB_DIR%\org\slf4j\slf4j-simple\1.7.36\slf4j-simple-1.7.36.jar" (
    set CLASSPATH=%CLASSPATH%;%LIB_DIR%\org\slf4j\slf4j-simple\1.7.36\slf4j-simple-1.7.36.jar
)

if exist "%LIB_DIR%\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar" (
    set CLASSPATH=%CLASSPATH%;%LIB_DIR%\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar
)

if exist "%LIB_DIR%\org\apache\logging\log4j\log4j-core\2.20.0\log4j-core-2.20.0.jar" (
    set CLASSPATH=%CLASSPATH%;%LIB_DIR%\org\apache\logging\log4j\log4j-core\2.20.0\log4j-core-2.20.0.jar
)

if exist "%LIB_DIR%\org\apache\logging\log4j\log4j-api\2.20.0\log4j-api-2.20.0.jar" (
    set CLASSPATH=%CLASSPATH%;%LIB_DIR%\org\apache\logging\log4j\log4j-api\2.20.0\log4j-api-2.20.0.jar
)

if exist "%LIB_DIR%\org\apache\logging\log4j\log4j-slf4j2-impl\2.20.0\log4j-slf4j2-impl-2.20.0.jar" (
    set CLASSPATH=%CLASSPATH%;%LIB_DIR%\org\apache\logging\log4j\log4j-slf4j2-impl\2.20.0\log4j-slf4j2-impl-2.20.0.jar
)

echo [步骤 1/2] 检查Maven...
where mvn >nul 2>&1
if not errorlevel 1 (
    echo   Maven已找到，开始构建...
    echo.
    call mvn clean compile exec:java -Dexec.mainClass="com.yll.Main"
    goto :end
)

echo   Maven未找到，尝试直接运行已编译的JAR...
echo.

if exist "target\boom_beach_exp-1.0-SNAPSHOT.jar" (
    echo [步骤 2/2] 运行程序...
    echo.
    java -jar target\boom_beach_exp-1.0-SNAPSHOT.jar
    goto :end
)

echo [错误] 未找到可执行文件！
echo.
echo 请先运行以下命令构建项目：
echo   mvn clean package
echo.

:end
echo.
pause
