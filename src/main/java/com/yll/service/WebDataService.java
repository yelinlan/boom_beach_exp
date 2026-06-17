package com.yll.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Web数据服务
 * 负责从网站抓取和解析游戏数据
 */
@Slf4j
public class WebDataService {

    private static final String BASE_URL = "https://bb.heiyu100.cn/shuju192.html";
    private static final String HOME_URL = "https://bb.heiyu100.cn/shuju.aspx";
    private static final String COMMON_PREFIX = "shuju";
    private static final String COMMON_SUFFIX = "html";
    private static final String PLAYER_DATA = "等级 经验 进攻成本 1 4 100 2 5 140 3 6 180 4 16 240 5 27 280 6 38 290 7 45 310 8 63 360 9 81 390 10 109 410 11 119 480 12 129 510 13 129 550 14 139 620 15 149 660 16 197 700 17 222 870 18 247 940 19 249 1000 20 272 1300 21 295 1400 22 295 1500 23 318 1800 24 341 1900 25 365 2100 26 412 2500 27 459 2700 28 462 2900 29 512 3400 30 562 3600 31 731 3900 32 758 4400 33 785 4700 34 785 5000 35 812 5500 36 839 5800 37 855 6000 38 899 6800 39 943 7200 40 1223 7600 41 1236 8400 42 1249 8800 43 1198 9200 44 1211 10000 45 1224 11000 46 2014 11000 47 2233 13000 48 2452 13000 49 1746 14000 50 1796 15000 51 1846 16000 52 1677 17000 53 1696 18000 54 1715 19000 55 1715 19000 56 1734 20000 57 1753 20000 58 1761 20000 59 1788 20000 60 1815 20000 61 3000 20000 62 3000 20000 63 3000 20000 64 3000 20000 65 3000 20000 66 3000 20000 67 3000 20000 68 3000 20000 69 3000 20000 70 3000 20000 71 3000 20000 72 3000 20000 73 3000 20000 74 3000 20000 75 3000 20000 76 3000 20000 77 3000 20000 78 3000 20000 79 3000 20000 80 3000 20000 81 3000 20000 82 3000 20000 83 3000 20000 84 3000 20000";

    private final String textCacheDir;

    public WebDataService(String textCacheDir) {
        this.textCacheDir = textCacheDir;
    }

    /**
     * 从网络获取并解析数据
     *
     * @return 解析后的数据映射
     */
    public Map<String, Map<String, List<Object>>> fetchAndParseData() {
        log.info("开始从网络抓取数据...");

        Map<String, String> rawDataMap = loadOrFetchRawData();

        // 添加玩家数据
        rawDataMap.put("player", PLAYER_DATA);

        // 解析所有数据
        Map<String, Map<String, List<Object>>> result = new HashMap<>();
        rawDataMap.forEach((key, value) -> {
            Map<String, List<Object>> parsed = parseHtmlData(key, value);
            result.put(key, parsed);
        });

        // 处理等级和数量数据
        processLevelAndAmountData(result);

        log.info("数据抓取和解析完成");
        return result;
    }

    /**
     * 加载或抓取原始数据
     */
    private Map<String, String> loadOrFetchRawData() {
        Path dirPath = Paths.get(textCacheDir);

        if (!Files.exists(dirPath)) {
            log.info("文本缓存目录不存在，开始抓取数据...");
            return fetchAndCacheRawData();
        } else {
            log.info("从文本缓存目录加载: {}", textCacheDir);
            return loadCachedRawData();
        }
    }

    /**
     * 抓取并缓存原始数据
     */
    private Map<String, String> fetchAndCacheRawData() {
        Map<String, String> dataMap = new HashMap<>();

        try {
            // 创建缓存目录
            Files.createDirectories(Paths.get(textCacheDir));

            // 获取所有数据页面
            List<String> htmlContents = fetchAllPages();

            // 解析每个页面并保存
            for (String html : htmlContents) {
                Document doc = Jsoup.parse(html);
                String title = doc.getElementsByClass("boxbt").text().replace("\s返回", "");

                // 查找包含特定信息的元素
                Elements elements = doc.getElementsByClass("haidaoshuju");
                String content = extractRelevantContent(elements);

                if (content != null && !content.isEmpty()) {
                    dataMap.put(title, content);

                    // 保存到文件
                    String fileName = title.replace("驻军碉堡", "地堡") + ".txt";
                    Files.write(Paths.get(textCacheDir, fileName), content.getBytes("UTF-8"));
                }
            }

        } catch (Exception e) {
            log.error("抓取数据失败", e);
            throw new RuntimeException("抓取数据失败", e);
        }

        return dataMap;
    }

    /**
     * 从缓存加载原始数据
     */
    private Map<String, String> loadCachedRawData() {
        try {
            return Arrays.stream(Objects.requireNonNull(new java.io.File(textCacheDir).listFiles()))
                    .filter(file -> file.getName().endsWith(".txt"))
                    .collect(Collectors.toMap(
                            file -> file.getName().replace(".txt", ""),
                            file -> {
                                try {
                                    return new String(Files.readAllBytes(file.toPath()), "UTF-8");
                                } catch (Exception e) {
                                    throw new RuntimeException("读取文件失败: " + file.getName(), e);
                                }
                            }
                    ));
        } catch (Exception e) {
            log.error("从缓存加载数据失败", e);
            throw new RuntimeException("从缓存加载数据失败", e);
        }
    }

    /**
     * 抓取所有数据页面
     */
    private List<String> fetchAllPages() {
        Set<String> urlSuffixes = getDataUrlSuffixes();
        return urlSuffixes.stream()
                .map(suffix -> {
                    String url = BASE_URL.replace("shuju192.html", suffix.trim());
                    return fetchPage(url);
                })
                .collect(Collectors.toList());
    }

    /**
     * 抓取单个页面
     */
    private String fetchPage(String url) {
        Map<String, String> headers = createRequestHeaders();
        String cookie = "ASP.NET_SessionId=101d0naxys32gyjoil0og22h; SL_G_WPT_TO=en; SL_GWPT_Show_Hide_tmp=1; uid=137711; ulx=1; ut1=202503141920363056; users=18482241198; uyz=b0d5aa1ee1f9f539; sjci=6; SL_wptGlobTipTmp=1";
        headers.put("Cookie", cookie);

        return HttpUtil.createGet(url).addHeaders(headers).execute().body();
    }

    /**
     * 创建请求头
     */
    private Map<String, String> createRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
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
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        return headers;
    }

    /**
     * 获取数据页面的URL后缀集合
     */
    private Set<String> getDataUrlSuffixes() {
        String html = HttpUtil.get(HOME_URL);
        Document document = Jsoup.parse(html);
        Set<String> targetUrls = new HashSet<>();

        for (Element element : document.getAllElements()) {
            for (org.jsoup.nodes.Attribute attr : element.attributes()) {
                String attrValue = attr.getValue();
                if (attrValue.startsWith(COMMON_PREFIX) && attrValue.endsWith(COMMON_SUFFIX)) {
                    targetUrls.add(attrValue);
                }
            }
        }

        return targetUrls;
    }

    /**
     * 提取相关内容
     */
    private String extractRelevantContent(Elements elements) {
        List<Element> timeElements = elements.stream()
                .filter(e -> e.text().contains("研究时间") || e.text().contains("宝石秒花费"))
                .collect(Collectors.toList());

        if (timeElements.isEmpty()) {
            return null;
        }

        return timeElements.get(0).text();
    }

    /**
     * 解析HTML数据
     */
    private Map<String, List<Object>> parseHtmlData(String key, String htmlContent) {
        Document document = Jsoup.parse(htmlContent);
        String text = document.text();
        String[] tokens = text.split("\\s");

        List<String> keys = new ArrayList<>();
        int index = 0;

        // 提取关键字段名
        for (String token : tokens) {
            try {
                Integer.parseInt(token);
            } catch (NumberFormatException e) {
                keys.add(token);
                index++;
                continue;
            }
            break;
        }

        // 构建数据映射
        Map<String, List<Object>> dataMap = new LinkedHashMap<>();
        for (int i = index; i < tokens.length; i++) {
            String dataKey = keys.get((i - index) % keys.size());
            dataMap.computeIfAbsent(dataKey, k -> new ArrayList<>()).add(tokens[i]);
        }

        return dataMap;
    }

    /**
     * 处理等级和数量数据
     */
    private void processLevelAndAmountData(Map<String, Map<String, List<Object>>> result) {
        Map<String, List<Object>> levelData = new LinkedHashMap<>();
        Map<String, List<Object>> amountData = new LinkedHashMap<>();
        List<String> keysToRemove = new ArrayList<>();

        result.forEach((key, value) -> {
            if (key.contains("level_")) {
                levelData.putAll(value);
                keysToRemove.add(key);
            } else if (key.contains("amount_")) {
                amountData.putAll(value);
                keysToRemove.add(key);
            }
        });

        result.put("司令部_level", levelData);
        result.put("司令部_amount", amountData);
        keysToRemove.forEach(result::remove);
    }
}
