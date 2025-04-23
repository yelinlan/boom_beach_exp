package com.yll;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

	/**
	 * 主程序入口
	 * 该方法负责加载游戏数据，处理和计算每个建筑的升级收益，并输出排序后的结果
	 * @param args 命令行参数
	 */
	public static void main(String[] args) {
		// 初始化游戏数据
		GameData gameData = DataDeal.init(DataUtil.loadData());
		// 获取游戏等级配置
		int level = Integer.parseInt(gameData.getConfig().getOther().get("level"));

		System.out.println("单独建筑升级顺序：");
		genBuildingUpgradeData(gameData, level);
		System.out.println("单独研究所升级顺序：");
		genResearchUpgradeData(gameData, level);
	}

	/**
	 * 根据游戏数据和等级生成建筑升级数据
	 * 此方法遍历所有建筑物，并计算每栋建筑在当前等级下的升级收益
	 *
	 * @param gameData 游戏数据对象，包含游戏的配置和状态信息
	 * @param level 当前游戏等级
	 */
	private static void genBuildingUpgradeData(GameData gameData, int level) {
		// 创建一个列表用于存储计算结果
		List<Result> list = new ArrayList<>();
		// 遍历每个建筑，计算每个建筑的升级收益
		gameData.getConfig().getBuildings().forEach((k, v) -> {
			// 获取当前建筑的数据映射
			Map<String, List<Object>> bu = gameData.getBuildingMap().get(k);
			// 获取当前建筑在当前等级的最大等级
			int currentMaxLevel = getCurrentMaxLevel(gameData, level, k);
			// 获取当前建筑在当前等级的数量
			Integer num = Convert.toInt(gameData.getAmount().get(k).get(level - 1).toString(), -1);
			// 创建一个计数器，用于记录当前建筑的处理数量
			list.addAll(calculateEachUnitAndEachLevel(k, v, currentMaxLevel, num, bu));
		});
		// 打印计算结果
		printData(list, gameData, level);
	}

	/**
	 * 根据游戏数据生成研究升级数据
	 * 该方法计算在特定等级下，所有建筑的研究升级收益，并按收益比降序排序
	 *
	 * @param gameData 游戏数据对象，包含游戏配置和研究数据
	 * @param level 当前游戏等级
	 */
	static void genResearchUpgradeData(GameData gameData, int level) {
		// 创建一个列表用于存储计算结果
		List<Result> list = new ArrayList<>();
		// 遍历每个建筑，计算每个建筑的升级收益
		gameData.getConfig().getResearch().forEach((k, v) -> {
			// 获取当前建筑的数据映射
			Map<String, List<Object>> bu = gameData.getResearchMap().get(k);
			// 获取当前建筑在当前等级的最大等级
			int currentMaxLevel = getCurrentMaxLevel(bu, gameData.getConfig().getBuildings().get("研究所"));
			// 获取当前建筑在当前等级的数量
			Integer num = 1;
			// 计算当前建筑每个等级的升级收益，并将结果添加到列表中
			list.addAll(calculateEachUnitAndEachLevel(k, v, currentMaxLevel, num, bu));
		});

		// 对结果列表按照收益比降序排序并打印
		printData(list, gameData, level);
	}

	/**
	 * 计算每个单位和每个等级的升级结果
	 * 此方法根据建筑的当前等级和最大等级，计算每个单位升级到最大等级所需的经验和时间
	 *
	 * @param k 建筑名称
	 * @param v 建筑等级配置字符串，以逗号分隔
	 * @param currentMaxLevel 当前最大等级
	 * @param num 限制处理的数量
	 * @param bu 包含升级经验和时间的数据
	 * @return 返回一个包含每个单位升级结果的列表
	 */
	private static List<Result> calculateEachUnitAndEachLevel(String k, String v, int currentMaxLevel, Integer num,
			Map<String, List<Object>> bu) {
		// 初始化结果列表
		List<Result> list = new ArrayList<>();
		// 创建一个计数器，用于记录当前建筑的处理数量
		AtomicInteger count = new AtomicInteger();
		// 分割建筑配置字符串，并遍历每个配置
		Arrays.stream(v.split(",")).forEach(p -> {
			count.getAndIncrement();
			// 解析当前建筑等级
			int currentLevel = Integer.parseInt(p);
			// 从当前等级升级到最大等级
			for (int upLevel = currentLevel; upLevel < currentMaxLevel; upLevel++) {
				// 如果处理数量超过限制，则停止处理
				if (count.get() > num) {
					return;
				}
				// 获取升级所需经验和时间
				int jy;
				try {
					jy = Integer.parseInt(bu.get("经验").get(upLevel).toString());
				} catch (Exception e) {
					System.out.println(bu);
					continue;
				}
				double t = parseTimeToHours(
						Optional.ofNullable(bu.get("升级时间")).orElse(bu.get("研究时间")).get(upLevel).toString());
				// 创建一个结果对象，存储计算结果
				Result result = new Result();
				result.setLevel(upLevel);
				result.setExp(jy);
				result.setName(k);
				// 计算经验时间比，并格式化结果
				result.setRatio(Double.parseDouble(NumberUtil.decimalFormat("#.0", jy / t)));
				result.setCurrentMaxLevel(currentMaxLevel);
				result.setTime(t);
				// 将结果添加到列表中
				list.add(result);
			}
		});
		// 返回结果列表
		return list;
	}


	/**
	 * 打印数据方法，根据结果列表、游戏数据和玩家等级来展示游戏信息
	 * 该方法首先对结果列表按照收益比降序排序，然后计算累计经验和总时间，
	 * 并根据经验判断玩家是否升级，最后输出玩家的最终等级和剩余经验
	 *
	 * @param list 结果列表，包含多个Result对象，用于展示游戏结果
	 * @param gameData 游戏数据对象，包含游戏配置和玩家数据
	 * @param level 当前司令部等级，用于展示游戏情境
	 */
	private static void printData(List<Result> list, GameData gameData, int level) {
		// 对结果列表按照收益比降序排序
		list = list.stream().sorted(Comparator.comparing(Result::getTime).reversed().thenComparing(Result::getRatio).reversed())
				.toList();
		// 计算累计经验和总时间
		int sumExp = Integer.parseInt(gameData.getConfig().getOther().get("exp"));
		int playerLevel = Integer.parseInt(gameData.getConfig().getOther().get("player"));
		for (int i = 0; i < list.size(); i++) {
			int exp = Integer.parseInt(gameData.getPlayer1().get("经验").get(playerLevel - 1).toString());
			if (i == 0) {
				list.get(i).setSum(list.get(i).getExp());
				list.get(i).setTotalTime(list.get(i).getTime());
			} else {
				list.get(i).setSum(list.get(i - 1).getSum() + list.get(i).getExp());
				list.get(i).setTotalTime(list.get(i - 1).getTotalTime() + list.get(i).getTime());
			}
			sumExp += list.get(i).getExp();
			System.out.println(list.get(i));
			if (sumExp > exp) {
				sumExp -= exp;
				System.out.println(
						"玩家升级了！！！  " + playerLevel + "--->" + (playerLevel + 1) + "，剩余" + sumExp + "经验");
				playerLevel++;
			}
		}
		System.out.println(
				"在当前司令部等级：" + level + "下,玩家能够升级到" + playerLevel + "级，剩余" + sumExp + "经验");
		System.out.println("Press enter!");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
	}

	/**
	 * 获取当前最大研究等级
	 * 此方法用于根据研究所的信息和指定的研究等级字符串来确定当前的最大研究等级
	 * 它通过查找包含指定研究等级的列表位置来实现
	 *
	 * @param bu 包含研究所信息的映射，键包括"研究所"、"所需研究所等级"等，值为包含研究等级信息的列表
	 * @param researchLevelStr 指定的研究等级字符串，用于在列表中查找对应的研究等级
	 * @return 返回当前最大研究等级如果找不到匹配的研究等级，则返回0
	 */
	private static int getCurrentMaxLevel(Map<String, List<Object>> bu, String researchLevelStr) {
		// 尝试获取"研究所"列表，如果不存在，则尝试获取"所需研究所等级"或"所需研究所等级需要"列表
		List<Object> list = Optional.ofNullable(bu.get("研究所"))
				.orElse(Optional.ofNullable(bu.get("所需研究所等级")).orElse(bu.get("所需研究所等级需要")));

		// 遍历列表，寻找与指定研究等级字符串匹配的元素
		for (int i = 0; i < list.size(); i++) {
			// 如果找到匹配项，返回其在列表中的位置加1，表示当前最大研究等级
			if (list.get(i).toString().equals(researchLevelStr)) {
				return i + 1;
			}
		}

		// 如果没有找到匹配项，返回0表示没有最大研究等级
		return 0;
	}

	/**
	 * 获取当前最大等级
	 * 该方法用于根据游戏数据、等级和键值，获取当前最大等级
	 *
	 * @param gameData 游戏数据对象，包含游戏中的所有数据
	 * @param level 当前等级，用于确定在数据中的哪个等级中查找
	 * @param k 键值，用于在游戏数据中特定等级的数据中查找
	 * @return 当前最大等级如果无法获取最大等级或数据不存在，则返回-1
	 */
	private static Integer getCurrentMaxLevel(GameData gameData, int level, String k) {
		// 使用游戏数据对象的getLevel方法获取等级数据，然后根据键值k和等级level获取对应的数据
		// 由于可能获取的数据类型不一定是Integer，因此使用Convert类的toInt方法将获取的数据转换为Integer类型
		// 如果转换失败或数据不存在，则返回-1作为默认值
		return Convert.toInt(gameData.getLevel().get(k).get(level - 1).toString(), -1);
	}

	/**
	 * 将表示时间的字符串解析为小时数
	 * 该方法处理的字符串格式包括天、时、分、秒，将其转换为总小时数
	 *
	 * @param timeString 表示时间的字符串，格式如"X天 Y时 Z分 A秒"
	 * @return 解析后得到的总小时数
	 */
	private static double parseTimeToHours(String timeString) {
		// 使用正则表达式匹配时间字符串中的天、时、分、秒
		Pattern pattern = Pattern.compile("(\\d+)天|\\s*(\\d+)时|\\s*(\\d+)分|\\s*(\\d+)秒");
		Matcher matcher = pattern.matcher(timeString);

		// 初始化时间单位变量
		double days = 0;
		double hours = 0;
		double minutes = 0;
		double seconds = 0;

		// 遍历匹配结果，将匹配到的时间单位赋值给相应变量
		while (matcher.find()) {
			if (matcher.group(1) != null) {
				days = Double.parseDouble(matcher.group(1));
			} else if (matcher.group(2) != null) {
				hours = Double.parseDouble(matcher.group(2));
			} else if (matcher.group(3) != null) {
				minutes = Double.parseDouble(matcher.group(3));
			} else if (matcher.group(4) != null) {
				seconds = Double.parseDouble(matcher.group(4));
			}
		}

		// 将所有时间单位转换为小时并累加返回
		return days * 24 + hours + minutes / 60.0 + seconds / 3600.0;
	}
}