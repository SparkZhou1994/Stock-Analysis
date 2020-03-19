package spark.stock.analysis.config.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName RabbitMqConfig
 * @Description TODO
 * @Author Spark
 * @Date 3/17/2020 2:45 PM
 **/
@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue rabbitMQ() {
        return new Queue("flink-rabbitmq");
    }
}
