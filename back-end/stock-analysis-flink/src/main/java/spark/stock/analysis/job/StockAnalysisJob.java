package spark.stock.analysis.job;


import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.stock.analysis.operate.StockSoldHotAnalyse;
import spark.stock.analysis.sink.RabbitMqStreamSink;
import spark.stock.analysis.stream.RabbitMQStreamConnector;


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
        DataStream<String> stream = RabbitMQStreamConnector.rabbitMqConnectBuilder(env);
        DataStream<String> result = StockSoldHotAnalyse.stockSoldHotAnalyse(stream);
        RabbitMqStreamSink.rabbitMqConnectBuilder(result);
        env.execute("StockAnalysisJob");
    }


}
