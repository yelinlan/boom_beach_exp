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

		// 创建一个列表用于存储计算结果
		List<Result> list = new ArrayList<>();

		// 获取游戏等级配置
		int level = Integer.parseInt(gameData.getConfig().getOther().get("level"));
		List<Result> finalList = list;

		// 遍历每个建筑，计算每个建筑的升级收益
		gameData.getConfig().getBuildings().forEach((k, v) -> {
			// 获取当前建筑的数据映射
			Map<String, List<Object>> bu = gameData.getBuildingMap().get(k);
			// 获取当前建筑在当前等级的数量
			Integer num = Convert.toInt(gameData.getAmount().get(k).get(level - 1).toString(), -1);
			// 创建一个计数器，用于记录当前建筑的处理数量
			AtomicInteger count = new AtomicInteger();
			// 获取当前建筑在当前等级的最大等级
			int currentMaxLevel = Convert.toInt(gameData.getLevel().get(k).get(level - 1).toString(), -1);
			// 分割建筑配置字符串，并遍历每个配置
			Arrays.stream(v.split(",")).forEach(p -> {
				count.getAndIncrement();
				int currentLevel = Integer.parseInt(p);
				// 从当前等级升级到最大等级
				for (int upLevel = currentLevel; upLevel < currentMaxLevel; upLevel++) {
					if (count.get() > num) {
						return;
					}
					// 获取升级所需经验和时间
					int jy = Integer.parseInt(bu.get("经验").get(upLevel).toString());
					double t = parseTimeToHours(bu.get("升级时间").get(upLevel).toString());
					// 创建一个结果对象，存储计算结果
					Result result = new Result();
					result.setLevel(upLevel);
					result.setExp(jy);
					result.setName(k);
					result.setRatio(Double.parseDouble(NumberUtil.decimalFormat("#.0", jy / t)));
					result.setCurrentMaxLevel(currentMaxLevel);
					result.setTime(t);
					finalList.add(result);
				}
			});
		});

		// 对结果列表按照收益比降序排序
		list = list.stream().sorted(Comparator.comparing(Result::getRatio).reversed()).toList();
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

	private static double parseTimeToHours(String timeString) {
		Pattern pattern = Pattern.compile("(\\d+)天|\\s*(\\d+)时|\\s*(\\d+)分|\\s*(\\d+)秒");
		Matcher matcher = pattern.matcher(timeString);

		double days = 0;
		double hours = 0;
		double minutes = 0;
		double seconds = 0;

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

		return days * 24 + hours + minutes / 60.0 + seconds / 3600.0;
	}
}