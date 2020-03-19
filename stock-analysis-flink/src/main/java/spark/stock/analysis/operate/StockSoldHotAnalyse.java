package spark.stock.analysis.operate;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import spark.stock.analysis.entity.FundBO;
import spark.stock.analysis.entity.StockBO;
import spark.stock.analysis.util.JsonUtil;

import java.util.Objects;

/**
 * @ClassName StockSoldHotAnalyse
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 2:07 PM
 **/
public class StockSoldHotAnalyse {
    public static DataStream<String> stockSoldHotAnalyse(DataStream<String> stream) {
        DataStream<String> result = stream.flatMap(new FlatMapFunction<String, StockCountBO>() {
            @Override
            public void flatMap(String s, Collector<StockCountBO> collector) throws Exception {
                FundBO fundBO = JsonUtil.json2Bean(s, FundBO.class);
                for (StockBO stockBO : fundBO.getHasStock()) {
                    collector.collect(new StockCountBO(stockBO, 1));
                }
            }
        }).keyBy(new KeySelector<StockCountBO, String>() {
            @Override
            public String getKey(StockCountBO stockCountBO) throws Exception {
                return stockCountBO.getStockCode();
            }
        }).timeWindow(Time.seconds(20L)).reduce(new ReduceFunction<StockCountBO>() {
            @Override
            public StockCountBO reduce(StockCountBO stockCountBO, StockCountBO t1) throws Exception {
                stockCountBO.setCount(stockCountBO.getCount() + t1.getCount());
                return stockCountBO;
            }
        }).map(new MapFunction<StockCountBO, String>() {
            @Override
            public String map(StockCountBO stockCountBO) throws Exception {
                return JsonUtil.bean2Json(stockCountBO);
            }
        }).setParallelism(1);
        result.print().setParallelism(1);
        return result;
    }

    public static class StockCountBO extends StockBO {
        private Integer count;

        public StockCountBO() {
        }

        public StockCountBO(StockBO stockBO, Integer count) {
            super.setStockCode(stockBO.getStockCode());
            this.count = count;
        }

        public StockCountBO(String stockCode, Integer count) {
            super.setStockCode(stockCode);
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "StockCountBO{" +
                    "stockCode=" + super.getStockCode() +
                    ",count=" + count +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            StockCountBO that = (StockCountBO) o;
            return Objects.equals(count, that.count);
        }

        @Override
        public int hashCode() {

            return Objects.hash(super.hashCode(), count);
        }
    }
}
