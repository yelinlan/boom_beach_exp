package com.yll;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *@项目名称: boom_beach_exp
 *@类名称: GameData
 *@类描述:
 *@创建人: yll
 *@创建时间: 2025/3/18 22:46
 **/
@Data
public class GameData {

	Map<String, Map<String, List<Object>>> researchMap = new HashMap<>();
	Map<String, Map<String, List<Object>>> buildingMap = new HashMap<>();
	Map<String, List<Object>> level = new HashMap<>();
	Map<String, List<Object>> amount = new HashMap<>();
	Map<String, List<Object>> player1 = new HashMap<>();
	Config config;


}