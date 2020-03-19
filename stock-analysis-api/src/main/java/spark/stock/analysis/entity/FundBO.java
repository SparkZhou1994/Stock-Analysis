package spark.stock.analysis.entity;

import lombok.Data;

import java.util.Date;
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
    private Date recodeTime;
    private String recodeTimeString;
    private List<StockBO> hasStock;

}
