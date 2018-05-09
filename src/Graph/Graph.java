package Graph;


import java.io.*;
import java.util.*;

/**
 * �洢��������ͼ�����ߺ�ʱ��
 */
public final class Graph implements Cloneable,Serializable{
  // ͼ�����
  private String firstVertax;
  private String firstTime,date;
  private String endVertex;
  private Map<String,String> minTimeLink=new IdentityHashMap<>();
  private Map<String,String> transTime=new HashMap<>();
  private Map<String, String> accInLine=new HashMap<>();
  private Map<String, String> acctoName=new IdentityHashMap<>();
  private Map<String, String> WalkTimeString=new IdentityHashMap<>();
  private Stack<String> stack=new Stack<>();
  private Stack<String> stackPath=new Stack<>();
  private Map<String, List<String>> adj = new HashMap<>();
  private Map<String, List<String>> timetable_weekday = new HashMap<>();
  private Map<String, List<String>> timetable_weekend = new HashMap<>();
  private Set<String> UnVisitedVertex=new HashSet<String>();

  private LinkedList<String> reachableSt = new LinkedList<String>();
  private int inc_sec;
  
  private boolean isWeekend;
  public String UpperLimitTime="25:59:59";

  public Object deepClone() throws IOException, OptionalDataException,ClassNotFoundException {
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      ObjectOutputStream oo = new ObjectOutputStream(bo);
      oo.writeObject(this);
      ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
      ObjectInputStream oi = new ObjectInputStream(bi);
      return (oi.readObject());
  }
  
  public void InitialSearchStartVertex(String startVertex,String dateString,String time,String end_Vertex) {
	firstVertax=startVertex;//��·����ʼ�����㸳ֵ��stack��
	firstTime=time;
	endVertex=end_Vertex;
	date=dateString;
    //algorithm.perform(this, firstVertax,date,firstTime,endVertex);
  }

  /**
   * ���һ����
   */
  
  public void addsec(int sec) {
	  inc_sec = sec;
  }
  public int getsec() {
	  return inc_sec;
  }
  
  public void addEdge(String fromVertex, String toVertex) {
    if (firstVertax == null) {
      firstVertax = fromVertex;
    }

    if (adj.get(fromVertex) != null) {
        adj.get(fromVertex).add(toVertex);
    } else {
        adj.put(fromVertex, new ArrayList<String>());
        adj.get(fromVertex).add(toVertex);
    }
  }

  public void RemoveEdge(String fromVertex, String toVertex) {
      if(adj.get(fromVertex).contains(toVertex))
      {
          adj.get(fromVertex).remove((toVertex));
      }
  }



  /**
   * ���һ������
   */
  
  public void AddStack(String fromvertex,String toVertex,String time,String arrtime,String time_start)
  {
	  stack.push(fromvertex+","+toVertex+","+time+","+arrtime+","+time_start);
  }
  
  
  public void AddStackPath(String str)
  {
	  stackPath.push(str);
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
  
  public Stack<String> getPathStack()
  {
	  return stackPath;
  }
  
  public void addTransTime(String ver_start,String ver_end,String time)
  {
	  transTime.put(ver_start+ver_end,time);
  }
  
  public void Add_weeekday_timetable(String acccode,String departureTime1,String departureTime2,String arrivingTime)
  {
	  if(timetable_weekday.get(acccode)==null)
	  {
		  timetable_weekday.put(acccode, new ArrayList<String>());
	  }
	  timetable_weekday.get(acccode).add(departureTime1+","+departureTime2+","+arrivingTime);
	 
  }
  
  public void Add_UnVisitedVertex(String str)
  {
	  UnVisitedVertex.add(str);
  }
  public Set<String> getUnVisitedVertex() { return UnVisitedVertex; }

  public void Add_weeekend_timetable(String acccode,String departureTime1,String departureTime2,String arrivingTime)
  {
	  if(timetable_weekend.get(acccode)==null)
	  {
		  timetable_weekend.put(acccode, new ArrayList<String>());
	  }
	  timetable_weekend.get(acccode).add(departureTime1+","+departureTime2+","+arrivingTime);
  }
  
  public void Add_AccInLine(String accCode,String line)
  {
	  accInLine.put(accCode,line);
  }
  
  public void Add_acctoName(String accCode,String name)
  {
	  acctoName.put(accCode,name);
  }
  
  public void addVertex(String vertex) {
	if (adj.get(vertex)==null)
	{
		adj.put(vertex, new ArrayList<>());	
	}
  }
  public Map<String, List<String>> getAdj() {
    return adj;
  }
  
  public void Add_WalkTime(String acccode,String time)
  {
		if (WalkTimeString.get(acccode)==null)
		{
			WalkTimeString.put(acccode, new String());	
		}  
		WalkTimeString.put(acccode,time);
  }
  
  public Map<String, String> getMinTimeLink() {
	    return minTimeLink;
	  }
  
  public Map<String, List<String>>  getTimetable_weekday() {
	    return timetable_weekday;
	  }
  
  public Map<String, List<String>>  getTimetable_weekend() {
	    return timetable_weekend;
	  }
  
  public Map<String, String>  getTransTime() {
	    return transTime;
	  }
  
  public Map<String, String>  getAccInLine() { return accInLine; }
  
  public Map<String, String>  getAcctoName() {
	    return acctoName;
	  }
  
  public boolean getIsWeekend(){
  return isWeekend;
}

  public void setIsWeekend(boolean weekend){
	  isWeekend=weekend;
  }
  
  public int getWalkTime(String acccode){
	  if(WalkTimeString.get(acccode)!=null)
		  return Integer.parseInt(WalkTimeString.get(acccode));
	  else {
		 return 0;
	}
}
  }
  





