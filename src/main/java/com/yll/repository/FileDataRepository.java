package com.yll.repository;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.yll.model.GameData;
import com.yll.service.WebDataService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 基于文件的数据仓储实现
 * 使用文件系统作为数据缓存层
 */
@Slf4j
public class FileDataRepository implements DataRepository {

    private static final String CACHE_FILE_PATH = System.getProperty("user.dir") + "\\unitMap.txt";
    private static final String TEXT_CACHE_DIR = System.getProperty("user.dir") + "\\text\\";

    @Override
    public GameData loadData() {
        log.info("开始加载游戏数据...");
        Map<String, Map<String, java.util.List<Object>>> rawData;

        if (cacheExists()) {
            log.info("从缓存文件加载数据: {}", CACHE_FILE_PATH);
            rawData = loadFromCache();
        } else {
            log.info("缓存不存在，从网络获取数据...");
            rawData = fetchFromWeb();
            saveToCache(rawData);
        }

        log.info("数据加载完成");
        return convertToGameData(rawData);
    }

    @Override
    public Map<String, Map<String, java.util.List<Object>>> fetchFromWeb() {
        // 委托给Web数据服务获取
        WebDataService webService = new WebDataService(TEXT_CACHE_DIR);
        return webService.fetchAndParseData();
    }

    @Override
    public void saveToCache(Map<String, Map<String, java.util.List<Object>>> data) {
        log.info("保存数据到缓存文件: {}", CACHE_FILE_PATH);
        try {
            String jsonContent = JSONUtil.toJsonPrettyStr(data);
            FileUtil.writeUtf8String(jsonContent, CACHE_FILE_PATH);
            log.info("缓存保存成功");
        } catch (Exception e) {
            log.error("保存缓存失败", e);
            throw new RuntimeException("保存缓存失败", e);
        }
    }

    @Override
    public Map<String, Map<String, java.util.List<Object>>> loadFromCache() {
        try {
            String jsonContent = FileUtil.readUtf8String(CACHE_FILE_PATH);
            return JSONUtil.toBean(jsonContent, Map.class);
        } catch (Exception e) {
            log.error("从缓存加载数据失败", e);
            throw new RuntimeException("从缓存加载数据失败", e);
        }
    }

    @Override
    public boolean cacheExists() {
        return FileUtil.exist(CACHE_FILE_PATH);
    }

    /**
     * 将原始数据转换为GameData对象
     */
    private GameData convertToGameData(Map<String, Map<String, java.util.List<Object>>> rawData) {
        GameData gameData = new GameData();

        // 分离研究数据和建筑数据
        rawData.forEach((key, value) -> {
            if (isResearchData(value)) {
                gameData.getResearchData().put(key, value);
            } else if (!isSpecialData(key)) {
                gameData.getBuildingData().put(key, value);
            }
        });

        // 提取特殊数据
        gameData.setLevelLimits(rawData.remove("司令部_level"));
        gameData.setAmountLimits(rawData.remove("司令部_amount"));
        gameData.setPlayerExperienceData(rawData.get("player"));

        return gameData;
    }

    /**
     * 判断是否为研究数据
     */
    private boolean isResearchData(Map<String, java.util.List<Object>> data) {
        return data.keySet().stream()
                .anyMatch(key -> key.contains("研究时间") || key.contains("所需研究所等级需要"));
    }

    /**
     * 判断是否为特殊数据（等级、数量、玩家数据）
     */
    private boolean isSpecialData(String key) {
        return "司令部_level".equals(key) ||
               "司令部_amount".equals(key) ||
               "player".equals(key);
    }
}
