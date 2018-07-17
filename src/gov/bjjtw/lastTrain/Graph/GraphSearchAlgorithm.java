package gov.bjjtw.lastTrain.Graph;

import gov.bjjtw.lastTrain.CommonTools.CommonTools;
import javafx.beans.binding.DoubleBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class GraphSearchAlgorithm {
    private Set<String> visitedVertex;
    public String date;
    public String transPath;
    public Set<String> unVisitedVertex =new HashSet<String>();
    public static final int TRANSFERWEIGHT = 10000000;


    public boolean perform(Graph g, String sourceVertex,String dateString,String time,String endVertex,String stationnametoacccode, Boolean isReverse, Boolean isLessTrans, int type) {
        if (null == visitedVertex) {
            visitedVertex = new HashSet<>();
        }
        date=dateString;
        transPath = stationnametoacccode;
        if (isReverse){
            // 不包含少换乘优先的分支
            return dijkstra3(g,sourceVertex,dateString,time,endVertex,isLessTrans);
        }

        if (type == 1){
            //基于距离
            return dijkstra(g,sourceVertex,dateString,time,endVertex,transPath);
        } else if (type == 3){
            //基于分数
            return scoreDijkstra(g,sourceVertex,dateString,time,endVertex,isLessTrans);
        }
        //最远可达
        return dijkstra2(g,sourceVertex,endVertex,transPath);
    }

    public boolean isWeekend(String dateString) {
        try{
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //基于最短时间
    private boolean dijkstra(Graph g, String sourceVertex, String dateString, String time, String endVertex, String transPath) {
        try {
            ArrayList<String> airlines = new ArrayList<String>();
            airlines.add("151020057");
            airlines.add("151020059");
            airlines.add("151020053");
            airlines.add("151020055");
            g.getReachable().clear();
            if (!initialMinTimeLink(g, sourceVertex, time,1)) {
                return false;
            }
            String ver = sourceVertex;
            visitedVertex.add(sourceVertex);
            unVisitedVertex = g.getUnVisitedVertex();
            unVisitedVertex.remove(ver);
            String verTime = time, verBefore = ver;

            while (!("null".equals(findLatestVertexMinTime(g, g.getMinTimeLink())))) {
                ver = findLatestVertexMinTime(g, g.getMinTimeLink());

                g.addReachable(ver);
                visitedVertex.add(ver);
                List<String> toBeVisitedVertex = g.getAdj().get(ver);

                for (String v : visitedVertex) {
                    if (toBeVisitedVertex.contains(v)) {
                        toBeVisitedVertex.remove(v);
                    }
                }

                verTime = g.getMinTimeLink().get(ver);

                for (String verEnd : toBeVisitedVertex) {
                    int deTime = 0;
                    int deTimeStart = 0;
                    int arrTime = 0;

                    String alltime;
                    String [] str2;
                    String s1 = g.getAccInLine().get(ver);
                    String s2 = g.getAccInLine().get(verEnd);
                    boolean notTransStation = s1.equals(s2);

                    if (notTransStation) {
                        alltime = findLatestTime(g, ver, verEnd, verTime, 0, 1);
                        str2 = alltime.split(",");
                        if ("25:59:59".equals(str2[0])||"25:59:59".equals(str2[1])||"25:59:59".equals(str2[2])) {
                            continue;
                        }
                        deTime = CommonTools.TransferTime(str2[0]);
                        deTimeStart = CommonTools.TransferTime(str2[2]);
                        arrTime = CommonTools.TransferTime(str2[1]);
                    } else {
                        String verTime2 = g.getMinTimeLink2().get(ver);
                        // bug: change first part to arriving time
                        deTime = CommonTools.TransferTime(verTime2) + Integer.parseInt(g.getTransTime().get(ver + verEnd));
                        arrTime = deTime;
                        deTimeStart = CommonTools.TransferTime(verTime2);
                    }

                    if (g.getMinTimeLink().get(verEnd) == null) {
                        g.getMinTimeLink().put(verEnd, CommonTools.SecondToTime(deTime));
                        g.getMinTimeLink2().put(verEnd, CommonTools.SecondToTime(arrTime));
                        g.addStack(ver, verEnd, CommonTools.SecondToTime(deTime), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                    } else {
                        int tempTime = CommonTools.TransferTime(g.getMinTimeLink().get(verEnd));
                        if (deTime < tempTime) {
                            g.getMinTimeLink().put(verEnd, CommonTools.SecondToTime(deTime));
                            g.getMinTimeLink2().put(verEnd, CommonTools.SecondToTime(arrTime));
                            g.addStack(ver, verEnd, CommonTools.SecondToTime(deTime), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //最晚出发时间
    private boolean dijkstra3(Graph g, String sourceVertex, String dateString, String time, String endVertex, Boolean isLessTrans) {
        try {
            g.getReachable().clear();
            if(!initialMinTimeLink(g,endVertex,time,3)) {
                return false;
            }
            String ver=endVertex;
            visitedVertex.add(endVertex);
            unVisitedVertex =g.getUnVisitedVertex();
            unVisitedVertex.remove(ver);
            String verTime=null,verBefore=ver;

            while(!("null".equals(findLatestVertexLatestDepartTime(g,g.getMinTimeLink())))) {
                ver= findLatestVertexLatestDepartTime(g,g.getMinTimeLink());
                g.addReachable(ver);
                visitedVertex.add(ver);
                List<String> toBeVisitedVertex = g.getAdj3().get(ver);

                for(String v:visitedVertex) {
                    if(toBeVisitedVertex.contains(v)) {
                        toBeVisitedVertex.remove(v);
                    }
                }

                verTime=g.getMinTimeLink().get(ver);

                for(String verStart : toBeVisitedVertex) {
                    int deTime=0;
                    //departure time of started station
                    int deTimeStart=0;
                    int arrTime=0;

                    String alltime;
                    String [] str2;
                    String s1=g.getAccInLine().get(ver);
                    String s2=g.getAccInLine().get(verStart);
                    boolean notTransStation=s1.equals(s2);

                    if(notTransStation) {
                        alltime = findLatestTime(g,ver,verStart,verTime,0,3);
                        if ("25:59:59".equals(alltime)) {
                            continue;
                        }
                        str2= alltime.split(",");
                        deTime = CommonTools.TransferTime(str2[0]);
                        arrTime = CommonTools.TransferTime(str2[1]);
                        deTimeStart =  CommonTools.TransferTime(str2[2]);
                    } else{
                        deTime = CommonTools.TransferTime(verTime);
                        arrTime = deTime;
                        deTimeStart =  deTime - Integer.parseInt(g.getTransTime().get(ver+verStart));
                    }

                    if(g.getMinTimeLink().get(verStart)==null) {

                        g.getMinTimeLink().put(verStart, CommonTools.SecondToTime(deTimeStart));
                        g.getMinTimeLink2().put(verStart, CommonTools.SecondToTime(arrTime));
                        g.addStack(verStart, ver, CommonTools.SecondToTime(deTime), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                    } else {

                        int tempTime= CommonTools.TransferTime(g.getMinTimeLink().get(verStart));
                        if(deTime<tempTime ) {
                            g.getMinTimeLink().put(verStart, CommonTools.SecondToTime(deTimeStart));
                            g.getMinTimeLink2().put(verStart, CommonTools.SecondToTime(arrTime));
                            g.addStack(verStart,ver, CommonTools.SecondToTime(deTime), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //基于最短距离
    private boolean dijkstra2(Graph g, String sourceVertex, String endVertex, String transPath) {
        try {
            if(!initialMinTimeLink(g,sourceVertex,"",2)) {
                return false;
            }
            visitedVertex.add(sourceVertex);
            while(!("null".equals(findLatestVertexDistance(g,g.getMinDisLink())))) {
                String ver= findLatestVertexDistance(g,g.getMinDisLink());
                g.addReachable(ver);
                visitedVertex.add(ver);
                List<String> toBeVisitedVertex = g.getAdj().get(ver);
                for(String v:visitedVertex) {
                    if (toBeVisitedVertex.contains(v)) {
                        toBeVisitedVertex.remove(v);
                    }
                }
                int verDist=g.getMinDisLink().get(ver);
                for(String verEnd : toBeVisitedVertex) {
                    int	transDist=g.getStationdistance().get(ver+verEnd);
                    if(g.getMinDisLink().get(verEnd)==null) {
                        g.getMinDisLink().put(verEnd,verDist+transDist);
                        g.addStack2(ver, verEnd,verDist+transDist);
                    } else {
                        int tempTime=g.getMinDisLink().get(verEnd);
                        if(verDist+transDist<tempTime) {
                            g.getMinDisLink().put(verEnd,verDist+transDist);
                            g.addStack2(ver,verEnd, verDist+transDist);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return true;
    }

    //基于分数（换乘次数越少，分数越大）
    private boolean scoreDijkstra(Graph g,String sourceVertex,String dateString,String time,String endVertex,Boolean isLessTrans) {
        try {
            ArrayList<String> airlines = new ArrayList<String>();
            airlines.add("151020057");
            airlines.add("151020059");
            airlines.add("151020053");
            airlines.add("151020055");
            g.getReachable().clear();

            if (isLessTrans){
                if (!initialMinTimeLink(g, sourceVertex, time, 4)) {
                    return false;
                }
            } else {
                if (!initialMinTimeLink(g, sourceVertex, time, 1)) {
                    return false;
                }
            }

            String ver = sourceVertex;
            visitedVertex.add(sourceVertex);
            unVisitedVertex = g.getUnVisitedVertex();
            unVisitedVertex.remove(ver);
            String verTime = time;
            String verBefore = ver;
            Double verScore = 0.0;

            if (isLessTrans){
                ver = findLatestVertexScore(g, g.getMinScoreLink());
            } else {
                ver = findLatestVertexMinTime(g, g.getMinTimeLink());
            }

            while (!("null".equals( ver ))) {
                g.addReachable(ver);
                visitedVertex.add(ver);
                List<String> toBeVisitedVertex = g.getAdj().get(ver);

                for (String v : visitedVertex) {
                    if (toBeVisitedVertex.contains(v)) {
                        toBeVisitedVertex.remove(v);
                    }
                }

                verTime = g.getMinTimeLink().get(ver);
                verScore = g.getMinScoreLink().get(ver);

                for (String verEnd : toBeVisitedVertex) {
                    int deTime= 0;
                    int deTimeStart = 0;
                    int arrTime = 0;
                    String alltime;
                    String [] str2;
                    String s1 = g.getAccInLine().get(ver);
                    String s2 = g.getAccInLine().get(verEnd);
                    boolean notTransStation = s1.equals(s2);

                    if (notTransStation) {
                        alltime = findLatestTime(g, ver, verEnd, verTime, 0, 1);
                        str2 = alltime.split(",");
                        if ("25:59:59".equals(str2[0]) || "25:59:59".equals(str2[1]) || "25:59:59".equals(str2[2])) {
                            continue;
                        }
                        //str2[0] = next_departure time, str2[1]= arriving time
                        deTime = CommonTools.TransferTime(str2[0]);
                        deTimeStart = CommonTools.TransferTime(str2[2]);
                        arrTime = CommonTools.TransferTime(str2[1]);
                    } else {
                        if (isLessTrans == false) {
                            verTime = g.getMinTimeLink2().get(ver);
                        }
                        deTime = CommonTools.TransferTime(verTime) + Integer.parseInt(g.getTransTime().get(ver + verEnd));
                        arrTime = deTime;
                        deTimeStart = CommonTools.TransferTime(verTime);
                        if (airlines.contains(ver) || airlines.contains(verEnd)) {
                            deTime += TRANSFERWEIGHT * 5;
                        } else if (isLessTrans == true) {
                            deTime += TRANSFERWEIGHT;
                        }
                    }

                    if (isLessTrans) {
                        if (g.getMinScoreLink().get(verEnd) == null) {
                            g.getMinScoreLink().put(verEnd, verScore+deTime+0.0);
                            if (deTime> TRANSFERWEIGHT && deTime < TRANSFERWEIGHT*5){
                                g.getMinTimeLink().put(verEnd, CommonTools.SecondToTime(deTime-TRANSFERWEIGHT));
                                g.addScoreStack(ver, verEnd, CommonTools.SecondToTime(deTime-TRANSFERWEIGHT), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                            } else if (deTime > TRANSFERWEIGHT*5){
                                g.getMinTimeLink().put(verEnd, CommonTools.SecondToTime(deTime-TRANSFERWEIGHT*5));
                                g.addScoreStack(ver, verEnd, CommonTools.SecondToTime(deTime-TRANSFERWEIGHT*5), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                            } else{
                                g.getMinTimeLink().put(verEnd, CommonTools.SecondToTime(deTime));
                                g.addScoreStack(ver, verEnd, CommonTools.SecondToTime(deTime), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                            }
                        } else {
                            double tempTime =g.getMinScoreLink().get(verEnd);
                            if (deTime < tempTime) {
                                g.getMinScoreLink().put(verEnd, verScore+deTime+0.0);
                                if (deTime> TRANSFERWEIGHT && deTime < TRANSFERWEIGHT*5){
                                    g.getMinTimeLink().put(verEnd, CommonTools.SecondToTime(deTime- TRANSFERWEIGHT));
                                    g.addScoreStack(ver, verEnd, CommonTools.SecondToTime(deTime- TRANSFERWEIGHT), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                                } else if (deTime > TRANSFERWEIGHT*5){
                                    g.getMinTimeLink().put(verEnd, CommonTools.SecondToTime(deTime- TRANSFERWEIGHT*5));
                                    g.addScoreStack(ver, verEnd, CommonTools.SecondToTime(deTime- TRANSFERWEIGHT*5), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                                } else {
                                    g.getMinTimeLink().put(verEnd, CommonTools.SecondToTime(deTime));
                                    g.addScoreStack(ver, verEnd, CommonTools.SecondToTime(deTime), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                                }
                            }
                        }
                    } else {
                        if (g.getMinTimeLink().get(verEnd) == null) {
                            g.getMinTimeLink().put(verEnd, CommonTools.SecondToTime(deTime));
                            g.getMinTimeLink2().put(verEnd, CommonTools.SecondToTime(arrTime));
                            g.addScoreStack(ver, verEnd, CommonTools.SecondToTime(deTime), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                        } else {
                            int tempTime = CommonTools.TransferTime(g.getMinTimeLink().get(verEnd));
                            if (deTime < tempTime) {
                                g.getMinTimeLink().put(verEnd, CommonTools.SecondToTime(deTime));
                                g.getMinTimeLink2().put(verEnd, CommonTools.SecondToTime(arrTime));
                                g.addScoreStack(ver, verEnd, CommonTools.SecondToTime(deTime), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                            }
                        }
                    }
                }

                if (isLessTrans){
                    ver = findLatestVertexScore(g, g.getMinScoreLink());
                } else {
                    ver = findLatestVertexMinTime(g, g.getMinTimeLink());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //时间最小
    private String findLatestVertexMinTime(Graph g, Map<String,String> map) {
        String vertex="null";
        String minTime=Graph.UPPER_LIMIT_TIME;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if(!visitedVertex.contains(entry.getKey())) {
                if(CommonTools.TransferTime(entry.getValue())- CommonTools.TransferTime(minTime)<0) {
                    vertex = entry.getKey();
                    minTime = entry.getValue();
                }
            }
        }
        if (!minTime.equals(Graph.UPPER_LIMIT_TIME)) {
            return vertex;
        }
        return "null";
    }

    //分数
    private String findLatestVertexScore(Graph g, Map<String,Double> map) {
        String vertex="null";
        Double minTime = TRANSFERWEIGHT*100+0.0;
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if(!visitedVertex.contains(entry.getKey())) {
                if(entry.getValue() < minTime) {
                    vertex = entry.getKey();
                    minTime = entry.getValue();
                }
            }
        }
        if (minTime != TRANSFERWEIGHT*100+0.0) {
            return vertex;
        }
        return "null";
    }

    //最晚出发时间
    private String findLatestVertexLatestDepartTime(Graph g, Map<String,String> map) {
        String vertex="null";
        String maxTime="01:00:00";
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if(!visitedVertex.contains(entry.getKey())) {
                int a1 = CommonTools.TransferTime(maxTime);
                int a2 = CommonTools.TransferTime(entry.getValue());
                if(CommonTools.TransferTime(entry.getValue())- CommonTools.TransferTime(maxTime)>0) {
                    vertex = entry.getKey();
                    maxTime = entry.getValue();
                }
            }
        }
        if (!maxTime.equals(Graph.UPPER_LIMIT_TIME)) {
            return vertex;
        }
        return "null";
    }

    //距离
    private String findLatestVertexDistance(Graph g, Map<String,Integer> map) {
        String vertex="null";
        int mindis=Graph.UPPER_LIMIT_DIS;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if(!visitedVertex.contains(entry.getKey())) {
                if(entry.getValue()-mindis<0) {
                    vertex=entry.getKey();
                    mindis=entry.getValue();
                };
            }
        }
        if(mindis!=Graph.UPPER_LIMIT_DIS) {
            return vertex;
        }
        return "null";
    }

    private boolean initialMinTimeLink(Graph g, String vertex, String verTime, int type) {
        List<String> toBeUpdatedVertex = null;
        if (type == 3) {
            toBeUpdatedVertex = g.getAdj3().get(vertex);
        } else {
            toBeUpdatedVertex = g.getAdj().get(vertex);
        }
        if (type == 2){
            g.getMinDisLink().clear();
        } else {
            g.getMinTimeLink().clear();
        }

        for(String adjVertex:toBeUpdatedVertex) {
            // 按距离算
            if (type == 2) {
                int distance=Graph.UPPER_LIMIT_DIS;
                if(g.getStationdistance().get(vertex+adjVertex)==null) {
                    g.getMinDisLink().put(adjVertex,Graph.UPPER_LIMIT_DIS);
                } else {
                    distance=g.getStationdistance().get(vertex+adjVertex);
                    g.getMinDisLink().put(adjVertex,distance);
                }
                g.addStack2(vertex, adjVertex, distance);
            } else {
                if (type == 4){
                    if(g.getMinTimeLink().get(adjVertex)==null) {
                        g.getMinTimeLink().put(adjVertex,new String());
                        g.getMinScoreLink().put(adjVertex,null);
                    }
                } else {
                    if (g.getMinTimeLink().get(adjVertex) == null) {
                        g.getMinTimeLink().put(adjVertex, new String());
                    }
                    if (g.getMinTimeLink2().get(adjVertex) == null) {
                        g.getMinTimeLink2().put(adjVertex, new String());
                    }
                }
                String alltime;
                int adjTime = 0;
                int arrTime = 0;
                int deTimeStart = 0;
                String[] str;
                double score = 0.0;
                if (g.getAccInLine().get(vertex).equals(g.getAccInLine().get(adjVertex))) {
                    if (type == 4) {
                        alltime = findLatestTime(g, vertex, adjVertex, verTime, 0, 1);
                    } else {
                        alltime = findLatestTime(g, vertex, adjVertex, verTime, 0, type);
                    }
                    if ("25:59:59".equals(alltime)) {
                        return false;
                    }
                    str = alltime.split(",");
                    adjTime = CommonTools.TransferTime(str[0]);
                    deTimeStart = CommonTools.TransferTime(str[2]);
                    arrTime = CommonTools.TransferTime(str[1]);
                    score = adjTime + 0.0;
                } else {
                    if (type == 1 || type == 4) {
                        adjTime = (CommonTools.TransferTime(verTime) + Integer.parseInt(g.getTransTime().get(vertex + adjVertex)));
                        arrTime = adjTime;
                        deTimeStart = CommonTools.TransferTime(verTime);
                    } else if (type == 3) {
                        adjTime = CommonTools.TransferTime(verTime);
                        arrTime = adjTime;
                        deTimeStart = adjTime - Integer.parseInt(g.getTransTime().get(adjVertex + vertex));
                    }
                    score = adjTime + TRANSFERWEIGHT + 0.0;
                }

                if (type == 1) {
                    g.getMinTimeLink().put(adjVertex, CommonTools.SecondToTime(adjTime));
                    g.getMinTimeLink2().put(adjVertex, CommonTools.SecondToTime(arrTime));
                    g.addStack(vertex, adjVertex, CommonTools.SecondToTime(adjTime), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                } else if (type == 3) {
                    g.getMinTimeLink().put(adjVertex, CommonTools.SecondToTime(deTimeStart));
                    g.getMinTimeLink2().put(adjVertex, CommonTools.SecondToTime(arrTime));
                    g.addStack(adjVertex, vertex, CommonTools.SecondToTime(adjTime), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                } else if (type == 4){
                    g.getMinScoreLink().put(adjVertex, score);
                    g.addScoreStack(vertex, adjVertex, CommonTools.SecondToTime(adjTime), CommonTools.SecondToTime(arrTime), CommonTools.SecondToTime(deTimeStart));
                    g.getMinTimeLink().put(adjVertex, CommonTools.SecondToTime(adjTime));
                }

            }
        }
        return true;
    }

    private String findLatestTime(Graph g, String verStart, String verEnd, String verStartTime, int isTransStation, int type) {
        List<String> toBeVisitedTime=new ArrayList<String>();
        int second = 0;
        if(isWeekend(date)) {
            if(g.getTimetableWeekend().get(verStart+verEnd)==null) {
                return Graph.UPPER_LIMIT_TIME;
            }
            toBeVisitedTime = g.getTimetableWeekend().get(verStart+verEnd);
        } else{
            if(g.getTimetableWeekday().get(verStart+verEnd)==null) {
                return Graph.UPPER_LIMIT_TIME;
            }
            toBeVisitedTime = g.getTimetableWeekday().get(verStart+verEnd);
        }
        int minSecond=1000000;
        //departure time of end_station
        String latestTime=Graph.UPPER_LIMIT_TIME;
        //departure time of start_station
        String latestTime1=Graph.UPPER_LIMIT_TIME;
        String arrtime = Graph.UPPER_LIMIT_TIME;
        String latestArrTime = Graph.UPPER_LIMIT_TIME;
        String [] str;
        String startTime,endTime,allTime;

        for(String vTime:toBeVisitedTime) {
            str=vTime.split(",");
            startTime=str[0];
            if(isTransStation==1) {
                // change route
                endTime=str[2];
            } else {
                //no change, same route
                endTime=str[1];
                arrtime = str[2];
            }
            if (type == 1) {
                second = CommonTools.TransferTime(startTime) - CommonTools.TransferTime(verStartTime);
            } else if (type == 3) {
                second= CommonTools.TransferTime(verStartTime) - CommonTools.TransferTime(arrtime);
            }
            if(second>=0&&second<minSecond) {
                minSecond=second;
                latestTime=endTime;
                latestArrTime = arrtime;
                latestTime1 = startTime;
            }
        }
        allTime = latestTime+ ","+ latestArrTime+","+ latestTime1;
        return allTime;
    }

}

