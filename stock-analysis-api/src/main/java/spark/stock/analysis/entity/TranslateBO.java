package spark.stock.analysis.entity;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName TranslateBO
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 9:01 AM
 **/
@Data
public class TranslateBO {
    private Date currentDate;
    private Long translate;
}
