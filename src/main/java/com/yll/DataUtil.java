package com.yll;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.stream.Collectors;

/**
 *@项目名称: boom_beach_exp
 *@类名称: DataUtil
 *@类描述:
 *@创建人: yll
 *@创建时间: 2025/3/18 22:36
 **/
public class DataUtil {

	private static final String player = "等级 经验 进攻成本 1 4 100 2 5 140 3 6 180 4 16 240 5 27 280 6 38 290 7 45 310 8 63 360 9 81 390 10 109 410 11 119 480 12 129 510 13 129 550 14 139 620 15 149 660 16 197 700 17 222 870 18 247 940 19 249 1000 20 272 1300 21 295 1400 22 295 1500 23 318 1800 24 341 1900 25 365 2100 26 412 2500 27 459 2700 28 462 2900 29 512 3400 30 562 3600 31 731 3900 32 758 4400 33 785 4700 34 785 5000 35 812 5500 36 839 5800 37 855 6000 38 899 6800 39 943 7200 40 1223 7600 41 1236 8400 42 1249 8800 43 1198 9200 44 1211 10000 45 1224 11000 46 2014 11000 47 2233 13000 48 2452 13000 49 1746 14000 50 1796 15000 51 1846 16000 52 1677 17000 53 1696 18000 54 1715 19000 55 1715 19000 56 1734 20000 57 1753 20000 58 1761 20000 59 1788 20000 60 1815 20000 61 3000 20000 62 3000 20000 63 3000 20000 64 3000 20000 65 3000 20000 66 3000 20000 67 3000 20000 68 3000 20000 69 3000 20000 70 3000 20000 71 3000 20000 72 3000 20000 73 3000 20000 74 3000 20000 75 3000 20000 76 3000 20000 77 3000 20000 78 3000 20000 79 3000 20000 80 3000 20000";
	public static final String DATA_PATH = System.getProperty("user.dir")+"\\unitMap.txt";
	public static final String TEXT = System.getProperty("user.dir")+"\\text\\";
	public static final String URL = "https://bb.heiyu100.cn/shuju192.html";
	public static final String HOME_URL = "https://bb.heiyu100.cn/shuju.aspx";
	public static final String COMMON_PREFIX = "shuju";
	public static final String COMMON_SUFFIX = "html";


	/**
	 * 加载数据方法
	 * 该方法首先检查指定路径下的文件是否存在，根据存在与否来决定数据的加载方式
	 * 如果文件不存在，表明是首次加载，将通过fetchData方法从源头获取数据
	 * 如果文件存在，则从文件中读取已保存的数据并解析为Map对象返回
	 *
	 * @return 返回一个映射，其中键是字符串，值是包含字符串键和对象列表的LinkedHashMap
	 */
	public static Map<String, Map<String, List<Object>>> loadData() {
		// 定义unitMap变量，用于存储加载的数据
		Map<String, Map<String, List<Object>>> unitMap;

		// 检查指定路径下的文件是否存在
		if (!FileUtil.exist(DATA_PATH)) {
			// 如果文件不存在，输出首次加载信息，并调用fetchData方法获取数据
			System.out.println("首次加载。。。");
			unitMap = fetchData();
			unitMap.remove("雕塑");
			System.out.println("加载完毕。。。");
		} else {
			// 如果文件存在，从文件中读取数据并解析为Map对象
			unitMap = JSONUtil.toBean(FileUtil.readUtf8String(DATA_PATH), Map.class);
			System.out.println("加载数据完毕");
		}

		return unitMap;
	}

	/**
	 * 获取数据
	 * 此方法从某个文本源解析数据，并将其组织成一个嵌套的映射结构
	 * 它首先将文本分割成单词，然后根据一定规则将这些单词分类存储
	 *
	 * @return 返回一个映射，其中键是单位名称，值是另一个映射，该映射的键是分类键，值是属于该分类的项目列表
	 */
	private static Map<String, Map<String, List<Object>>> fetchData() {
		// 初始化一个有序的映射，用于存储最终的数据结构
		Map<String, Map<String, List<Object>>> unitMap = new HashMap<>();

		Map<String, String> temp = new HashMap<>();
		if (!FileUtil.exist(TEXT)){
			System.out.println("解析成文本");
			temp = toText();
			temp.forEach((k, v) -> FileUtil.writeUtf8String(v, TEXT+k+".txt"));
		}else{
			System.out.println("加载已解析文本");
			temp = Arrays.stream(FileUtil.ls(TEXT)).collect(
					Collectors.toMap(p -> p.getName().replace(".txt", ""),
							p -> FileUtil.readString(p, "UTF-8")));
		}
		// 加入玩家经验和等级
		temp.put("player", player);
		// 遍历文本数据，其中键是单位名称，值是文本内容
		temp.forEach((k, v) -> {
			// 解析HTML文本
			Document document = Jsoup.parse(v);
			// 提取纯文本内容
			String text = document.text();
			// 将文本按空格分割成单词数组
			String[] strs = text.split("\s");

			// 初始化长度计数器和关键字列表
			List<String> keyList = new ArrayList<>();

			int index =0;
			// 遍历单词数组，识别关键字
			for (String key : strs) {
				try {
					// 尝试将单词解析为整数，如果失败，则认为是一个关键字
					Integer.parseInt(key);
				} catch (NumberFormatException e) {
					keyList.add(key);
					index++;
					continue;
				}
				break;
			}

			final String[] keyStr = {String.join(",", keyList)};
			Map<String, String> temp1 = new LinkedHashMap<>();
			Map<String, String> kv = new LinkedHashMap<>();
			kv.put("黄金,木材,石材,钢材","存储");
			kv.put("木材,石材,钢材", "升级费用");
			kv.put("木材,钢材", "升级费用");
			kv.put("木材,石材", "升级费用");
			//特殊处理
			kv.forEach((k1, v1) -> {
				if (keyStr[0].contains(k1) && keyStr[0].contains(v1)) {
					keyStr[0] = keyStr[0].replace(k1, "");
					temp1.put(k1,v1);
				}
			});
			temp1.forEach((key, value) -> keyStr[0] = keyStr[0].replace(value, key));
			keyList = Arrays.stream(keyStr[0].split(",")).filter(StrUtil::isNotBlank).toList();

			// 初始化一个有序的映射，用于存储分类数据
			LinkedHashMap<String, List<Object>> map = new LinkedHashMap<>();

			// 遍历单词数组，根据关键字进行分类
			for (int i = index; i < strs.length; i++) {
				// 根据关键字索引，将当前单词添加到对应的列表中
				map.computeIfAbsent(keyList.get((i-index) % keyList.size()), e -> new ArrayList<>()).add(strs[i]);
			}

			// 将分类数据添加到最终的数据结构中
			unitMap.put(k, map);
		});

		LinkedHashMap<String, List<Object>> level = new LinkedHashMap<>();
		LinkedHashMap<String, List<Object>> amount = new LinkedHashMap<>();
		List<String> keys = new ArrayList<>();
		unitMap.forEach((k, v) -> {
			if (k.contains("level_")) {
				level.putAll(v);
				keys.add(k);
			} else if (k.contains("amount_")) {
				amount.putAll(v);
				keys.add(k);
			}
		});
		unitMap.put("司令部_level", level);
		unitMap.put("司令部_amount", amount);
		keys.forEach(unitMap::remove);

		// 将最终的数据结构以JSON格式写入文件
		FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(unitMap), DATA_PATH);

		// 返回最终的数据结构
		return unitMap;
	}

	/**
	 * 发送HTTP GET请求并获取结果
	 * 此方法用于向指定的URL发送一个GET请求，并携带一系列预定义的请求头，包括Cookie，以获取网页内容
	 *
	 * @param url 请求的目标URL
	 * @return 返回请求的结果内容
	 */
	private static String getResult(String url) {
		// 创建请求头
		Map<String, String> headers = new HashMap<>();
		headers.put("accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
		headers.put("accept-language", "zh-CN,zh;q=0.9");
		headers.put("priority", "u=0, i");
		headers.put("sec-ch-ua", "\"Not(A:Brand\";v=\"99\", \"Google Chrome\";v=\"133\", \"Chromium\";v=\"133\"");
		headers.put("sec-ch-ua-mobile", "?0");
		headers.put("sec-ch-ua-platform", "\"Windows\"");
		headers.put("sec-fetch-dest", "document");
		headers.put("sec-fetch-mode", "navigate");
		headers.put("sec-fetch-site", "none");
		headers.put("sec-fetch-user", "?1");
		headers.put("upgrade-insecure-requests", "1");
		headers.put("user-agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");

		// 设置Cookie
		String cookie = "ASP.NET_SessionId=101d0naxys32gyjoil0og22h; SL_G_WPT_TO=en; SL_GWPT_Show_Hide_tmp=1; uid=137711; ulx=1; ut1=202503141920363056; users=18482241198; uyz=b0d5aa1ee1f9f539; sjci=6; SL_wptGlobTipTmp=1";
		headers.put("Cookie", cookie);

		// 发送请求
		String result = HttpUtil.createGet(url).addHeaders(headers).execute().body();
		return result;
	}

	/**
	 * 将获取的数据转换为文本信息并存储在映射中
	 * 此方法主要用于爬取包含特定关键字的数据，并将其整理到一个映射中，以便进一步处理或存储
	 *
	 * @return 包含转换后数据的映射
	 */
	private static Map<String, String> toText() {
		// 创建一个映射来存储转换后的数据
		Map<String, String> dataMap = new HashMap<>();

		// 获取所有数据并逐个处理
		getAllShuJu().forEach(str -> {
			// 解析HTML字符串
			Document document = Jsoup.parse(str);

			// 提取并处理标题信息
			String title = document.getElementsByClass("boxbt").text().replace("\s返回", "");

			// 获取所有所需的数据元素
			Elements elements = document.getElementsByClass("haidaoshuju");

			// 筛选出包含"宝石秒花费"的数据项
			List<Element> list = elements.stream().filter(q -> q.text().contains("宝石秒花费")).toList();

			// 如果找到一个符合条件的数据项
			if (list.size() == 1) {
				// 尝试寻找包含"研究时间"的数据项
				List<Element> list1 = elements.stream().filter(q -> q.text().contains("研究时间")).toList();

				// 如果找到"研究时间"的数据项
				if (!list1.isEmpty()) {
					// 将标题和研究时间添加到映射中
					dataMap.put(title, list1.get(0).text());
				} else {
					// 如果没有找到"研究时间"的数据项，移除不必要的列标题，避免干扰
					List<Element> list2 = elements.stream().filter(q -> q.text().contains("宝石秒花费")).toList();
					// 将标题和宝石秒花费添加到映射中
					dataMap.put(title, list2.get(0).text());

					// 寻找包含特定关键字的数据项，可能是建筑物的最大等级或最大数量
					List<Element> list3 = elements.stream()
							.filter(q -> q.text().contains("司令部-建筑物Max等级") || q.text()
									.contains("司令部-最大编号of建筑物s")).toList();

					// 如果找到了相关数据项
					if (!list3.isEmpty()) {
						// 移除不必要的列标题，避免干扰，并将数据添加到映射中
						list3.forEach(q -> {
							boolean isMax = q.text().contains("司令部-建筑物Max等级");
							q.select("caption").remove();
							dataMap.put(
									isMax ? "level_" + IdUtil.fastSimpleUUID() : "amount_" + IdUtil.fastSimpleUUID(),
									q.text());
						});
					}
				}
			}
		});

		// 返回包含所有转换后数据的映射
		return dataMap;
	}

	/**
	 * 获取所有数据信息
	 *
	 * 该方法从getAllResult()方法获取结果流，并使用Jsoup库解析每个结果的HTML内容
	 * 具体来说，它提取每个结果中类名为"shujuxx"的元素的HTML内容，将这些内容收集到一个新的字符串列表中
	 *
	 * @return 包含所有数据信息HTML内容的列表
	 */
	private static List<String> getAllShuJu() {
		return getAllResult().stream().map(p -> Jsoup.parse(p).getElementsByClass("shujuxx").html()).toList();
	}

	/**
	 * 获取所有结果数据
	 * 本函数通过调用多个数据接口获取数据，合并后返回
	 *
	 * @return 包含所有结果数据的列表
	 */
	private static List<String> getAllResult() {
		// 基础URL，用于构建完整的数据请求URL
		String baseUrl = URL;

		// 通过流处理，将每个数据接口的后缀转换为完整的URL，并获取数据
		return getDataUrlSuffix().stream().map(p -> getResult(baseUrl.replace("shuju192.html", p.trim()))).toList();
	}

	/**
	 * 获取数据页面的URL后缀集合
	 * 该方法通过访问一个特定的URL来获取页面内容，并解析该页面中的所有元素和属性
	 * 它特别关注属性值以"shuju"开头且以"html"结尾的属性，将其视为目标URL后缀并收集起来
	 *
	 * @return Set<String> 包含符合条件的URL后缀的集合
	 */
	private static Set<String> getDataUrlSuffix() {
		// 从指定的URL获取HTML内容
		String html = HttpUtil.get(HOME_URL);
		// 解析HTML内容为Document对象
		Document document = Jsoup.parse(html);
		// 创建一个集合用于存储目标URL后缀
		Set<String> targetUrls = new HashSet<>();
		// 获取文档中的所有元素
		Elements allElements = document.getAllElements();
		// 遍历所有元素
		for (Element element : allElements) {
			// 遍历元素的所有属性
			for (org.jsoup.nodes.Attribute attr : element.attributes()) {
				String attrValue = attr.getValue();
				// 检查属性值是否以 shuju 开头且以 html 结尾
				if (attrValue.startsWith(COMMON_PREFIX) && attrValue.endsWith(COMMON_SUFFIX)) {
					targetUrls.add(attrValue);
				}
			}
		}
		// 返回收集到的目标URL后缀集合
		return targetUrls;
	}


}