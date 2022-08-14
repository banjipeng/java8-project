package org.pingan.payment.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DateUtils {

    //线程局部变量
    public static final ThreadLocal<SimpleDateFormat> sf1 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };

    public static final ThreadLocal<SimpleDateFormat> sf2 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    public static final ThreadLocal<SimpleDateFormat> sf3 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };

    /**
     * 时间格式化方法
     */
    public static String getStringByFormat(Date date, ThreadLocal<SimpleDateFormat> format) {
        return format.get().format(date);
    }

}
