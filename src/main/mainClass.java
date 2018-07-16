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
    //暂缓开通的车站list
    private static ArrayList<String> postpone_opening_stations = new ArrayList<String>();
    //S1线的车站
    private static ArrayList<String> S1Line = new ArrayList<String>();
    private static String dir=null;
    private static String sameTransStationAdjDist=null;
    private static String stationdistance=null;
    private static String timetable_weekday=null;
    private static String timetable_weekend=null;
    private static String acccodeInLine=null;
    private static String acccodeLatLng=null;
    private static Set<String> unVisitedVertex=new HashSet<String>();
    private static Map<String,List<String>>  adj = null;
    //存放站点acc与中文名对应
    private static Map<String,String>  map = new HashMap<>();
    //可查询末班车路径从此时刻开始，时间越晚g加载的列车运行时刻表数据越少
    private static String loadTimetableTime=null;
    private static final double EPSILON = 1e-15;

    public enum Cate {
        REACHABLE_PATH, REACHABLE_STATION, REACHABLE_REVERSE_PATH, REACHABLE_MINTRANSFER_PATH, OTHER
    }

    public static void fit(){
        initConf("",true);
        graph = fit(true);
        unVisitedVertex = graph.getUnVisitedVertex();
        adj = graph.getAdj();
        postpone_opening_stations.add("150998575");
        postpone_opening_stations.add("150998609");
        postpone_opening_stations.add("150996547");
        postpone_opening_stations.add("150996551");
        postpone_opening_stations.add("150996793");
        postpone_opening_stations.add("151019567");
        postpone_opening_stations.add("150996779");
        S1Line.add("150999061");
        S1Line.add("150999063");
        S1Line.add("150999065");
        S1Line.add("150999067");
        S1Line.add("150999069");
        S1Line.add("150999071");
        S1Line.add("150999073");
    }

    private static void resetGraph(){
        graph.setAdj(adj);
        graph.setAdj3(adj);
        graph.setUnVisitedVertex(unVisitedVertex);
        graph.cleanMinDisLink();
        graph.cleanMinTimeLink();
        graph.cleanMinScoreLink();
        graph.cleanReachableSt();
        graph.cleanWalkTimeString();
        graph.cleanStack3();
        graph.cleanStack();
        graph.cleanScoreStack();
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
            timetable_weekday = prop.getProperty("path.timetable_weekday").toString();
            timetable_weekend = prop.getProperty("path.timetable_weekend").toString();
            acccodeInLine = prop.getProperty("path.acccodeInLine").toString();
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

    private static void specialStation(Graph emptyGraph) {
        // 朱辛庄换乘站不同路径建模
        emptyGraph.addAccInLine("151019045_151019043","L131");
        emptyGraph.addAccInLine("151019045_151019047","L131");


        emptyGraph.addEdge("151019045_151019043","151019043");
        emptyGraph.addEdge("151019045_151019047","151019047");
        emptyGraph.addEdge("151019043","151019045_151019043");
        emptyGraph.addEdge("151019047","151019045_151019047");
        emptyGraph.addEdge("150996997","151019045_151019047");
        emptyGraph.addEdge("150996997","151019045_151019043");
        emptyGraph.addEdge("151019045_151019043","150996997");
        emptyGraph.addEdge("151019045_151019047","150996997");
        emptyGraph.removeEdge("150996997","151019045");
        emptyGraph.removeEdge("151019045","150996997");

        emptyGraph.getStationdistance().put("151019045_151019043151019043",emptyGraph.getStationdistance().get("151019045151019043"));
        emptyGraph.getStationdistance().put("151019045_151019047151019047",emptyGraph.getStationdistance().get("151019045151019047"));
        emptyGraph.getStationdistance().put("151019043151019045_151019043",emptyGraph.getStationdistance().get("151019043151019045"));
        emptyGraph.getStationdistance().put("151019047151019045_151019047",emptyGraph.getStationdistance().get("151019047151019045"));

        emptyGraph.getStationdistance().put("150996997151019045_151019047",emptyGraph.getStationdistance().get("150996997151019045"));
        emptyGraph.getStationdistance().put("150996997151019045_151019043",emptyGraph.getStationdistance().get("150996997151019045"));
        emptyGraph.getStationdistance().put("151019045_151019043150996997",emptyGraph.getStationdistance().get("151019045150996997"));
        emptyGraph.getStationdistance().put("151019045_151019047150996997",emptyGraph.getStationdistance().get("151019045150996997"));

        emptyGraph.getStationdistance().remove("151019045150996997");
        emptyGraph.getStationdistance().remove("150996997151019045");

        Float [] tmp = emptyGraph.getGeoPosition("150996997");
        String tmpStr = tmp[0].toString()+","+tmp[1].toString();
        emptyGraph.addGeoPosition("151019045_151019047",tmpStr);
        emptyGraph.addGeoPosition("151019045_151019043",tmpStr);

        emptyGraph.getTransTime().put("151019045_151019047150996997","105");
        emptyGraph.getTransTime().put("150996997151019045_151019047","105");
        emptyGraph.getTransTime().put("150996997151019045_151019043","10");
        emptyGraph.getTransTime().put("151019045_151019043150996997","10");

        emptyGraph.getTransTime().remove("150996997151019045");
        emptyGraph.getTransTime().remove("151019045150996997");

        emptyGraph.getTimetableWeekday().put("151019045_151019043151019043",emptyGraph.getTimetableWeekday().get("151019045151019043"));
        emptyGraph.getTimetableWeekday().put("151019045_151019047151019047",emptyGraph.getTimetableWeekday().get("151019045151019047"));
        emptyGraph.getTimetableWeekday().put("151019043151019045_151019043",emptyGraph.getTimetableWeekday().get("151019043151019045"));
        emptyGraph.getTimetableWeekday().put("151019047151019045_151019047",emptyGraph.getTimetableWeekday().get("151019047151019045"));

        emptyGraph.getTimetableWeekend().put("151019045_151019043151019043",emptyGraph.getTimetableWeekend().get("151019045151019043"));
        emptyGraph.getTimetableWeekend().put("151019045_151019047151019047",emptyGraph.getTimetableWeekend().get("151019045151019047"));
        emptyGraph.getTimetableWeekend().put("151019043151019045_151019043",emptyGraph.getTimetableWeekend().get("151019043151019045"));
        emptyGraph.getTimetableWeekend().put("151019047151019045_151019047",emptyGraph.getTimetableWeekend().get("151019047151019045"));
    }

    private static Graph fit(boolean isResource) {
        Graph emptyGraph = new Graph();
        int startTimeToSec= CommonTools.TransferTime(loadTimetableTime);
        //将所有站点以及与其邻接的边添加到图中（不包括同站换乘点）
        addAllVertexAndEdge( emptyGraph,acccodeInLine,isResource);
        //同一站点的不同编码acc添加到图中
        addSameTransferVertexAndEdge( emptyGraph,sameTransStationAdjDist,isResource);
        //addStationWalkingTime( emptyGraph,entertime,isResource);
        readTimeTable( emptyGraph,stationdistance,timetable_weekday,timetable_weekend,acccodeInLine,startTimeToSec,isResource);
        initialTransVertexList( emptyGraph,acccodeLatLng,isResource);
        loadStationGeoPosition( emptyGraph,acccodeLatLng,isResource);
        loadAcc(acccodeLatLng,map,isResource);
        specialStation( emptyGraph);

        return  emptyGraph;
    }

    private static void loadAcc(String station, Map<String, String> map, boolean isResource) {
        // TODO Auto-generated method stub
        try {
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(dir+station)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(station);
                br = new BufferedReader(new InputStreamReader(is,"UTF8"));
            }
            String [] str = null;
            String temp = "";
            while((temp=br.readLine())!=null) {
                str=temp.split(",");
                map.put(str[2], str[1]);
            }
            br.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private static void addStationWalkingTime(Graph g, String entertime, boolean isResource) {
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
                if("进站".equals(str[5])&&"全天".equals(str[6])&&"工作日".equals(str[7])) {
                    g.addWalkTime(str[0],str[4]);
                }
            }
            br.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private static void initialTransVertexList(Graph g, String transPath, boolean isResource) {
        try {
            String temp="";
            String [] str;
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(dir+transPath)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(transPath);
                br = new BufferedReader(new InputStreamReader(is));
            }
            while((temp=br.readLine())!=null) {
                str=temp.split(",");
                g.addUnVisitedVertex(str[2]);
            }
            br.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private static void addAllVertexAndEdge(Graph g, String filePath, boolean isResource) {
        String temp1 = "", line = "", acccode = "";
        String [] str1;
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
                if (str1[1].equals(line)) {
                    g.addEdge(acccode, str1[2]);
                    g.addEdge(str1[2], acccode);
                }
                line = str1[1];
                acccode = str1[2];
            }
            g.addEdge("151020057", "151020055");
            //去除机场线的单行问题
            g.removeEdge("151020059", "151020055");
            g.removeEdge("151020057", "151020059");
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

    private static void loadStationGeoPosition(Graph g, String acccodeLatLng, Boolean isResource) {
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

    private static void readTimeTable(Graph g, String stationdistance, String timetableWeekday, String timetableWeekend, String acccodeInLine, int startTimeToSec, boolean isResource) {
        try {
            String [] str = null;
            String temp = "";
            BufferedReader brWeekday = null;
            BufferedReader brWeekend = null;
            BufferedReader brAccLine = null;
            BufferedReader brDist = null;
            if (isResource == false) {
                brWeekday = new BufferedReader(new FileReader(new File(dir + timetableWeekday)));
                brDist = new BufferedReader(new FileReader(new File(dir + stationdistance)));
                brWeekend = new BufferedReader(new FileReader(new File(dir + timetableWeekend)));
                brAccLine = new BufferedReader(new FileReader(new File(dir + acccodeInLine)));
            } else {
                InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(timetableWeekday);
                brWeekday = new BufferedReader(new InputStreamReader(is1));
                InputStream is2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(timetableWeekend);
                brWeekend = new BufferedReader(new InputStreamReader(is2));
                InputStream is3 = Thread.currentThread().getContextClassLoader().getResourceAsStream(acccodeInLine);
                brAccLine = new BufferedReader(new InputStreamReader(is3));
                InputStream is5 = Thread.currentThread().getContextClassLoader().getResourceAsStream(stationdistance);
                brDist = new BufferedReader(new InputStreamReader(is5));
            }


            while ((temp = brWeekday.readLine()) != null) {
                str = temp.split(",");
                //按终点站或起始站有无机场线，间数据加载入两个map中
                String acccode1 = str[0] + str[1];
                boolean judge1 =  "151020057".equals(str[0]) || "151020059".equals(str[0]);
                boolean judge2 =  "151020055".equals(str[1]) || "151020057".equals(str[1]) || "151020059".equals(str[1]);
                //每次加载列车运行时刻表时，只加载计算时间之后的数据
                if (startTimeToSec <= CommonTools.TransferTime(str[2])) {
                    g.addWeekdayTimetable(acccode1, str[2], str[3], str[5]);
                }
            }

            brWeekday.close();

            while ((temp = brDist.readLine()) != null) {
                str = temp.split(",");
                String acccode1 = str[0] + str[1];
                int dis=Integer.parseInt(str[2]);
                g.addStationDistance(acccode1, dis);
            }

            brDist.close();
            //按终点站或起始站有无机场线，间数据加载入两个map中
            while ((temp = brWeekend.readLine()) != null) {
                str = temp.split(",");
                String acccode1 = str[0] + str[1];
                boolean judge1 =  "151020057".equals(str[0]) || "151020059".equals(str[0]);
                boolean judge2 =  "151020057".equals(str[1]) || "151020059".equals(str[1]);
                //每次加载列车运行时刻表时，只加载计算时间之后的数据
                if (startTimeToSec <= CommonTools.TransferTime(str[2])) {
                    g.addWeekendTimetable(acccode1, str[2], str[3], str[5]);
                }
            }

            brWeekend.close();

            while ((temp = brAccLine.readLine()) != null) {
                str = temp.split(",");
                g.addAccInLine(str[2], str[1]);
            }

            brAccLine.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    private static void addSameTransferVertexAndEdge(Graph g, String sameTransStationAdjDist, boolean isResource) {
        String temp1 = "";
        String [] str1 = null;
        try {
            BufferedReader br = null;
            BufferedReader brDist = null;
            if (isResource == false) {
                brDist = new BufferedReader(new FileReader(new File(dir+sameTransStationAdjDist)));
            } else {
                InputStream is2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(sameTransStationAdjDist);
                brDist = new BufferedReader(new InputStreamReader(is2));
            }

            brDist.readLine();
            while((temp1=brDist.readLine())!=null) {
                str1=temp1.split(",");
                g.addVertex(str1[0]);
                g.addEdge(str1[0],str1[1]);
                g.addTransTime(str1[0],str1[1],str1[2]);
                int dist=Integer.parseInt(str1[3]);
                g.addStationDistance(str1[0]+str1[1],dist);
            }
            brDist.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    //基于最小时间
    private static LinkedList<String> computeReachablePath(String startvertex, String dateString, String time, String endvertex, Boolean isReverse, int type) {
        String ver=endvertex;
        String content="";
        Boolean hasValidStation = false;
        LinkedList<String> path = new LinkedList<String>();
        Stack<String> stack = null;

        if (type == 1) {
            graph.setStack3();
            stack = graph.getStack();
        } else if (type == 3){
            graph.setStack4();
            stack = graph.getStack();
            for (String item : graph.getStack4()){
                stack.push(item);
            }
        }

        if (isReverse) {
            ver=startvertex;
        }

        while(!stack.empty()) {
            content=stack.pop();
            String [] str =content.split(",");
            String station1 = str[1];
            String station0 = str[0];
            if (isReverse){
                station1 = str[0];
                station0 = str[1];
            }
            if(station1.equals(ver)) {
                graph.addStackPath(content);
                ver=station0;
            }
        }

        try {
            String context = "";
            while(!graph.getPathStack().empty()) {
                content=graph.getPathStack().pop();
                String [] str = content.split(",");
                context = str[0]+","+str[1]+","+str[4]+","+str[3]+","+str[2];
                if (str[2].equals(Graph.UPPER_LIMIT_TIME) == false) {
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

    public static LinkedList<String> getReachablePath(String datestring, String starttime, String startvertex, String endvertex,boolean isMinTransfer) {
        String [] tmp;
        LinkedList<String> result ;

        if (isMinTransfer){
            result = getReachable(datestring,starttime,startvertex,endvertex,Cate.REACHABLE_MINTRANSFER_PATH);
        }else{
            result = getReachable(datestring,starttime,startvertex,endvertex,Cate.REACHABLE_PATH);
        }

        LinkedList<String> output = new LinkedList<String>();
        //记录输出路径包含的站点acc
        LinkedList<String> simple = new LinkedList<String>();
        //记录输出路径包含的站点中文名
        LinkedList<String> simplenames = new LinkedList<String>();
        if (result.size()>0) {
            for (String item : result) {
                tmp = item.split(",");
                String item1 = tmp[0];
                if (item1.contains("_")){
                    item1 = item1.split("_")[0];
                }
                if (postpone_opening_stations.contains(item1)) {
                    output.add(item1 + "," + tmp[2] + ",2");
                }else {
                    output.add(item1 + "," + tmp[2] + ",1");
                }
                simple.add(item1);
            }

            tmp = result.getLast().split(",");
            String item1 = tmp[1];
            if (item1.contains("_")){
                item1 = item1.split("_")[0];
            }

            if (postpone_opening_stations.contains(tmp[1])) {
                output.add(item1 + "," + tmp[3] + ",2");
            }else {
                output.add(item1 + "," + tmp[3] + ",1");
            }
            startvertex = item1;
            simple.add(item1);
        } else {
            output.add(startvertex+","+starttime+",1");
            simple.add(startvertex);
        }

        for(String s: simple) {
            if (!simplenames.contains(map.get(s))) {
                simplenames.add(map.get(s));
            }
        }

        //不可达的情况
        if (startvertex.equals(endvertex) == false){
            resetGraph();
            LinkedList<String> unreachableResult = graphTraversal(startvertex, endvertex, starttime, datestring, acccodeLatLng, false, false, Cate.OTHER);
            for (String item: unreachableResult){
                String accname = item.split(",")[1];
                if (accname.contains("_")){
                    accname = accname.split("_")[0];
                }
                if (!simplenames.contains(map.get(accname))) {
                    output.add(accname+",,0");
                }
            }
        }

        return output;
    }

    private static double getGeoDistanceBetweenStations(String station1, String station2){
        Float [] lastStationPosition = graph.getGeoPosition(station1);
        Float [] destStationPosition = graph.getGeoPosition(station2);
        if (lastStationPosition[0] - 0.0 > EPSILON && lastStationPosition[1] - 0.0 > EPSILON && destStationPosition[0] - 0.0 > EPSILON && destStationPosition[1] - 0.0 > EPSILON){
            return CommonTools.SimpleDist(lastStationPosition[1],lastStationPosition[0],destStationPosition[1],destStationPosition[0]);
        }
        return -1.0;
    }

    private static LinkedList<String> reachablePath(String startvertex, String endvertex, String starttime, String datestring, Boolean isLessTrans,Cate type){
        LinkedList<String> reachableStation = graphTraversal(startvertex, endvertex, starttime, datestring, acccodeLatLng,false,isLessTrans,type);
        if(reachableStation !=null && reachableStation.size()>0){
            return reachableStation;
        } else {
            String minStation = startvertex;
            double minDist = getGeoDistanceBetweenStations(startvertex,endvertex);
            for(String line: graph.getReachable()){
                String [] items = line.split(",");
                double currentDistance = getGeoDistanceBetweenStations(items[0],endvertex);
                if (currentDistance - 0.0000001 > EPSILON && minDist - currentDistance > EPSILON) {
                    minStation = items[0];
                    minDist = currentDistance;
                }
            }
            if (minStation.equals(startvertex) == false){
                return computeReachablePath(startvertex,datestring,starttime,minStation,false,3);
            } else {
                return reachableStation;
            }
        }
    }

    private static LinkedList<String> getReachable(String datestring, String starttime, String startvertex, String endvertex, Cate type) {
        resetGraph();
        graph.setIsWeekend(CommonTools.isWeekend(datestring));
        //0是00:00:00的秒数，18000是05:00:00的秒数
        LinkedList<String> reachableStation = new LinkedList<>();
        switch (type) {
            case REACHABLE_STATION:
                reachableStation = graphTraversal(startvertex, endvertex, starttime, datestring, acccodeLatLng,false, false, type);
                LinkedList<String> stations = graph.getReachable();
                if (reachableStation != null ) {
                    if (!S1Line.contains(startvertex) && graph.getMinTimeLink().get("151018273")!=null) {
                        int jinanqiaoArrTime= CommonTools.TransferTime(graph.getMinTimeLink().get("151018273"));
                        //比较到达金安桥的时间是否小于金安桥末班车的时间
                        int timeDifference = jinanqiaoArrTime - CommonTools.TransferTime("21:07:00");
                        if (timeDifference>0) {
                            stations.remove("151018273");
                            return stations;
                        }
                        return stations;
                    }else if(S1Line.contains(startvertex) && graph.getMinTimeLink().get("150995203")!=null){
                        int jinanqiaoArrTime = CommonTools.TransferTime(graph.getMinTimeLink().get("150995203"));
                        //比较到达苹果园的时间是否小于苹果园末班车的时间
                        int timeDifference = jinanqiaoArrTime - CommonTools.TransferTime("23:30:00");
                        if (timeDifference>0) {
                            stations.remove("150995203");
                            return stations;
                        }
                        return stations;
                    }else {
                        return stations;
                    }
                } else {
                    return null;
                }
            case REACHABLE_REVERSE_PATH:
                reachableStation = graphTraversal(startvertex, endvertex, starttime, datestring, acccodeLatLng,true, false, type);
                if (reachableStation != null && reachableStation.size()>0) {
                    return reachableStation;
                } else {
                    return null;
                }
            case REACHABLE_MINTRANSFER_PATH:
                return reachablePath(startvertex, endvertex, starttime, datestring, true, type);
            case REACHABLE_PATH:
                return reachablePath(startvertex, endvertex, starttime, datestring, false, type);
            default:
                return null;
        }
    }

    private static LinkedList<String> computeReachablePath2(String startvertex2, String datestring2, String starttime2,
                                                            String minStation, boolean isReverse) {
        String ver = minStation;
        String content = "";
        Boolean hasValidStation = false;
        LinkedList<String> path = new LinkedList<String>();

        if (isReverse) {
            ver = startvertex2;
        }

        while (!graph.getStack3().empty()) {
            content = graph.getStack3().pop();
            String str[] = content.split(",");
            String station1 = str[1];
            String station0 = str[0];
            if (isReverse) {
                station1 = str[0];
                station0 = str[1];
            }
            if (station1.equals(ver)) {
                graph.addStackPath(content);
                ver = station0;
            }
        }

        try {
            String context = "";
            while (!graph.getPathStack().empty()) {
                content = graph.getPathStack().pop();
                String str[] = content.split(",");
                context = str[0] + "," + str[1] + "," + str[4] + "," + str[3] + "," + str[2];
                if (str[2].equals(Graph.UPPER_LIMIT_TIME) == false) {
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

    // 三元桥机场线bug
    public static void main(String[] args) throws IOException {

        fit();

        //东直门 -> 三元桥
        LinkedList<String> path = mainClass.getReachablePath("2018-06-13","20:58:00","150995470","150997531",false);
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


    private static LinkedList<String> graphTraversal(String startVertex, String endVertex, String startTime, String dateString, String stationnametoacccode, Boolean isReverse, Boolean isLessTrans, Cate type) {
        graph.initialSearchStartVertex(startVertex,dateString,startTime,endVertex);
        GraphSearchAlgorithm graphSearchAlgorithm =new GraphSearchAlgorithm();
        int retId = 0;
        //基于分数最小的遍历
        if (Cate.REACHABLE_MINTRANSFER_PATH == type || Cate.REACHABLE_PATH == type) {
            retId = 3;
        } else if (Cate.REACHABLE_REVERSE_PATH == type || Cate.REACHABLE_STATION == type){
            retId = 1;
        } else {
            retId = 0;
        }

        if (graphSearchAlgorithm.perform(graph, startVertex, dateString, startTime, endVertex,stationnametoacccode, isReverse,isLessTrans,retId)) {
            if (retId > 0) {
                return computeReachablePath(startVertex, dateString, startTime, endVertex, isReverse, retId);
            } else {
                return getShortPath(startVertex,endVertex);
            }
        } else {
            return null;
        }


    }

    private static LinkedList<String> getShortPath(String startvertex, String endvertex) {
        String ver=endvertex;
        String content="";
        LinkedList<String> path = new LinkedList<String>();
        while(!graph.getStack2().empty()) {
            content=graph.getStack2().pop();
            String [] str = content.split(",");
            if(str[1].equals(ver)) {
                graph.addStackPath2(content);
                ver=str[0];
            }
        }
        try {
            String context = "";
            while(!graph.getPathStack2().empty()) {
                content=graph.getPathStack2().pop();
                String [] str = content.split(",");
                context = str[0]+","+str[1]+","+str[2];
                path.add(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }


}