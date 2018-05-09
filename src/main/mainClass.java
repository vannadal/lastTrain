package main;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import Graph.Graph;
import Graph.GraphSearchAlgorithm;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.stream.Collectors;

public class mainClass {
    private static Graph graph;
    private static String startVertex="";
    private static String endVertex="";
    private static String startTime="";
    private static String dateString="";
    private static String dir=null;
    private static String sameTransStationAdj=null;
    private static String timetable_weekday=null;
    private static String timetable_weekend=null;
    private static String acccodeInLine=null;
    private static String stationnametoacccode=null;
    private static String entertime=null;
    private static String loadTimetableTime=null;
    //可查询末班车路径从此时刻开始，时间越晚g加载的列车运行时刻表数据越少

	public static void main(String[] args) throws IOException {
		initConf("./src/conf/conf.properties");
        Graph g = fit(false);

        String temp="",line[]=null;

		BufferedReader br=new BufferedReader(new FileReader(acccodeInLine));
        br.readLine();

        ArrayList<Long> testing = new ArrayList<Long>();

        //本循环为测试车站代码
		while((temp=br.readLine())!=null)
		{
		    try{
		        graph = (Graph)g.deepClone();
		    } catch (ClassNotFoundException e){
		        e.printStackTrace();
            }
			line=temp.split(",");
			if (line[2].equals("151020059")||line[2].equals("151020057")) {
                continue;
            }
			long time=System.currentTimeMillis();
			LinkedList<String> reachableStations=GetReachableStation(dateString,startTime,line[2]);
            long consume = System.currentTimeMillis()-time;

			if(reachableStations==null)
				System.out.println(line[2]+","+(consume)+"ms,"+0);
			else
				System.out.println(line[2]+","+(consume)+"ms,"+reachableStations.size());

			testing.add(consume);
			graph.getReachable().clear();
			graph.getUnVisitedVertex().clear();
		}
		br.close();

		System.out.println("0ms - 100ms:"+testing.stream().filter(x -> x <= 100).count());
        System.out.println("101ms - 150ms:"+testing.stream().filter(x-> x>100 && x<= 150).count());
        System.out.println("151ms - 200ms:"+testing.stream().filter(x-> x>150 && x<=200).count());
        System.out.println("201ms - 250ms:"+testing.stream().filter(x-> x>200 && x<=250).count());
        System.out.println("251ms - 350ms:"+testing.stream().filter(x-> x>250 && x<=350).count());
        System.out.println("351ms - :"+testing.stream().filter(x-> x>350).count());
        System.out.println("avg:"+ testing.stream().collect(Collectors.averagingInt(x -> x.intValue() ))  );


	}

    public static void initConf(String filename) {
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(filename);
            Properties prop = new Properties();
            prop.load(inStream);
            sameTransStationAdj = prop.getProperty("path.sameTransStationAdj",null).toString();
            timetable_weekday = prop.getProperty("path.timetable_weekday",null).toString();
            timetable_weekend = prop.getProperty("path.timetable_weekend",null).toString();
            acccodeInLine = prop.getProperty("path.acccodeInLine",null).toString();
            stationnametoacccode = prop.getProperty("path.stationnametoacccode",null).toString();
            entertime = prop.getProperty("path.entertime",null).toString();
            loadTimetableTime = prop.getProperty("conf.loadTimetableTime",null).toString();
            dir = prop.getProperty("path.dir",null).toString();
            startTime = prop.getProperty("conf.startTime",null).toString();
            startVertex = prop.getProperty("conf.startVertex",null).toString();
            endVertex= prop.getProperty("conf.endVertex",null).toString();
            dateString= prop.getProperty("conf.dateString",null).toString();
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
    public static int TransferTime(String time)
    {
        String str[]=time.split(":");
        int h=Integer.parseInt(str[0]);
        if (h==0) {
            h = 24;
        }

        int m=Integer.parseInt(str[1]);
        int s=Integer.parseInt(str[2]);
        return h*3600+m*60+s;
    }

    public static Graph fit(boolean isResource) {
        Graph empty_graph = new Graph();
        int startTimeToSec= TransferTime(loadTimetableTime);
        AddAllVertexAndEdge(empty_graph,acccodeInLine,isResource);//将所有站点以及与其邻接的边添加到图中（不包括同站换乘点）
        AddSameTransferVertexAndEdge(empty_graph,sameTransStationAdj,isResource);//同一站点的不同编码acc添加到图中
        AddStationWalkingTime(empty_graph,entertime,isResource);
        ReadTimeTable(empty_graph,timetable_weekday,timetable_weekend,acccodeInLine,stationnametoacccode,startTimeToSec,isResource);
        InitialTransVertexList(empty_graph,stationnametoacccode,isResource);
        return empty_graph;
	}



    private static void AddStationWalkingTime(Graph g,String entertime,boolean isResource)
    {
        try {
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(dir+entertime)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(entertime);
                br = new BufferedReader(new InputStreamReader(is));
            }

            br.readLine();

            String str[],temp="";
            while((temp=br.readLine())!=null)
            {
                str=temp.split(",");
                if(str[5].equals("进站")&&str[6].equals("全天")&&str[7].equals("工作日"))
                {
                    g.Add_WalkTime(str[0],str[4]);
                }
            }
            br.close();
        }

        catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private static void InitialTransVertexList(Graph g,String transPath,boolean isResource)
    {
        try {
            String temp="",str[];
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(dir+transPath)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(transPath);
                br = new BufferedReader(new InputStreamReader(is));
            }
            while((temp=br.readLine())!=null)
            {
                str=temp.split(",");
                g.Add_UnVisitedVertex(str[2]);
            }
            br.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private static void AddAllVertexAndEdge(Graph g, String filePath) {
        String temp1 = "", str1[], line = "", acccode = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(dir + filePath)));
            temp1 = br.readLine();
            str1 = temp1.split(",");
            line = str1[1];
            acccode = str1[2];
            g.addVertex(str1[2]);

            while ((temp1 = br.readLine()) != null) {

                str1 = temp1.split(",");
                g.addVertex(str1[2]);

                switch (str1[2]) {
                    case "151020053":
                        g.addEdge("151020053", "151020055");
                        g.addEdge("151020055", "151020053");
                        break;
                    case "151020055":
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
//			            System.out.println("default");break;
                }


//				if(str1[2].equals("151020057"))
//				{
////					System.out.println(str1);
//					g.addEdge("151020057","151020055");
//				}
//				if(str1[2].equals("150995474"))//2号线和10号线首末站需要考虑环路
//				{
//					g.addEdge("150995474","150995473");
//					g.addEdge("150995473", "150995474");
//				}
//				if(str1[2].equals("151018007"))
//				{
//					g.addEdge("151018007","151018009");
//					g.addEdge("151018009", "151018007");
//				}


                if (str1[1].equals(line)) {
                    g.addEdge(acccode, str1[2]);
                    g.addEdge(str1[2], acccode);
                }
                line = str1[1];
                acccode = str1[2];
            }
            //去除机场线的单行问题
            g.RemoveEdge("151020059", "151020055");
            g.RemoveEdge("151020057", "151020059");
            g.RemoveEdge("151020055", "151020057");

            br.close();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    private static void ReadTimeTable(Graph g,String timetable_weekday,String timetable_weekend,String acccodeInLine,String stationnametoacccode,int startTimeToSec,boolean isResource) {
        try {
            String str[] = null, temp = "";

            BufferedReader br_weekday = null;
            BufferedReader br_weekend = null;
            BufferedReader br_acc_line = null;
            BufferedReader br_acc_name = null;
            if (isResource == false) {
                br_weekday = new BufferedReader(new FileReader(new File(dir + timetable_weekday)));
                br_weekend = new BufferedReader(new FileReader(new File(dir + timetable_weekend)));
                br_acc_line = new BufferedReader(new FileReader(new File(dir + acccodeInLine)));
                br_acc_name = new BufferedReader(new FileReader(new File(dir + stationnametoacccode)));
            } else {
                InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(timetable_weekday);
                br_weekday = new BufferedReader(new InputStreamReader(is1));
                InputStream is2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(timetable_weekend);
                br_weekend = new BufferedReader(new InputStreamReader(is2));
                InputStream is3 = Thread.currentThread().getContextClassLoader().getResourceAsStream(acccodeInLine);
                br_acc_line = new BufferedReader(new InputStreamReader(is3));
                InputStream is4 = Thread.currentThread().getContextClassLoader().getResourceAsStream(stationnametoacccode);
                br_acc_name = new BufferedReader(new InputStreamReader(is4));

            }

            while ((temp = br_weekday.readLine()) != null) {
                str = temp.split(",");
                //System.out.println(str[5]);
                String acccode1 = str[0] + str[1];
                if (startTimeToSec <= TransferTime(str[2]))//每次加载列车运行时刻表时，只加载计算时间之后的数据
                {
                    g.Add_weeekday_timetable(acccode1, str[2], str[3], str[5]);
                }
            }
            br_weekday.close();

            while ((temp = br_weekend.readLine()) != null) {
                str = temp.split(",");
                String acccode1 = str[0] + str[1];
                if (startTimeToSec <= TransferTime(str[2]))//每次加载列车运行时刻表时，只加载计算时间之后的数据
                {
                    g.Add_weeekend_timetable(acccode1, str[2], str[3], str[5]);
                }
            }
            br_weekend.close();
            while ((temp = br_acc_line.readLine()) != null) {
                str = temp.split(",");
                g.Add_AccInLine(str[2], str[1]);
            }
            br_acc_line.close();

            while ((temp = br_acc_name.readLine()) != null) {
                str = temp.split(",");
                g.Add_acctoName(str[2], str[1]);
            }
            br_acc_name.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    private static void AddSameTransferVertexAndEdge(Graph g,String sameTransStationAdj,boolean isResource)
    {
        String temp1="",str1[];
        try {
            BufferedReader br = null;
            if (isResource == false) {
                br = new BufferedReader(new FileReader(new File(dir+sameTransStationAdj)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(sameTransStationAdj);
                br = new BufferedReader(new InputStreamReader(is));
            }

            while((temp1=br.readLine())!=null)
            {
                str1=temp1.split(",");
                if(str1[0]!=null)
                {
                    g.addVertex(str1[0]);
                    g.addEdge(str1[0], str1[1]);//
                    g.addTransTime(str1[0],str1[1],str1[2]);
                }
            }
            br.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }
    }

    private static void AddAllVertexAndEdge(Graph g, String filePath,boolean isResource)
    {
        String temp1="",str1[],line="",acccode="";
        try
        {
            BufferedReader br = null;
            if (isResource == false) {
                br = new BufferedReader(new FileReader(new File(dir + filePath)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
                br = new BufferedReader(new InputStreamReader(is));
            }

            temp1=br.readLine();
            str1=temp1.split(",");
            line=str1[1];
            acccode=str1[2];
            g.addVertex(str1[2]);

            while((temp1=br.readLine())!=null)
            {
                str1=temp1.split(",");
                g.addVertex(str1[2]);
                if(str1[1].equals("150995474"))//2号线和10号线首末站需要考虑环路
                {
                    g.addEdge("150995474","150995473");
                    g.addEdge("150995473", "150995474");
                }
                if(str1[1].equals("151018007"))
                {
                    g.addEdge("151018007","151018009");
                    g.addEdge("151018009", "151018007");
                }
                if(str1[1].equals(line))
                {
                    g.addEdge(acccode,str1[2]);
                    g.addEdge(str1[2], acccode);
                }
                line=str1[1];
                acccode=str1[2];
            }
            br.close();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public static LinkedList<String> GetReachablePath(String startvertex,String dateString,String time,String endvertex)
    {
        String ver=endvertex;
        String content="";
        LinkedList<String> path = new LinkedList<String>();
        while(!graph.getStack().empty())
        {
            content=graph.getStack().pop();
            String str[]=content.split(",");
            if(str[1].equals(ver))
            {
                graph.AddStackPath(content);
                ver=str[0];
            }
        }

        try {
            String context = "";
            while(!graph.getPathStack().empty())
            {
                content=graph.getPathStack().pop();
                String str[]=content.split(",");
                context = str[0]+","+str[1]+","+str[4]+","+str[3]+","+str[2];
                path.add(context);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }


    private static LinkedList<String> ReachablePerStation(String dateString, String startTime ,String startVertex)   {
        return graph.getReachable();
    }

    private static LinkedList<String> GraphTraversal(String startVertex, String endVertex, String startTime, String dateString,String stationnametoacccode) {

        graph.InitialSearchStartVertex(startVertex,dateString,startTime,endVertex);
        GraphSearchAlgorithm graphSearchAlgorithm =new GraphSearchAlgorithm();
        if(graphSearchAlgorithm.perform(graph, startVertex, dateString, startTime, endVertex,stationnametoacccode))
        {
            return GetReachablePath(startVertex,dateString,startTime,endVertex); //将可达路径返回至sh输出
        }
        else
            return null;


    }

    public static void setGraph(Graph g){
        try{
            graph = (Graph)g.deepClone();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static Graph getGraph(){
        return graph;
    }

    public static LinkedList<String> GetReachableStation(String datestring, String starttime, String startvertex) {

        if ((TransferTime(starttime) > 0) && (TransferTime(starttime) < 18000))//0是00:00:00的秒数，18000是05:00:00的秒数
            return new LinkedList<>();
        else {
            startTime = starttime;
            startVertex = startvertex;
            dateString = datestring;
            LinkedList<String> reachableStation = GraphTraversal(startVertex, endVertex, startTime, dateString, stationnametoacccode);
            if (reachableStation != null) {
                return ReachablePerStation(dateString, startTime, startVertex);
            } else return null;
        }
    }

    }
