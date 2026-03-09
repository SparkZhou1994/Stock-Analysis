package com.spark.stockdashboard.repository;

import com.spark.stockdashboard.entity.dashboard.HistoricalDataBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 历史数据访问接口
 * 提供对股票和基金历史数据的CRUD操作和自定义查询
 */
@Repository
public interface HistoricalDataRepository extends JpaRepository<HistoricalDataBO, Long> {

    /**
     * 根据标的代码和交易日期查找历史数据
     * @param code 标的代码
     * @param tradeDate 交易日期
     * @return 历史数据实体Optional
     */
    Optional<HistoricalDataBO> findByCodeAndTradeDate(String code, LocalDate tradeDate);

    /**
     * 根据标的代码查找历史数据
     * @param code 标的代码
     * @return 历史数据实体列表
     */
    List<HistoricalDataBO> findByCode(String code);

    /**
     * 根据标的代码和类型查找历史数据
     * @param code 标的代码
     * @param type 标的类型
     * @return 历史数据实体列表
     */
    List<HistoricalDataBO> findByCodeAndType(String code, String type);

    /**
     * 根据标的代码查找最新历史数据
     * @param code 标的代码
     * @return 最新历史数据实体Optional
     */
    @Query("SELECT h FROM HistoricalDataBO h WHERE h.code = :code ORDER BY h.tradeDate DESC")
    Optional<HistoricalDataBO> findLatestByCode(@Param("code") String code);

    /**
     * 根据标的代码查找指定日期范围的历史数据
     * @param code 标的代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 历史数据实体列表
     */
    @Query("SELECT h FROM HistoricalDataBO h WHERE h.code = :code AND h.tradeDate BETWEEN :startDate AND :endDate ORDER BY h.tradeDate ASC")
    List<HistoricalDataBO> findByCodeAndTradeDateBetween(@Param("code") String code,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    /**
     * 根据标的代码查找最近N天的历史数据
     * @param code 标的代码
     * @param days 天数
     * @return 历史数据实体列表
     */
    @Query("SELECT h FROM HistoricalDataBO h WHERE h.code = :code ORDER BY h.tradeDate DESC")
    List<HistoricalDataBO> findRecentByCode(@Param("code") String code, @Param("days") int days);

    /**
     * 根据标的代码和类型查找指定日期范围的历史数据
     * @param code 标的代码
     * @param type 标的类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 历史数据实体列表
     */
    @Query("SELECT h FROM HistoricalDataBO h WHERE h.code = :code AND h.type = :type AND h.tradeDate BETWEEN :startDate AND :endDate ORDER BY h.tradeDate ASC")
    List<HistoricalDataBO> findByCodeAndTypeAndTradeDateBetween(@Param("code") String code,
                                                                @Param("type") String type,
                                                                @Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);

    /**
     * 根据标的代码查找最高价
     * @param code 标的代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 最高价
     */
    @Query("SELECT MAX(h.highPrice) FROM HistoricalDataBO h WHERE h.code = :code AND h.tradeDate BETWEEN :startDate AND :endDate")
    Optional<Double> findMaxHighPrice(@Param("code") String code,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    /**
     * 根据标的代码查找最低价
     * @param code 标的代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 最低价
     */
    @Query("SELECT MIN(h.lowPrice) FROM HistoricalDataBO h WHERE h.code = :code AND h.tradeDate BETWEEN :startDate AND :endDate")
    Optional<Double> findMinLowPrice(@Param("code") String code,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * 根据标的代码计算平均成交量
     * @param code 标的代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 平均成交量
     */
    @Query("SELECT AVG(h.volume) FROM HistoricalDataBO h WHERE h.code = :code AND h.tradeDate BETWEEN :startDate AND :endDate")
    Optional<Double> findAverageVolume(@Param("code") String code,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * 根据标的代码计算平均成交额
     * @param code 标的代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 平均成交额
     */
    @Query("SELECT AVG(h.turnover) FROM HistoricalDataBO h WHERE h.code = :code AND h.tradeDate BETWEEN :startDate AND :endDate")
    Optional<Double> findAverageTurnover(@Param("code") String code,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * 根据标的代码查找涨跌幅最大的交易日
     * @param code 标的代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 涨跌幅最大的历史数据
     */
    @Query("SELECT h FROM HistoricalDataBO h WHERE h.code = :code AND h.tradeDate BETWEEN :startDate AND :endDate ORDER BY ABS(h.changePercent) DESC")
    Optional<HistoricalDataBO> findMaxChangeDay(@Param("code") String code,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * 根据标的代码和交易日期删除历史数据
     * @param code 标的代码
     * @param tradeDate 交易日期
     * @return 删除的记录数
     */
    int deleteByCodeAndTradeDate(String code, LocalDate tradeDate);

    /**
     * 根据标的代码删除历史数据
     * @param code 标的代码
     * @return 删除的记录数
     */
    int deleteByCode(String code);

    /**
     * 根据标的代码和日期范围删除历史数据
     * @param code 标的代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 删除的记录数
     */
    @Query("DELETE FROM HistoricalDataBO h WHERE h.code = :code AND h.tradeDate BETWEEN :startDate AND :endDate")
    int deleteByCodeAndTradeDateBetween(@Param("code") String code,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * 统计标的的历史数据数量
     * @param code 标的代码
     * @return 历史数据数量
     */
    @Query("SELECT COUNT(h) FROM HistoricalDataBO h WHERE h.code = :code")
    long countByCode(@Param("code") String code);

    /**
     * 获取标的的最早交易日期
     * @param code 标的代码
     * @return 最早交易日期
     */
    @Query("SELECT MIN(h.tradeDate) FROM HistoricalDataBO h WHERE h.code = :code")
    Optional<LocalDate> findEarliestTradeDate(@Param("code") String code);

    /**
     * 获取标的的最新交易日期
     * @param code 标的代码
     * @return 最新交易日期
     */
    @Query("SELECT MAX(h.tradeDate) FROM HistoricalDataBO h WHERE h.code = :code")
    Optional<LocalDate> findLatestTradeDate(@Param("code") String code);

    /**
     * 根据复权类型查找历史数据
     * @param code 标的代码
     * @param adjustmentType 复权类型
     * @return 历史数据实体列表
     */
    List<HistoricalDataBO> findByCodeAndAdjustmentType(String code, String adjustmentType);

    /**
     * 根据标的代码查找不同复权类型的历史数据
     * @param code 标的代码
     * @return 复权类型列表
     */
    @Query("SELECT DISTINCT h.adjustmentType FROM HistoricalDataBO h WHERE h.code = :code")
    List<String> findAdjustmentTypesByCode(@Param("code") String code);

    /**
     * 批量插入历史数据
     * @param historicalDataList 历史数据列表
     * @return 保存后的历史数据列表
     */
    default List<HistoricalDataBO> saveAllHistoricalData(List<HistoricalDataBO> historicalDataList) {
        return saveAll(historicalDataList);
    }
}