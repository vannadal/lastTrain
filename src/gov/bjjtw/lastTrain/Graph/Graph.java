package gov.bjjtw.lastTrain.Graph;

import gov.bjjtw.lastTrain.CommonTools.CommonTools;

import java.io.*;
import java.util.*;

public final class Graph implements Serializable{
    private String firstVertax;
    private String firstTime,date;
    private String endVertex;
    private String distance;
    private Map<String,String> minTimeLink=new HashMap<>();
    private Map<String,String> minTimeLink2=new HashMap<>();
    private Map<String,Double> minScoreLink=new HashMap<>();
    private Map<String,Integer> minDisLink=new HashMap<>();
    private Map<String,String> transTime=new HashMap<>();
    private Map<String, String> accInLine=new HashMap<>();
    private Map<String, String> walkTimeString =new HashMap<>();
    private Stack<String> stack=new Stack<>();
    private Stack<String> scoreStack=new Stack<>();
    private Stack<String> stackPath=new Stack<>();
    private Stack<String> stack2=new Stack<>();
    private Stack<String> stackPath2=new Stack<>();
    private Stack<String> stack3=new Stack<>();
    private Stack<String> stack4=new Stack<>();
    private Map<String, List<String>> adj = new HashMap<>();
    private Map<String, List<String>> adj3 = new HashMap<>();
    private Map<String, Integer> stationdistance =new HashMap<>();
    private Map<String, List<String>> timetableWeekday = new HashMap<>();
    private Map<String, List<String>> timetableWeekend = new HashMap<>();
    private Set<String> unVisitedVertex =new HashSet<String>();
    private HashMap<String, String> stationGeo = new HashMap<String, String>();
    private LinkedList<String> reachableSt = new LinkedList<String>();

    private boolean isWeekend;
    public static final String UPPER_LIMIT_TIME ="25:59:59";
    public static final int UPPER_LIMIT_DIS = 10000000;
    public static final int GEO_STRING_LENGTH = 2;

    public void addGeoPosition(String acccode, String geoposition){
        stationGeo.put(acccode,geoposition);
    }

    public Float [] getGeoPosition(String accode){
        Float[] positionfloat = new Float[2];
        positionfloat[0] = null;
        positionfloat[1] = null;
        if (stationGeo.containsKey(accode) == true) {
            String[] positionstr = stationGeo.get(accode).split(",");
            if (positionstr.length == GEO_STRING_LENGTH) {
                positionfloat[0] = Float.valueOf(positionstr[0]);
                positionfloat[1] = Float.valueOf(positionstr[1]);
            }
        }
        return positionfloat;
    }

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

    public void initialSearchStartVertex(String startVertex, String dateString, String time, String endVertex) {
        firstVertax=startVertex;
        firstTime=time;
        this.endVertex =endVertex;
        date=dateString;
    }

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

    public void removeEdge(String fromVertex, String toVertex) {
        if(adj.get(fromVertex).contains(toVertex)) {
            adj.get(fromVertex).remove((toVertex));
        }
        if(adj3.get(toVertex).contains(fromVertex)){
            adj3.get(toVertex).remove((fromVertex));
        }
    }

    public void addStack(String fromvertex, String toVertex, String time, String arrtime, String timeStart) {
        stack.push(fromvertex+","+toVertex+","+time+","+arrtime+","+timeStart);
    }

    public void addScoreStack(String fromvertex,String toVertex,String time,String arrtime,String timeStart) {
        scoreStack.push(fromvertex+","+toVertex+","+time+","+arrtime+","+timeStart);
    }

    public void addStackPath(String str) {
          stackPath.push(str);
      }
    public void addStackPath2(String str)
      {
          stackPath2.push(str);
      }
    public void addReachable(String station) {
          reachableSt.add(station);
      }

    public LinkedList<String> getReachable(){
          return reachableSt;
      }

    public Stack<String> getStack()
      {
          return stack;
      }

    public Stack<String> getScoreStack() {
        return scoreStack;
    }
    public Stack<String> getStack4() {
        return stack4;
    }

    public void setStack4(){
        stack4.clear();
        for(String s:scoreStack){
            stack4.add(s);
        }
    }

    public Stack<String> getPathStack()
      {
          return stackPath;
      }
    public Stack<String> getStack2()
    {
          return stack2;
    }

    public Stack<String> getStack3() {
        return stack3;
    }

    public void setStack3(){
        stack3.clear();
        for(String s:scoreStack){
            stack3.add(s);
        }
    }

    public Stack<String> getPathStack2() {
          return stackPath2;
    }

    public void addTransTime(String verStart, String verEnd,String time) {
        transTime.put(verStart+verEnd,time);
    }

    public void addWeekdayTimetable(String acccode, String departureTime1, String departureTime2, String arrivingTime) {
        if(timetableWeekday.get(acccode)==null) {
            timetableWeekday.put(acccode, new ArrayList<String>());
        }
        timetableWeekday.get(acccode).add(departureTime1+","+departureTime2+","+arrivingTime);
    }

    public void addStationDistance(String acccode, int distance) {
        if(stationdistance.get(acccode)==null) {
            stationdistance.put(acccode, 0);
        }
        stationdistance.put(acccode,distance);
    }

    public void addUnVisitedVertex(String str) {
          unVisitedVertex.add(str);
      }

    public void cleanMinTimeLink(){
        minTimeLink.clear();
    }

    public void cleanMinScoreLink(){
        minScoreLink.clear();
    }

    public void cleanMinTimeLink2(){
        minTimeLink2.clear();
    }

    public void cleanScoreStack(){
        scoreStack.clear();
    }

    public Map<String, Double> getMinScoreLink() {
        return minScoreLink;
    }

    public void cleanMinDisLink(){
        minDisLink.clear();
    }

    public void cleanWalkTimeString(){
        walkTimeString.clear();
    }

    public void cleanStack(){
        stack.clear();
    }

    public void cleanStackPath(){
        stackPath.clear();
    }

    public void cleanStack2(){
        stack2.clear();
    }

    public void cleanStack3(){
        stack3.clear();
    }

    public void cleanStackPath2(){
        stackPath2.clear();
    }

    public void cleanReachableSt(){
        reachableSt.clear();
    }

    public void resetParams(){
        firstTime = null;
        date = null;
        endVertex = null;
        distance = null;
    }

    public Set<String> getUnVisitedVertex() { return unVisitedVertex; }

    public void setUnVisitedVertex(Set<String> v) {
        unVisitedVertex = (Set<String>) CommonTools.DeepCopy(v);
    }

    public void addWeekendTimetable(String acccode, String departureTime1, String departureTime2, String arrivingTime) {
        if(timetableWeekend.get(acccode)==null) {
            timetableWeekend.put(acccode, new ArrayList<String>());
        }
        timetableWeekend.get(acccode).add(departureTime1+","+departureTime2+","+arrivingTime);
    }

    public void addAccInLine(String accCode, String line) {
        accInLine.put(accCode,line);
    }

    public void addVertex(String vertex) {
        if (adj.get(vertex)==null) {
              adj.put(vertex, new ArrayList<>());
        }
        if (adj3.get(vertex)==null){
              adj3.put(vertex, new ArrayList<>());
        }
    }

    public Map<String, List<String>> getAdj() {
        return adj;
    }

    public Map<String, List<String>> getAdj3() {
        return adj3;
    }

    public void addWalkTime(String acccode, String time) {
        if (walkTimeString.get(acccode)==null) {
            walkTimeString.put(acccode, new String());
        }
        walkTimeString.put(acccode,time);
    }

    public Map<String, String> getMinTimeLink() {
            return minTimeLink;
    }

    public Map<String, String> getMinTimeLink2() {
        return minTimeLink2;
    }

    public Map<String, Integer> getMinDisLink() {
        return minDisLink;
    }

    public Map<String, List<String>> getTimetableWeekday() {
            return timetableWeekday;
    }

    public Map<String, List<String>> getTimetableWeekend() {
            return timetableWeekend;
    }

    public Map<String, Integer> getStationdistance() {
        return stationdistance;
    }

    public Map<String, String> getTransTime() {
            return transTime;
    }

    public Map<String, String> getAccInLine() { return accInLine; }

    public boolean getIsWeekend(){
      return isWeekend;
    }

    public void setIsWeekend(boolean weekend){
          isWeekend=weekend;
    }

    public int getWalkTime(String acccode){
        if(walkTimeString.get(acccode)!=null) {
            return Integer.parseInt(walkTimeString.get(acccode));
        } else {
            return 0;
        }
    }

    public void addStack2(String vertex, String adjVertex, int distance2) {
        stack2.push(vertex+","+adjVertex+","+distance2);
    }

}
