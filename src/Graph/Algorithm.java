package Graph;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public interface Algorithm {
	/**
	 * 执行算法
	 */
	void perform(Graph g, String sourceVertex,String date,String time,String end_vertex);
	/**
	 * 得到路径
	 */
	Map<String, String> getPath();
}

