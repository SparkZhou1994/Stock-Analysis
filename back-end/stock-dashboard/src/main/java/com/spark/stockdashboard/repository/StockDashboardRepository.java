package com.spark.stockdashboard.repository;

import com.spark.stockdashboard.entity.dashboard.StockDashboardBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 股票看板数据访问接口
 * 提供对股票看板数据的CRUD操作和自定义查询
 */
@Repository
public interface StockDashboardRepository extends JpaRepository<StockDashboardBO, Long> {

    /**
     * 根据股票代码查找股票
     * @param stockCode 股票代码
     * @return 股票实体Optional
     */
    Optional<StockDashboardBO> findByStockCode(String stockCode);

    /**
     * 根据股票代码列表查找股票
     * @param stockCodes 股票代码列表
     * @return 股票实体列表
     */
    List<StockDashboardBO> findByStockCodeIn(List<String> stockCodes);

    /**
     * 根据是否自选查找股票
     * @param isWatchlist 是否自选
     * @return 股票实体列表
     */
    List<StockDashboardBO> findByIsWatchlist(Boolean isWatchlist);

    /**
     * 根据自选顺序排序查找自选股票
     * @return 按自选顺序排序的股票列表
     */
    List<StockDashboardBO> findByIsWatchlistTrueOrderByWatchlistOrderAsc();

    /**
     * 根据股票名称模糊查询
     * @param stockName 股票名称（模糊匹配）
     * @return 股票实体列表
     */
    List<StockDashboardBO> findByStockNameContaining(String stockName);

    /**
     * 根据行业分类查找股票
     * @param industry 行业分类
     * @return 股票实体列表
     */
    List<StockDashboardBO> findByIndustry(String industry);

    /**
     * 根据股票类型查找股票
     * @param stockType 股票类型
     * @return 股票实体列表
     */
    List<StockDashboardBO> findByStockType(String stockType);

    /**
     * 根据是否活跃查找股票
     * @param isActive 是否活跃
     * @return 股票实体列表
     */
    List<StockDashboardBO> findByIsActive(Boolean isActive);

    /**
     * 查找涨跌幅大于指定值的股票
     * @param minChangePercent 最小涨跌幅（百分比）
     * @param maxChangePercent 最大涨跌幅（百分比）
     * @return 股票实体列表
     */
    @Query("SELECT s FROM StockDashboardBO s WHERE s.changePercent BETWEEN :minChangePercent AND :maxChangePercent")
    List<StockDashboardBO> findByChangePercentBetween(@Param("minChangePercent") Double minChangePercent,
                                                      @Param("maxChangePercent") Double maxChangePercent);

    /**
     * 查找涨幅前N的股票
     * @param limit 返回数量限制
     * @return 按涨幅降序排列的股票列表
     */
    @Query("SELECT s FROM StockDashboardBO s ORDER BY s.changePercent DESC")
    List<StockDashboardBO> findTopGainers(@Param("limit") int limit);

    /**
     * 查找跌幅前N的股票
     * @param limit 返回数量限制
     * @return 按涨幅升序排列的股票列表
     */
    @Query("SELECT s FROM StockDashboardBO s ORDER BY s.changePercent ASC")
    List<StockDashboardBO> findTopLosers(@Param("limit") int limit);

    /**
     * 查找成交量前N的股票
     * @param limit 返回数量限制
     * @return 按成交量降序排列的股票列表
     */
    @Query("SELECT s FROM StockDashboardBO s ORDER BY s.volume DESC")
    List<StockDashboardBO> findTopVolume(@Param("limit") int limit);

    /**
     * 查找成交额前N的股票
     * @param limit 返回数量限制
     * @return 按成交额降序排列的股票列表
     */
    @Query("SELECT s FROM StockDashboardBO s ORDER BY s.turnover DESC")
    List<StockDashboardBO> findTopTurnover(@Param("limit") int limit);

    /**
     * 根据股票代码删除股票
     * @param stockCode 股票代码
     * @return 删除的记录数
     */
    int deleteByStockCode(String stockCode);

    /**
     * 批量删除股票
     * @param stockCodes 股票代码列表
     * @return 删除的记录数
     */
    int deleteByStockCodeIn(List<String> stockCodes);

    /**
     * 更新股票的自选状态
     * @param stockCode 股票代码
     * @param isWatchlist 是否自选
     * @param watchlistOrder 自选顺序
     * @return 更新的记录数
     */
    @Query("UPDATE StockDashboardBO s SET s.isWatchlist = :isWatchlist, s.watchlistOrder = :watchlistOrder WHERE s.stockCode = :stockCode")
    int updateWatchlistStatus(@Param("stockCode") String stockCode,
                              @Param("isWatchlist") Boolean isWatchlist,
                              @Param("watchlistOrder") Integer watchlistOrder);

    /**
     * 统计自选股票数量
     * @return 自选股票数量
     */
    @Query("SELECT COUNT(s) FROM StockDashboardBO s WHERE s.isWatchlist = true")
    long countWatchlistStocks();

    /**
     * 统计活跃股票数量
     * @return 活跃股票数量
     */
    @Query("SELECT COUNT(s) FROM StockDashboardBO s WHERE s.isActive = true")
    long countActiveStocks();

    /**
     * 获取所有行业分类
     * @return 行业分类列表
     */
    @Query("SELECT DISTINCT s.industry FROM StockDashboardBO s WHERE s.industry IS NOT NULL")
    List<String> findAllIndustries();

    /**
     * 获取所有股票类型
     * @return 股票类型列表
     */
    @Query("SELECT DISTINCT s.stockType FROM StockDashboardBO s WHERE s.stockType IS NOT NULL")
    List<String> findAllStockTypes();
}