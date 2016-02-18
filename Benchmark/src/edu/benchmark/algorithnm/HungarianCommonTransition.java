package edu.benchmark.algorithnm;

import static java.lang.Math.floor;

import com.structure.*;

import de.hpi.bpt.process.petri.Flow;
import de.hpi.bpt.process.petri.PetriNet;
import de.hpi.bpt.process.petri.Place;
import de.hpi.bpt.process.petri.Transition;
import edu.benchmark.datamodel.PetriNetExtension;
import static java.lang.Math.round;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class HungarianCommonTransition {
	public static final double wskipn = 0.1;   
	public static final double wsubn = 0.9;  
	public static final double wskipe = 0.4;
	
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
	
	public static Comparator<Place> compPlace= new Comparator<Place>() {
		@Override
		/* 比较库所id */
		public int compare(Place place1, Place place2) {
			String v1 = place1.getName();
			String v2 = place2.getName();
			if (v1 .compareTo(v2)<0 ) {
				return -1;
			}
			if (v1 .compareTo(v2)>0 ) {
				return 1;
			} else {
				return 0;
			}
		}
	};


	//用匈牙利算法来计算流程相似度
	public static double executeSimilarity(PetriNetExtension pnA,PetriNetExtension pnB,Map<String,Integer> placeOrderStringIntegerA,Map<String,Integer> placeOrderStringIntegerB,Map<Integer,String> placeOrderIntegerStringA,Map<Integer,String> placeOrderIntegerStringB) throws Exception{
		List<Context> mappedNodes = AlgorithnmUtil.initializeTaskMapping( (List<Transition>)pnA.getTransitions(), (List<Transition>) pnB.getTransitions());
		if(mappedNodes.size()==0){
			return 0.0;
		}else{
				pnA.executePlaceToLeftRightTransitions();
				pnB.executePlaceToLeftRightTransitions();
				Map<String, String[]> mapA = pnA.placeToLeftRightTransitions;
				Map<String, String[]> mapB = pnB.placeToLeftRightTransitions;
				Map<String,List<TPT>> initialPlaceMappingTable = initializePlaceMappingCommonTransition(mapA,mapB);
			
				//将流程中的Place编号
				double[][] context = new double[pnA.getPlaces().size()][pnB.getPlaces().size()];
				
				
				//创建匈牙利的输入：库所映射表
				Set<String> key = initialPlaceMappingTable.keySet();
		        for (Iterator it = key.iterator(); it.hasNext();) {
		            String placeA = (String) it.next();
		            int row = placeOrderStringIntegerA.get(placeA);
		            List<TPT> tpt = initialPlaceMappingTable.get(placeA);
		            for(TPT t:tpt){
		            		String placeB = t.getPlaceName();
		            		int column = placeOrderStringIntegerB.get(placeB);
		            	 context[row][column]=t.getPlaceContextSimilarity();
		            	//System.out.print("("+row+","+column+")"+":"+context[row][column]+"\t");
		            }
		            //System.out.println("\n");
		        }                                          

			String sumType = "max";	
			if (context.length > context[0].length) //需要转置
			{
				System.out.println("Array transposed (because rows>columns).\n");	//Cols must be >= Rows.
				context = HungarianAlgorithm.transpose(context);
			}

			int[][] assignment = new int[initialPlaceMappingTable.size()][2];
			assignment = HungarianAlgorithm.hgAlgorithm(context, sumType);	//Call Hungarian algorithm.

			for(int i1=0;i1<context.length;i1++){
				for(int j=0;j<context[i1].length;j++){
					/*if(context[i1][j]!=0)
					System.out.println(i1+"-"+j+":"+context[i1][j]);*/
					//System.out.print("("+i1+","+j+")"+":"+context[i1][j]+"\t");
				}
				// System.out.print("\n");
			}
			//替换节点的数目
			int subnSize = mappedNodes.size();
			//找出映射的库所对
			for (int z=0; z<assignment.length; z++)
			{
				if(context[assignment[z][0]][assignment[z][1]]!=0){
					subnSize++; 
					String placeP = placeOrderIntegerStringA.get(assignment[z][0]);
					String placeQ = placeOrderIntegerStringB.get(assignment[z][1]);
					Context mappedPlace= new Context();
					mappedPlace.setNode1(placeP);
					mappedPlace.setNode2(placeQ);
					mappedPlace.setSimilarity(context[assignment[z][0]][assignment[z][1]]);
					mappedNodes.add(mappedPlace);
					//System.out.println(assignment[z][0]+"("+placeP+")->"+assignment[z][1]+"("+placeQ+"):"+context[assignment[z][0]][assignment[z][1]]);

				}
			}
			
			int subeSize=0;//替换边数量
			for(int m1=0;m1<mappedNodes.size();m1++){
				for(int m2=0;m2<mappedNodes.size();m2++){
					if((AlgorithnmUtil.ifExistEdge((List<Flow>)(pnA.getEdges()),mappedNodes.get(m1).getNode1(),mappedNodes.get(m2).getNode1())&&(AlgorithnmUtil.ifExistEdge((List<Flow>)(pnB.getEdges()),mappedNodes.get(m1).getNode2(),mappedNodes.get(m2).getNode2())))){
						subeSize++;
					}
				}
			}
			int nodeSize =pnA.getPlaces().size()+pnB.getPlaces().size()+pnA.getTransitions().size()+pnB.getTransitions().size();
			int edgeSize = pnA.getEdges().size() +pnB.getEdges().size();
					
			double totalNodeSimilarity = 0;		
			double fskipn, fskipe, fsubn;

			fskipn = (nodeSize - subnSize * 2)
					* 1.0 / (nodeSize);
			fskipe = (edgeSize - subeSize * 2) * 1.0 / (edgeSize);

			for (Context c : mappedNodes) {
				totalNodeSimilarity = totalNodeSimilarity + (1.0 - c.getSimilarity());
			}
			fsubn = 2.0 * totalNodeSimilarity / (subnSize * 2);
			double similarity = 1.0- (wskipn * fskipn + wskipe * fskipe + wsubn * fsubn)/ (wskipn + wskipe + wsubn);
			return similarity;
		}
		
	}
	
	public static Map<String,List<TPT>> initializePlaceMappingCommonTransition(Map<String, String[]> mapA, Map<String, String[]> mapB){
		Map<String,List<TPT>> placeList = new HashMap<String,List<TPT>>();
		Iterator<String> iterMapA = mapA.keySet().iterator();
		Iterator<String> iterMapB = null;
		
		while (iterMapA.hasNext()) {
			List<TPT> list = new ArrayList<TPT>();
			iterMapB = mapB.keySet().iterator();
			String placeA = iterMapA.next();
			String[] transitionA = mapA.get(placeA);// placeA的左右变迁
			String[] leftTransitionsA = transitionA[0].trim().split(" "); // 左变迁
			String[] rightTransitionsA = transitionA[1].trim().split(" ");// 右变迁
			int leftTransitionsASize=AlgorithnmUtil.getArrayCountNotNull(leftTransitionsA);
			int rightTransitionsASize= AlgorithnmUtil.getArrayCountNotNull(rightTransitionsA);;

			while (iterMapB.hasNext()) {
				String placeB = iterMapB.next();
				String[] transitionB = mapB.get(placeB);
				String[] leftTransitionsB = transitionB[0].trim().split(" ");
				String[] rightTransitionsB = transitionB[1].trim().split(" ");
				int leftTransitionsBSize=AlgorithnmUtil.getArrayCountNotNull(leftTransitionsB);
				int rightTransitionsBSize=AlgorithnmUtil.getArrayCountNotNull(rightTransitionsB);

				String[] leftIntersection = AlgorithnmUtil.intersect(leftTransitionsA,leftTransitionsB);//左变迁交集
				String[] rightIntersection = AlgorithnmUtil.intersect(rightTransitionsA,rightTransitionsB);//右变迁交集
				int leftIntersectionSize=AlgorithnmUtil.getArrayCountNotNull(leftIntersection);
				int rightIntersectionSize=AlgorithnmUtil.getArrayCountNotNull(rightIntersection);

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
				TPT tpt = new TPT();
				tpt.setPlaceName(placeB);
				tpt.setPlaceContextSimilarity(similarity);
				list.add(tpt);
			}
			placeList.put(placeA, list);
		}
		return placeList;
	}
	
	//将placeName编号
	public static Map<String,Integer> createPlaceMapStringInteger(List<Place> placeSet){
		Map<String,Integer> placeOrder = new HashMap<String,Integer>();
		for(int i=0;i<placeSet.size();i++){
			 placeOrder.put( placeSet.get(i).getName(),i);
		}
		return placeOrder;
	}
	
	//将placeName编号
	public static Map<Integer,String> createPlaceMapIntegerString(List<Place> placeSet){
		Map<Integer,String> placeOrder = new HashMap<Integer,String>();
		for(int i=0;i<placeSet.size();i++){
			 placeOrder.put( i,placeSet.get(i).getName());
		}
		return placeOrder;
	}
	
	static final String searchProcessID = "11";
	static final int startProcessID = 31,endProcessID=40; //相关流程ID范围
	static final int relateProcessSize = 10; //相关流程数目
	public static void main(String[] args) throws Exception {
		Map<Integer,Double> map = new TreeMap<Integer,Double>();


		for(int index=1;index<=100;index++){

			String fileNameX = "./Benchmark1210/"+searchProcessID+".lola";
			PetriNetExtension pnA = new PetriNetExtension(fileNameX);
			List<Place> placeA= (List<Place>) pnA.getPlaces();
			String fileNameY= "./Benchmark1210/"+index+".lola";
			PetriNetExtension pnB = new PetriNetExtension(fileNameY);
			List<Place> placeB= (List<Place>) pnB.getPlaces();
			if(placeB.size()<placeA.size()){
				PetriNetExtension pnx = pnA;
				pnA = pnB;
				pnB = pnx;
			}
			List<Place> sortPlaceA= (List<Place>) pnA.getPlaces();
			List<Place> sortPlaceB= (List<Place>) pnB.getPlaces();
			//对每个流程中的库所进行排序
			Collections.sort(sortPlaceA, compPlace);
			Collections.sort(sortPlaceB, compPlace);

			//对place集合中的所有place进行编号
			Map<String,Integer> placeListStringIntegerA = createPlaceMapStringInteger(sortPlaceA); 
			Map<String,Integer> placeListStringIntegerB = createPlaceMapStringInteger(sortPlaceB); 
			Map<Integer,String> placeListIntegerStringA = createPlaceMapIntegerString(sortPlaceA); 
			Map<Integer,String> placeListIntegerStringB = createPlaceMapIntegerString(sortPlaceB); 

			//System.out.println("--------------------------"+index+"------------------------------------");
			double similarity =executeSimilarity(pnA,pnB,placeListStringIntegerA,placeListStringIntegerB,placeListIntegerStringA,placeListIntegerStringB);
			System.out.println("similarity:"+similarity);
			map.put(index, similarity);
		}

		List<Map.Entry<Integer, Double>> list = new ArrayList<Map.Entry<Integer, Double>>(map.entrySet()); 		
		System.out.println("-----------------排序前------------------------");
		AlgorithnmUtil.logListContent(list);
		AlgorithnmUtil.sortList(list);
		System.out.println("-----------------排序后------------------------");
		AlgorithnmUtil.logListContent(list);
		AlgorithnmUtil.storeResult(list,searchProcessID,startProcessID,endProcessID,relateProcessSize);
		System.out.println("over");
	}
}
