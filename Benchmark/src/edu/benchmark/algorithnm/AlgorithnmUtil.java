package edu.benchmark.algorithnm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.structure.Context;

import de.hpi.bpt.process.petri.Flow;
import de.hpi.bpt.process.petri.PetriNet;
import de.hpi.bpt.process.petri.Place;
import de.hpi.bpt.process.petri.Transition;

public class AlgorithnmUtil {

	public static String[] intersect(String[] arr1, String[] arr2) {
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		LinkedList<String> list = new LinkedList<String>();
		for (String str : arr1) {
			if (!map.containsKey(str)) {
				map.put(str, Boolean.FALSE);
			}
		}
		for (String str : arr2) {
			if (map.containsKey(str)) {
				map.put(str, Boolean.TRUE);
			}
		}

		for (Entry<String, Boolean> e : map.entrySet()) {
			if (e.getValue().equals(Boolean.TRUE)) {
				list.add(e.getKey());
			}
		}
		String[] result = {};
		return list.toArray(result);
	}

	public static boolean ifExistEdge(List<Flow> edgeList,String fromNode,String toNode){
		boolean flag = false;
		for(Flow edge : edgeList){
			if(edge.getSource().getName().equals(fromNode)&&edge.getTarget().getName().equals(toNode))
				flag =  true;
		}
		return flag;
	}

	public static int subeSize(List<Context> mapTasks,List<Context> mapPlaces,List<Flow> edgeListA,List<Flow> edgeListB,int nodeSize,int edgeSize){
		List<Context> map= new ArrayList<Context>();
		map.addAll(mapTasks);
		map.addAll(mapPlaces);
		int subeSize=0;
		for(Context map1:map){
			for(Context map2:map){
				if((ifExistEdge(edgeListA,map1.getNode1(),map2.getNode1()))&&(ifExistEdge(edgeListB,map1.getNode2(),map2.getNode2()))){
					subeSize++;
				}
			}
		}
		return subeSize;
	}

	/*判断两个集合的元素是否相同*/
	public static boolean compare( List<String> set1, List<String> set2)
	{
		for(String s:set1)
		{
			if(!set2.contains(s))
				return false;
		}
		return true;
	}
	
	//对list内容进行排序
	public static void sortList(List<Map.Entry<Integer, Double>> list){
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() { 
			public int compare(Map.Entry<Integer, Double> o1, 
					Map.Entry<Integer, Double> o2) { 
				return (o2.getValue()).compareTo(o1.getValue()); 
			} 
		}); 
	}
	
	//按照格式log list内容
	public static void logListContent(List<Map.Entry<Integer, Double>> list){
		for (int i = 0; i < list.size(); i++) { 
			System.out.println((list.get(i).getKey())+"-"+list.get(i).getValue()); 
		} 
	}
	
	//把内容存储到excel里面
    public static void storeResult(List<Map.Entry<Integer, Double>> resultList,String searchProcessID,int start, int end,int relateProcessSize) throws IOException, RowsExceededException, WriteException{
		String filePath = "N:\\Greedy_"+searchProcessID+".xls";
		System.out.println("-----------------把数据写到Excel:"+filePath+"------------------------");
		double recall,precision;
		File file = new File(filePath);
		OutputStream os = new FileOutputStream(file);
		WritableWorkbook wwb = Workbook.createWorkbook(os);
		WritableSheet sheet = wwb.createSheet("sheet1", 0);
		for(int p1=0;p1<100;p1++){
			int relateSize = 0;
			for(int p2=0;p2<=p1;p2++){
				if((resultList.get(p2).getKey())>=start && (resultList.get(p2).getKey())<=end)
					relateSize++;
			}
			System.out.println("召回前"+(p1+1)+"个流程"+"-relateSize:"+relateSize);
			precision = relateSize*1.0/(p1+1);
			recall = relateSize*1.0/relateProcessSize;
			//System.out.println("precision-recall:"+p1+"\t"+precision+"\t"+recall);
			Label label = new Label(1,p1,Double.toString(precision));
			sheet.addCell(label);
			label = new Label(2,p1,Double.toString(recall));
			sheet.addCell(label);	
		}
		wwb.write();
		os.flush();
		wwb.close();
		os.close();
    }
    
    //变迁映射
    public static List<Context> initializeTaskMapping(List<Transition> taskSet1,
			List<Transition> taskSet2) {
		List<Context> taskOpenpairs = new ArrayList<Context>();
		for (Transition task1 : taskSet1) {
			for (Transition task2 : taskSet2) {
				if (task1.getName().equals(task2.getName())){
					Context taskContext = new Context();
					taskContext.setNode1(task1.getName());
					taskContext.setNode2(task2.getName());
					taskContext.setSimilarity(1);
					taskOpenpairs.add(taskContext);
				}
			}
		}
		return taskOpenpairs;
	}
    
    //初始化库所映射
    public static List<Context> initializePlaceMappingCommonTransition(Map<String, String[]> mapA, Map<String, String[]> mapB){
		List<Context> placeList = new ArrayList<Context>();
		Iterator<String> iterMapA = mapA.keySet().iterator();
		Iterator<String> iterMapB = null;
		while (iterMapA.hasNext()) {
			iterMapB = mapB.keySet().iterator();
			String placeA = iterMapA.next();
			String[] transitionA = mapA.get(placeA);// placeA的左右变迁
			String[] leftTransitionsA = transitionA[0].trim().split(" "); // 左变迁
			String[] rightTransitionsA = transitionA[1].trim().split(" ");// 右变迁
			int leftTransitionsASize=getArrayCountNotNull(leftTransitionsA);
			int rightTransitionsASize= getArrayCountNotNull(rightTransitionsA);;

			while (iterMapB.hasNext()) {
				String placeB = iterMapB.next();
				String[] transitionB = mapB.get(placeB);
				String[] leftTransitionsB = transitionB[0].trim().split(" ");
				String[] rightTransitionsB = transitionB[1].trim().split(" ");
				int leftTransitionsBSize=getArrayCountNotNull(leftTransitionsB);
				int rightTransitionsBSize=getArrayCountNotNull(rightTransitionsB);

				String[] leftIntersection = AlgorithnmUtil.intersect(leftTransitionsA,leftTransitionsB);//左变迁交集
				String[] rightIntersection = AlgorithnmUtil.intersect(rightTransitionsA,rightTransitionsB);//右变迁交集
				int leftIntersectionSize=getArrayCountNotNull(leftIntersection);
				int rightIntersectionSize=getArrayCountNotNull(rightIntersection);

				int maxLeftSize = Math.max(leftTransitionsASize, leftTransitionsBSize);
				int maxRightSize =Math.max(rightTransitionsASize, rightTransitionsBSize);
				
				double similarity =0;	                
				if(leftTransitionsASize==0 && leftTransitionsBSize==0){
					similarity =rightIntersectionSize*1.0/maxRightSize;
				}else if(rightTransitionsASize==0 && rightTransitionsBSize == 0){
					similarity =leftIntersectionSize*1.0/maxLeftSize;
				}else{
					similarity = (leftIntersectionSize+rightIntersectionSize)*1.0/(maxLeftSize+maxRightSize);
				}
				Context PlaceContext = new Context();
				PlaceContext.setNode1(placeA);		
				PlaceContext.setNode2(placeB);
				PlaceContext.setSimilarity(similarity);
				placeList.add(PlaceContext);
			}
		}
		return placeList;
	}
    
    //找到string数组中不为空的数据
    public static int getArrayCountNotNull(String[] stringArray){
		int totalSize=0;
		for(int i=0;i<stringArray.length;i++)
			if(!stringArray[i].equals("")){
				totalSize++;
			}
		return totalSize;
	}
    
    //根绝placeName寻找对应的Place
	public static Place findPlace(PetriNet pn,String placeName){
		List<Place> places = (List<Place>) pn.getPlaces();
		Place place = null;
		for(Place p:places){
			if(p.getName().equals(placeName)){
				place = p;
				return place;
			}
		}
		return place;
	}

	//两个字符串的并集
	public static String[] union(String[] arr1, String[] arr2){
		Set<String> set = new HashSet<String>();
		for (String str : arr1){
			set.add(str);
		}
		for (String str : arr2){
			set.add(str);
		}
		String[] result = {};
		return set.toArray(result);
	}
}
