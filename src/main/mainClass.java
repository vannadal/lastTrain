package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import gov.bjjtw.lastTrain.Graph.Graph;
import gov.bjjtw.lastTrain.Graph.GraphSearchAlgorithm;
import gov.bjjtw.lastTrain.CommonTools.CommonTools;

import java.util.*;
import java.io.FileInputStream;
import java.util.stream.Collectors;


public class mainClass {
    private static Graph graph;
    private static String startVertex="";
    private static String endVertex="";
    private static String startTime="";
    private static String dateString="";
    private static String dir=null;
    private static String sameTransStationAdjDist=null;
    private static String stationdistance=null;
    private static String sameTransStationAdj=null;
    private static String timetable_weekday=null;
    private static String timetable_weekend=null;
    private static String acccodeInLine=null;
    private static String stationnametoacccode=null;
    private static String entertime=null;
    private static String acccodeLatLng=null;
    private static Set<String> unVisitedVertex=new HashSet<String>();
    private static Map<String,List<String>>  adj = null;
    //可查询末班车路径从此时刻开始，时间越晚g加载的列车运行时刻表数据越少
    private static String loadTimetableTime=null;
    private static final double epsilon = 1e-15;

    public enum Cate {
        REACHABLE_PATH, REACHABLE_STATION, REACHABLE_REVERSE_PATH
    }

    public static void fit(){
        initConf("",true);
        graph = fit(true);
        unVisitedVertex = graph.getUnVisitedVertex();
        adj = graph.getAdj();
    }

    private static void resetGraph(){
        graph.setAdj(adj);
        graph.setUnVisitedVertex(unVisitedVertex);
        graph.cleanMinDisLink();
        graph.cleanMinTimeLink();
        graph.cleanReachableSt();
        graph.cleanWalkTimeString();
        graph.cleanStack();
        graph.cleanStack2();
        graph.cleanStackPath();
        graph.cleanStackPath2();
        graph.resetParams();
    }


    private static void initConf(String filename,Boolean isResource) {
        FileInputStream inStream = null;
        try {
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(filename)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("conf.properties");
                br = new BufferedReader(new InputStreamReader(is));
            }

            Properties prop = new Properties();
            prop.load(br);
            sameTransStationAdjDist = prop.getProperty("path.sameTransStationAdjDist").toString();
            sameTransStationAdj = prop.getProperty("path.sameTransStationAdj").toString();
            timetable_weekday = prop.getProperty("path.timetable_weekday").toString();
            timetable_weekend = prop.getProperty("path.timetable_weekend").toString();
            acccodeInLine = prop.getProperty("path.acccodeInLine").toString();
            stationnametoacccode = prop.getProperty("path.stationnametoacccode").toString();
            entertime = prop.getProperty("path.entertime").toString();
            loadTimetableTime = prop.getProperty("conf.loadTimetableTime").toString();
            dir = prop.getProperty("path.dir").toString();
            startTime = prop.getProperty("conf.startTime").toString();
            startVertex = prop.getProperty("conf.startVertex").toString();
            endVertex= prop.getProperty("conf.endVertex").toString();
            dateString= prop.getProperty("conf.dateString").toString();
            stationdistance= prop.getProperty("path.stationdistance");
            acccodeLatLng = prop.getProperty("path.acccodeLatlng").toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex){
            System.out.println("conf keys wrong");
            ex.printStackTrace();
        }finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Graph fit(boolean isResource) {
        Graph empty_graph = new Graph();
        int startTimeToSec= CommonTools.TransferTime(loadTimetableTime);
        //将所有站点以及与其邻接的边添加到图中（不包括同站换乘点）
        AddAllVertexAndEdge(empty_graph,acccodeInLine,isResource);
        //同一站点的不同编码acc添加到图中
        AddSameTransferVertexAndEdge(empty_graph,sameTransStationAdj,sameTransStationAdjDist,isResource);
        AddStationWalkingTime(empty_graph,entertime,isResource);
        ReadTimeTable(empty_graph,stationdistance,timetable_weekday,timetable_weekend,acccodeInLine,stationnametoacccode,startTimeToSec,isResource);
        InitialTransVertexList(empty_graph,stationnametoacccode,isResource);
        LoadStationGeoPosition(empty_graph,acccodeLatLng,isResource);
        return empty_graph;
    }

    private static void AddStationWalkingTime(Graph g,String entertime,boolean isResource) {
        try {
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(dir+entertime)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(entertime);
                br = new BufferedReader(new InputStreamReader(is));
            }
            br.readLine();

            String [] str = null;
            String temp = "";
            while((temp=br.readLine())!=null) {
                str=temp.split(",");
                if(str[5].equals("进站")&&str[6].equals("全天")&&str[7].equals("工作日")) {
                    g.Add_WalkTime(str[0],str[4]);
                }
            }
            br.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private static void InitialTransVertexList(Graph g,String transPath,boolean isResource) {
        try {
            String temp="",str[];
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(dir+transPath)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(transPath);
                br = new BufferedReader(new InputStreamReader(is));
            }
            while((temp=br.readLine())!=null) {
                str=temp.split(",");
                g.Add_UnVisitedVertex(str[2]);
            }
            br.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private static void AddAllVertexAndEdge(Graph g, String filePath,boolean isResource) {
        String temp1 = "", str1[], line = "", acccode = "";
        BufferedReader br = null;
        try {
            if (isResource == false) {
                br = new BufferedReader(new FileReader(new File(dir + filePath)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
                br = new BufferedReader(new InputStreamReader(is));
            }

            temp1 = br.readLine();
            str1 = temp1.split(",");
            line = str1[1];
            acccode = str1[2];
            g.addVertex(str1[2]);

            while ((temp1 = br.readLine()) != null) {
                str1 = temp1.split(",");
                g.addVertex(str1[2]);
/*
                    switch (str1[2]) {
                        case "151020053":没用
                            g.addEdge("151020053", "151020055");
                            g.addEdge("151020055", "151020053");
                            break;
                        case "151020055":没用
                            g.addEdge("151020055", "151020059");
                            g.addEdge("151020055", "151020053");
                            break;
                        case "151020057":
                            g.addEdge("151020057", "151020055");
                            break;
                        case "151020059":
                            g.addEdge("151020059", "151020057");
                            g.addEdge("151020055", "151020059");
                            break;
                        case "150995474":
                            g.addEdge("150995474", "150995473");
                            g.addEdge("150995473", "150995474");
                            break;
                        case "151018007":
                            g.addEdge("151018007", "151018009");
                            g.addEdge("151018009", "151018007");
                            break;
                        default:
                    }
*/
                if (str1[1].equals(line)) {
                    g.addEdge(acccode, str1[2]);
                    g.addEdge(str1[2], acccode);
                }
                line = str1[1];
                acccode = str1[2];
            }
            g.addEdge("151020057", "151020055");
            //去除机场线的单行问题
            g.RemoveEdge("151020059", "151020055");
            g.RemoveEdge("151020057", "151020059");

            //g.RemoveEdge("151020055", "151020057");
            // 2号线和10号线首末站需要考虑环路
            g.addEdge("150995474","150995473");
            g.addEdge("150995473", "150995474");
            g.addEdge("151018007","151018009");
            g.addEdge("151018009", "151018007");
            br.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private static void LoadStationGeoPosition(Graph g, String acccodeLatLng, Boolean isResource) {
        String temp1 = null;
        String [] str1 = null;
        try {
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(dir+acccodeLatLng)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(acccodeLatLng);
                br = new BufferedReader(new InputStreamReader(is));
            }
            while((temp1=br.readLine())!=null) {
                str1=temp1.split(",");
                g.addGeoPosition(str1[2],str1[3]+","+str1[4]);
            }
            br.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void ReadTimeTable(Graph g,String stationdistance,String timetable_weekday,String timetable_weekend,String acccodeInLine,String stationnametoacccode,int startTimeToSec,boolean isResource) {
        try {
            String str[] = null, temp = "";
            BufferedReader br_weekday = null;
            BufferedReader br_weekend = null;
            BufferedReader br_acc_line = null;
            BufferedReader br_dist = null;
            if (isResource == false) {
                br_weekday = new BufferedReader(new FileReader(new File(dir + timetable_weekday)));
                br_dist = new BufferedReader(new FileReader(new File(dir + stationdistance)));
                br_weekend = new BufferedReader(new FileReader(new File(dir + timetable_weekend)));
                br_acc_line = new BufferedReader(new FileReader(new File(dir + acccodeInLine)));
            } else {
                InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(timetable_weekday);
                br_weekday = new BufferedReader(new InputStreamReader(is1));
                InputStream is2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(timetable_weekend);
                br_weekend = new BufferedReader(new InputStreamReader(is2));
                InputStream is3 = Thread.currentThread().getContextClassLoader().getResourceAsStream(acccodeInLine);
                br_acc_line = new BufferedReader(new InputStreamReader(is3));
                InputStream is4 = Thread.currentThread().getContextClassLoader().getResourceAsStream(stationnametoacccode);
                InputStream is5 = Thread.currentThread().getContextClassLoader().getResourceAsStream(stationdistance);
                br_dist = new BufferedReader(new InputStreamReader(is5));
            }


            while ((temp = br_weekday.readLine()) != null) {
                str = temp.split(",");
                String acccode1 = str[0] + str[1];
                //每次加载列车运行时刻表时，只加载计算时间之后的数据
                if (startTimeToSec <= CommonTools.TransferTime(str[2])) {
                    g.Add_weeekday_timetable(acccode1, str[2], str[3], str[5]);
                }
            }

            br_weekday.close();


            while ((temp = br_dist.readLine()) != null) {
                str = temp.split(",");
                String acccode1 = str[0] + str[1];
                int dis=Integer.parseInt(str[2]);
                g.Add_stationdistance(acccode1, dis);
            }

            br_dist.close();

            while ((temp = br_weekend.readLine()) != null) {
                str = temp.split(",");
                String acccode1 = str[0] + str[1];
                //每次加载列车运行时刻表时，只加载计算时间之后的数据
                if (startTimeToSec <= CommonTools.TransferTime(str[2])) {
                    g.Add_weeekend_timetable(acccode1, str[2], str[3], str[5]);
                }
            }
            br_weekend.close();

            while ((temp = br_acc_line.readLine()) != null) {
                str = temp.split(",");
                g.Add_AccInLine(str[2], str[1]);
            }

            br_acc_line.close();

            /*
            BufferedReader br_acc_name = null;
            //br_acc_name = new BufferedReader(new InputStreamReader(new FileInputStream(dir + stationnametoacccode),"GBK"));
            //br_acc_name = new BufferedReader(new InputStreamReader(is4,"GBK"));
            while ((temp = br_acc_name.readLine()) != null) {
                str = temp.split(",");
                g.Add_acctoName(str[2], str[1]);
            }
            br_acc_name.close();
            */
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    private static void AddSameTransferVertexAndEdge(Graph g,String sameTransStationAdj,String sameTransStationAdjDist,boolean isResource) {
        String temp1 = "";
        String [] str1 = null;
        try {
            BufferedReader br = null;
            BufferedReader br_dist = null;
            if (isResource == false) {
                br = new BufferedReader(new FileReader(new File(dir+sameTransStationAdj)));
                br_dist = new BufferedReader(new FileReader(new File(dir+sameTransStationAdjDist)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(sameTransStationAdj);
                br = new BufferedReader(new InputStreamReader(is));
                InputStream is2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(sameTransStationAdjDist);
                br_dist = new BufferedReader(new InputStreamReader(is2));
            }

            while((temp1=br.readLine())!=null) {
                str1=temp1.split(",");
                g.addVertex(str1[0]);
                g.addEdge(str1[0], str1[1]);
                g.addTransTime(str1[0],str1[1],str1[2]);
            }
            br.close();
            br_dist.readLine();
            while((temp1=br_dist.readLine())!=null) {
                str1=temp1.split(",");
                int dist=Integer.parseInt(str1[3]);
                g.Add_stationdistance(str1[0]+str1[1],dist);
            }
            br_dist.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private static LinkedList<String> ComputeReachablePath(String startvertex,String dateString,String time,String endvertex,Boolean isReverse) {
        String ver=endvertex;
        String content="";
        Boolean hasValidStation = false;
        LinkedList<String> path = new LinkedList<String>();
        graph.setStack3();

        if (isReverse) {
            ver=startvertex;
        }

        while(!graph.getStack().empty()) {
            content=graph.getStack().pop();
            String str[]=content.split(",");
            String station1 = str[1];
            String station0 = str[0];
            if (isReverse){
                station1 = str[0];
                station0 = str[1];
            }
            if(station1.equals(ver)) {
                graph.AddStackPath(content);
                ver=station0;
            }
        }
        try {
            String context = "";
            while(!graph.getPathStack().empty()) {
                content=graph.getPathStack().pop();
                String str[]=content.split(",");
                context = str[0]+","+str[1]+","+str[4]+","+str[3]+","+str[2];
                if (str[2].equals(Graph.UpperLimitTime) == false) {
                    hasValidStation = true;
                }
                path.add(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (hasValidStation == false) {
            path.clear();
        }
        return path;
    }

    public static LinkedList<String> GetReachablePath(String datestring, String starttime, String startvertex, String endvertex) {
        String [] tmp;
        LinkedList<String> result = GetReachable(datestring,starttime,startvertex,endvertex,Cate.REACHABLE_PATH);
        LinkedList<String> output = new LinkedList<String>();
        if (result.size()>0) {
            for (String item : result) {
                tmp = item.split(",");
                output.add(tmp[0] + "," + tmp[2] + ",1");
            }
            tmp = result.getLast().split(",");
            output.add(tmp[1] + "," + tmp[3] + ",1");
            startvertex = tmp[1];
        } else {
            output.add(startvertex+","+starttime+",1");
        }
        if (startvertex.equals(endvertex) == false){
            resetGraph();
            result = GraphTraversal2(startvertex, endvertex, stationnametoacccode);
            for (String item: result){
                output.add(item.split(",")[1]+",,0");
            }
        }
        return output;
    }

    public static LinkedList<String> GetReachableStation(String datestring, String starttime, String startvertex){
        return GetReachable(datestring,starttime,startvertex,"",Cate.REACHABLE_STATION);
    }

    public static LinkedList<String> GetReachableStationLatestPath(String datestring, String endtime, String startvertex, String endvertex){
        LinkedList<String> result = GetReachable(datestring,endtime,startvertex,endvertex,Cate.REACHABLE_REVERSE_PATH);
        if (result != null){
            Collections.reverse(result);
        } else {
            result = new LinkedList<String>();
        }
        return result;
    }

    private static LinkedList<String> GraphTraversal(String startVertex, String endVertex, String startTime, String dateString,String stationnametoacccode,Boolean isReverse) {
        graph.InitialSearchStartVertex(startVertex,dateString,startTime,endVertex);
        GraphSearchAlgorithm graphSearchAlgorithm =new GraphSearchAlgorithm();
        if(graphSearchAlgorithm.perform(graph, startVertex, dateString, startTime, endVertex,stationnametoacccode, isReverse)) {
        //if(graphSearchAlgorithm.perform(graph, startVertex, dateString, startTime, endVertex,stationnametoacccode,false)) {
            //将可达路径返回至sh输出 150995457,151018037
            return ComputeReachablePath(startVertex,dateString,startTime,endVertex,isReverse);
        } else {
            return null;
        }
    }

    private static double GetGeoDistanceBetweenStations(String station1, String station2){
        Float [] lastStationPosition = graph.getGeoPosition(station1);
        Float [] destStationPosition = graph.getGeoPosition(station2);
        if (lastStationPosition[0] - 0.0 > epsilon && lastStationPosition[1] - 0.0 > epsilon && destStationPosition[0] - 0.0 > epsilon && destStationPosition[1] - 0.0 > epsilon){
            return CommonTools.SimpleDist(lastStationPosition[1],lastStationPosition[0],destStationPosition[1],destStationPosition[0]);
        }
        return -1.0;
    }

    public static LinkedList<String> GetReachable(String datestring, String starttime, String startvertex, String endvertex, Cate type) {
        resetGraph();
        graph.setIsWeekend(CommonTools.isWeekend(datestring));
        //0是00:00:00的秒数，18000是05:00:00的秒数
        LinkedList<String> reachableStation = new LinkedList<>();
        if ((CommonTools.TransferTime(starttime) > 0) && (CommonTools.TransferTime(starttime) < 18000)) {
            return reachableStation;
        } else {
            switch (type) {
                case REACHABLE_STATION:
                    reachableStation = GraphTraversal(startvertex, endvertex, starttime, datestring, stationnametoacccode,false);
                    if (reachableStation != null) {
                        return graph.getReachable();
                    } else {
                        return null;
                    }
                case REACHABLE_REVERSE_PATH:
                    reachableStation = GraphTraversal(startvertex, endvertex, starttime, datestring, stationnametoacccode,true);
                    if (reachableStation != null && reachableStation.size()>0) {
                        return reachableStation;
                    } else {
                        return null;
                    }
                case REACHABLE_PATH:
                    reachableStation = GraphTraversal(startvertex, endvertex, starttime, datestring, stationnametoacccode,false);
                    if (reachableStation != null && reachableStation.size()>0) {
                        return reachableStation;
                    } else {
                        String minStation = startvertex;
                        double minDist = GetGeoDistanceBetweenStations(startvertex,endvertex);
                        for(String line: graph.getStack3()){
                            String [] items = line.split(",");
                            double currentDistance = GetGeoDistanceBetweenStations(items[0],endvertex);
                            if (currentDistance - 0.0000001 > epsilon && minDist - currentDistance > epsilon) {
                                minStation = items[0];
                                minDist = currentDistance;
                            }
                        }
                        if (minStation.equals(startvertex) == false){
                            resetGraph();
                            return GraphTraversal(startvertex, minStation, starttime, datestring, stationnametoacccode, false);
                        } else {
                            return reachableStation;
                        }
                    }
                default:
                    return null;
            }
        }
    }

    // 三元桥机场线bug
    public static void main(String[] args) throws IOException {
        String temp = "";
        String [] line = null;

        fit();

        BufferedReader br = new BufferedReader( new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(acccodeInLine)));
        HashSet<String> accodes = new HashSet<String>();
        while((temp=br.readLine())!=null) {
            line=temp.split(",");
            accodes.add(line[2]);
        }
        br.close();

        System.out.println("==========Output Demo==========");
        System.out.println("23:10:00 XiZhiMen-LiuLiQiao Path");
        System.out.println(mainClass.GetReachableStationLatestPath("2018-04-25","23:45:00","150995457","151018037"));
        System.out.println("23:10:00 XiZhiMen-LianHuaQiao Path");
        System.out.println(mainClass.GetReachableStationLatestPath("2018-04-25","23:45:00","150995457","150997279"));
        System.out.println("23:10:00 XiZhiMen-LiuLiQiao Path");
        System.out.println(mainClass.GetReachablePath("2018-04-25","23:10:00","150995457","151018037"));
        System.out.println("23:10:00 XiZhiMen-LiuLiQiao Path");
        System.out.println(mainClass.GetReachablePath("2018-04-25","22:10:00","150995457","151018037"));
        System.out.println("23:10:00 XiZhiMen-XiZhiMen Path");
        System.out.println(mainClass.GetReachablePath("2018-04-25","23:10:00","150995457","150995474"));
        System.out.println("22:10:00 XiZhiMen Reachable Stations");
        System.out.println(mainClass.GetReachableStation("2018-04-25","22:10:00","150995457"));
        System.out.println("23:10:00 XiZhiMen Reachable Stations");
        System.out.println(mainClass.GetReachableStation("2018-04-25","23:10:00","150995457"));

        ArrayList<Long> testing = new ArrayList<Long>();
        //本循环为测试车站代码
        for(String ac: accodes){
            long time=System.currentTimeMillis();
            LinkedList<String> reachableStations=GetReachableStation(dateString,startTime,ac);
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
        for(String ac: accodes){
            long time=System.currentTimeMillis();
            LinkedList<String> reachableStations=GetReachablePath(dateString,startTime,ac,"151018037");
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
        for(String ac: accodes){
            long time=System.currentTimeMillis();
            LinkedList<String> reachableStations=GetReachableStationLatestPath(dateString,startTime,ac,"151018037");
            long consume = System.currentTimeMillis()-time;
            if (reachableStations==null) {
                System.out.println(ac + "," + (consume) + "ms," + 0);
            } else {
                System.out.println(ac + "," + (consume) + "ms," + reachableStations.size());
            }
            testing.add(consume);
        }
        outputTestResult("ReachableStationLatestPathTest",testing);

    }

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

    /*
    public static void setGraph(Graph g){
        graph = (Graph) CommonTools.DeepCopy(g);
    }

    public static Graph getGraph(){
        return graph;
    }
    */

    public static LinkedList<String> GetReachableStation2(String startvertex,String endvertex) {
        resetGraph();
        String startVertex1 = startvertex;
        String  endVertex1=endvertex;
        LinkedList<String> reachableStation = GraphTraversal2(startVertex1, endVertex1, stationnametoacccode);
        if (reachableStation != null) {
            return reachableStation;
        } else {
            return null;
        }
    }


    private static LinkedList<String> GraphTraversal2(String startVertex, String endVertex,String stationnametoacccode) {
        graph.InitialSearchStartVertex2(startVertex,endVertex);
        GraphSearchAlgorithm graphSearchAlgorithm =new GraphSearchAlgorithm();
        if(graphSearchAlgorithm.perform2(graph, startVertex, endVertex,stationnametoacccode)) {
            //将可达路径返回至sh输出
            return GetshortPath(startVertex,endVertex);
        } else {
            return null;
        }
    }

    public static LinkedList<String> GetshortPath(String startvertex,String endvertex) {
        String ver=endvertex;
        String content="";
        LinkedList<String> path = new LinkedList<String>();
        while(!graph.getStack2().empty()) {
            content=graph.getStack2().pop();
            String str[]=content.split(",");
            if(str[1].equals(ver)) {
                graph.AddStackPath2(content);
                ver=str[0];
            }
        }
        try {
            String context = "";
            while(!graph.getPathStack2().empty()) {
                content=graph.getPathStack2().pop();
                String str[]=content.split(",");
                context = str[0]+","+str[1]+","+str[2];
                path.add(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }


}
