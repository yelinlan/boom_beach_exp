package com.yll.repository;

import com.yll.model.GameData;
import java.util.Map;

/**
 * 数据仓储接口
 * 定义游戏数据的加载和存储操作
 */
public interface DataRepository {

    /**
     * 加载游戏数据
     * 优先从缓存加载，如果缓存不存在则从网络获取
     *
     * @return 游戏数据对象
     */
    GameData loadData();

    /**
     * 从网络抓取原始数据
     *
     * @return 原始数据映射
     */
    Map<String, Map<String, java.util.List<Object>>> fetchFromWeb();

    /**
     * 保存数据到缓存
     *
     * @param data 要缓存的数据
     */
    void saveToCache(Map<String, Map<String, java.util.List<Object>>> data);

    /**
     * 从缓存加载数据
     *
     * @return 缓存的数据，如果不存在返回null
     */
    Map<String, Map<String, java.util.List<Object>>> loadFromCache();

    /**
     * 检查缓存是否存在
     *
     * @return true如果缓存存在
     */
    boolean cacheExists();
}
