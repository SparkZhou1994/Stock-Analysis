package spark.stock.analysis.job;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.stock.analysis.stream.RabbitMqStreamConnector;

/**
 * @ClassName OrderJob
 * @Description TODO
 * @Author Spark
 * @Date 3/17/2020 8:42 PM
 **/
public class OrderJob {
    private final static Logger LOGGER = LoggerFactory.getLogger(OrderJob.class);

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
        env.execute("OrderJob");
    }
}
