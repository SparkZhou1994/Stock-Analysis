package spark.stock.analysis.stream;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

/**
 * @ClassName RabbitMqStreamConnector
 * @Description TODO
 * @Author Spark
 * @Date 3/17/2020 8:34 PM
 **/
public class RabbitMqStreamConnector {

    public static DataStream<String> rabbitMqConnectBuilder(StreamExecutionEnvironment env){
        RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
                .setHost("127.0.0.1")
                .setPort(5672)
                .setUserName("Spark")
                .setPassword("Spark")
                .setVirtualHost("/")
                .build();

        DataStream<String> stream = env
                .addSource(new RMQSource<String>(
                        connectionConfig,            // config for the RabbitMQ connection
                        "flink-rabbitmq",                 // name of the RabbitMQ queue to consume
                        true,                        // use correlation ids; can be false if only at-least-once is required
                        new SimpleStringSchema()))   // deserialization schema to turn messages into Java objects
                .setParallelism(1);              // non-parallel source is only required for exactly-once
        return stream;
    }
}
