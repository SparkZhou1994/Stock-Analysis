package spark.stock.analysis.job;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.stock.analysis.stream.RabbitMqStreamConnector;

/**
 * @ClassName StockAnalysisJob
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 11:58 AM
 **/
public class StockAnalysisJob {
    private final static Logger LOGGER = LoggerFactory.getLogger(StockAnalysisJob.class);

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = RabbitMqStreamConnector.rabbitMqConnectBuilder(env);
        LOGGER.info("Receive from rabbitmq {}", stream.print().setParallelism(1));
        stream.map(new MapFunction<String, Object>() {
            @Override
            public Object map(String s) throws Exception {
                return null;
            }
        });
        env.execute("StockAnalysisJob");
    }
}
