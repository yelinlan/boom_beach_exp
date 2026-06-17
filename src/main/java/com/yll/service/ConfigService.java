package com.yll.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.yll.model.GameData;
import com.yll.model.PlayerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.IntStream;

/**
 * 配置服务
 * 管理玩家配置的加载、保存和初始化
 */
@Slf4j
public class ConfigService {

    private static final String CONFIG_FILE_PATH = System.getProperty("user.dir") + "\\config.txt";

    /**
     * 加载玩家配置
     *
     * @return 玩家配置对象
     */
    public PlayerConfig loadConfig() {
        try {
            log.info("从文件加载配置: {}", CONFIG_FILE_PATH);
            String jsonContent = FileUtil.readUtf8String(CONFIG_FILE_PATH);
            return JSONUtil.toBean(jsonContent, PlayerConfig.class);
        } catch (Exception e) {
            log.error("加载配置失败，使用默认配置", e);
            return new PlayerConfig();
        }
    }

    /**
     * 为玩家配置设置经验数据引用
     *
     * @param config     玩家配置
     * @param gameData   游戏数据
     */
    public void setPlayerExperienceData(PlayerConfig config, GameData gameData) {
        config.setPlayerExperienceData(gameData.getPlayerExperienceData());
    }

    /**
     * 保存配置模板
     * 如果配置文件不存在，则创建默认配置
     *
     * @param gameData 游戏数据
     */
    public void saveConfigTemplate(GameData gameData) {
        if (FileUtil.exist(CONFIG_FILE_PATH)) {
            log.info("配置文件已存在，跳过创建");
            return;
        }

        log.info("创建默认配置模板");
        PlayerConfig config = new PlayerConfig();
        config.initializeDefaults();

        // 初始化建筑配置
        gameData.getBuildingData().keySet().forEach(buildingName -> {
            var amountLimits = gameData.getAmountLimits();
            if (amountLimits != null && amountLimits.containsKey(buildingName)) {
                int maxCount = Integer.parseInt(amountLimits.get(buildingName)
                        .get(amountLimits.get(buildingName).size() - 1).toString());

                String levels = IntStream.range(0, maxCount)
                        .mapToObj(i -> "0")
                        .reduce((a, b) -> a + "," + b)
                        .orElse("0");

                config.getBuildings().put(buildingName, levels);
            }
        });

        // 初始化研究配置
        gameData.getResearchData().keySet().forEach(researchName ->
                config.getResearch().put(researchName, "1"));

        // 保存配置文件
        try {
            String jsonContent = JSONUtil.toJsonPrettyStr(config);
            FileUtil.writeUtf8String(jsonContent, CONFIG_FILE_PATH);
            log.info("配置模板保存成功");
        } catch (Exception e) {
            log.error("保存配置模板失败", e);
        }
    }
}
