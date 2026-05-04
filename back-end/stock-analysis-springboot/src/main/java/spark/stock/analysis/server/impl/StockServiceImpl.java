package spark.stock.analysis.server.impl;

import org.springframework.stereotype.Service;
import spark.stock.analysis.entity.StockBO;
import spark.stock.analysis.server.StockService;
import spark.stock.analysis.util.RandomUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName StockServiceImpl
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 11:31 AM
 **/
@Service
public class StockServiceImpl implements StockService {

    @Override
    public StockBO createStock() {
        StockBO stockBO = new StockBO();
        stockBO.setStockCode(String.valueOf(RandomUtil.getRandomInt(20)));
        return stockBO;
    }

    @Override
    public List<StockBO> createStock(int num) {
        Map<String, StockBO> stockMap = new HashMap<String, StockBO>();
        for(int i = 0; i < num; i++){
            StockBO stockBO = createStock();
            stockMap.put(stockBO.getStockCode(), stockBO);
        }
        List stockBOList = new ArrayList<StockBO>(stockMap.values());
        return stockBOList;
    }
}
