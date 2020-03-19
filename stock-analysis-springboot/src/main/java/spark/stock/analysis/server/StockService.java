package spark.stock.analysis.server;

import spark.stock.analysis.entity.StockBO;

import java.util.List;

/**
 * @ClassName StockService
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 11:31 AM
 **/
public interface StockService {
    StockBO createStock();
    List<StockBO> createStock(int num);
}
