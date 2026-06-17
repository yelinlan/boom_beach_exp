package com.yll.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import com.yll.model.GameData;
import com.yll.model.PlayerConfig;
import com.yll.model.UpgradeResult;
import com.yll.parser.TimeParser;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 升级计算服务
 * 负责计算建筑和研究的升级收益
 */
@Slf4j
public class UpgradeCalculationService {

    /**
     * 计算建筑升级数据
     *
     * @param gameData   游戏数据
     * @param config     玩家配置
     * @param commanderLevel 司令部等级
     * @return 升级结果列表
     */
    public List<UpgradeResult> calculateBuildingUpgrades(GameData gameData, PlayerConfig config, int commanderLevel) {
        log.info("开始计算建筑升级收益，司令部等级: {}", commanderLevel);

        List<UpgradeResult> results = new ArrayList<>();

        config.getBuildings().forEach((buildingName, levelConfig) -> {
            Map<String, List<Object>> buildingData = gameData.getBuildingData().get(buildingName);
            if (buildingData == null) {
                log.warn("未找到建筑数据: {}", buildingName);
                return;
            }

            int maxLevel = getMaxLevelForBuilding(gameData, buildingName, commanderLevel);
            int currentCount = getCurrentBuildingCount(gameData, buildingName, commanderLevel);

            List<UpgradeResult> buildingResults = calculateUpgradesForUnit(
                    buildingName, levelConfig, maxLevel, currentCount, buildingData);

            results.addAll(buildingResults);
        });

        log.info("建筑升级计算完成，共 {} 条结果", results.size());
        return results;
    }

    /**
     * 计算研究升级数据
     *
     * @param gameData 游戏数据
     * @param config   玩家配置
     * @return 升级结果列表
     */
    public List<UpgradeResult> calculateResearchUpgrades(GameData gameData, PlayerConfig config) {
        log.info("开始计算研究升级收益");

        List<UpgradeResult> results = new ArrayList<>();

        config.getResearch().forEach((researchName, levelConfig) -> {
            Map<String, List<Object>> researchData = gameData.getResearchData().get(researchName);
            if (researchData == null) {
                log.warn("未找到研究数据: {}", researchName);
                return;
            }

            String instituteLevel = config.getBuildings().getOrDefault("研究所", "1");
            int maxLevel = getMaxResearchLevel(researchData, instituteLevel);

            List<UpgradeResult> researchResults = calculateUpgradesForUnit(
                    researchName, levelConfig, maxLevel, 1, researchData);

            results.addAll(researchResults);
        });

        log.info("研究升级计算完成，共 {} 条结果", results.size());
        return results;
    }

    /**
     * 计算单个单位的所有等级升级
     */
    private List<UpgradeResult> calculateUpgradesForUnit(String name, String levelConfig,
                                                          int maxLevel, int count,
                                                          Map<String, List<Object>> data) {
        List<UpgradeResult> results = new ArrayList<>();
        String[] levels = levelConfig.split(",");

        for (int i = 0; i < levels.length && i < count; i++) {
            int currentLevel = Convert.toInt(levels[i], 0);

            // 从当前等级升级到最大等级
            for (int level = currentLevel; level < maxLevel; level++) {
                try {
                    UpgradeResult result = calculateSingleUpgrade(name, level, maxLevel, level + 1, data);
                    if (result != null) {
                        results.add(result);
                    }
                } catch (Exception e) {
                    log.debug("跳过无效升级: {} 等级 {} -> {}", name, level, level + 1);
                }
            }
        }

        return results;
    }

    /**
     * 计算单次升级的收益
     */
    private UpgradeResult calculateSingleUpgrade(String name, int currentLevel, int maxLevel,
                                                  int nextLevel, Map<String, List<Object>> data) {
        try {
            // 获取经验值
            int experience = getExperience(data, nextLevel);
            if (experience <= 0) {
                return null;
            }

            // 获取时间
            double timeCost = getTimeCost(data, nextLevel);
            if (timeCost <= 0) {
                return null;
            }

            // 计算收益比
            double efficiencyRatio = Double.parseDouble(
                    NumberUtil.decimalFormat("#.0", experience / timeCost));

            UpgradeResult result = new UpgradeResult();
            result.setName(name);
            result.setCurrentLevel(currentLevel);
            result.setExperience(experience);
            result.setEfficiencyRatio(efficiencyRatio);
            result.setMaxLevel(maxLevel);
            result.setTimeCost(timeCost);

            return result;
        } catch (Exception e) {
            log.error("计算升级收益失败: {} 等级 {}", name, currentLevel, e);
            return null;
        }
    }

    /**
     * 获取升级所需经验
     */
    private int getExperience(Map<String, List<Object>> data, int level) {
        try {
            List<Object> expList = data.get("经验");
            if (expList == null || level >= expList.size()) {
                return 0;
            }
            return Integer.parseInt(expList.get(level).toString());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取升级时间（小时）
     */
    private double getTimeCost(Map<String, List<Object>> data, int level) {
        try {
            List<Object> timeList = Optional.ofNullable(data.get("升级时间"))
                    .orElse(data.get("研究时间"));

            if (timeList == null || level >= timeList.size()) {
                return 0;
            }

            String timeString = timeList.get(level).toString();
            return TimeParser.parseToHours(timeString);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取建筑在指定司令部等级下的最大等级
     */
    private int getMaxLevelForBuilding(GameData gameData, String buildingName, int commanderLevel) {
        Map<String, List<Object>> levelLimits = gameData.getLevelLimits();
        if (levelLimits == null || !levelLimits.containsKey(buildingName)) {
            return -1;
        }

        List<Object> limits = levelLimits.get(buildingName);
        if (commanderLevel <= 0 || commanderLevel > limits.size()) {
            return -1;
        }

        return Convert.toInt(limits.get(commanderLevel - 1).toString(), -1);
    }

    /**
     * 获取当前建筑数量
     */
    private int getCurrentBuildingCount(GameData gameData, String buildingName, int commanderLevel) {
        Map<String, List<Object>> amountLimits = gameData.getAmountLimits();
        if (amountLimits == null || !amountLimits.containsKey(buildingName)) {
            return 0;
        }

        List<Object> amounts = amountLimits.get(buildingName);
        if (commanderLevel <= 0 || commanderLevel > amounts.size()) {
            return 0;
        }

        return Convert.toInt(amounts.get(commanderLevel - 1).toString(), 0);
    }

    /**
     * 获取研究的最大等级
     */
    private int getMaxResearchLevel(Map<String, List<Object>> researchData, String instituteLevel) {
        List<Object> instituteList = Optional.ofNullable(researchData.get("研究所"))
                .orElse(Optional.ofNullable(researchData.get("所需研究所等级"))
                        .orElse(researchData.get("所需研究所等级需要")));

        if (instituteList == null) {
            return 0;
        }

        for (int i = 0; i < instituteList.size(); i++) {
            if (instituteList.get(i).toString().equals(instituteLevel)) {
                return i + 1;
            }
        }

        return 0;
    }

    /**
     * 对结果进行排序和累计计算
     *
     * @param results 原始结果列表
     * @param config  玩家配置
     */
    public void processAndDisplayResults(List<UpgradeResult> results, PlayerConfig config) {
        if (results == null || results.isEmpty()) {
            System.out.println("没有可升级的项目！");
            return;
        }

        // 按收益比降序排序，相同时按时间升序
        results.sort(Comparator.comparing(UpgradeResult::getEfficiencyRatio).reversed()
                .thenComparing(UpgradeResult::getTimeCost));

        // 计算累计值
        int totalExp = Convert.toInt(config.getOther().get("exp"), 0);
        int playerLevel = Convert.toInt(config.getOther().get("player"), 1);
        double lastSpendTime = 0;

        for (int i = 0; i < results.size(); i++) {
            UpgradeResult current = results.get(i);

            if (i == 0) {
                current.setCumulativeExperience(current.getExperience());
                current.setCumulativeTime(current.getTimeCost());
            } else {
                UpgradeResult previous = results.get(i - 1);
                current.setCumulativeExperience(previous.getCumulativeExperience() + current.getExperience());
                current.setCumulativeTime(previous.getCumulativeTime() + current.getTimeCost());
            }

            totalExp += current.getExperience();
            System.out.println(current);

            // 检查玩家是否升级
            int requiredExp = getPlayerLevelExp(config, playerLevel);
            if (totalExp > requiredExp) {
                totalExp -= requiredExp;
                double phaseTime = current.getCumulativeTime() - lastSpendTime;
                System.out.printf("玩家升级了！！！  %d--->%d，剩余%d经验，阶段耗时：%s%n",
                        playerLevel, playerLevel + 1, totalExp, TimeParser.formatTime(phaseTime));
                lastSpendTime = current.getCumulativeTime();
                playerLevel++;
            }
        }

        // 输出最终状态
        UpgradeResult lastResult = results.get(results.size() - 1);
        double finalPhaseTime = lastResult.getCumulativeTime() - lastSpendTime;
        System.out.printf("在当前配置下，玩家能够升级到%d级，剩余%d经验，阶段耗时：%s%n",
                playerLevel, totalExp, TimeParser.formatTime(finalPhaseTime));
    }

    /**
     * 获取玩家升级所需经验
     */
    private int getPlayerLevelExp(PlayerConfig config, int playerLevel) {
        if (playerLevel <= 0 || playerLevel > config.getPlayerExperienceData().size()) {
            return Integer.MAX_VALUE;
        }

        List<Object> expData = config.getPlayerExperienceData().get("经验");
        if (expData == null || playerLevel > expData.size()) {
            return Integer.MAX_VALUE;
        }

        return Convert.toInt(expData.get(playerLevel - 1).toString(), Integer.MAX_VALUE);
    }
}
