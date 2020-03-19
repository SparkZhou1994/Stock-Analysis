package spark.stock.analysis.config.rabbitmq;

import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

/**
 * @ClassName RabbitMqConfig
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 2:30 PM
 **/
public class RabbitMQConfig {

    public static RMQConnectionConfig connectionConfig(){
        RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
                .setHost("127.0.0.1")
                .setPort(5672)
                .setUserName("Spark")
                .setPassword("Spark")
                .setVirtualHost("/")
                .build();
        return connectionConfig;
    }
}
