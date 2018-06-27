package gov.bjjtw.lastTrain.Preprocess;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import gov.bjjtw.lastTrain.CommonTools.CommonTools;


import main.mainClass;

public class DivideTimeTable {

	public void ReadTimeTable(String filename)//将列车时刻表按照工作日/周末以及上行/下行拆分成四个表格
	{
		try {
			sortTable(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"GBK"));
			BufferedWriter bw_weekday=new BufferedWriter(new FileWriter(new File("D:\\weekday_timetable_allstation_0621fixed.csv"),true));
			BufferedWriter bw_weekend=new BufferedWriter(new FileWriter(new File("D:\\weekend_timetable_allstation__0621fixed.csv"),true));
			br.readLine();
			String temp,start_acc,start_departuretime,end_acc,end_departuretime,str[],tripNumber,week,end_arrivingtime;
			str=br.readLine().split(",");
			start_acc=str[0];
			start_departuretime=str[1];
			week=str[2];
			tripNumber=str[5];
			while((temp=br.readLine())!=null){
				str=temp.split(",");
				if(tripNumber.equals(str[5]))
				{
					end_acc=str[0];
					end_departuretime=str[1];
					end_arrivingtime=str[6];
					week=str[2];
					if(week.equals("双休"))
					{//最后一列是下一站的到达时间
						bw_weekend.write(start_acc+","+end_acc+","+start_departuretime+","+end_departuretime+","+tripNumber+","+end_arrivingtime+"\n");
					}
					else{
						bw_weekday.write(start_acc+","+end_acc+","+start_departuretime+","+end_departuretime+","+tripNumber+","+end_arrivingtime+"\n");
					}
					start_acc=end_acc;
					start_departuretime=end_departuretime;
				}
				else{
					start_acc=str[0];
					start_departuretime=str[1];
					tripNumber=str[5];
					week=str[2];
				}
			}
			br.close();
			bw_weekday.close();
			bw_weekend.close();
		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
	}

	private void sortTable(String filename) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"GBK"));
		BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"GBK"));
		BufferedWriter weekdayr=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"GBK"));
		String temp="";
		br.readLine();
		Map<String, Integer> map = new HashMap<>();
		int s = 0;
		while((temp=br.readLine())!=null){
			String[] string = temp.split(",");
	 			String tripnumber = string[5];
			map.put(tripnumber, 1);
			s++;
		}
		br.close();
		java.util.Iterator<Entry<String, Integer>> entries = map.entrySet().iterator(); 
		br1.readLine();
		int mintime=0;

		}

}
