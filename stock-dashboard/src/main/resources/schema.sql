-- Stock Dashboard Database Schema
-- MySQL 8.0 compatible
-- Created: 2026-03-09

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `stock_dashboard` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `stock_dashboard`;

-- 设置SQL模式
SET sql_mode = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ============================================
-- 表：stock_dashboard - 股票看板信息表
-- ============================================
CREATE TABLE IF NOT EXISTS `stock_dashboard` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '股票ID',
    `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码',
    `stock_name` VARCHAR(100) NOT NULL COMMENT '股票名称',
    `current_price` DECIMAL(10,2) COMMENT '当前价格',
    `change_amount` DECIMAL(10,2) COMMENT '涨跌额',
    `change_percent` DECIMAL(10,4) COMMENT '涨跌幅（百分比）',
    `open_price` DECIMAL(10,2) COMMENT '开盘价',
    `high_price` DECIMAL(10,2) COMMENT '最高价',
    `low_price` DECIMAL(10,2) COMMENT '最低价',
    `previous_close` DECIMAL(10,2) COMMENT '昨日收盘价',
    `volume` BIGINT COMMENT '成交量',
    `turnover` DECIMAL(20,2) COMMENT '成交额',
    `turnover_rate` DECIMAL(10,4) COMMENT '换手率（百分比）',
    `pe_ratio` DECIMAL(10,2) COMMENT '市盈率',
    `pb_ratio` DECIMAL(10,2) COMMENT '市净率',
    `market_cap` DECIMAL(20,2) COMMENT '总市值',
    `circulating_market_cap` DECIMAL(20,2) COMMENT '流通市值',
    `industry` VARCHAR(100) COMMENT '行业分类',
    `stock_type` VARCHAR(20) COMMENT '股票类型（A股、港股、美股等）',
    `is_watchlist` TINYINT(1) DEFAULT 0 COMMENT '是否自选',
    `watchlist_order` INT COMMENT '自选顺序',
    `update_time` DATETIME COMMENT '数据更新时间',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否活跃',
    `data_source` VARCHAR(50) COMMENT '数据来源（东方财富、雪球等）',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_code` (`stock_code`),
    KEY `idx_stock_code` (`stock_code`),
    KEY `idx_stock_type` (`stock_type`),
    KEY `idx_industry` (`industry`),
    KEY `idx_is_watchlist` (`is_watchlist`),
    KEY `idx_update_time` (`update_time`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='股票看板信息表';

-- ============================================
-- 表：fund_dashboard - 基金看板信息表
-- ============================================
CREATE TABLE IF NOT EXISTS `fund_dashboard` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '基金ID',
    `fund_code` VARCHAR(20) NOT NULL COMMENT '基金代码',
    `fund_name` VARCHAR(100) NOT NULL COMMENT '基金名称',
    `fund_type` VARCHAR(50) COMMENT '基金类型（股票型、混合型、债券型等）',
    `current_net_value` DECIMAL(10,4) COMMENT '当前净值',
    `daily_change_amount` DECIMAL(10,4) COMMENT '日涨跌额',
    `daily_change_percent` DECIMAL(10,4) COMMENT '日涨跌幅（百分比）',
    `weekly_change_percent` DECIMAL(10,4) COMMENT '周涨跌幅',
    `monthly_change_percent` DECIMAL(10,4) COMMENT '月涨跌幅',
    `yearly_change_percent` DECIMAL(10,4) COMMENT '年涨跌幅',
    `accumulated_net_value` DECIMAL(10,4) COMMENT '累计净值',
    `establishment_date` DATETIME COMMENT '成立日期',
    `fund_size` DECIMAL(15,2) COMMENT '基金规模（亿元）',
    `fund_manager` VARCHAR(100) COMMENT '基金经理',
    `fund_company` VARCHAR(100) COMMENT '基金管理公司',
    `risk_level` VARCHAR(20) COMMENT '风险等级（低、中、高）',
    `purchase_status` VARCHAR(50) COMMENT '申购状态（开放申购、暂停申购等）',
    `redemption_status` VARCHAR(50) COMMENT '赎回状态（开放赎回、暂停赎回等）',
    `is_watchlist` TINYINT(1) DEFAULT 0 COMMENT '是否自选',
    `watchlist_order` INT COMMENT '自选顺序',
    `update_time` DATETIME COMMENT '数据更新时间',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否活跃',
    `data_source` VARCHAR(50) COMMENT '数据来源（东方财富、天天基金等）',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fund_code` (`fund_code`),
    KEY `idx_fund_code` (`fund_code`),
    KEY `idx_fund_type` (`fund_type`),
    KEY `idx_fund_company` (`fund_company`),
    KEY `idx_is_watchlist` (`is_watchlist`),
    KEY `idx_update_time` (`update_time`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='基金看板信息表';

-- ============================================
-- 表：realtime_price - 实时价格表
-- ============================================
CREATE TABLE IF NOT EXISTS `realtime_price` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '价格ID',
    `code` VARCHAR(20) NOT NULL COMMENT '标的代码（股票代码或基金代码）',
    `type` VARCHAR(10) NOT NULL COMMENT '标的类型（STOCK-股票，FUND-基金）',
    `price` DECIMAL(10,4) COMMENT '当前价格',
    `change_amount` DECIMAL(10,4) COMMENT '涨跌额',
    `change_percent` DECIMAL(10,4) COMMENT '涨跌幅（百分比）',
    `open_price` DECIMAL(10,4) COMMENT '开盘价',
    `high_price` DECIMAL(10,4) COMMENT '最高价',
    `low_price` DECIMAL(10,4) COMMENT '最低价',
    `previous_close` DECIMAL(10,4) COMMENT '昨日收盘价',
    `volume` BIGINT COMMENT '成交量',
    `turnover` DECIMAL(20,2) COMMENT '成交额',
    `turnover_rate` DECIMAL(10,4) COMMENT '换手率（百分比）',
    `amplitude` DECIMAL(10,4) COMMENT '振幅（百分比）',
    `commission_ratio` DECIMAL(10,4) COMMENT '委比（百分比）',
    `volume_ratio` DECIMAL(10,4) COMMENT '量比',
    `pe_ratio` DECIMAL(10,2) COMMENT '市盈率',
    `pb_ratio` DECIMAL(10,2) COMMENT '市净率',
    `market_cap` DECIMAL(20,2) COMMENT '总市值',
    `circulating_market_cap` DECIMAL(20,2) COMMENT '流通市值',
    `bid_price1` DECIMAL(10,4) COMMENT '买一价',
    `bid_volume1` BIGINT COMMENT '买一量',
    `ask_price1` DECIMAL(10,4) COMMENT '卖一价',
    `ask_volume1` BIGINT COMMENT '卖一量',
    `timestamp` DATETIME NOT NULL COMMENT '时间戳（精确到秒）',
    `trading_status` VARCHAR(50) COMMENT '交易状态（交易中、停牌、休市等）',
    `data_source` VARCHAR(50) COMMENT '数据来源',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_timestamp` (`code`, `timestamp`),
    KEY `idx_code_type` (`code`, `type`),
    KEY `idx_timestamp` (`timestamp`),
    KEY `idx_trading_status` (`trading_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实时价格表';

-- ============================================
-- 表：historical_data - 历史数据表
-- ============================================
CREATE TABLE IF NOT EXISTS `historical_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史数据ID',
    `code` VARCHAR(20) NOT NULL COMMENT '标的代码（股票代码或基金代码）',
    `type` VARCHAR(10) NOT NULL COMMENT '标的类型（STOCK-股票，FUND-基金）',
    `trade_date` DATE NOT NULL COMMENT '交易日期',
    `open_price` DECIMAL(10,4) COMMENT '开盘价',
    `high_price` DECIMAL(10,4) COMMENT '最高价',
    `low_price` DECIMAL(10,4) COMMENT '最低价',
    `close_price` DECIMAL(10,4) COMMENT '收盘价',
    `change_amount` DECIMAL(10,4) COMMENT '涨跌额',
    `change_percent` DECIMAL(10,4) COMMENT '涨跌幅（百分比）',
    `volume` BIGINT COMMENT '成交量',
    `turnover` DECIMAL(20,2) COMMENT '成交额',
    `turnover_rate` DECIMAL(10,4) COMMENT '换手率（百分比）',
    `amplitude` DECIMAL(10,4) COMMENT '振幅（百分比）',
    `pe_ratio` DECIMAL(10,2) COMMENT '市盈率',
    `pb_ratio` DECIMAL(10,2) COMMENT '市净率',
    `market_cap` DECIMAL(20,2) COMMENT '总市值',
    `circulating_market_cap` DECIMAL(20,2) COMMENT '流通市值',
    `adjustment_factor` DECIMAL(10,6) COMMENT '调整因子（用于复权计算）',
    `adjustment_type` VARCHAR(20) COMMENT '复权类型（NONE-不复权，FORWARD-前复权，BACKWARD-后复权）',
    `data_source` VARCHAR(50) COMMENT '数据来源',
    `create_time` DATE NOT NULL COMMENT '创建时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_trade_date_adjustment` (`code`, `trade_date`, `adjustment_type`),
    KEY `idx_code_type` (`code`, `type`),
    KEY `idx_trade_date` (`trade_date`),
    KEY `idx_adjustment_type` (`adjustment_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='历史数据表';

-- ============================================
-- 表：system_config - 系统配置表
-- ============================================
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(50) COMMENT '配置类型',
    `description` VARCHAR(500) COMMENT '配置描述',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `update_time` DATETIME COMMENT '更新时间',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_config_type` (`config_type`),
    KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================
-- 表：data_sync_log - 数据同步日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `data_sync_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `sync_type` VARCHAR(50) NOT NULL COMMENT '同步类型',
    `sync_source` VARCHAR(100) NOT NULL COMMENT '数据来源',
    `sync_status` VARCHAR(20) NOT NULL COMMENT '同步状态（SUCCESS, FAILED, PARTIAL）',
    `records_count` INT COMMENT '记录数量',
    `success_count` INT COMMENT '成功数量',
    `failed_count` INT COMMENT '失败数量',
    `error_message` TEXT COMMENT '错误信息',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `duration_ms` BIGINT COMMENT '持续时间（毫秒）',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',

    PRIMARY KEY (`id`),
    KEY `idx_sync_type` (`sync_type`),
    KEY `idx_sync_status` (`sync_status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据同步日志表';

-- ============================================
-- 表：user_watchlist - 用户自选表
-- ============================================
CREATE TABLE IF NOT EXISTS `user_watchlist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自选ID',
    `user_id` VARCHAR(100) NOT NULL COMMENT '用户ID',
    `target_type` VARCHAR(10) NOT NULL COMMENT '目标类型（STOCK, FUND）',
    `target_code` VARCHAR(20) NOT NULL COMMENT '目标代码',
    `target_name` VARCHAR(100) COMMENT '目标名称',
    `watch_order` INT NOT NULL DEFAULT 0 COMMENT '自选顺序',
    `notes` VARCHAR(500) COMMENT '备注',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否活跃',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME COMMENT '更新时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_target_type` (`target_type`),
    KEY `idx_target_code` (`target_code`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户自选表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入系统配置
INSERT IGNORE INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `create_time`) VALUES
('data.sync.interval.stock', '30000', 'SYNC', '股票数据同步间隔（毫秒）', NOW()),
('data.sync.interval.fund', '60000', 'SYNC', '基金数据同步间隔（毫秒）', NOW()),
('data.sync.batch.size', '100', 'SYNC', '数据同步批次大小', NOW()),
('cache.ttl.realtime', '5', 'CACHE', '实时数据缓存时间（秒）', NOW()),
('cache.ttl.historical', '3600', 'CACHE', '历史数据缓存时间（秒）', NOW()),
('api.rate.limit', '100', 'API', 'API速率限制（每分钟）', NOW()),
('system.timezone', 'Asia/Shanghai', 'SYSTEM', '系统时区', NOW()),
('stock.market.open.time', '09:30', 'MARKET', '股市开盘时间', NOW()),
('stock.market.close.time', '15:00', 'MARKET', '股市收盘时间', NOW());

-- ============================================
-- 存储过程和函数
-- ============================================

-- 清理过期实时数据（保留最近7天）
DELIMITER //
CREATE PROCEDURE `cleanup_old_realtime_data`()
BEGIN
    DELETE FROM `realtime_price`
    WHERE `timestamp` < DATE_SUB(NOW(), INTERVAL 7 DAY)
    AND `create_time` < DATE_SUB(NOW(), INTERVAL 1 DAY);
END //
DELIMITER ;

-- 获取股票统计信息
DELIMITER //
CREATE FUNCTION `get_stock_statistics`(p_stock_code VARCHAR(20))
RETURNS VARCHAR(500)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_count INT;
    DECLARE v_avg_price DECIMAL(10,2);
    DECLARE v_max_price DECIMAL(10,2);
    DECLARE v_min_price DECIMAL(10,2);

    SELECT COUNT(*), AVG(price), MAX(price), MIN(price)
    INTO v_count, v_avg_price, v_max_price, v_min_price
    FROM `realtime_price`
    WHERE `code` = p_stock_code AND `type` = 'STOCK'
    AND `timestamp` >= DATE_SUB(NOW(), INTERVAL 1 DAY);

    RETURN CONCAT('Count: ', v_count, ', Avg: ', v_avg_price, ', Max: ', v_max_price, ', Min: ', v_min_price);
END //
DELIMITER ;

-- ============================================
-- 事件调度
-- ============================================

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;

-- 每天凌晨清理过期数据
DELIMITER //
CREATE EVENT IF NOT EXISTS `daily_cleanup`
ON SCHEDULE EVERY 1 DAY
STARTS TIMESTAMP(CURRENT_DATE, '03:00:00')
DO
BEGIN
    CALL `cleanup_old_realtime_data`;

    -- 更新看板数据状态
    UPDATE `stock_dashboard`
    SET `is_active` = 0
    WHERE `update_time` < DATE_SUB(NOW(), INTERVAL 30 DAY);

    UPDATE `fund_dashboard`
    SET `is_active` = 0
    WHERE `update_time` < DATE_SUB(NOW(), INTERVAL 30 DAY);
END //
DELIMITER ;

-- ============================================
-- 视图
-- ============================================

-- 活跃股票视图
CREATE OR REPLACE VIEW `vw_active_stocks` AS
SELECT
    sd.*,
    rp.price as latest_price,
    rp.change_percent as latest_change_percent,
    rp.timestamp as latest_update
FROM `stock_dashboard` sd
LEFT JOIN `realtime_price` rp ON sd.stock_code = rp.code AND rp.type = 'STOCK'
WHERE sd.is_active = 1
AND rp.timestamp = (
    SELECT MAX(timestamp)
    FROM `realtime_price`
    WHERE code = sd.stock_code AND type = 'STOCK'
);

-- 活跃基金视图
CREATE OR REPLACE VIEW `vw_active_funds` AS
SELECT
    fd.*,
    rp.price as latest_price,
    rp.change_percent as latest_change_percent,
    rp.timestamp as latest_update
FROM `fund_dashboard` fd
LEFT JOIN `realtime_price` rp ON fd.fund_code = rp.code AND rp.type = 'FUND'
WHERE fd.is_active = 1
AND rp.timestamp = (
    SELECT MAX(timestamp)
    FROM `realtime_price`
    WHERE code = fd.fund_code AND type = 'FUND'
);

-- ============================================
-- 完成信息
-- ============================================
SELECT 'Database schema c表也reated successfully!' as message;