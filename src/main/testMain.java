package main;

import main.mainClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class testMain {

    private static void outputTestResult(String testName,ArrayList<Long> timeConsume){
        System.out.println("=========="+testName+"==========");
        System.out.println("0ms - 100ms:"+timeConsume.stream().filter(x -> x <= 100).count());
        System.out.println("101ms - 150ms:"+timeConsume.stream().filter(x-> x>100 && x<= 150).count());
        System.out.println("151ms - 200ms:"+timeConsume.stream().filter(x-> x>150 && x<=200).count());
        System.out.println("201ms - 250ms:"+timeConsume.stream().filter(x-> x>200 && x<=250).count());
        System.out.println("251ms - 350ms:"+timeConsume.stream().filter(x-> x>250 && x<=350).count());
        System.out.println("351ms - :"+timeConsume.stream().filter(x-> x>350).count());
        System.out.println("avg:"+ timeConsume.stream().collect(Collectors.averagingInt(x -> x.intValue() ))  );
    }

    public static void main(String[] args) throws IOException {

        String dateString = "2018-04-25";
        String startTime = "20:10:00";

        mainClass.fit();
        Map<String,String> map = mainClass.getAccCodeMap();

        LinkedList<String> path = mainClass.getReachablePath("2018-06-13","22:38:00","150998817","150996009",false);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }
        System.out.println("");
        path = mainClass.getReachablePath("2018-06-13","22:20:00","150998817","150997277",false);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }
        System.out.println("");
        path = mainClass.getReachablePath("2018-06-13","23:05:00","150998817","150995989",false);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }

        //东直门 -> 三元桥
        path = mainClass.getReachablePath("2018-06-13","20:58:00","150995470","150997531",false);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }
        System.out.println("");

        path = mainClass.getReachablePath("2018-06-13","20:58:00","150995470","150997531",true);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }
        System.out.println("");

        // 六里桥 -> 北京南站
        path = mainClass.getReachablePath("2018-06-13","20:58:00","151018037","150996029",true);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }
        System.out.println("");

        path = mainClass.getReachablePath("2018-06-13","22:58:00","151018037","150996029",true);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }
        System.out.println("");

        // 平西府 -> 巩华城
        path = mainClass.getReachablePath("2018-06-13","23:40:00","150997001","151019043",true);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }
        System.out.println("");

        // 平西府 -> 生命科学
        path = mainClass.getReachablePath("2018-06-13","20:00:00","150997001","151019047",false);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }

        System.out.println("==========Output Demo==========");
        System.out.println("23:10:00 XiZhiMen-LiuLiQiao Path");
        System.out.println(mainClass.getReachableStationLatestPath("2018-04-25","23:45:00","151018037","150995457"));
        System.out.println(mainClass.getReachableStationLatestPath("2018-04-25","23:45:00","150995457","151018037"));
        System.out.println("23:10:00 XiZhiMen-LianHuaQiao Path");
        System.out.println(mainClass.getReachableStationLatestPath("2018-04-25","23:45:00","150995457","150997279"));
        System.out.println("23:10:00 XiZhiMen-LiuLiQiao Path");
        System.out.println(mainClass.getReachablePath("2018-04-25","23:10:00","150995457","151018037",true));
        System.out.println("23:10:00 XiZhiMen-LiuLiQiao Path");
        System.out.println(mainClass.getReachablePath("2018-04-25","22:10:00","150995457","151018037",false));
        System.out.println("23:10:00 XiZhiMen-XiZhiMen Path");
        System.out.println(mainClass.getReachablePath("2018-04-25","23:10:00","150995457","150995474",true));
        System.out.println("22:10:00 XiZhiMen Reachable Stations");
        System.out.println(mainClass.getReachableStation("2018-04-25","22:10:00","150995457"));
        System.out.println("23:10:00 XiZhiMen Reachable Stations");
        System.out.println(mainClass.getReachableStation("2018-04-25","23:10:00","150995457"));

        ArrayList<Long> testing = new ArrayList<Long>();

        //本循环为测试车站代码
        for(String ac: mainClass.getAccCodeSet()){
            long time=System.currentTimeMillis();
            LinkedList<String> reachableStations=mainClass.getReachableStation(dateString,startTime,ac);
            long consume = System.currentTimeMillis()-time;
            if (reachableStations==null) {
                System.out.println(ac + "," + (consume) + "ms," + 0);
            } else {
                System.out.println(ac + "," + (consume) + "ms," + reachableStations.size());
            }
            testing.add(consume);
        }
        outputTestResult("ReachableStationTest",testing);
        testing.clear();
        //本循环为测试车站代码
        for(String ac: mainClass.getAccCodeSet()){
            long time=System.currentTimeMillis();
            LinkedList<String> reachableStations=mainClass.getReachablePath(dateString,startTime,ac,"151018037",true);
            long consume = System.currentTimeMillis()-time;
            if (reachableStations==null) {
                System.out.println(ac + "," + (consume) + "ms," + 0);
            } else {
                System.out.println(ac + "," + (consume) + "ms," + reachableStations.size());
            }
            testing.add(consume);
        }
        outputTestResult("ReachablePathTest",testing);

        //本循环为测试车站代码
        for(String ac: mainClass.getAccCodeSet()){
            long time=System.currentTimeMillis();
            LinkedList<String> reachableStations=mainClass.getReachableStationLatestPath(dateString,startTime,ac,"151018037");
            long consume = System.currentTimeMillis()-time;
            if (reachableStations==null) {
                System.out.println(ac + "," + (consume) + "ms," + 0);
            } else {
                System.out.println(ac + "," + (consume) + "ms," + reachableStations.size());
            }
            testing.add(consume);
        }
        outputTestResult("ReachableStationLatestPathTestOUT",testing);
    }

}
