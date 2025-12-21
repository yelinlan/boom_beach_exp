package com.yll;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *@项目名称: boom_beach_exp
 *@类名称: Config
 *@类描述:
 *@创建人: yll
 *@创建时间: 2025/3/16 14:07
 **/
@Data
public class Config implements Serializable {

	private static final String PATH = System.getProperty("user.dir") + "\\config.txt";

	private Map<String, String> buildings = new LinkedHashMap<>();

	private Map<String, String> research = new LinkedHashMap<>();

	private Map<String, String> other = new LinkedHashMap<>();

	/**
	 * 保存游戏模板数据到文件
	 * 此方法用于将游戏数据转换为JSON格式并保存到指定路径如果指定路径的文件已存在，则不执行任何操作
	 *
	 * @param gameData 游戏数据对象，包含建筑物地图、研究地图等信息
	 */
	void saveTemplate(GameData gameData) {
		// 检查文件是否存在，如果存在则直接返回，无需执行后续操作
		if (FileUtil.exist(PATH)) {
			return;
		}

		// 遍历建筑物地图的键集，对于每个建筑物类型，获取其对应的数量信息，并构建初始状态字符串
		gameData.getBuildingMap().keySet().forEach(k -> gameData.getAmount().forEach((k1, v1) -> {
			if (k1.equals(k)) {
				int len = Integer.parseInt(v1.get(v1.size() - 1).toString());
				buildings.put(k, IntStream.range(0, len).mapToObj(i -> "0").collect(Collectors.joining(",")));
			}
		}));

		// 初始化其他游戏数据，如等级、经验等
		other.put("level", "1");
		other.put("exp", "0");
		other.put("player", "1");
		buildings.put("战舰", "1");

		// 遍历研究地图的键集，初始化所有研究项目的等级为0
		gameData.getResearchMap().keySet().forEach(k -> research.put(k, "1"));

		// 将当前游戏数据对象转换为JSON字符串并写入文件
		FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(this), Config.PATH);
	}

	/**
	 * 加载配置信息
	 * 该方法从指定路径读取配置文件，并将其解析为Config对象
	 * 使用了外部工具类FileUtil读取文件内容，并通过JSONUtil将其解析为Bean对象
	 *
	 * @return Config对象，包含从配置文件中读取的配置信息
	 */
	public static Config loadConfig() {
		return JSONUtil.toBean(FileUtil.readUtf8String(PATH), Config.class);
	}

}