package edu.benchmark.algorithnm;

import java.io.*;
import java.util.*;

import com.structure.Context;
import com.structure.CurrentMappingNodes;

import de.hpi.bpt.process.petri.*;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import edu.benchmark.datamodel.*;

public class GreedyCommonTransition {

	public static final double wskipn = 0.1;
	public static final double wsubn = 0.9;
	public static final double wskipe = 0.4;
	
	public static double getCurrentSimilarity(List<Context> mapTasks,List<Context> mapPlaces,List<Flow> edgeListA,List<Flow>edgeListB,int totalNodeSize,int totalEdgeSize){
		double fskipn,fskipe,fsubn;
		int subeSize=AlgorithnmUtil.subeSize(mapTasks,mapPlaces,edgeListA,edgeListB,totalNodeSize, totalEdgeSize);
		fskipn = (totalNodeSize-(mapTasks.size()+mapPlaces.size())*2)*1.0/(totalNodeSize);
		fskipe = (totalEdgeSize-subeSize*2)*1.0/(totalEdgeSize);
		double totalSimilarity =0;
		for(Context context:mapTasks){
			totalSimilarity =totalSimilarity +(1.0-context.getSimilarity());
		}
		for(Context context:mapPlaces){
			totalSimilarity =totalSimilarity +(1.0-context.getSimilarity());
		}
		fsubn = 2.0*totalSimilarity/((mapTasks.size()+mapPlaces.size())*2);
		double similarity=1.0-(wskipn*fskipn+wskipe*fskipe+wsubn*fsubn)/(wskipn+wskipe+wsubn);
		return similarity;
	}
	
	public static  CurrentMappingNodes max(List<CurrentMappingNodes> currentMappingNodesList){
		double similarity = 0;
		CurrentMappingNodes maxMappingNodes = new CurrentMappingNodes();
		for(CurrentMappingNodes currentMappingNode:currentMappingNodesList){
			List<Context> allMapping = new ArrayList<Context>();
			allMapping.addAll(currentMappingNode.getMappingNodes());
			allMapping.add(currentMappingNode.getContext());
			double currentSim = currentMappingNode.getSimailrity();
			if(currentSim > similarity){
				similarity = currentSim;
				maxMappingNodes = currentMappingNode;
			}
		}
		
		return maxMappingNodes;
	}
	
	

	public static double executeSimilarity(PetriNetExtension pnA,PetriNetExtension pnB) throws Exception{
		List<Context> mappedTaskpairs = AlgorithnmUtil.initializeTaskMapping( (List<Transition>)pnA.getTransitions(), (List<Transition>) pnB.getTransitions());
		if(mappedTaskpairs.size()==0){
			return 0.0;
		}else{
			pnA.executePlaceToLeftRightTransitions();
			pnB.executePlaceToLeftRightTransitions();
			List<Context> placeOpenpairs = AlgorithnmUtil.initializePlaceMappingCommonTransition(pnA.placeToLeftRightTransitions,pnB.placeToLeftRightTransitions); 
			List<Context> mappedPlaces = new ArrayList<Context>();
			
			int totalNodeSize =pnA.nodeSize + pnB.nodeSize;
			int totalEdgeSize =  ((List<Flow>) pnA.getEdges()).size() + ( (List<Flow>) pnB.getEdges()).size();

			double similarity = getCurrentSimilarity(mappedTaskpairs, mappedPlaces,
					 (List<Flow>)pnA.getEdges(), (List<Flow>)pnB.getEdges(), totalNodeSize, totalEdgeSize);
			while (placeOpenpairs != null && placeOpenpairs.size() != 0) {
				Context maxContext = new Context();
				double maxSim = similarity;
				for (int i1 = 0; i1 < placeOpenpairs.size(); i1++) {
					List<Context> allContext = new ArrayList<Context>();
					allContext.addAll(mappedPlaces);
					allContext.add(placeOpenpairs.get(i1));
					double currentSimailrity = getCurrentSimilarity(mappedTaskpairs, allContext,
							 (List<Flow>)pnA.getEdges(),  (List<Flow>)pnB.getEdges(), totalNodeSize, totalEdgeSize);
					if(  currentSimailrity>maxSim){
						maxSim = currentSimailrity;
						maxContext = placeOpenpairs.get(i1);
					}
				}
				if (maxSim <= similarity) {
					break;
				} else {
					mappedPlaces.add(maxContext);
					similarity = maxSim;
				}
				Iterator<Context> openpairsIterator = placeOpenpairs
						.iterator();
				while (openpairsIterator.hasNext()) {
					Context openpair = openpairsIterator.next();
					if (openpair.getNode1().equals(
							maxContext.getNode1())
							|| openpair.getNode2().equals(
									maxContext.getNode2()))
						openpairsIterator.remove();
				}
			}
			return similarity;
		}
	}
	
	static final String searchProcessID = "91";
	static final int startProcessID = 21,endProcessID=30; //相关流程ID范围
	static final int relateProcessSize = 10; //相关流程数目
	public static void main(String args[]) throws Exception{		
		String fileNameX = "/home/hadoop/wjx/Benchmark1210/"+searchProcessID+".lola";
		//String fileNameX = "Benchmark1210/"+searchProcessID+".lola";
		//String fileNameX = "varyOneFixTwo/"+searchProcessID+".lola";
		PetriNetExtension pnA = new PetriNetExtension(fileNameX) ;
		Map<Integer,Double> map = new TreeMap<Integer,Double>();
		//流程库中的流程,100个逐个比较
		System.out.println("searchProcessID:"+searchProcessID);
		long t1 = System.currentTimeMillis();
		for (int i = 1; i <= 100; i++) {
			String fileNameY = "/home/hadoop/wjx/Benchmark1210//"+i+".lola";
			//String fileNameY = "Benchmark1210//"+i+".lola";
			PetriNetExtension pnB = new PetriNetExtension(fileNameY) ;
			double similarity = executeSimilarity(pnA,pnB);
			map.put(i, similarity);
			System.out.println(i+":similarity="+similarity);
		}
		long t2 = System.currentTimeMillis();
		
		System.out.println("time:"+(t2-t1));
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

