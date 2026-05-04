package spark.stock.analysis.server.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.stock.analysis.entity.FundBO;
import spark.stock.analysis.server.FundService;
import spark.stock.analysis.server.StockService;
import spark.stock.analysis.util.DateUtil;
import spark.stock.analysis.util.RandomUtil;

import java.text.ParseException;
import java.util.Date;

/**
 * @ClassName FundServiceImpl
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 10:34 AM
 **/
@Service
public class FundServiceImpl implements FundService {

    @Autowired
    private StockService stockService;

    @Override
    public FundBO createFund() {
        FundBO fundBO = new FundBO();
        fundBO.setFundCode(String.valueOf(RandomUtil.getRandomInt(30)));
        int sign = RandomUtil.getRandomInt(10) > 5 ? (-1) : 1;
        fundBO.setTranslate(Long.parseLong(String.valueOf(RandomUtil.getRandomInt(99))) * sign);
        fundBO.setPrice(Long.parseLong(String.valueOf(RandomUtil.getRandomInt(299))));
        try {
            fundBO.setRecodeTime(RandomUtil.randomDate("2019-01-01", "2020-03-19"));
            fundBO.setRecodeTimeString(DateUtil.formatDate(fundBO.getRecodeTime(), "yyyy-MM-dd HH:mm:ss"));
        } catch (ParseException e) {
            fundBO.setRecodeTime(new Date());
        }
        fundBO.setEvaluationStar(String.valueOf(RandomUtil.getRandomInt(5)));
        fundBO.setHasStock(stockService.createStock(5));
        return fundBO;
    }
}
