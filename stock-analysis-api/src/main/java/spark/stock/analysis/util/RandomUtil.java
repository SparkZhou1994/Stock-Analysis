package spark.stock.analysis.util;

import java.text.ParseException;
import java.util.Date;
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


    public static Date randomDate(String startDate, String endDate) throws ParseException {
        Date start = DateUtil.parseDate(startDate, "yyyy-MM-dd");
        Date end = DateUtil.parseDate(endDate, "yyyy-MM-dd");
        long date = random(start.getTime(), end.getTime());
        return new Date(date);
    }

    private static long random(long begin,long end){
        long diff = begin + (long)(Math.random() * (end - begin));
        if(diff == begin || diff == end){
            return random(begin,end);
        }
        return diff;
    }

}
