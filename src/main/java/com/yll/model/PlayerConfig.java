package com.yll.model;

import lombok.Data;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 玩家配置模型
 * 存储玩家的建筑等级、研究等级等配置信息
 */
@Data
public class PlayerConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 建筑当前等级配置：建筑名称 -> 等级列表（逗号分隔）
     */
    private Map<String, String> buildings = new LinkedHashMap<>();

    /**
     * 研究项目当前等级：研究名称 -> 等级
     */
    private Map<String, String> research = new LinkedHashMap<>();

    /**
     * 其他配置项
     * - level: 司令部等级
     * - exp: 当前累计经验
     * - player: 玩家等级
     */
    private Map<String, String> other = new LinkedHashMap<>();

    /**
     * 玩家等级经验数据（从GameData获取，不保存到配置文件）
     */
    private transient Map<String, java.util.List<Object>> playerExperienceData;

    /**
     * 初始化默认配置
     */
    public void initializeDefaults() {
        other.put("level", "1");
        other.put("exp", "0");
        other.put("player", "1");
        buildings.put("战舰", "1");
    }
}
