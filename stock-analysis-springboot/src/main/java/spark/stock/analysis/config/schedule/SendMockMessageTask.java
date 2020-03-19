package spark.stock.analysis.config.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import spark.stock.analysis.rabbitmq.SendMessage;
import spark.stock.analysis.server.FundService;


/**
 * @ClassName SendMockMessageTask
 * @Description TODO
 * @Author Spark
 * @Date 3/17/2020 3:03 PM
 **/
@Configuration
@EnableScheduling
public class SendMockMessageTask {

    @Autowired
    private SendMessage sendMessage;

    @Autowired
    private FundService fundService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Logger LOGGER = LoggerFactory.getLogger(SendMockMessageTask.class);

    @Scheduled(fixedRate = 5000)
    public void sendMessage() throws Exception {
        sendMessage.send(objectMapper.writeValueAsString(fundService.createFund()));
    }
}
