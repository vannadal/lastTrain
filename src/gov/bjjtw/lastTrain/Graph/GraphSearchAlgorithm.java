package gov.bjjtw.lastTrain.Graph;

import gov.bjjtw.lastTrain.CommonTools.CommonTools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


//http://www.jb51.net/article/64443.htm 
public class GraphSearchAlgorithm {
	private Set<String> visitedVertex;
	public String date;
	public String transPath;
	public Set<String> UnVisitedVertex=new HashSet<String>();

	public boolean perform(Graph g, String sourceVertex,String dateString,String time,String end_Vertex,String stationnametoacccode) {
		if (null == visitedVertex) {
			visitedVertex = new HashSet<>();
	    }
	    date=dateString;
	    transPath = stationnametoacccode;
	    return Dijkstra(g,sourceVertex,dateString,time,end_Vertex,transPath);
	}
	  
	public boolean perform2(Graph g, String sourceVertex,String end_Vertex,String stationnametoacccode) {
		if (null == visitedVertex) {
	      visitedVertex = new HashSet<>();
	    }
	    transPath = stationnametoacccode;
		return Dijkstra2(g,sourceVertex,end_Vertex,transPath);
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
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
	  
	private boolean Dijkstra(Graph g,String sourceVertex,String dateString,String time,String end_Vertex,String transPath) {
		try {
			g.getReachable().clear();
			if(!InitialMinTimeLink(g,sourceVertex,time)) {
				return false;
			}
		  	String ver=sourceVertex;
		    visitedVertex.add(sourceVertex);

		    UnVisitedVertex=g.getUnVisitedVertex();

		    UnVisitedVertex.remove(ver);
		    String ver_Time=time,ver_before=ver;
		    
		    while(!(FindLatestVertex(g,g.getMinTimeLink()).equals("null"))) {
		    	ver=FindLatestVertex(g,g.getMinTimeLink());
		    	g.addReachable(ver);
		    	visitedVertex.add(ver);
		    	List<String> toBeVisitedVertex = g.getAdj().get(ver);

		    	for(String v:visitedVertex) {
			    	if(toBeVisitedVertex.contains(v)) {
			    		toBeVisitedVertex.remove(v);
			    	}
		    	}

		    	String latest_time=g.UpperLimitTime;
		    	String latest_Vertex="";
		    	ver_Time=g.getMinTimeLink().get(ver);
          
		    	for(String ver_end : toBeVisitedVertex) {
		    		int de_t=0;
					//departure time of started station
		    		int de_t_s=0;
		    		int arr_t=0;

		    		String alltime;
		    		String str2[];
		    		String s1=g.getAccInLine().get(ver);
		    		String s2=g.getAccInLine().get(ver_end);
		    		boolean notTransStation=s1.equals(s2);
		    		
		    		if(notTransStation) {
		    			alltime = FindLatestTime(g,ver,ver_end,ver_Time,0);
		    			str2= alltime.split(",");
						//str2[0] = departure time, str2[1]= arriving time
		    			de_t = CommonTools.TransferTime(str2[0]);
		    			de_t_s =  CommonTools.TransferTime(str2[2]);
		    			arr_t = CommonTools.TransferTime(str2[1]);
		    		} else{
						// bug: change first part to arriving time
		    			de_t= CommonTools.TransferTime(ver_Time)+Integer.parseInt(g.getTransTime().get(ver+ver_end));
		    			arr_t = de_t;
		    			de_t_s= CommonTools.TransferTime(ver_Time);
		    		}

		    		if(g.getMinTimeLink().get(ver_end)==null) {
		    			g.getMinTimeLink().put(ver_end, CommonTools.SecondToTime(de_t));
		    			g.AddStack(ver, ver_end, CommonTools.SecondToTime(de_t), CommonTools.SecondToTime(arr_t), CommonTools.SecondToTime(de_t_s));
		    		} else {
		    			int temp_t= CommonTools.TransferTime(g.getMinTimeLink().get(ver_end));
		    			if(de_t<temp_t) {
		    				g.getMinTimeLink().put(ver_end, CommonTools.SecondToTime(de_t));
		    				g.AddStack(ver,ver_end, CommonTools.SecondToTime(de_t), CommonTools.SecondToTime(arr_t), CommonTools.SecondToTime(de_t_s));
		    			}
		    		}
		    	}
		    	
		    	if(latest_time.equals(g.UpperLimitTime)==false) {
					ver_before=ver;
					ver=latest_Vertex;
					ver_Time=latest_time;
					UnVisitedVertex.remove(latest_Vertex);
				}
		    }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return true;
	  }
	  
	private boolean Dijkstra2(Graph g,String sourceVertex,String end_Vertex,String transPath) {
		try {
			if(!InitialMinDisLink(g,sourceVertex)) {
				return false;
			}
			String ver=sourceVertex;
		    visitedVertex.add(sourceVertex);
		    while(!(FindLatestVertex2(g,g.getMinDisLink()).equals("null"))) {
		    	ver=FindLatestVertex2(g,g.getMinDisLink());
		    	g.addReachable(ver);
		    	visitedVertex.add(ver);
		    	List<String> toBeVisitedVertex = g.getAdj().get(ver);/*
		    	int latest_dist=g.UpperLimitDis;
		    	String latest_Vertex="";
		    	Map<String, Integer> innermap=new HashMap<>();*/
		    	for(String v:visitedVertex) {
			    	if (toBeVisitedVertex.contains(v)) {
			    		toBeVisitedVertex.remove(v);
			    	}
		    	}
		    	int ver_dist;
		    	ver_dist=g.getMinDisLink().get(ver);          
		    	for(String ver_end : toBeVisitedVertex) {
		    	   int trans_dist=g.getStationdistance().get(ver+ver_end);
		    	   if(g.getMinDisLink().get(ver_end)==null) {
		    			g.getMinDisLink().put(ver_end,ver_dist+trans_dist);
		    			g.AddStack2(ver, ver_end,ver_dist+trans_dist);
		    		} else {
		    			int temp_t=g.getMinDisLink().get(ver_end);
		    			if(ver_dist+trans_dist<temp_t) {
		    				g.getMinDisLink().put(ver_end,ver_dist+trans_dist);
		    				g.AddStack2(ver,ver_end, ver_dist+trans_dist);
		    			}
		    		}

		    		//innermap.put(ver_end,g.getMinDisLink().get(ver_end));//新建一个innermap,将内层循环可达的所有站点距离存入
		    	}
		    	/*
		    	//找出距离最小的站点
		    	Entry<String, Integer> teMap =null;
		    	int temp =100000;
		    	for(Map.Entry<String, Integer> map:innermap.entrySet()) {	
		    		if(map.getValue()<=temp) {
		    			temp=map.getValue();
		    			teMap=map;
		    		}
		    	}
		    	latest_dist=teMap.getValue();
		    	latest_Vertex=teMap.getKey();
		    	//将距离最小的站点加入到visitedvertex中
		    	if(latest_dist!=g.UpperLimitDis)
	    		{
	    			ver_dist=latest_dist;
	    			visitedVertex.add(latest_Vertex);//unvisited没用？
	    		}*/
		    }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return true;
	}

	private String FindLatestVertex(Graph g,Map<String,String> map) {
		String vertex="null";
		String minTime=g.UpperLimitTime;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if(!visitedVertex.contains(entry.getKey())) {
				if(CommonTools.TransferTime(entry.getValue())- CommonTools.TransferTime(minTime)<0) {
					vertex = entry.getKey();
					minTime = entry.getValue();
				}
			}
		}
		if (!minTime.equals(g.UpperLimitTime)) {
			return vertex;
		}
		return "null";
	}

	private String FindLatestVertex2(Graph g,Map<String,Integer> map) {
		String vertex="null";
		int mindis=g.UpperLimitDis;
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if(!visitedVertex.contains(entry.getKey())) {
				if(entry.getValue()-mindis<0) {
					vertex=entry.getKey();
					mindis=entry.getValue();
				};
			}
		}

		//System.out.println(vertex+":"+mindis);
		if(mindis!=g.UpperLimitDis) {
			return vertex;
		}
		return "null";
	}

	private boolean InitialMinDisLink(Graph g,String vertex) {
		List<String> toBeUpdatedVertex = g.getAdj().get(vertex);
		g.getMinDisLink().clear();
		for(String adjVertex:toBeUpdatedVertex) {
			int distance=g.UpperLimitDis;
			if(g.getStationdistance().get(vertex+adjVertex)==null) {
				g.getMinDisLink().put(adjVertex,g.UpperLimitDis);
			} else {
				distance=g.getStationdistance().get(vertex+adjVertex);
			  	g.getMinDisLink().put(adjVertex,distance);
			}
			g.AddStack2(vertex, adjVertex, distance);
		}
		return true;
	}

	private boolean InitialMinTimeLink(Graph g,String vertex,String ver_time) {
		List<String> toBeUpdatedVertex = g.getAdj().get(vertex);
		g.getMinTimeLink().clear();
		for(String adjVertex:toBeUpdatedVertex) {
			if(g.getMinTimeLink().get(adjVertex)==null) {
				g.getMinTimeLink().put(adjVertex,new String());
			}
			String alltime;
			int adj_time;
			int arr_t;
			int de_t_s;
			String str[];
			if(g.getAccInLine().get(vertex).equals(g.getAccInLine().get(adjVertex))) {
				alltime = FindLatestTime(g,vertex,adjVertex,ver_time,0);
				if(alltime.equals("25:59:59")) {
					return false;
				}
				str = alltime.split(",");
				adj_time = CommonTools.TransferTime(str[0]);
				  
				de_t_s =  CommonTools.TransferTime(str[2]);
				arr_t = CommonTools.TransferTime(str[1]);
			} else {
				adj_time= (CommonTools.TransferTime(ver_time)+Integer.parseInt(g.getTransTime().get(vertex+adjVertex)));
				arr_t = adj_time;
				de_t_s= CommonTools.TransferTime(ver_time);
			}
			g.getMinTimeLink().put(adjVertex, CommonTools.SecondToTime(adj_time));
			g.AddStack(vertex, adjVertex, CommonTools.SecondToTime(adj_time), CommonTools.SecondToTime(arr_t), CommonTools.SecondToTime(de_t_s));
		}

		return true;
	}

	private String FindLatestTime(Graph g,String ver_start,String ver_end,String ver_start_Time,int isTransStation) {
		List<String> toBeVisitedTime=new ArrayList<String>();
		if(isWeekend(date)) {
			if(g.getTimetable_weekend().get(ver_start+ver_end)==null) {
				return g.UpperLimitTime;
			}
			//ȡ��v��verʱ��ver���뿪ʱ��
			toBeVisitedTime = g.getTimetable_weekend().get(ver_start+ver_end);
		} else{
			if(g.getTimetable_weekday().get(ver_start+ver_end)==null) {
				return g.UpperLimitTime;
			}
			//ȡ��v��verʱ��ver���뿪ʱ��
			toBeVisitedTime = g.getTimetable_weekday().get(ver_start+ver_end);
		}

		int minSecond=1000000;
		//departure time of end_station
		String latestTime=g.UpperLimitTime;
		//departure time of start_station
		String latestTime1=g.UpperLimitTime;
		String arrtime = g.UpperLimitTime;
		String latestArrTime = g.UpperLimitTime;
		String str[],start_time,end_time,allTime;
		  
		for(String v_time:toBeVisitedTime) {
			str=v_time.split(",");
			start_time=str[0];
			if(isTransStation==1) {
				// change route
				end_time=str[2];
			} else {
				//no change, same route
				end_time=str[1];
				arrtime = str[2];
			}

			int second= CommonTools.TransferTime(start_time) - CommonTools.TransferTime(ver_start_Time);
			//System.out.println(SecondToTime(second));

			//ע����Ա���ʱ������Ƿ����bug���Լ�v_time�п����пո��´���
			if(second>=0&&second<minSecond) {
				minSecond=second;
				//System.out.println(SecondToTime(minSecond));
				latestTime=end_time;
				latestArrTime = arrtime;
				latestTime1 = start_time;
				//System.out.println(latestTime);;
			}
		  }

          //System.out.println(ver_start+","+ver_end+","+minSecond+","+latestTime);
		  allTime = latestTime+ ","+ latestArrTime+","+ latestTime1;
		  //System.out.println(allTime);
		  return allTime;
	}

}


