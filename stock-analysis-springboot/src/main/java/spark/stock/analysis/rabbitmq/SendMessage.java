package spark.stock.analysis.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName SendMessage
 * @Description TODO
 * @Author Spark
 * @Date 3/17/2020 2:48 PM
 **/

@Component
public class SendMessage {

    private final Logger LOGGER = LoggerFactory.getLogger(SendMessage.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String message){
        LOGGER.info("Send message to rabbitmq:{}",message);
        this.rabbitTemplate.convertAndSend("rabbitmq-flink",message);
    }
}
