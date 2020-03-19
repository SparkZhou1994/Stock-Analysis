package spark.stock.analysis.stream;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import spark.stock.analysis.config.rabbitmq.RabbitMQConfig;

/**
 * @ClassName RabbitMqStreamConnector
 * @Description TODO
 * @Author Spark
 * @Date 3/17/2020 8:34 PM
 **/
public class RabbitMQStreamConnector {

    public static DataStream<String> rabbitMqConnectBuilder(StreamExecutionEnvironment env){


        DataStream<String> stream = env
                .addSource(new RMQSource<String>(
                        RabbitMQConfig.connectionConfig(),            // config for the RabbitMQ connection
                        "rabbitmq-flink",                 // name of the RabbitMQ queue to consume
                        true,                        // use correlation ids; can be false if only at-least-once is required
                        new SimpleStringSchema()))   // deserialization schema to turn messages into Java objects
                .setParallelism(1);              // non-parallel source is only required for exactly-once
        return stream;
    }
}
