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

/**
 * mainClass{@link String}查询类使用方法
 *
 * <p> 末班车可达技术应用的接口及框架
 * <a href="https://github.com/bjjtwxxzx/lastTrain">项目位置</a>
 * 更全面的项目 {@code String} 信息.
 *
 * <p> 当前类通过Java核心{@link String}和{@link StringBuilder}类，
 * 以图模型算法为基础，实现一系列末班车可达场景的应用，并提供相关功能接口。
 *
 * @author wuxinran@bjjtw.gov.cn
 */

public class mainClass {
    /** 图模型静态对象 */
    private static Graph graph;
    /** 算法未访问节点集合 */
    private static Set<String> unVisitedVertex=new HashSet<String>();
    /** 算法邻接边 */
    private static Map<String,List<String>>  adj = null;

    /**
     * 默认输入参数: 起止节点、时间和日期
     *  可查询末班车路径从此时刻开始，时间越晚加载的列车运行时刻表数据越少
     */
    private static String startVertex="";
    private static String endVertex="";
    private static String startTime="";
    private static String dateString="";
    private static String loadTimetableTime=null;

    /** 存放站点acc与中文名对应 */
    private static Map<String,String>  map = null;
    /** Epsilon常量 */
    private static final double EPSILON = 1e-15;

    /** 配置文件路径 */
    private static String dir=null;

    /** 暂缓开通的车站列表 */
    private static ArrayList<String> postponeOpeningStations = new ArrayList<String>();
    /** S1线的车站 */
    private static ArrayList<String> S1Line = new ArrayList<String>();

    /** 换乘站信息：距离/换乘时间 */
    private static String sameTransStationAdjDist=null;
    /** 相邻站距离 */
    private static String stationDistance =null;
    /** 工作日列车行驶时刻表 */
    private static String timetableWeekday =null;
    /** 休息日列车行驶时刻表 */
    private static String timetableWeekend =null;
    /** 列车行驶停站顺序表 */
    private static String acccodeInLine=null;
    /** 车站对应的经纬度 */
    private static String acccodeLatLng=null;

    /** 算法查询类型 */
    public enum Cate {
        /** 当前站出发最短时间到目标站路径（目标可达情况）；最远可达车站（目标不可达情况）*/
        REACHABLE_PATH,
        /** 当前站出发可以到达的站点 */
        REACHABLE_STATION,
        /** 给定时间前到达目标站点，在出发站点最晚出发时间是？ */
        REACHABLE_REVERSE_PATH,
        /** 当前站出发最少换乘到目标站路径（目标可达情况）；最远可达车站（目标不可达情况） */
        REACHABLE_MINTRANSFER_PATH,
        /** 无时间约束的最短路径 */
        OTHER
    }


    /**
     * fit 初始化模型 (图模型，全局变量)
     * @return void
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    public static void fit(){
        initConf("",true);
        graph = loadGraphResource(true);
        unVisitedVertex = graph.getUnVisitedVertex();
        adj = graph.getAdj();
        /** 增加暂缓开通站标记 */
        postponeOpeningStations.add("150998575");
        postponeOpeningStations.add("150998609");
        postponeOpeningStations.add("150996547");
        postponeOpeningStations.add("150996551");
        postponeOpeningStations.add("150996793");
        postponeOpeningStations.add("151019567");
        postponeOpeningStations.add("150996779");
        /** 增加S1线标记 */
        S1Line.add("150999061");
        S1Line.add("150999063");
        S1Line.add("150999065");
        S1Line.add("150999067");
        S1Line.add("150999069");
        S1Line.add("150999071");
        S1Line.add("150999073");
    }

    /**
     * resetGraph 恢复graph到初始状态.
     * @return void
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static void resetGraph(){
        graph.setAdj(adj);
        graph.setAdj3(adj);
        graph.setUnVisitedVertex(unVisitedVertex);
        graph.cleanMinDisLink();
        graph.cleanMinTimeLink();
        graph.cleanMinTimeLink2();
        graph.cleanMinScoreLink();
        graph.cleanReachableSt();
        graph.cleanWalkTimeString();
        graph.cleanStack();
        graph.cleanScoreStack();
        graph.cleanStack2();
        graph.cleanStackPath();
        graph.cleanStackPath2();
        graph.resetParams();
    }

    /**
     * initConf 通过配置文件初始化全局变量
     * @param filename 配置文件名
     * @param isResource 是否直接从资源目录获取
     * @return void
     * @exception IOException 读取文件异常
     * @exception NullPointerException 读取键值异常
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
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
            timetableWeekday = prop.getProperty("path.timetable_weekday").toString();
            timetableWeekend = prop.getProperty("path.timetable_weekend").toString();
            acccodeInLine = prop.getProperty("path.acccodeInLine").toString();
            loadTimetableTime = prop.getProperty("conf.loadTimetableTime").toString();
            dir = prop.getProperty("path.dir").toString();
            startTime = prop.getProperty("conf.startTime").toString();
            startVertex = prop.getProperty("conf.startVertex").toString();
            endVertex= prop.getProperty("conf.endVertex").toString();
            dateString= prop.getProperty("conf.dateString").toString();
            stationDistance = prop.getProperty("path.stationdistance");
            acccodeLatLng = prop.getProperty("path.acccodeLatlng").toString();
        } catch (IOException ex) {
            System.out.println("读取配置文件" + filename + "失败。");
            ex.printStackTrace();
        } catch (NullPointerException ex){
            System.out.println("读取配置key失败。");
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

    /**
     * specialStation 修改特殊构造站台图模型： 替换岛式站台到侧式站台, 上下行换乘步行时间存在差异
     * @param emptyGraph 图模型对象
     * @return void
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
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

        emptyGraph.getStationDistance().put("151019045_151019043151019043",emptyGraph.getStationDistance().get("151019045151019043"));
        emptyGraph.getStationDistance().put("151019045_151019047151019047",emptyGraph.getStationDistance().get("151019045151019047"));
        emptyGraph.getStationDistance().put("151019043151019045_151019043",emptyGraph.getStationDistance().get("151019043151019045"));
        emptyGraph.getStationDistance().put("151019047151019045_151019047",emptyGraph.getStationDistance().get("151019047151019045"));

        emptyGraph.getStationDistance().put("150996997151019045_151019047",emptyGraph.getStationDistance().get("150996997151019045"));
        emptyGraph.getStationDistance().put("150996997151019045_151019043",emptyGraph.getStationDistance().get("150996997151019045"));
        emptyGraph.getStationDistance().put("151019045_151019043150996997",emptyGraph.getStationDistance().get("151019045150996997"));
        emptyGraph.getStationDistance().put("151019045_151019047150996997",emptyGraph.getStationDistance().get("151019045150996997"));

        emptyGraph.getStationDistance().remove("151019045150996997");
        emptyGraph.getStationDistance().remove("150996997151019045");

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

    /**
     * loadGraphResource 加载图模型资源
     * @param isResource 是否直接从资源目录获取
     * @return Graph 加载资源后的图模型对象
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static Graph loadGraphResource(boolean isResource) {
        Graph emptyGraph = new Graph();
        int startTimeToSec= CommonTools.transferTime(loadTimetableTime);
        //将所有站点以及与其邻接的边添加到图中（不包括同站换乘点）
        addAllVertexAndEdge( emptyGraph,acccodeInLine,isResource);
        //同一站点的不同编码acc添加到图中
        addSameTransferVertexAndEdge( emptyGraph,sameTransStationAdjDist,isResource);
        //addStationWalkingTime( emptyGraph,entertime,isResource);
        readTimeTable( emptyGraph, stationDistance, timetableWeekday, timetableWeekend,acccodeInLine,startTimeToSec,isResource);
        initialUnVisitedVertexList( emptyGraph,acccodeLatLng,isResource);
        loadStationGeoPosition( emptyGraph,acccodeLatLng,isResource);
        map = loadAcc(acccodeLatLng,isResource);
        specialStation( emptyGraph);
        return  emptyGraph;
    }

    /**
     * loadAcc 从资源初始化AccCode和中文站名映射关系
     * @param stationLatLngFilename 车站经纬度位置文件位置
     * @param isResource 是否直接从资源目录获取
     * @return Map<String, String> 加载过的AccCode和中文站名映射关系
     * @exception Exception 文件操作异常
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static Map<String , String> loadAcc(String stationLatLngFilename, boolean isResource) {
        Map<String,String> map = new HashMap<String,String>();
        try {
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(dir+stationLatLngFilename)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(stationLatLngFilename);
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
            e.printStackTrace();
        }
        return map;
    }

    /**
     * addStationWalkingTime 增加图模型中加载进站步行时间
     * @param g 图模型对象
     * @param entertimeFilename 进站步行时间文件位置
     * @param isResource 是否直接从资源目录获取
     * @return void
     * @exception Exception 文件操作异常
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static void addStationWalkingTime(Graph g, String entertimeFilename, boolean isResource) {
        try {
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(dir+entertimeFilename)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(entertimeFilename);
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
            e.printStackTrace();
        }
    }

    /**
     * initialUnVisitedVertexList 初始化未访问车站列表
     * @param g 图模型对象
     * @param stationLatLngFilename 车站位置资源文件位置
     * @param isResource 是否直接从资源目录获取
     * @return void
     * @exception Exception 文件操作异常
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static void initialUnVisitedVertexList(Graph g, String stationLatLngFilename, boolean isResource) {
        try {
            String temp="";
            String [] str;
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(dir+stationLatLngFilename)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(stationLatLngFilename);
                br = new BufferedReader(new InputStreamReader(is));
            }
            while((temp=br.readLine())!=null) {
                str=temp.split(",");
                g.addUnVisitedVertex(str[2]);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * addAllVertexAndEdge 建立站点之间的关联关系(不包含换乘关联)
     * @param g 图模型对象
     * @param accCodeInLineFilename 列车行驶停站顺序列表文件位置
     * @param isResource 是否直接从资源目录获取
     * @return void
     * @exception Exception 文件操作异常
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static void addAllVertexAndEdge(Graph g, String accCodeInLineFilename, boolean isResource) {
        String temp1 = "", line = "", acccode = "";
        String [] str1;
        BufferedReader br = null;
        try {
            if (isResource == false) {
                br = new BufferedReader(new FileReader(new File(dir + accCodeInLineFilename)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(accCodeInLineFilename);
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
            e.printStackTrace();
        }
    }

    /**
     * loadStationGeoPosition 加载车站位置信息
     * @param g 图模型对象
     * @param acccodeLatLngFilename accCode对应经纬度文件位置
     * @param isResource 是否直接从资源目录获取
     * @return void
     * @exception IOException 文件操作异常
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static void loadStationGeoPosition(Graph g, String acccodeLatLngFilename, Boolean isResource) {
        String temp1 = null;
        String [] str1 = null;
        try {
            BufferedReader br = null;
            if (isResource == false) {
                br=new BufferedReader(new FileReader(new File(dir+acccodeLatLngFilename)));
            } else {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(acccodeLatLngFilename);
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

    /**
     * readTimeTable 按照配置加载列车行驶时间表、站间距离以及关联关系更新图模型对象。
     * @param g 图模型对象
     * @param stationDistanceFilename 两站之间距离文件位置
     * @param timetableWeekdayFilename 工作日列车行驶时间表文件位置
     * @param timetableWeekendFilename 休息日列车行驶时间表文件位置
     * @param acccodeInLineFilename 列车行驶停站顺序列表文件位置
     * @param startTimeToSec 配置中的系统起始时间
     * @param isResource 是否直接从资源目录获取
     * @return void
     * @exception Exception 文件操作异常
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static void readTimeTable(Graph g, String stationDistanceFilename, String timetableWeekdayFilename, String timetableWeekendFilename, String acccodeInLineFilename, int startTimeToSec, boolean isResource) {
        try {
            String [] str = null;
            String temp = "";
            BufferedReader brWeekday = null;
            BufferedReader brWeekend = null;
            BufferedReader brAccLine = null;
            BufferedReader brDist = null;
            if (isResource == false) {
                brWeekday = new BufferedReader(new FileReader(new File(dir + timetableWeekdayFilename)));
                brDist = new BufferedReader(new FileReader(new File(dir + stationDistanceFilename)));
                brWeekend = new BufferedReader(new FileReader(new File(dir + timetableWeekendFilename)));
                brAccLine = new BufferedReader(new FileReader(new File(dir + acccodeInLineFilename)));
            } else {
                InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(timetableWeekdayFilename);
                brWeekday = new BufferedReader(new InputStreamReader(is1));
                InputStream is2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(timetableWeekendFilename);
                brWeekend = new BufferedReader(new InputStreamReader(is2));
                InputStream is3 = Thread.currentThread().getContextClassLoader().getResourceAsStream(acccodeInLineFilename);
                brAccLine = new BufferedReader(new InputStreamReader(is3));
                InputStream is5 = Thread.currentThread().getContextClassLoader().getResourceAsStream(stationDistanceFilename);
                brDist = new BufferedReader(new InputStreamReader(is5));
            }

            while ((temp = brWeekday.readLine()) != null) {
                str = temp.split(",");
                //按终点站或起始站有无机场线，间数据加载入两个map中
                String acccode1 = str[0] + str[1];
                boolean judge1 =  "151020057".equals(str[0]) || "151020059".equals(str[0]);
                boolean judge2 =  "151020055".equals(str[1]) || "151020057".equals(str[1]) || "151020059".equals(str[1]);
                //每次加载列车运行时刻表时，只加载计算时间之后的数据
                if (startTimeToSec <= CommonTools.transferTime(str[2])) {
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
                if (startTimeToSec <= CommonTools.transferTime(str[2])) {
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
            e.printStackTrace();
        }
    }

    /**
     * addSameTransferVertexAndEdge 图模型中加载并构建换乘站间关联关系
     * @param g 图模型对象
     * @param sameTransStationAdjDist 换乘站信息：距离/换乘时间文件位置
     * @param isResource 是否直接从资源目录获取
     * @return void
     * @exception Exception 文件操作异常
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
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
            e.printStackTrace();
        }
    }

    /**
     * computeReachablePath 在给定条件下，在结果堆栈中处理路径时间表结果
     * @param startvertex 起始节点ACC码
     * @param endvertex 终止节点ACC码
     * @param isReverse 是否从终点逆向查询起始点最晚出发时间
     * @param type 1(获取无约束寻路结果) / 3(获取有目标的寻路结果) / 其他返回空
     * @return LinkedList<String> 查询结果列表
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static LinkedList<String> computeReachablePath(String startvertex, String endvertex, Boolean isReverse, int type) {
        String ver=endvertex;
        String content="";
        Boolean hasValidStation = false;
        LinkedList<String> path = new LinkedList<String>();
        Stack<String> stack = null;

        if (type == 1) {
            stack = graph.getStack();
        } else if (type == 3){
            stack = graph.getStack();
            for (String item : graph.getScoreStack()){
                stack.push(item);
            }
        } else {
            return path;
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
        if (hasValidStation == false) {
            path.clear();
        }
        return path;
    }

    /**
     * getReachablePath
     * <p> 输出对应的最早到达路径</br>
     * 输出在某时刻（datestring+starttime）从startvert站点出发,到endvertex站点的路径，包括车站ACC码,发车时间(最后一个可达车站，时间为到达时间)，是否可达标识（0,1表示），返回LinkedList。
     * @param dateStr 日期字符串
     * @param startTimeStr 时间字符串
     * @param startVertex 起始节点ACC码
     * @param endVertex 终止节点ACC码
     * @param isMinTransfer 是否按最少换乘优先寻路(否则按最早到达优先)
     * @return LinkedList<String> 查询结果对应方案明细列表
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    public static LinkedList<String> getReachablePath(String dateStr, String startTimeStr, String startVertex, String endVertex,boolean isMinTransfer) {
        String [] tmp;
        LinkedList<String> result ;

        if (isMinTransfer){
            result = getReachable(dateStr,startTimeStr,startVertex,endVertex,Cate.REACHABLE_MINTRANSFER_PATH);
        }else{
            result = getReachable(dateStr,startTimeStr,startVertex,endVertex,Cate.REACHABLE_PATH);
        }

        LinkedList<String> output = new LinkedList<String>();
        //记录输出路径包含的站点acc
        LinkedList<String> simple = new LinkedList<String>();

        if (result.size()>0) {
            for (String item : result) {
                tmp = item.split(",");
                String item1 = tmp[0];
                if (item1.contains("_")){
                    item1 = item1.split("_")[0];
                }
                if (postponeOpeningStations.contains(item1)) {
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

            if (postponeOpeningStations.contains(tmp[1])) {
                output.add(item1 + "," + tmp[3] + ",2");
            }else {
                output.add(item1 + "," + tmp[3] + ",1");
            }
            startVertex = item1;
            simple.add(item1);
        } else {
            output.add(startVertex+","+startTimeStr+",1");
            simple.add(startVertex);
        }

        //不可达的情况
        if (startVertex.equals(endVertex) == false){
            resetGraph();
            LinkedList<String> unreachableResult = graphTraversal(startVertex, endVertex, startTimeStr, dateStr, false, false, Cate.OTHER);
            for (String item: unreachableResult){
                String accname = item.split(",")[1];
                if (accname.contains("_")){
                    accname = accname.split("_")[0];
                }
                if (!simple.contains(accname)) {
                    output.add(accname+",,0");
                }
            }
        }
        return output;
    }


    /**
     * getReachableStation 输出在某时刻(日期+时间) 从startvert站点出发,所有可以到达的车站ACC码
     * @param dateStr 日期字符串
     * @param startTimeStr 时间字符串
     * @param startVertex 起始节点
     * @return LinkedList<String> 可达车站ACC码集合
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    public static LinkedList<String> getReachableStation(String dateStr, String startTimeStr, String startVertex){
        return getReachable(dateStr,startTimeStr,startVertex,"",Cate.REACHABLE_STATION);
    }

     /**
     * getReachableStationLatestPath 输出给定参数下到达终点，起点的最晚出发时间对应方案。
     * @param dateStr 日期字符串
     * @param endTimeStr 到达终止节点,时间字符串
     * @param startVertex 起始节点ACC码
     * @param endVertex 终止节点ACC码
     * @return LinkedList<String> 查询结果对应方案明细列表
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    public static LinkedList<String> getReachableStationLatestPath(String dateStr, String endTimeStr, String startVertex, String endVertex){
        LinkedList<String> result = getReachable(dateStr,endTimeStr,startVertex,endVertex,Cate.REACHABLE_REVERSE_PATH);
        if (result != null){
            Collections.reverse(result);
        } else {
            result = new LinkedList<String>();
        }
        return result;
    }

    /**
     * getGeoDistanceBetweenStations
     * @param stationACC1 站点1的ACC码
     * @param stationACC2 站点2的ACC码
     * @return double 距离(米)
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static double getGeoDistanceBetweenStations(String stationACC1, String stationACC2){
        Float [] lastStationPosition = graph.getGeoPosition(stationACC1);
        Float [] destStationPosition = graph.getGeoPosition(stationACC2);
        if (lastStationPosition[0] - 0.0 > EPSILON && lastStationPosition[1] - 0.0 > EPSILON && destStationPosition[0] - 0.0 > EPSILON && destStationPosition[1] - 0.0 > EPSILON){
            return CommonTools.simpleDist(lastStationPosition[1],lastStationPosition[0],destStationPosition[1],destStationPosition[0]);
        }
        return -1.0;
    }

    /**
     * reachablePath 获取相应的最优路径(区分少换乘/优先到达)
     * @param startVertex 起始节点ACC码
     * @param endVertex 终止节点ACC码
     * @param startTimeStr 起始时间字符串
     * @param dateStr 日期字符串
     * @param isLessTrans 是否按最少换乘优先寻路(否则按最早到达优先)
     * @param type REACHABLE_MINTRANSFER_PATH最少换乘优先，REACHABLE_PATH最早到达优先
     * @return LinkedList<String> 查询结果对应方案明细列表
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static LinkedList<String> reachablePath(String startVertex, String endVertex, String startTimeStr, String dateStr, Boolean isLessTrans,Cate type){
        LinkedList<String> reachableStation = graphTraversal(startVertex, endVertex, startTimeStr, dateStr,false,isLessTrans,type);
        if(reachableStation !=null && reachableStation.size()>0){
            return reachableStation;
        } else {
            String minStation = startVertex;
            double minDist = getGeoDistanceBetweenStations(startVertex,endVertex);
            for(String line: graph.getReachable()){
                String [] items = line.split(",");
                double currentDistance = getGeoDistanceBetweenStations(items[0],endVertex);
                if (currentDistance - 0.0000001 > EPSILON && minDist - currentDistance > EPSILON) {
                    minStation = items[0];
                    minDist = currentDistance;
                }
            }
            if (minStation.equals(startVertex) == false){
                return computeReachablePath(startVertex,minStation,false,3);
            } else {
                return reachableStation;
            }
        }
    }

    /**
     * getReachable 对不同最优路径计算的分类处理
     * @param dateStr 日期字符串
     * @param startTimeStr 起始时间字符串
     * @param startVertex 起始节点ACC码
     * @param endVertex 终止节点ACC码
     * @param type 获取路径类型
     * @return 查询结果对应方案明细列表
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static LinkedList<String> getReachable(String dateStr, String startTimeStr, String startVertex, String endVertex, Cate type) {
        resetGraph();
        graph.setIsWeekend(CommonTools.isWeekend(dateStr));
        //0是00:00:00的秒数，18000是05:00:00的秒数
        LinkedList<String> reachableStation = new LinkedList<>();
        switch (type) {
            case REACHABLE_STATION:
                reachableStation = graphTraversal(startVertex, endVertex, startTimeStr, dateStr, false, false, type);
                LinkedList<String> stations = graph.getReachable();
                if (reachableStation != null ) {
                    if (!S1Line.contains(startVertex) && graph.getMinTimeLink().get("151018273")!=null) {
                        int jinanqiaoArrTime= CommonTools.transferTime(graph.getMinTimeLink().get("151018273"));
                        //比较到达金安桥的时间是否小于金安桥末班车的时间
                        int timeDifference = jinanqiaoArrTime - CommonTools.transferTime("21:07:00");
                        if (timeDifference>0) {
                            stations.remove("151018273");
                            return stations;
                        }
                        return stations;
                    }else if(S1Line.contains(startVertex) && graph.getMinTimeLink().get("150995203")!=null){
                        int jinanqiaoArrTime = CommonTools.transferTime(graph.getMinTimeLink().get("150995203"));
                        //比较到达苹果园的时间是否小于苹果园末班车的时间
                        int timeDifference = jinanqiaoArrTime - CommonTools.transferTime("23:30:00");
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
                reachableStation = graphTraversal(startVertex, endVertex, startTimeStr, dateStr,true, false, type);
                if (reachableStation != null && reachableStation.size()>0) {
                    return reachableStation;
                } else {
                    return null;
                }
            case REACHABLE_MINTRANSFER_PATH:
                return reachablePath(startVertex, endVertex, startTimeStr, dateStr, true, type);
            case REACHABLE_PATH:
                return reachablePath(startVertex, endVertex, startTimeStr, dateStr, false, type);
            default:
                return null;
        }
    }

    /**
     * getAccCodeSet 获取全部ACC码的集合
     * @return ACC码集合
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    public static Set<String> getAccCodeSet(){
        return graph.getAccInLine().keySet();
    }

    /**
     * getAccCodeMap 获取ACC码到文本的映射集
     * @return ACC码映射关系
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     *
     */
    public static Map<String, String> getAccCodeMap(){
        return map;
    }

    /**
     * graphTraversal 根据查询路径类型，在图模型上应用对应的寻路算法
     * @param startVertex 起始节点ACC码
     * @param endVertex 终止节点ACC码
     * @param startTimeStr 起始时间字符串
     * @param dateStr 日期字符串
     * @param isReverse 是否从终点逆向查询起始点最晚出发时间
     * @param isLessTrans 是否按最少换乘优先寻路(否则按最早到达优先)
     * @param type 获取路径类型
     * @return 查询结果对应方案明细列表
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static LinkedList<String> graphTraversal(String startVertex, String endVertex, String startTimeStr, String dateStr, Boolean isReverse, Boolean isLessTrans, Cate type) {
        graph.initialSearchStartVertex(startVertex,dateStr,startTimeStr,endVertex);
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

        if (graphSearchAlgorithm.perform(graph, startVertex, dateStr, startTimeStr, endVertex, isReverse,isLessTrans,retId)) {
            if (retId > 0) {
                return computeReachablePath(startVertex, endVertex, isReverse, retId);
            } else {
                return getShortPath(endVertex);
            }
        } else {
            return null;
        }
    }

    /**
     * getShortPath 获取图模型数据结构中起点到指定终点的最短路径
     * @param endVertex 目的地ACC码
     * @return 无约束最短路径
     * @author wuxinran@bjjtw.gov.cn
     * @date 2018/7/17
     */
    private static LinkedList<String> getShortPath(String endVertex) {
        String ver=endVertex;
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