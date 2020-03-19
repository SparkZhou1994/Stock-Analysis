package spark.stock.analysis.entity;

import lombok.Data;

import java.util.List;

/**
 * @ClassName FundBO
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 8:56 AM
 **/
@Data
public class FundBO {

    private String fundCode;
    private String fundName;
    private String fundOrg;
    private String evaluationStar;
    private Long buyFee;
    private Long soldFee;
    private Long price;
    private Long translate;
    private List<TranslateBO> translateRecode; //涨跌幅
    private List<StockBO> hasStocks; //持有股票
    private List<PriceBO> priceHistory; //价格历史记录

}
