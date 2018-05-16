package gov.bjjtw.lastTrain.Preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class DivideTimeTable {

	//将列车时刻表按照工作日/周末以及上行/下行拆分成四个表格
	public void ReadTimeTable(String filename) {
		try {
			BufferedReader br=new BufferedReader(new FileReader(new File(filename)));
			BufferedWriter bw_weekday=new BufferedWriter(new FileWriter(new File("D:\\末班车\\available\\weekday_timetable_allstation.csv"),true));
			BufferedWriter bw_weekend=new BufferedWriter(new FileWriter(new File("D:\\末班车\\available\\weekend_timetable_allstation.csv"),true));
			br.readLine();
			String temp,start_acc,start_departuretime,end_acc,end_departuretime,str[],tripNumber,week,end_arrivingtime;
			str=br.readLine().split(",");
			start_acc=str[0];
			start_departuretime=str[1];
			week=str[2];
			tripNumber=str[5];
			while((temp=br.readLine())!=null){
				str=temp.split(",");
				if(tripNumber.equals(str[5])) {
					end_acc=str[0];
					end_departuretime=str[1];
					end_arrivingtime=str[6];
					week=str[2];
					//最后一列是下一站的到达时间
					if(week.equals("双休")) {
						bw_weekend.write(start_acc+","+end_acc+","+start_departuretime+","+end_departuretime+","+tripNumber+","+end_arrivingtime+"\n");
					} else{
						bw_weekday.write(start_acc+","+end_acc+","+start_departuretime+","+end_departuretime+","+tripNumber+","+end_arrivingtime+"\n");
					}
					start_acc=end_acc;
					start_departuretime=end_departuretime;
				} else{
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
	
}
