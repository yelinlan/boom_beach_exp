# 代码重构说明

## 重构概述

本次重构对原有的海岛奇兵数据分析工具进行了全面的架构优化和代码改进。

## 重构目标

1. **提高代码可维护性**：通过职责分离和模块化设计
2. **增强可测试性**：引入接口抽象和依赖注入
3. **改进异常处理**：统一的异常处理和日志记录
4. **提升代码可读性**：清晰的包结构和命名规范
5. **支持扩展性**：便于未来添加新功能

## 重构内容

### 1. 新的包结构

```
com.yll
├── model/              # 数据模型层
│   ├── GameData.java          # 游戏数据容器
│   ├── PlayerConfig.java      # 玩家配置
│   └── UpgradeResult.java     # 升级计算结果
├── repository/         # 数据访问层
│   ├── DataRepository.java        # 数据仓储接口
│   └── FileDataRepository.java    # 文件仓储实现
├── service/            # 业务服务层
│   ├── WebDataService.java        # Web数据抓取服务
│   ├── ConfigService.java         # 配置管理服务
│   └── UpgradeCalculationService.java  # 升级计算服务
├── parser/             # 解析器层
│   └── TimeParser.java            # 时间字符串解析器
└── Main.java           # 应用入口
```

### 2. 主要改进点

#### 2.1 数据模型层 (model)

- **GameData**: 清晰分离研究数据和建筑数据
- **PlayerConfig**: 独立配置管理，支持序列化
- **UpgradeResult**: 标准化的结果数据结构

#### 2.2 数据访问层 (repository)

- **DataRepository接口**: 定义数据操作的标准接口
- **FileDataRepository实现**: 基于文件的缓存策略
- **职责单一**: 专注于数据的加载和存储

#### 2.3 业务服务层 (service)

- **WebDataService**: 封装所有网络请求和HTML解析逻辑
- **ConfigService**: 统一管理配置的加载、保存和初始化
- **UpgradeCalculationService**: 独立的升级计算引擎

#### 2.4 解析器层 (parser)

- **TimeParser**: 专门处理中文时间字符串的解析和格式化

### 3. 设计模式应用

#### 3.1 仓储模式 (Repository Pattern)
```java
DataRepository repository = new FileDataRepository();
GameData data = repository.loadData();
```

#### 3.2 服务层模式 (Service Layer Pattern)
```java
ConfigService configService = new ConfigService();
UpgradeCalculationService calcService = new UpgradeCalculationService();
```

#### 3.3 依赖注入 (Dependency Injection)
```java
public class Main {
    private final DataRepository dataRepository;
    private final ConfigService configService;
    private final UpgradeCalculationService upgradeService;
    // 构造函数注入
}
```

### 4. 代码质量改进

#### 4.1 异常处理
- 统一的异常捕获和处理
- 详细的错误日志记录
- 友好的用户提示信息

#### 4.2 日志记录
- 使用SLF4J + Log4j2日志框架
- 分级日志（DEBUG, INFO, WARN, ERROR）
- 结构化的日志输出

#### 4.3 代码注释
- 完整的JavaDoc文档
- 清晰的业务逻辑注释
- 参数和返回值说明

### 5. 向后兼容性

保留了原有的核心功能：
- 数据抓取和缓存机制
- 配置文件管理
- 升级收益计算算法
- 结果排序和输出格式

### 6. 新增功能

1. **更好的错误恢复**：配置加载失败时使用默认值
2. **灵活的缓存策略**：支持更换缓存实现
3. **可扩展的数据源**：可通过实现DataRepository接口添加新数据源
4. **独立的日志配置**：log4j2.xml配置文件

## 迁移指南

### 对于现有用户

1. **配置文件**：`config.txt` 格式保持不变，可直接使用
2. **缓存文件**：`unitMap.txt` 格式保持不变，可直接使用
3. **运行方式**：主类仍为 `com.yll.Main`，命令不变

### 对于开发者

如果需要添加新功能：

1. **新增数据源**：实现 `DataRepository` 接口
2. **修改计算逻辑**：在 `UpgradeCalculationService` 中扩展
3. **添加新的解析器**：在 `parser` 包中创建新类
4. **扩展数据模型**：在 `model` 包中添加新类

## 编译和运行

### 编译
```bash
mvn clean package
```

### 运行
```bash
java -jar target/boom_beach_exp-1.0-SNAPSHOT.jar
```

或使用Maven：
```bash
mvn exec:java -Dexec.mainClass="com.yll.Main"
```

## 测试建议

由于原项目没有单元测试，建议后续添加：

1. **TimeParser测试**：验证各种时间格式解析
2. **UpgradeCalculationService测试**：验证计算逻辑
3. **ConfigService测试**：验证配置加载和保存
4. **集成测试**：端到端测试完整流程

## 注意事项

1. **首次运行**：会尝试从网络抓取数据，需要网络连接
2. **缓存清理**：删除 `unitMap.txt` 可强制重新抓取数据
3. **配置重置**：删除 `config.txt` 可生成新的默认配置
4. **日志查看**：日志文件位于 `logs/app.log`

## 未来改进方向

1. 添加单元测试覆盖
2. 支持多语言界面
3. 图形化用户界面 (GUI)
4. 导出结果为Excel或PDF
5. 支持更多游戏版本
6. 在线更新检测
