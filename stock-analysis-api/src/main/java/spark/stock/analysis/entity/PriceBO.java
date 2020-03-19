package spark.stock.analysis.entity;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName PriceBO
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 9:31 AM
 **/
@Data
public class PriceBO {
    private Long price;
    private Date currentDate;
}
