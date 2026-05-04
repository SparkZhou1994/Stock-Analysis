package spark.stock.analysis.entity;

import lombok.Data;

import java.util.Objects;

/**
 * @ClassName StockBO
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 8:55 AM
 **/
@Data
public class StockBO {

    private String stockCode;
    private String stockName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockBO stockBO = (StockBO) o;
        return Objects.equals(stockCode, stockBO.stockCode) &&
                Objects.equals(stockName, stockBO.stockName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(stockCode, stockName);
    }
}
