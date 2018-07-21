package gov.bjjtw.lastTrain.CommonTools;


import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Math.toRadians;

/**
 * CommonTools{@link String}通用工具
 *
 * <p> 末班车可达技术应用的接口及框架
 * <a href="https://github.com/bjjtwxxzx/lastTrain">项目位置</a>
 * 更全面的项目 {@code String} 信息.
 *
 * <p> 当前类通过Java核心{@link String}和{@link StringBuilder}类，
 * 实现末班车可达算法用到的通用工具。
 *
 * @author wuxinran@bjjtw.gov.cn
 */

public class CommonTools {
    /**
     * transferTime 将文本时间转为整数型
     * @param timeStr 时间文本
     * @return int 整数型时间
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    public static int transferTime(String timeStr) {
    	String str[]=timeStr.split(":");
    	int h=Integer.parseInt(str[0]);
    	if(h==0) {
    	       h=24;
    	}
    	int m=Integer.parseInt(str[1]);
    	int s=Integer.parseInt(str[2]);    		
        return h*3600+m*60+s;
    }

    /**
     * secondToTime 将整数型时间转为文本
     * @param second 整数型时间
     * @return String 时间文本
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    public static String secondToTime(int second) {
        int h=second/3600;
        int m=(second-h*3600)/60;
        int s=(second-h*3600-m*60);
        String hours,minutes,seconds;
        if (h<10) {
            hours = "0"+h;
        } else {
            hours = String.valueOf(h);
        }
        if (m<10){
            minutes = "0"+m;
        } else{
            minutes = String.valueOf(m);
        }
        if (s<10){
            seconds = "0"+s;
        } else {
            seconds = String.valueOf(s);
        }
        return hours+":"+minutes+":"+seconds;
    }

    /**
     * deepCopy 基于字节，对象进行深复制
     * @param orig 原对象
     * @return Object 复制对象
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    public static Object deepCopy(Object orig) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Retrieve an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(fbos.getInputStream());
            obj = in.readObject();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }

    /**
     * simpleDist 简化经纬度距离计算
     * @link https://tech.meituan.com/lucene_distance.html
     * @param lat1 纬度1
     * @param lng1 经度1
     * @param lat2 纬度2
     * @param lng2 经度2
     * @return double 两点欧式距离
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    public static double simpleDist(double lat1, double lng1, double lat2, double lng2) {
        // 经度差值
        double dx = lng1 - lng2;
        // 纬度差值
        double dy = lat1 - lat2;
        // 平均纬度
        double b = (lat1 + lat2) / 2.0;
        // 东西距离
        double lx = toRadians(dx) * 6367000.0* Math.cos(toRadians(b));
        // 南北距离
        double ly = 6367000.0 * toRadians(dy);
        // 用平面的矩形对角距离公式计算总距离
        return Math.sqrt(lx * lx + ly * ly);
    }

    /**
     * 判断输入日期是否周末休息日
     * @param dateString 日期文本格式
     * @return boolean 是否周末
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    public static boolean isWeekend(String dateString) {
        try{
        	ArrayList<String> holidays = new ArrayList<String>();
        	holidays.add("2018-09-24");
        	holidays.add("2018-10-01");
        	holidays.add("2018-10-02");
        	holidays.add("2018-10-03");
        	holidays.add("2018-10-04");
        	holidays.add("2018-10-05");
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if(holidays.contains(dateString)||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
