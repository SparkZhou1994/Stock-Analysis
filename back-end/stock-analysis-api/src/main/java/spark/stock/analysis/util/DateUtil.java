package spark.stock.analysis.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName DateUtil
 * @Description TODO
 * @Author Spark
 * @Date 3/19/2020 10:42 AM
 **/
public class DateUtil {

    /*
    * @author Spark
    * @Description
    * @Date 10:43 AM 3/19/2020
    * @Param [format] es:yyyyMMddHHmmss
    * @return java.lang.String
    **/
    public static String getNowString(String format){
        Date currentTime = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(currentTime);
    }

    public static Date parseDate(String date,String format) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.parse(date);
    }

    public static String formatDate(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }
}
