package gov.bjjtw.lastTrain.Graph;

import gov.bjjtw.lastTrain.CommonTools.CommonTools;

import java.io.*;
import java.util.*;

/**
 * Graph{@link String}图模型数据结构
 *
 * <p> 末班车可达技术应用图模型的数据结构
 * <a href="https://github.com/bjjtwxxzx/lastTrain">项目位置</a>
 * 更全面的项目 {@code String} 信息.
 *
 * @author wuxinran@bjjtw.gov.cn
 */

public final class Graph implements Serializable{
    /** 起始节点accCode*/
    private String firstVertax;
    /** 起始时间 日期*/
    private String firstTime, date;
    /** 终止节点accCode*/
    private String endVertex;

    /** accCode站最早到达时间映射关系（给定起始时间最早到达时间场景) */
    private Map<String,String> minTimeLink=new HashMap<>();
    /** accCode站最晚到达时间映射关系（给定到达时间最晚出发时间场景) */
    private Map<String,String> minTimeLink2=new HashMap<>();
    /** accCode站点最小累计分数映射关系（给定到达时间最早到达时间场景) */
    private Map<String,Double> minScoreLink=new HashMap<>();
    /** accCode站点最小距离分数映射关系（给定到达时间最早到达时间场景) */
    private Map<String,Integer> minDisLink=new HashMap<>();

    /** 时间优先的结果候选路线堆栈（给定起始时间最早到达时间场景) */
    private Stack<String> stack=new Stack<>();
    /** 分数优先的结果候选路线堆栈 */
    private Stack<String> scoreStack=new Stack<>();
    /** 结果路径节点堆栈（给定起始时间最早到达时间场景)  */
    private Stack<String> stackPath=new Stack<>();
    /** 时间优先的结果路径节点堆栈（给定到达时间最晚出发时间场景) */
    private Stack<String> stack2=new Stack<>();
    /** 结果路径节点堆栈（给定到达时间最晚出发时间场景) */
    private Stack<String> stackPath2=new Stack<>();

    /** 换乘站换乘时间映射关系 */
    private Map<String,String> transTime=new HashMap<>();
    /** 站点accCode和线路间映射关系 */
    private Map<String, String> accInLine=new HashMap<>();
    /** 站点accCode进站时间映射关系 */
    private Map<String, String> walkTimeString =new HashMap<>();
    /** 边关联关系映射（给定起始时间最早到达时间场景) */
    private Map<String, List<String>> adj = new HashMap<>();
    /** 边关联关系映射（给定到达时间最晚出发时间场景) */
    private Map<String, List<String>> adj3 = new HashMap<>();
    /** 车站间距映射关系表 */
    private Map<String, Integer> stationDistance =new HashMap<>();
    /** 工作日列车运行时间表 */
    private Map<String, List<String>> timetableWeekday = new HashMap<>();
    /** 休息日列车运行时间表 */
    private Map<String, List<String>> timetableWeekend = new HashMap<>();
    /** 未访问节点集合 */
    private Set<String> unVisitedVertex =new HashSet<String>();
    /** 车站经纬度映射关系 */
    private HashMap<String, String> stationGeo = new HashMap<String, String>();
    /** 可以到达车站accCode列表 */
    private LinkedList<String> reachableSt = new LinkedList<String>();
    /** 日期是否休息日 */
    private boolean isWeekend;
    /** 时间上限常量 */
    public static final String UPPER_LIMIT_TIME ="25:59:59";
    /** 距离上限常量 */
    public static final int UPPER_LIMIT_DIS = 10000000;
    /** 经纬度元组长度常量 */
    public static final int GEO_STRING_LENGTH = 2;

    /**
     * addGeoPosition 增加accCode对应经纬度映射
     * @param accCode accCode码
     * @param geoPosition 经纬度位置字符串
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addGeoPosition(String accCode, String geoPosition){
        stationGeo.put(accCode,geoPosition);
    }

    /**
     * getGeoPosition 获取accCode对应经纬度
     * @param accCode 查询accCode
     * @return Float [] 经纬度坐标
     * @author wuxinran@bjjtw.gov.cn
     */
    public Float [] getGeoPosition(String accCode){
        Float[] positionFloat = new Float[2];
        positionFloat[0] = null;
        positionFloat[1] = null;
        if (stationGeo.containsKey(accCode) == true) {
            String[] positionstr = stationGeo.get(accCode).split(",");
            if (positionstr.length == GEO_STRING_LENGTH) {
                positionFloat[0] = Float.valueOf(positionstr[0]);
                positionFloat[1] = Float.valueOf(positionstr[1]);
            }
        }
        return positionFloat;
    }

    /**
     * setAdj 构建图模型边（给定起始时间最早到达时间场景)
     * @param inputAdj accCode(Key)关联的accCode(Values)
     * @author wuxinran@bjjtw.gov.cn
     */
    public void setAdj(Map<String,List<String>> inputAdj){
        adj =new HashMap<>();
        for(String k: inputAdj.keySet()){
            List<String> tmpList = new ArrayList<String>();
            for(String v: inputAdj.get(k)){
                tmpList.add(v);
            }
            adj.put(k,tmpList);
        }
    }


    /**
     * setAdj 构建图模型边(给定到达时间最晚出发时间场景)
     * @param inputAdj accCode(Key)关联的accCode(Values)
     * @author wuxinran@bjjtw.gov.cn
     */
    public void setAdj3(Map<String,List<String>> inputAdj){
        adj3 =new HashMap<>();
        for(String k: inputAdj.keySet()){
            List<String> tmpList = new ArrayList<String>();
            for(String v: inputAdj.get(k)){
                tmpList.add(v);
            }
            adj3.put(k,tmpList);
        }
    }


    /**
     * initialSearchStartVertex 初始化日期、起止节点及开始时间
     * @param startVertex 起始节点
     * @param dateString 日期
     * @param startTime 起始时间
     * @param endVertex 终止节点
     * @author wuxinran@bjjtw.gov.cn
     */
    public void initialSearchStartVertex(String startVertex, String dateString, String startTime, String endVertex) {
        firstVertax=startVertex;
        firstTime=startTime;
        this.endVertex = endVertex;
        date=dateString;
    }

    /**
     * addEdge 增加边关联
     * @param fromVertex 起始节点
     * @param toVertex 终止节点
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addEdge(String fromVertex, String toVertex) {
        if (firstVertax == null) {
            firstVertax = fromVertex;
        }
        if (adj.get(fromVertex) == null) {
            adj.put(fromVertex, new ArrayList<String>());
            adj.get(fromVertex).add(toVertex);
        } else {
            adj.get(fromVertex).add(toVertex);
        }
        if (adj3.get(toVertex) == null){
            adj3.put(toVertex, new ArrayList<String>());
            adj3.get(toVertex).add(fromVertex);
        } else {
            adj3.get(toVertex).add(fromVertex);
        }
    }

    /**
     * removeEdge 移除边关联
     * @param fromVertex 起始节点
     * @param toVertex 终止节点
     * @author wuxinran@bjjtw.gov.cn
     */
    public void removeEdge(String fromVertex, String toVertex) {
        if(adj.get(fromVertex).contains(toVertex)) {
            adj.get(fromVertex).remove((toVertex));
        }
        if(adj3.get(toVertex).contains(fromVertex)){
            adj3.get(toVertex).remove((fromVertex));
        }
    }

    /**
     * addStack 在时间优先的结果候选路线堆栈中增加节点
     * @param fromVertex 起始节点
     * @param toVertex 终止节点
     * @param startTime 起始时间
     * @param arrTime 到达时间
     * @param timeStart 发车时间
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addStack(String fromVertex, String toVertex, String startTime, String arrTime, String timeStart) {
        stack.push(fromVertex+","+toVertex+","+startTime+","+arrTime+","+timeStart);
    }

    /**
     * addScoreStack 在分数优先的结果候选路线堆栈中增加节点
     * @param fromVertex 起始节点
     * @param toVertex 终止节点
     * @param time 起始时间
     * @param arrTime 到达时间
     * @param timeStart 发车时间
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addScoreStack(String fromVertex,String toVertex,String time,String arrTime,String timeStart) {
        scoreStack.push(fromVertex+","+toVertex+","+time+","+arrTime+","+timeStart);
    }

    /**
     * addStackPath 通过候选路线堆栈中增加结果路径节点
     * @param str 路径节点信息
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addStackPath(String str) {
          stackPath.push(str);
    }

    /**
     * addStackPath2 通过候选路线堆栈中增加无时间约束路径节点
     * @param str 路径节点信息
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addStackPath2(String str) {
          stackPath2.push(str);
    }

    /**
     * addReachable 增加可到达车站accCode
     * @param station 车站accCode
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addReachable(String station) {
          reachableSt.add(station);
    }

    /**
     * getReachable 获取可到达车站accCode列表
     * @return 车站accCode列表
     * @author wuxinran@bjjtw.gov.cn
     */
    public LinkedList<String> getReachable(){
          return reachableSt;
    }

    /**
     * getStack 获取结果路径节点堆栈
     * @return 结果路径节点栈
     * @author wuxinran@bjjtw.gov.cn
     */
    public Stack<String> getStack() {
          return stack;
    }

    /**
     * getScoreStack 获取分数优先结果路径节点堆栈
     * @return 结果路径节点栈
     * @author wuxinran@bjjtw.gov.cn
     */
    public Stack<String> getScoreStack() {
        return scoreStack;
    }

    /**
     * getPathStack 获取候选路线堆栈中结果路径节点
     * @return 结果路径节点栈
     * @author wuxinran@bjjtw.gov.cn
     */
    public Stack<String> getPathStack() {
          return stackPath;
    }

    /**
     * getStack2 获取逆向候选路线堆栈中结果路径节点
     * @return 结果路径节点栈
     * @author wuxinran@bjjtw.gov.cn
     */
    public Stack<String> getStack2() {
          return stack2;
    }

    /**
     * addStack2 增加逆向候选路线堆栈中增加结果节点
     * @param startVertex 前继节点accCode码
     * @param endVertex 后续节点accCode码
     * @param distance2 区间距离
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addStack2(String startVertex, String endVertex, int distance2) {
        stack2.push(startVertex+","+endVertex+","+distance2);
    }

    /**
     * getPathStack2 获取逆向候选路线堆栈中无时间约束路径节点
     * @return 结果路径节点栈
     * @author wuxinran@bjjtw.gov.cn
     */
    public Stack<String> getPathStack2() {
          return stackPath2;
    }

    /**
     * addTransTime 增加换乘时间
     * @param verStart 起始节点
     * @param verEnd 终止节点
     * @param startTime 开始时间
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addTransTime(String verStart, String verEnd,String startTime) {
        transTime.put(verStart+verEnd,startTime);
    }

    /**
     * addWeekdayTimetable 增加工作日列车运行时间表条目
     * @param accCode 车站accCode码
     * @param departureTime1 起始时间
     * @param departureTime2 出发时间
     * @param arrivingTime 到达时间
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addWeekdayTimetable(String accCode, String departureTime1, String departureTime2, String arrivingTime) {
        if(timetableWeekday.get(accCode)==null) {
            timetableWeekday.put(accCode, new ArrayList<String>());
        }
        timetableWeekday.get(accCode).add(departureTime1+","+departureTime2+","+arrivingTime);
    }

    /**
     * addWeekendTimetable 增加周末列车运行时间表条目
     * @param accCode 车站accCode码
     * @param departureTime1 起始时间
     * @param departureTime2 出发时间
     * @param arrivingTime 到达时间
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addWeekendTimetable(String accCode, String departureTime1, String departureTime2, String arrivingTime) {
        if(timetableWeekend.get(accCode)==null) {
            timetableWeekend.put(accCode, new ArrayList<String>());
        }
        timetableWeekend.get(accCode).add(departureTime1+","+departureTime2+","+arrivingTime);
    }

    /**
     * addStationDistance 增加相邻accCode距离
     * @param accCode 相邻accCode码
     * @param distance 距离(m)
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addStationDistance(String accCode, int distance) {
        if(stationDistance.get(accCode)==null) {
            stationDistance.put(accCode, 0);
        }
        stationDistance.put(accCode,distance);
    }

    /**
     * addUnVisitedVertex 增加算法未访问集合中节点accCode
     * @param accCode accCode码
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addUnVisitedVertex(String accCode) {
          unVisitedVertex.add(accCode);
    }

    /**
     * getUnVisitedVertex 获取未访问集合中节点accCode的集合
     * @return accCode码集合
     * @author wuxinran@bjjtw.gov.cn
     */
    public Set<String> getUnVisitedVertex() {
        return unVisitedVertex;
    }

    /**
     * setUnVisitedVertex 配置未访问集合中节点accCode的集合
     * @param vertexSet 未访问节点accCode集合
     * @author wuxinran@bjjtw.gov.cn
     */
    public void setUnVisitedVertex(Set<String> vertexSet) {
        unVisitedVertex = (Set<String>) CommonTools.deepCopy(vertexSet);
    }

    /**
     * cleanScoreStack 清空分数优先结果候选路线堆栈
     * @author wuxinran@bjjtw.gov.cn
     */
    public void cleanScoreStack(){
        scoreStack.clear();
    }
    /**
     * cleanMinDisLink 清空accCode站点最小距离分数映射关系（给定到达时间最早到达时间场景)
     * @author wuxinran@bjjtw.gov.cn
     */
    public void cleanMinDisLink(){
        minDisLink.clear();
    }
    /**
     * cleanMinTimeLink 清空accCode站最早到达时间映射关系（给定起始时间最早到达时间场景)
     * @author wuxinran@bjjtw.gov.cn
     */
    public void cleanMinTimeLink(){
        minTimeLink.clear();
    }

    /**
     * cleanMinScoreLink 清空accCode站点最小累计分数映射关系
     * @author wuxinran@bjjtw.gov.cn
     */
    public void cleanMinScoreLink(){
        minScoreLink.clear();
    }

    /**
     * cleanMinTimeLink2 清空accCode站最晚到达时间映射关系（给定到达时间最晚出发时间场景)
     * @author wuxinran@bjjtw.gov.cn
     */
    public void cleanMinTimeLink2(){
        minTimeLink2.clear();
    }

    /**
     * cleanWalkTimeString 清空站点accCode进站时间映射关系
     * @author wuxinran@bjjtw.gov.cn
     */
    public void cleanWalkTimeString(){
        walkTimeString.clear();
    }

    /**
     * getMinTimeLink 获取当前accCode站点最早到达时间映射关系(给定起始时间最早到达时间场景)
     * @return 最早到达映射关系
     * @author wuxinran@bjjtw.gov.cn
     */
    public Map<String, String> getMinTimeLink() {
        return minTimeLink;
    }

    /**
     * getMinTimeLink2 获取当前accCode站点最晚到达时间映射关系(给定到达时间最晚出发时间场景)
     * @return 最晚到达映射关系
     * @author wuxinran@bjjtw.gov.cn
     */
    public Map<String, String> getMinTimeLink2() {
        return minTimeLink2;
    }

    /**
     * getMinDisLink 获取当前accCode站点最小距离分数映射关系
     * @return 最小距离到达映射关系
     * @author wuxinran@bjjtw.gov.cn
     */
    public Map<String, Integer> getMinDisLink() {
        return minDisLink;
    }

    /**
     * getMinScoreLink 获取当前accCode站点最小累计分数映射关系(给定起始时间最早到达时间场景)
     * @return 最小分数到达映射关系
     * @author wuxinran@bjjtw.gov.cn
     */
    public Map<String, Double> getMinScoreLink() {
        return minScoreLink;
    }

    /**
     * cleanStack 清空结果堆栈中路径节
     * @author wuxinran@bjjtw.gov.cn
     */
    public void cleanStack(){
        stack.clear();
    }

    /**
     * cleanStackPath 清空候选路线堆栈中结果
     * @author wuxinran@bjjtw.gov.cn
     */
    public void cleanStackPath(){
        stackPath.clear();
    }

    /**
     * cleanStackPath 清空逆向候选路线堆栈中结果
     * @author wuxinran@bjjtw.gov.cn
     */
    public void cleanStack2(){
        stack2.clear();
    }

    /**
     * cleanStackPath2 清空候选路线堆栈中无时间约束路径节点
     * @author wuxinran@bjjtw.gov.cn
     */
    public void cleanStackPath2(){
        stackPath2.clear();
    }

    /**
     * cleanReachableSt 清空可到达站集合
     * @author wuxinran@bjjtw.gov.cn
     */
    public void cleanReachableSt(){
        reachableSt.clear();
    }

    /**
     * resetParams 重置参数
     * @author wuxinran@bjjtw.gov.cn
     */
    public void resetParams(){
        firstTime = null;
        date = null;
        endVertex = null;
    }

    /**
     * addAccInLine 增加accCode站归属线路映射关系
     * @param accCode accCode码
     * @param line 列车对应线路
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addAccInLine(String accCode, String line) {
        accInLine.put(accCode,line);
    }

    /**
     * addVertex 初始化accCode节点关联关系
     * @param vertex 站点accCode
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addVertex(String vertex) {
        if (adj.get(vertex)==null) {
              adj.put(vertex, new ArrayList<>());
        }
        if (adj3.get(vertex)==null){
              adj3.put(vertex, new ArrayList<>());
        }
    }

    /**
     * getAdj 获取图模型边关系
     * @return 节点accCode关联边集合映射
     * @author wuxinran@bjjtw.gov.cn
     */
    public Map<String, List<String>> getAdj() {
        return adj;
    }

    /**
     * getAdj3 获取图模型边关系(给定到达时间最晚出发时间场景)
     * @return 节点accCode关联边集合映射
     * @author wuxinran@bjjtw.gov.cn
     */
    public Map<String, List<String>> getAdj3() {
        return adj3;
    }

    /**
     * getTimetableWeekday 获取工作日列车运行时间表 (车站accCode组合, 3列时间为记录的时间表序列)
     * @return 工作日列车运行时间表映射关系
     * @author wuxinran@bjjtw.gov.cn
     */
    public Map<String, List<String>> getTimetableWeekday() {
            return timetableWeekday;
    }

    /**
     * getTimetableWeekend 获取休息日列车运行时间表 (车站accCode组合, 3列时间为记录的时间表序列)
     * @return 休息日列车运行时间表映射关系
     * @author wuxinran@bjjtw.gov.cn
     */
    public Map<String, List<String>> getTimetableWeekend() {
            return timetableWeekend;
    }

    /**
     * getStationDistance 获取相邻站距离映射(车站accCode组合, 距离间隔)
     * @return 车站间距映射关系
     * @author wuxinran@bjjtw.gov.cn
     */
    public Map<String, Integer> getStationDistance() {
        return stationDistance;
    }

    /**
     * getTransTime 获取换乘站换乘时间——车站accCode组合, 时间间隔
     * @return 车站见换乘时间映射官谢
     * @author wuxinran@bjjtw.gov.cn
     */
    public Map<String, String> getTransTime() {
            return transTime;
    }

    /**
     * getAccInLine 获取列车行驶停站顺序表中记录
     * @return 车站accCode和线路映射集合
     * @author wuxinran@bjjtw.gov.cn
     */
    public Map<String, String> getAccInLine() { return accInLine; }

    /**
     * setIsWeekend 设置当前日期是否为周末
     * @param weekend 是否周末 true/false
     * @author wuxinran@bjjtw.gov.cn
     */
    public void setIsWeekend(boolean weekend){
          isWeekend=weekend;
    }

    /**
     * getWalkTime 获取accCode站进站时间
     * @param accCode 站点accCode
     * @return int 进站用时
     * @author wuxinran@bjjtw.gov.cn
     */
    public int getWalkTime(String accCode){
        if(walkTimeString.get(accCode)!=null) {
            return Integer.parseInt(walkTimeString.get(accCode));
        } else {
            return 0;
        }
    }

    /**
     * addWalkTime 增加accCode进站时间映射关系
     * @param accCode 站点accCode
     * @param time 进站用时
     * @author wuxinran@bjjtw.gov.cn
     */
    public void addWalkTime(String accCode, String time) {
        if (walkTimeString.get(accCode)==null) {
            walkTimeString.put(accCode, new String());
        }
        walkTimeString.put(accCode,time);
    }

}
