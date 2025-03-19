package com.yll;

import java.util.List;
import java.util.Map;

/**
 *@项目名称: boom_beach_exp
 *@类名称: DataDeal
 *@类描述:
 *@创建人: yll
 *@创建时间: 2025/3/18 22:42
 **/
public class DataDeal {

	private DataDeal(){}

	private static GameData gameData = new GameData();

	/**
	 * 初始化游戏数据
	 * 根据提供的单位映射，区分研究信息和建筑信息，并设置游戏数据的相应属性
	 *
	 * @param unitMap 包含单位信息的映射，其键是单位类型，值是包含具体单位数据的映射
	 * @return 初始化后的GameData对象，包含研究信息、建筑信息、等级、数量和玩家信息等
	 */
	public static GameData init(Map<String, Map<String, List<Object>>> unitMap) {
	    //遍历单位映射，区分研究信息和建筑信息
	    unitMap.forEach((k, v) -> {
	        //检查当前单位数据是否包含研究相关的关键字
	        boolean allMatch = v.keySet().stream()
	                .anyMatch(p -> p.contains("研究时间") || p.contains("所需研究所等级需要"));
	        if (allMatch) {
	            //如果包含研究关键字，将数据添加到研究映射中
	            gameData.getResearchMap().put(k, v);
	        } else {
	            //如果不包含研究关键字，将数据添加到建筑映射中
	            gameData.getBuildingMap().put(k, v);
	        }
	    });

	    //从建筑映射中移除并设置等级和数量信息
	    gameData.setLevel(gameData.getBuildingMap().remove("司令部_level"));
	    gameData.setAmount(gameData.getBuildingMap().remove("司令部_amount"));
	    //设置玩家1的信息
	    gameData.setPlayer1(gameData.getBuildingMap().get("player"));

	    //保存配置模板
	    new Config().saveTemplate(gameData);
	    //加载配置
	    gameData.setConfig(Config.loadConfig());
	    return gameData;
	}

}