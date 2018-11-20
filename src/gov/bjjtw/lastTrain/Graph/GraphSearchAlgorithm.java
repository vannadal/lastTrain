package gov.bjjtw.lastTrain.Graph;

import gov.bjjtw.lastTrain.CommonTools.CommonTools;

import java.util.*;

/**
 * Graph{@link String}图模型算法
 *
 * <p> 末班车可达技术应用图模型的算法
 * <a href="https://github.com/bjjtwxxzx/lastTrain">项目位置</a>
 * 更全面的项目 {@code String} 信息.
 *
 * @author wuxinran@bjjtw.gov.cn
 */
public class GraphSearchAlgorithm {
    /** 图模型已访问过的节点集合 */
    private Set<String> visitedVertex;
    /** 图模型未访问过的节点集合*/
    public Set<String> unVisitedVertex =new HashSet<String>();
    /** 日期字符串 */
    public String date;
    /** 少换乘优先时换乘站分数代价的常数*/
    public static final int TRANSFERWEIGHT = 10000000;

    /**
     * 选取对应场景，调用寻路算法
     * @param g 图模型对象
     * @param sourceVertex 出发地节点accCode
     * @param dateString 日期字符串
     * @param timeStr 时间字符串
     * @param endVertex 目的地节点accCode
     * @param isReverse true=给定到达时间最晚出发时间场景, false=给定起始时间最早到达时间场景
     * @param isLessTrans true=少换乘优先, false=时间优先
     * @param type 1=基于距离优先可达模式, 3=基于分数优先可达模式, 2=最远可达模式
     * @return boolean 是否可以找到一条通路
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    public boolean perform(Graph g, String sourceVertex,String dateString,String timeStr,String endVertex, Boolean isReverse, Boolean isLessTrans, int type) {
        if (null == visitedVertex) {
            visitedVertex = new HashSet<>();
        }
        date=dateString;
        if (isReverse){
            // 不包含少换乘优先的分支
            return dijkstra3(g,timeStr,endVertex,isLessTrans);
        }

        if (type == 1){
            //基于距离
            return dijkstra(g,sourceVertex,timeStr);
        } else if (type == 3){
            //基于分数
            return scoreDijkstra(g,sourceVertex,timeStr,isLessTrans);
        }
        //最远可达
        return dijkstra2(g,sourceVertex);
    }

    /**
     * 基于最短时间到达优先场景下dijkstra寻路算法
     * @param g 图模型对象
     * @param sourceVertex 出发地节点accCode
     * @param timeStr 出发时间字符串
     * @return boolean 是否找到一条通路
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    private boolean dijkstra(Graph g, String sourceVertex, String timeStr) {
        try {
            ArrayList<String> airlines = new ArrayList<String>();
            airlines.add("151020057");
            airlines.add("151020059");
            airlines.add("151020053");
            airlines.add("151020055");
            g.getReachable().clear();
            if (!initialMinTimeLink(g, sourceVertex, timeStr,1)) {
                return false;
            }
            String ver = sourceVertex;
            visitedVertex.add(sourceVertex);
            unVisitedVertex = g.getUnVisitedVertex();
            unVisitedVertex.remove(ver);
            String verTime = timeStr, verBefore = ver;

            while (!("null".equals(findLatestVertexMinTime(g.getMinTimeLink())))) {
                ver = findLatestVertexMinTime(g.getMinTimeLink());

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
                        deTime = CommonTools.transferTime(str2[0]);
                        deTimeStart = CommonTools.transferTime(str2[2]);
                        arrTime = CommonTools.transferTime(str2[1]);
                    } else {
                        String verTime2 = g.getMinTimeLink2().get(ver);
                        // bug: change first part to arriving time
                        deTime = CommonTools.transferTime(verTime2) + Integer.parseInt(g.getTransTime().get(ver + verEnd));
                        arrTime = deTime;
                        deTimeStart = CommonTools.transferTime(verTime2);
                    }

                    if (g.getMinTimeLink().get(verEnd) == null) {
                        g.getMinTimeLink().put(verEnd, CommonTools.secondToTime(deTime));
                        g.getMinTimeLink2().put(verEnd, CommonTools.secondToTime(arrTime));
                        g.addStack(ver, verEnd, CommonTools.secondToTime(deTime), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                    } else {
                        int tempTime = CommonTools.transferTime(g.getMinTimeLink().get(verEnd));
                        if (deTime < tempTime) {
                            g.getMinTimeLink().put(verEnd, CommonTools.secondToTime(deTime));
                            g.getMinTimeLink2().put(verEnd, CommonTools.secondToTime(arrTime));
                            g.addStack(ver, verEnd, CommonTools.secondToTime(deTime), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 通过目的地最晚到达时间/计算出发地最晚出发时间场景下dijkstra寻路算法
     * @param g 图模型对象
     * @param timeStr 到达目的地最晚时间
     * @param endVertex 目的地AccCode
     * @param isLessTrans 是否少换乘模式(未实现)
     * @return boolean 是否找到一条通路
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    //最晚出发时间
    private boolean dijkstra3(Graph g, String timeStr, String endVertex, Boolean isLessTrans) {
        try {
            g.getReachable().clear();
            if(!initialMinTimeLink(g,endVertex,timeStr,3)) {
                return false;
            }
            String ver=endVertex;
            visitedVertex.add(endVertex);
            unVisitedVertex =g.getUnVisitedVertex();
            unVisitedVertex.remove(ver);
            String verTime=null,verBefore=ver;

            while(!("null".equals(findLatestVertexLatestDepartTime(g.getMinTimeLink())))) {
                ver= findLatestVertexLatestDepartTime(g.getMinTimeLink());
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
                            g.getMinTimeLink().remove(verStart);
                            g.getMinTimeLink2().remove(verStart);
                            continue;
                        }
                        str2= alltime.split(",");
                        deTime = CommonTools.transferTime(str2[0]);
                        arrTime = CommonTools.transferTime(str2[1]);
                        deTimeStart =  CommonTools.transferTime(str2[2]);
                    } else{
                        deTime = CommonTools.transferTime(verTime);
                        arrTime = deTime;
                        deTimeStart =  deTime - Integer.parseInt(g.getTransTime().get(ver+verStart));
                    }

                    if(g.getMinTimeLink().get(verStart)==null) {

                        g.getMinTimeLink().put(verStart, CommonTools.secondToTime(deTimeStart));
                        g.getMinTimeLink2().put(verStart, CommonTools.secondToTime(arrTime));
                        g.addStack(verStart, ver, CommonTools.secondToTime(deTime), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                    } else {

                        int tempTime= CommonTools.transferTime(g.getMinTimeLink().get(verStart));
                        if(deTime<tempTime ) {
                            g.getMinTimeLink().put(verStart, CommonTools.secondToTime(deTimeStart));
                            g.getMinTimeLink2().put(verStart, CommonTools.secondToTime(arrTime));
                            g.addStack(verStart,ver, CommonTools.secondToTime(deTime), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 基于最短距离到达优先场景下dijkstra寻路算法
     * @param g 图模型对象
     * @param sourceVertex 出发地AccCode
     * @return boolean 是否找到一条通路
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    //基于最短距离
    private boolean dijkstra2(Graph g, String sourceVertex) {
        try {
            if(!initialMinTimeLink(g,sourceVertex,"",2)) {
                return false;
            }
            visitedVertex.add(sourceVertex);
            while(!("null".equals(findLatestVertexDistance(g.getMinDisLink())))) {
                String ver= findLatestVertexDistance(g.getMinDisLink());
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
                    //System.out.println(ver+verEnd);
                    int transDist = 0;
                    if (g.getStationDistance().containsKey(ver+verEnd) == false) {
                        continue;
                    } else {
                        transDist = g.getStationDistance().get(ver + verEnd);
                    }
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
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 基于最低分数到达优先场景下dijkstra寻路算法
     * @param g 图模型对象
     * @param sourceVertex 出发地AccCode
     * @param timeStr 出发时间字符串
     * @param isLessTrans 是否少换乘模式
     * @return boolean 是否找到一条通路
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    //基于分数（换乘次数越少，分数越大）
    private boolean scoreDijkstra(Graph g,String sourceVertex,String timeStr,Boolean isLessTrans) {
        try {
            ArrayList<String> airlines = new ArrayList<String>();
            airlines.add("151020057");
            airlines.add("151020059");
            airlines.add("151020053");
            airlines.add("151020055");
            g.getReachable().clear();

            if (isLessTrans){
                if (!initialMinTimeLink(g, sourceVertex, timeStr, 4)) {
                    return false;
                }
            } else {
                if (!initialMinTimeLink(g, sourceVertex, timeStr, 1)) {
                    return false;
                }
            }

            String ver = sourceVertex;
            visitedVertex.add(sourceVertex);
            unVisitedVertex = g.getUnVisitedVertex();
            unVisitedVertex.remove(ver);
            String verTime = timeStr;
            String verBefore = ver;
            Double verScore = 0.0;

            if (isLessTrans){
                ver = findLatestVertexScore(g.getMinScoreLink());
            } else {
                ver = findLatestVertexMinTime(g.getMinTimeLink());
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
                        deTime = CommonTools.transferTime(str2[0]);
                        deTimeStart = CommonTools.transferTime(str2[2]);
                        arrTime = CommonTools.transferTime(str2[1]);
                    } else {
                        if (isLessTrans == false) {
                            verTime = g.getMinTimeLink2().get(ver);
                        }
                        deTime = CommonTools.transferTime(verTime) + Integer.parseInt(g.getTransTime().get(ver + verEnd));
                        arrTime = deTime;
                        deTimeStart = CommonTools.transferTime(verTime);
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
                                g.getMinTimeLink().put(verEnd, CommonTools.secondToTime(deTime-TRANSFERWEIGHT));
                                g.addScoreStack(ver, verEnd, CommonTools.secondToTime(deTime-TRANSFERWEIGHT), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                            } else if (deTime > TRANSFERWEIGHT*5){
                                g.getMinTimeLink().put(verEnd, CommonTools.secondToTime(deTime-TRANSFERWEIGHT*5));
                                g.addScoreStack(ver, verEnd, CommonTools.secondToTime(deTime-TRANSFERWEIGHT*5), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                            } else{
                                g.getMinTimeLink().put(verEnd, CommonTools.secondToTime(deTime));
                                g.addScoreStack(ver, verEnd, CommonTools.secondToTime(deTime), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                            }
                        } else {
                            double tempTime =g.getMinScoreLink().get(verEnd);
                            if (deTime < tempTime) {
                                g.getMinScoreLink().put(verEnd, verScore+deTime+0.0);
                                if (deTime> TRANSFERWEIGHT && deTime < TRANSFERWEIGHT*5){
                                    g.getMinTimeLink().put(verEnd, CommonTools.secondToTime(deTime- TRANSFERWEIGHT));
                                    g.addScoreStack(ver, verEnd, CommonTools.secondToTime(deTime- TRANSFERWEIGHT), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                                } else if (deTime > TRANSFERWEIGHT*5){
                                    g.getMinTimeLink().put(verEnd, CommonTools.secondToTime(deTime- TRANSFERWEIGHT*5));
                                    g.addScoreStack(ver, verEnd, CommonTools.secondToTime(deTime- TRANSFERWEIGHT*5), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                                } else {
                                    g.getMinTimeLink().put(verEnd, CommonTools.secondToTime(deTime));
                                    g.addScoreStack(ver, verEnd, CommonTools.secondToTime(deTime), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                                }
                            }
                        }
                    } else {
                        if (g.getMinTimeLink().get(verEnd) == null) {
                            g.getMinTimeLink().put(verEnd, CommonTools.secondToTime(deTime));
                            g.getMinTimeLink2().put(verEnd, CommonTools.secondToTime(arrTime));
                            g.addScoreStack(ver, verEnd, CommonTools.secondToTime(deTime), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                        } else {
                            int tempTime = CommonTools.transferTime(g.getMinTimeLink().get(verEnd));
                            if (deTime < tempTime) {
                                g.getMinTimeLink().put(verEnd, CommonTools.secondToTime(deTime));
                                g.getMinTimeLink2().put(verEnd, CommonTools.secondToTime(arrTime));
                                g.addScoreStack(ver, verEnd, CommonTools.secondToTime(deTime), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                            }
                        }
                    }
                }

                if (isLessTrans){
                    ver = findLatestVertexScore(g.getMinScoreLink());
                } else {
                    ver = findLatestVertexMinTime(g.getMinTimeLink());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * findLatestVertexMinTime 寻找集合中到达后最小时间节点
     * @param map 候选节点集合对应时间
     * @return String 集合中最小时间节点
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    //时间最小
    private String findLatestVertexMinTime(Map<String,String> map) {
        String vertex="null";
        String minTime=Graph.UPPER_LIMIT_TIME;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if(!visitedVertex.contains(entry.getKey())) {
                if(CommonTools.transferTime(entry.getValue())- CommonTools.transferTime(minTime)<0) {
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

    /**
     * findLatestVertexMinScore 寻找集合中到达后最小分数节点
     * @param map 候选节点集合对应分数
     * @return String 集合中最小分数节点
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    //分数
    private String findLatestVertexScore(Map<String,Double> map) {
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

    /**
     * findLatestVertexLatestDepartTime 寻找集合中从该站出发最晚时间的节点
     * @param map 候选节点集合对应时间
     * @return String 集合中最晚时间节点
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    //最晚出发时间
    private String findLatestVertexLatestDepartTime(Map<String,String> map) {
        String vertex="null";
        String maxTime="01:00:00";
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if(!visitedVertex.contains(entry.getKey())) {
                int a1 = CommonTools.transferTime(maxTime);
                int a2 = CommonTools.transferTime(entry.getValue());
                if(CommonTools.transferTime(entry.getValue())- CommonTools.transferTime(maxTime)>0) {
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


    /**
     * findLatestVertexDistance 寻找集合中到达该站最短距离的节点
     * @param map 候选节点集合对应的距离
     * @return String 集合中最短距离节点
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    //距离
    private String findLatestVertexDistance(Map<String,Integer> map) {
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


    /**
     * initialMinTimeLink 初始化最早到达时间
     * @param g 图模型对象
     * @param vertex 起始节点accCode
     * @param verTime 起始时间字符串
     * @param type 1.分数早到达 2.最短距离 3.最晚出发 4.分数少换乘
     * @return 是否成功初始化
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
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
                if(g.getStationDistance().get(vertex+adjVertex)==null) {
                    g.getMinDisLink().put(adjVertex,Graph.UPPER_LIMIT_DIS);
                } else {
                    distance=g.getStationDistance().get(vertex+adjVertex);
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
                        g.getMinTimeLink().remove(adjVertex);
                        g.getMinTimeLink2().remove(adjVertex);
                        continue;
                    }
                    str = alltime.split(",");
                    adjTime = CommonTools.transferTime(str[0]);
                    deTimeStart = CommonTools.transferTime(str[2]);
                    arrTime = CommonTools.transferTime(str[1]);
                    score = adjTime + 0.0;
                } else {
                    if (type == 1 || type == 4) {
                        adjTime = (CommonTools.transferTime(verTime) + Integer.parseInt(g.getTransTime().get(vertex + adjVertex)));
                        arrTime = adjTime;
                        deTimeStart = CommonTools.transferTime(verTime);
                    } else if (type == 3) {
                        adjTime = CommonTools.transferTime(verTime);
                        arrTime = adjTime;
                        deTimeStart = adjTime - Integer.parseInt(g.getTransTime().get(adjVertex + vertex));
                    }
                    score = adjTime + TRANSFERWEIGHT + 0.0;
                }

                if (type == 1) {
                    g.getMinTimeLink().put(adjVertex, CommonTools.secondToTime(adjTime));
                    g.getMinTimeLink2().put(adjVertex, CommonTools.secondToTime(arrTime));
                    g.addStack(vertex, adjVertex, CommonTools.secondToTime(adjTime), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                } else if (type == 3) {
                    g.getMinTimeLink().put(adjVertex, CommonTools.secondToTime(deTimeStart));
                    g.getMinTimeLink2().put(adjVertex, CommonTools.secondToTime(arrTime));
                    g.addStack(adjVertex, vertex, CommonTools.secondToTime(adjTime), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                } else if (type == 4){
                    g.getMinScoreLink().put(adjVertex, score);
                    g.addScoreStack(vertex, adjVertex, CommonTools.secondToTime(adjTime), CommonTools.secondToTime(arrTime), CommonTools.secondToTime(deTimeStart));
                    g.getMinTimeLink().put(adjVertex, CommonTools.secondToTime(adjTime));
                }

            }
        }
        return true;
    }

    /**
     * findLatestTime 获取给定起止节点的最早到达时间字符串
     * @param g 图模型对象
     * @param verStart 起始节点accCode
     * @param verEnd 终止节点accCode
     * @param verStartTime 起始节点时间字符串
     * @param isTransStation 是否换乘优先场景
     * @param type 1.给定起始时间最早到达时间场景 3.给定到达时间最晚出发时间场景
     * @return 最早时间字符串
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/21
     */
    private String findLatestTime(Graph g, String verStart, String verEnd, String verStartTime, int isTransStation, int type) {
        List<String> toBeVisitedTime=new ArrayList<String>();
        int second = 0;
        if(CommonTools.isWeekend(date)) {
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
                second = CommonTools.transferTime(startTime) - CommonTools.transferTime(verStartTime);
            } else if (type == 3) {
                second= CommonTools.transferTime(verStartTime) - CommonTools.transferTime(arrtime);
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
