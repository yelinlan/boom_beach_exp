package com.yll.model;

import lombok.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏数据模型
 * 存储从数据源加载的所有游戏相关数据
 */
@Data
public class GameData {
    /**
     * 研究项目数据映射：项目名称 -> 属性数据
     */
    private Map<String, Map<String, List<Object>>> researchData = new HashMap<>();

    /**
     * 建筑数据映射：建筑名称 -> 属性数据
     */
    private Map<String, Map<String, List<Object>>> buildingData = new HashMap<>();

    /**
     * 等级限制数据：建筑名称 -> 各司令部等级下的最大等级
     */
    private Map<String, List<Object>> levelLimits;

    /**
     * 数量限制数据：建筑名称 -> 各司令部等级下的最大数量
     */
    private Map<String, List<Object>> amountLimits;

    /**
     * 玩家等级经验数据
     */
    private Map<String, List<Object>> playerExperienceData;
}
