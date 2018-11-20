package main;

import main.mainClass;

import java.io.*;
import java.util.*;
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

    private static int [] repeatTimes(LinkedList<String> path, Map<String,String> map, String act_ed){
        HashSet<String> repeat = new HashSet<String>();
        int count = 0;
        repeat.clear();
        String [] act = act_ed.split(":");
        String [] st = path.getFirst().split(",")[1].split(":");
        String [] ed = path.getLast().split(",")[1].split(":");
        Integer stInt = Integer.parseInt(st[0])*3600 + Integer.parseInt(st[1])* 60 + Integer.parseInt(st[2]);
        Integer edInt = Integer.parseInt(ed[0])*3600 + Integer.parseInt(ed[1])* 60 + Integer.parseInt(ed[2]);
        Integer actInt = Integer.parseInt(act[0])*3600 + Integer.parseInt(act[1])* 60 + Integer.parseInt(act[2]);

        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            if (repeat.contains(name) && repeat.size() > 1) {
                count += 1;
            } else {
                repeat.add(name);
            }
        }
        int [] result = new int[3];
        result[0] = count;
        result[1] = edInt-stInt;
        result[2] = actInt - edInt;
        return result;
    }

    public static void main(String[] args) throws IOException {

        String dateString = "2018-04-25";
        String startTime = "20:10:00";

        mainClass.fit();
        Map<String,String> map = mainClass.getAccCodeMap();
        LinkedList<String> path;
        String temp,str [];

        /*BufferedReader br=new BufferedReader(new FileReader(new File("d:\\717_16.csv")));
        BufferedWriter bw=new BufferedWriter(new FileWriter(new File("d:\\717_16.txt")));

        int cnt = 0, row = 0;
        HashMap<String,Integer> stat = new HashMap<String, Integer>();
        while((temp=br.readLine())!=null) {
            row += 1;
            str=temp.split(",");
            if (temp.startsWith("TRIP")) {continue;}
            String hour = str[1].split(" ")[1].split(":")[0];
            if ("16".equals(hour) == false) {
                continue;
            }
            if ("151018053".equals(str[0]) || "151018053".equals(str[2]) || str[0].equals(str[2])) {continue;}
            path = mainClass.getReachablePath("2018-06-13",str[1].split(" ")[1],str[0],str[2],false);
            int [] vals = repeatTimes(path,map,str[3].split(" ")[1]);

            String idx = String.valueOf(vals[0]);
            if (stat.containsKey(idx) == false){
                stat.put(idx,1);
            } else {
                Integer tmp_val = stat.get(idx);
                stat.put(idx,tmp_val+1);
            }*/
            /*if (Integer.parseInt(idx)>4) {
                for(String string : path) {
                    String [] line = string.split(",");
                    String name = map.get(line[0]);
                    System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
                }
            }
            String result = path.getLast().split(",")[1];*/
            /*if (cnt > 2600) {
                for(String string : path) {
                    String [] line = string.split(",");
                    String name = map.get(line[0]);
                    System.out.println(row+","+line[0]+","+line[1]+","+line[2]+","+name);
                }
            }*/
            //System.out.print(temp+","+String.valueOf(vals[0])+","+String.valueOf(vals[1])+"\n");
            /*bw.write(temp+","+String.valueOf(vals[0])+","+String.valueOf(vals[1])+","+String.valueOf(vals[2])+"\n");
            cnt += 1;
            if (cnt % 100 == 0) {
                System.out.println(cnt);
                for (String it:stat.keySet() ) {
                    System.out.println(it+"\t"+String.valueOf(stat.get(it)));
                }
                System.out.println("");
            }
        }
        br.close();
        bw.close();
        System.out.println("finished");
        for (String it:stat.keySet() ) {
            System.out.println(it+"\t"+String.valueOf(stat.get(it)));
        }
        System.out.println("");
        */

        path = mainClass.getReachablePath("2018-06-13","23:36:00","150996525","150997535",false);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }
        //System.out.println(""+String.valueOf(repeatTimes(path,map)));

        path = mainClass.getReachablePath("2018-06-13","22:48:00","150998817","150996009",false);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }
        //System.out.println(""+String.valueOf(repeatTimes(path,map)));

        path = mainClass.getReachablePath("2018-06-13","22:20:00","150998817","150997277",false);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }
        //System.out.println(""+String.valueOf(repeatTimes(path,map)));

        path = mainClass.getReachablePath("2018-06-13","23:05:00","150998817","150995989",false);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }
        //System.out.println(""+String.valueOf(repeatTimes(path,map)));

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

        // 平西府 -> 望和桥东
        path = mainClass.getReachablePath("2018-06-13","20:00:00","150997001","B00101975",false);
        for(String string : path) {
            String [] line = string.split(",");
            String name = map.get(line[0]);
            System.out.println(line[0]+","+line[1]+","+line[2]+","+name);
        }

        path = mainClass.getReachablePath("2018-06-13","10:50:00","B00001988","B00002068",false);
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

        testing = new ArrayList<Long>();
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
