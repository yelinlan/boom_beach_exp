package com.yll;

import cn.hutool.core.util.NumberUtil;
import lombok.Data;

/**
 *@项目名称: boom_beach_exp
 *@类名称: Result
 *@类描述:
 *@创建人: yll
 *@创建时间: 2025/3/19 00:07
 **/
@Data
public class Result {

	private String name;
	private int level;
	private int exp;
	private double ratio;
	private int sum;
	private int currentMaxLevel;
	private double time;
	private double totalTime;

	@Override
	public String toString() {
		return name + "：" + level + "->"+(level+1)+ "（ max：" + currentMaxLevel +"） exp："+ exp + " ratio：" + ratio + " sumExp："
				+ sum +" totalTime:"+  DataUtil.formatTime(totalTime)
				+"  spend:"+time+"h";
	}
}