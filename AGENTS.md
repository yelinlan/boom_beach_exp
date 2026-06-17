# AGENTS.md

This file provides guidance to Qoder (lingma.aliyun.com) when working with code in this repository.

## Project Overview

This is a Java-based data analysis tool for the game "Boom Beach" (海岛奇兵). It scrapes game data from https://bb.heiyu100.cn/shuju.aspx, parses building and research information, calculates upgrade benefits, and outputs optimal upgrade paths.

## Technology Stack

- **Language**: Java 17
- **Build Tool**: Maven
- **Core Dependencies**:
  - Hutool v5.8.20: Java utility library
  - Lombok v1.18.34: Code simplification annotations
  - Jsoup v1.10.2: HTML parsing
  - Apache POI v5.2.5: Excel processing
  - EasyExcel v3.3.2: Excel read/write
  - Log4j2 v2.20.0: Logging framework

## Common Commands

### Build the project
```bash
mvn clean package
```

### Run the application
```bash
java -jar target/boom_beach_exp-1.0-SNAPSHOT.jar
```

### Run with Maven
```bash
mvn exec:java -Dexec.mainClass="com.yll.Main"
```

### Skip tests during build
```bash
mvn clean package -DskipTests
```

## Project Architecture

### Main Components

The application follows a simple layered architecture focused on data scraping, processing, and calculation:

#### 1. Data Layer (`DataUtil.java`)
- **Purpose**: Handles all data fetching, parsing, and caching
- **Key Methods**:
  - `loadData()`: Loads data from cache or fetches from web
  - `fetchData()`: Scrapes game data website and parses HTML tables
  - `toText()`: Converts HTML data to structured text format
  - `getAllResult()`: Fetches data from multiple endpoints
- **Cache Strategy**: Stores parsed data in `unitMap.txt` to avoid repeated network requests

#### 2. Data Processing Layer (`DataDeal.java`)
- **Purpose**: Initializes and categorizes game data
- **Key Method**:
  - `init(unitMap)`: Separates research data from building data, loads player configuration
- **Output**: Returns initialized `GameData` object

#### 3. Data Model (`GameData.java`, `Config.java`, `Result.java`)
- **GameData**: Central data container holding:
  - `researchMap`: Research/study data
  - `buildingMap`: Building construction data
  - `level`: Level requirements
  - `amount`: Quantity limits
  - `player1`: Player experience data
  - `config`: User configuration
- **Config**: User settings for buildings, research, and game state (stored in `config.txt`)
- **Result**: Calculation results for upgrade recommendations

#### 4. Business Logic (`Main.java`)
- **Purpose**: Core calculation engine and output
- **Key Methods**:
  - `genBuildingUpgradeData()`: Calculates building upgrade benefits
  - `genResearchUpgradeData()`: Calculates research upgrade benefits
  - `calculateEachUnitAndEachLevel()`: Computes exp/time ratio for each upgrade level
  - `printData()`: Sorts and displays results with cumulative calculations
- **Algorithm**: Ranks upgrades by efficiency ratio (experience / time)

#### 5. Utility Module (`excel/ExcelConverter.java`)
- **Purpose**: Standalone tool for converting test case Excel files
- **Independent**: Not used by main application logic
- **Input/Output**: Transforms source Excel format to standardized test case format

### Data Flow

```
Web Scraper (DataUtil)
    ↓
HTML Parser (Jsoup)
    ↓
Data Cache (unitMap.txt)
    ↓
Data Initializer (DataDeal)
    ↓
Game Data Container (GameData)
    ↓
Calculation Engine (Main)
    ↓
Results Output (Console)
```

### File Structure

Runtime files generated during execution:
- `unitMap.txt`: Cached parsed game data (JSON format)
- `config.txt`: User configuration (buildings, research levels, player stats)
- `text/`: Directory containing raw HTML fragments for each data category

### Key Design Patterns

1. **Lazy Loading**: Data is only fetched from web if cache doesn't exist
2. **Configuration Persistence**: User config saved as JSON, loaded on subsequent runs
3. **Separation of Concerns**: Clear separation between data fetching, processing, and calculation
4. **Functional Style**: Uses Java streams extensively for data transformation

## Important Notes

### Data Sources
- Primary URL: `https://bb.heiyu100.cn/shuju.aspx`
- Data pages follow pattern: `shuju192.html`, `shuju193.html`, etc.
- Authentication requires cookies (hardcoded in `DataUtil.getResult()`)

### Configuration Management
- First run: Generates `config.txt` template with default values
- Subsequent runs: Loads existing config, preserves user modifications
- Config fields:
  - `buildings`: Current building levels (comma-separated)
  - `research`: Current research levels
  - `other`: Player level, experience, commander level

### Calculation Logic
- Efficiency ratio = Experience / Time (in hours)
- Results sorted by ratio (descending), then by time (ascending)
- Cumulative experience tracks player level progression
- Time parsing supports Chinese format: "X天 Y时 Z分 A秒"

### Excel Converter
- Located in `com.yll.excel` package
- Independent utility, not invoked by main application
- Requires hardcoded file paths (modify `ORIGIN` constant before use)

## Testing

No formal test framework configured. The project uses manual testing through console output verification.

## Build Configuration

Maven shade plugin creates executable JAR with main class `com.yll.Main`. All dependencies are bundled into the fat JAR.
