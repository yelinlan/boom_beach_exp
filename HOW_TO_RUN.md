# 运行重构后的代码

## 快速开始

### 方法一：使用Maven（推荐）

1. **确保已安装Maven**
   ```bash
   mvn -version
   ```
   如果未安装，请从 https://maven.apache.org/download.cgi 下载并安装

2. **编译项目**
   ```bash
   mvn clean package
   ```

3. **运行程序**
   ```bash
   java -jar target/boom_beach_exp-1.0-SNAPSHOT.jar
   ```

   或者使用Maven直接运行：
   ```bash
   mvn exec:java -Dexec.mainClass="com.yll.Main"
   ```

### 方法二：使用IDE（最简单）

1. **导入项目到IDE**
   - IntelliJ IDEA: File -> Open -> 选择pom.xml
   - Eclipse: File -> Import -> Existing Maven Projects

2. **等待依赖下载完成**

3. **运行主类**
   - 找到 `src/main/java/com/yll/Main.java`
   - 右键 -> Run 'Main.main()'

### 方法三：手动编译（无Maven）

如果没有Maven，需要先下载所有依赖JAR包：

```bash
# 创建lib目录
mkdir lib

# 下载依赖（需要手动从Maven Central下载）
# hutool-all-5.8.20.jar
# jsoup-1.10.2.jar
# lombok-1.18.34.jar
# slf4j-simple-1.7.36.jar
# slf4j-api-1.7.36.jar
# log4j-core-2.20.0.jar
# log4j-api-2.20.0.jar
# log4j-slf4j2-impl-2.20.0.jar

# 编译
javac -d classes -cp "lib/*" -encoding UTF-8 src/main/java/com/yll/**/*.java

# 运行
java -cp "classes;lib/*" com.yll.Main
```

## 首次运行说明

### 有缓存数据的情况

如果项目中已有 `unitMap.txt` 和 `config.txt` 文件（当前已有）：

```
[INFO] === 海岛奇兵数据分析工具启动 ===
[INFO] 步骤1: 加载游戏数据...
[INFO] 从缓存文件加载数据: D:\...\unitMap.txt
[INFO] 数据加载完成
[INFO] 步骤2: 加载玩家配置...
[INFO] 从文件加载配置: D:\...\config.txt
...
========== 单独建筑升级顺序 ==========
...
========== 单独研究所升级顺序 ==========
...
```

### 无缓存数据的情况

如果是首次运行（没有缓存文件）：

1. **需要网络连接** - 会从网站抓取数据
2. **可能需要Cookie** - 某些数据需要登录
3. **耗时较长** - 取决于网络速度

## 常见问题

### Q1: 编译错误 "找不到符号"

**原因**: Lombok注解处理器未启用

**解决方案**:
- Maven: 无需额外配置，自动处理
- IDE: 安装Lombok插件
- 手动编译: 添加 `-processor-path lombok.jar`

### Q2: 运行时错误 "ClassNotFoundException"

**原因**: 依赖JAR包未在类路径中

**解决方案**: 使用Maven构建会自动处理依赖

### Q3: 日志警告 "SLF4J: Failed to load class"

**原因**: SLF4J绑定问题

**解决方案**: 
```bash
mvn clean package  # 重新构建
```

### Q4: 无法连接数据源

**原因**: 
- 网络问题
- Cookie过期
- 网站不可用

**解决方案**: 
- 检查网络连接
- 更新 `WebDataService.java` 中的Cookie
- 使用已有的缓存数据

## 验证重构成功

运行以下PowerShell脚本验证文件结构：

```powershell
.\verify_refactoring.ps1
```

应该看到所有新文件都标记为 `[OK]`。

## 新旧版本对比

| 特性 | 旧版本 | 新版本（重构后） |
|------|--------|------------------|
| 代码组织 | 所有代码在com.yll包下 | 分层架构（model/repository/service/parser） |
| 职责分离 | DataUtil承担多种职责 | 清晰的职责划分 |
| 异常处理 | 空catch块 | 统一的异常处理和日志 |
| 可测试性 | 难以测试 | 易于单元测试 |
| 可扩展性 | 修改困难 | 支持扩展 |

## 下一步建议

1. **添加单元测试**
   ```bash
   # 创建测试类
   src/test/java/com/yll/service/UpgradeCalculationServiceTest.java
   ```

2. **改进配置管理**
   - 支持命令行参数
   - 支持环境变量

3. **图形界面**
   - 考虑添加JavaFX或Swing界面

4. **导出功能**
   - 导出结果为Excel
   - 导出为PDF报告

## 技术支持

如遇到问题，请检查：

1. Java版本是否为17或更高
2. Maven是否正确安装
3. 网络连接是否正常（首次运行）
4. 查看日志文件 `logs/app.log`

## 性能优化建议

如果数据量很大：

1. 增加缓存过期策略
2. 使用数据库替代文件存储
3. 实现增量更新而非全量抓取
4. 添加多线程处理
