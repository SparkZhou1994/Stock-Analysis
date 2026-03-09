package com.spark.stockdashboard.repository;

import com.spark.stockdashboard.entity.dashboard.FundDashboardBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 基金看板数据访问接口
 * 提供对基金看板数据的CRUD操作和自定义查询
 */
@Repository
public interface FundDashboardRepository extends JpaRepository<FundDashboardBO, Long> {

    /**
     * 根据基金代码查找基金
     * @param fundCode 基金代码
     * @return 基金实体Optional
     */
    Optional<FundDashboardBO> findByFundCode(String fundCode);

    /**
     * 根据基金代码列表查找基金
     * @param fundCodes 基金代码列表
     * @return 基金实体列表
     */
    List<FundDashboardBO> findByFundCodeIn(List<String> fundCodes);

    /**
     * 根据是否自选查找基金
     * @param isWatchlist 是否自选
     * @return 基金实体列表
     */
    List<FundDashboardBO> findByIsWatchlist(Boolean isWatchlist);

    /**
     * 根据自选顺序排序查找自选基金
     * @return 按自选顺序排序的基金列表
     */
    List<FundDashboardBO> findByIsWatchlistTrueOrderByWatchlistOrderAsc();

    /**
     * 根据基金名称模糊查询
     * @param fundName 基金名称（模糊匹配）
     * @return 基金实体列表
     */
    List<FundDashboardBO> findByFundNameContaining(String fundName);

    /**
     * 根据基金类型查找基金
     * @param fundType 基金类型
     * @return 基金实体列表
     */
    List<FundDashboardBO> findByFundType(String fundType);

    /**
     * 根据基金管理公司查找基金
     * @param fundCompany 基金管理公司
     * @return 基金实体列表
     */
    List<FundDashboardBO> findByFundCompany(String fundCompany);

    /**
     * 根据风险等级查找基金
     * @param riskLevel 风险等级
     * @return 基金实体列表
     */
    List<FundDashboardBO> findByRiskLevel(String riskLevel);

    /**
     * 根据是否活跃查找基金
     * @param isActive 是否活跃
     * @return 基金实体列表
     */
    List<FundDashboardBO> findByIsActive(Boolean isActive);

    /**
     * 查找日涨跌幅大于指定值的基金
     * @param minChangePercent 最小涨跌幅（百分比）
     * @param maxChangePercent 最大涨跌幅（百分比）
     * @return 基金实体列表
     */
    @Query("SELECT f FROM FundDashboardBO f WHERE f.dailyChangePercent BETWEEN :minChangePercent AND :maxChangePercent")
    List<FundDashboardBO> findByDailyChangePercentBetween(@Param("minChangePercent") Double minChangePercent,
                                                          @Param("maxChangePercent") Double maxChangePercent);

    /**
     * 查找涨幅前N的基金
     * @param limit 返回数量限制
     * @return 按日涨幅降序排列的基金列表
     */
    @Query("SELECT f FROM FundDashboardBO f ORDER BY f.dailyChangePercent DESC")
    List<FundDashboardBO> findTopGainers(@Param("limit") int limit);

    /**
     * 查找跌幅前N的基金
     * @param limit 返回数量限制
     * @return 按日涨幅升序排列的基金列表
     */
    @Query("SELECT f FROM FundDashboardBO f ORDER BY f.dailyChangePercent ASC")
    List<FundDashboardBO> findTopLosers(@Param("limit") int limit);

    /**
     * 查找年化收益前N的基金
     * @param limit 返回数量限制
     * @return 按年涨跌幅降序排列的基金列表
     */
    @Query("SELECT f FROM FundDashboardBO f ORDER BY f.yearlyChangePercent DESC")
    List<FundDashboardBO> findTopYearlyPerformers(@Param("limit") int limit);

    /**
     * 查找基金规模前N的基金
     * @param limit 返回数量限制
     * @return 按基金规模降序排列的基金列表
     */
    @Query("SELECT f FROM FundDashboardBO f ORDER BY f.fundSize DESC")
    List<FundDashboardBO> findTopFundSize(@Param("limit") int limit);

    /**
     * 根据基金代码删除基金
     * @param fundCode 基金代码
     * @return 删除的记录数
     */
    int deleteByFundCode(String fundCode);

    /**
     * 批量删除基金
     * @param fundCodes 基金代码列表
     * @return 删除的记录数
     */
    int deleteByFundCodeIn(List<String> fundCodes);

    /**
     * 更新基金的自选状态
     * @param fundCode 基金代码
     * @param isWatchlist 是否自选
     * @param watchlistOrder 自选顺序
     * @return 更新的记录数
     */
    @Query("UPDATE FundDashboardBO f SET f.isWatchlist = :isWatchlist, f.watchlistOrder = :watchlistOrder WHERE f.fundCode = :fundCode")
    int updateWatchlistStatus(@Param("fundCode") String fundCode,
                              @Param("isWatchlist") Boolean isWatchlist,
                              @Param("watchlistOrder") Integer watchlistOrder);

    /**
     * 统计自选基金数量
     * @return 自选基金数量
     */
    @Query("SELECT COUNT(f) FROM FundDashboardBO f WHERE f.isWatchlist = true")
    long countWatchlistFunds();

    /**
     * 统计活跃基金数量
     * @return 活跃基金数量
     */
    @Query("SELECT COUNT(f) FROM FundDashboardBO f WHERE f.isActive = true")
    long countActiveFunds();

    /**
     * 获取所有基金类型
     * @return 基金类型列表
     */
    @Query("SELECT DISTINCT f.fundType FROM FundDashboardBO f WHERE f.fundType IS NOT NULL")
    List<String> findAllFundTypes();

    /**
     * 获取所有基金管理公司
     * @return 基金管理公司列表
     */
    @Query("SELECT DISTINCT f.fundCompany FROM FundDashboardBO f WHERE f.fundCompany IS NOT NULL")
    List<String> findAllFundCompanies();

    /**
     * 获取所有风险等级
     * @return 风险等级列表
     */
    @Query("SELECT DISTINCT f.riskLevel FROM FundDashboardBO f WHERE f.riskLevel IS NOT NULL")
    List<String> findAllRiskLevels();

    /**
     * 根据基金经理查找基金
     * @param fundManager 基金经理
     * @return 基金实体列表
     */
    List<FundDashboardBO> findByFundManagerContaining(String fundManager);

    /**
     * 查找申购状态为开放的基金
     * @return 开放申购的基金列表
     */
    List<FundDashboardBO> findByPurchaseStatus(String purchaseStatus);

    /**
     * 查找赎回状态为开放的基金
     * @return 开放赎回的基金列表
     */
    List<FundDashboardBO> findByRedemptionStatus(String redemptionStatus);
}