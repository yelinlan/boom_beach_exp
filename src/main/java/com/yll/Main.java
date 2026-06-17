package com.yll;

import cn.hutool.core.convert.Convert;
import com.yll.model.GameData;
import com.yll.model.PlayerConfig;
import com.yll.model.UpgradeResult;
import com.yll.repository.DataRepository;
import com.yll.repository.FileDataRepository;
import com.yll.service.ConfigService;
import com.yll.service.UpgradeCalculationService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Scanner;

/**
 * 海岛奇兵数据分析工具 - 主程序入口
 *
 * <p>该工具通过分析游戏数据，计算建筑和研究的升级收益，
 * 并提供最优升级路线建议。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>从网站抓取或本地缓存加载游戏数据</li>
 *   <li>管理玩家配置（建筑等级、研究等级等）</li>
 *   <li>计算每个升级项目的经验/时间收益比</li>
 *   <li>按收益比排序并输出升级建议</li>
 *   <li>追踪玩家等级提升情况</li>
 * </ul>
 */
@Slf4j
public class Main {

    private final DataRepository dataRepository;
    private final ConfigService configService;
    private final UpgradeCalculationService upgradeService;

    public Main() {
        this.dataRepository = new FileDataRepository();
        this.configService = new ConfigService();
        this.upgradeService = new UpgradeCalculationService();
    }

    /**
     * 程序主入口
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        log.info("=== 海岛奇兵数据分析工具启动 ===");

        try {
            Main app = new Main();
            app.run();
        } catch (Exception e) {
            log.error("程序执行出错", e);
            System.err.println("程序执行出错: " + e.getMessage());
            System.exit(1);
        }

        log.info("=== 程序执行完毕 ===");
    }

    /**
     * 执行主流程
     */
    public void run() {
        // 1. 加载游戏数据
        GameData gameData = loadGameData();

        // 2. 加载或创建玩家配置
        PlayerConfig config = loadOrCreateConfig(gameData);

        // 3. 获取司令部等级
        int commanderLevel = Convert.toInt(config.getOther().get("level"), 1);
        log.info("当前司令部等级: {}", commanderLevel);

        // 4. 计算并显示建筑升级建议
        System.out.println("\n========== 单独建筑升级顺序 ==========");
        calculateAndDisplayBuildingUpgrades(gameData, config, commanderLevel);

        // 5. 计算并显示研究升级建议
        System.out.println("\n========== 单独研究所升级顺序 ==========");
        calculateAndDisplayResearchUpgrades(gameData, config);

        // 6. 等待用户确认
        waitForExit();
    }

    /**
     * 加载游戏数据
     */
    private GameData loadGameData() {
        log.info("步骤1: 加载游戏数据...");
        return dataRepository.loadData();
    }

    /**
     * 加载或创建玩家配置
     */
    private PlayerConfig loadOrCreateConfig(GameData gameData) {
        log.info("步骤2: 加载玩家配置...");

        // 保存配置模板（如果不存在）
        configService.saveConfigTemplate(gameData);

        // 加载配置
        PlayerConfig config = configService.loadConfig();

        // 确保玩家经验数据可用
        configService.setPlayerExperienceData(config, gameData);

        return config;
    }

    /**
     * 计算并显示建筑升级建议
     */
    private void calculateAndDisplayBuildingUpgrades(GameData gameData, PlayerConfig config, int commanderLevel) {
        log.info("步骤3: 计算建筑升级收益...");

        List<UpgradeResult> results = upgradeService.calculateBuildingUpgrades(gameData, config, commanderLevel);

        if (results.isEmpty()) {
            System.out.println("没有可升级的建筑！");
        } else {
            upgradeService.processAndDisplayResults(results, config);
        }
    }

    /**
     * 计算并显示研究升级建议
     */
    private void calculateAndDisplayResearchUpgrades(GameData gameData, PlayerConfig config) {
        log.info("步骤4: 计算研究升级收益...");

        List<UpgradeResult> results = upgradeService.calculateResearchUpgrades(gameData, config);

        if (results.isEmpty()) {
            System.out.println("没有可升级的研究项目！");
        } else {
            upgradeService.processAndDisplayResults(results, config);
        }
    }

    /**
     * 等待用户确认后退出
     */
    private void waitForExit() {
        System.out.println("\n按回车键退出...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }
}
