package spark.stock.analysis.util;

import java.util.Random;

/**
 * @ClassName RandomUtil
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 10:35 AM
 **/
public class RandomUtil {

    public static int getRandomInt(int range){
        Random random = new Random();
        return random.nextInt(range);
    }
}
