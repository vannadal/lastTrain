package Preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;

public class Operation{
	private String filepath;
	public String[][] a=null;
	public  Operation(String filepath){
		this.filepath=filepath;
	}
	public  void readCsv(){	
		try
		{
			BufferedReader br=new BufferedReader(new FileReader(new File(filepath)));
			String str=null;
			String temp[]=null;
			a=new String[380][21];
			int i=0;
			while((str=br.readLine())!=null)
			{
				temp=str.split(",");// 字符串为单位的二维数组
				int j=0;
				while(j<temp.length){
					a[i][j]=temp[j];
					//System.out.print(a[i][j]);
					j++;
				}
//				System.out.print("\n");
				i++;
			}
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
	}
}
