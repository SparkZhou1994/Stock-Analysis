package spark.stock.analysis.sink;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import spark.stock.analysis.config.rabbitmq.RabbitMQConfig;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName RabbitMqStreamSink
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 2:21 PM
 **/
public class RabbitMqStreamSink {

    public static DataStream<String> rabbitMqConnectBuilder(DataStream<String> stream){
        stream.addSink(new RichRMQSink<String>(RabbitMQConfig.connectionConfig(),
                "flink-rabbitmq",
                new SimpleStringSchema()));
        return stream;
    }

    public static class RichRMQSink<String> extends RMQSink<String>{
        public RichRMQSink(RMQConnectionConfig rmqConnectionConfig, java.lang.String queueName, SerializationSchema schema){
            super(rmqConnectionConfig,queueName,schema);
        }

        @Override
        protected void setupQueue() throws IOException {
            if (this.queueName != null) {
                this.channel.queueDeclare(this.queueName, true, false, false, (Map)null);
            }
        }
    }
}
