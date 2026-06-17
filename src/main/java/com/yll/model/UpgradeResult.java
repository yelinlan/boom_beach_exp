package com.yll.model;

import lombok.Data;

/**
 * 升级结果数据模型
 * 存储单个建筑或研究项目的升级计算结果
 */
@Data
public class UpgradeResult {
    /**
     * 建筑或研究名称
     */
    private String name;

    /**
     * 当前等级
     */
    private int currentLevel;

    /**
     * 升级到下一等级所需经验
     */
    private int experience;

    /**
     * 收益比（经验/时间）
     */
    private double efficiencyRatio;

    /**
     * 累计经验值
     */
    private int cumulativeExperience;

    /**
     * 最大可达等级
     */
    private int maxLevel;

    /**
     * 单次升级耗时（小时）
     */
    private double timeCost;

    /**
     * 累计总耗时（小时）
     */
    private double cumulativeTime;

    @Override
    public String toString() {
        return String.format("%s：%d->%d（max：%d）exp：%d ratio：%.1f sumExp：%d totalTime:%.1fh spend:%.1fh",
                name, currentLevel, currentLevel + 1, maxLevel, experience,
                efficiencyRatio, cumulativeExperience, cumulativeTime, timeCost);
    }
}
