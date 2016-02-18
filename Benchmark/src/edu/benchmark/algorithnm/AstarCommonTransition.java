package edu.benchmark.algorithnm;

import java.io.*;
import java.util.*;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.structure.Context;
import com.structure.map;

import edu.benchmark.datamodel.*;
import de.hpi.bpt.process.petri.*;
public class AstarCommonTransition {

	public static final double ledcutoff=0;
	public static final double wskipn = 0.2; //定义权重  
	public static final double wsubn = 0.1;  
	public static final double wskipe =0.7;  
	public static final String s="ε";

	public static List<Context> initializePlaceMapping(Map<String, String[]> mapA, Map<String, String[]> mapB){
		List<Context> placeList = new ArrayList<Context>();
		Iterator<String> iterA = mapA.keySet().iterator();//获得第一个流程的keyset
		Iterator<String> iterB = null;
		while (iterA.hasNext()) {
			iterB = mapB.keySet().iterator();//获得第二个流程的keyset
			String placeA = iterA.next();
			String[] transitionA = mapA.get(placeA);// placeA的左右变迁
			String[] leftTransitionsA = transitionA[0].trim().split(" "); // placeA的左变迁
			String[] rightTransitionsA = transitionA[1].trim().split(" ");//placeA的 右变迁

			while (iterB.hasNext()) {
				String placeB = iterB.next();
				String[] transitionB = mapB.get(placeB);
				String[] leftTransitionsB = transitionB[0].trim().split(" ");// placeB的左变迁
				String[] rightTransitionsB = transitionB[1].trim().split(" ");//placeA的 右变迁
				String[] leftIntersection = AlgorithnmUtil.intersect(leftTransitionsA,
						leftTransitionsB);//左变迁交集
				String[] rightIntersection = AlgorithnmUtil.intersect(rightTransitionsA,
						rightTransitionsB);//右变迁交集
				Context PlaceContext = new Context();
				PlaceContext.setNode1(placeA);		
				PlaceContext.setNode2(placeB);
				int maxLeftSize,maxRightSize =0;
				if(leftTransitionsA.length>leftTransitionsB.length)
					maxLeftSize = leftTransitionsA.length;
				else
					maxLeftSize = leftTransitionsB.length;

				if(rightTransitionsA.length>rightTransitionsB.length)
					maxRightSize = rightTransitionsA.length;
				else
					maxRightSize = rightTransitionsB.length;
				PlaceContext.setSimilarity((leftIntersection.length+rightIntersection.length)*1.0/(maxLeftSize+maxRightSize));
				placeList.add(PlaceContext);
			}
			Context PlaceContext = new Context();
			PlaceContext.setNode1(placeA);
			PlaceContext.setNode2(s);
			PlaceContext.setSimilarity(0);
			placeList.add(PlaceContext);			
		}

		return placeList;
	}

	public static List<Context> initializeMapping(PetriNetExtension pn1,PetriNetExtension pn2)
	{
		List<Context> initializeMapping=new ArrayList<Context>();
		Map<String, String[]> map1 = null;
		Map<String, String[]> map2 = null;

		try {
			pn1.executePlaceToLeftRightTransitions();
			pn2.executePlaceToLeftRightTransitions();
			map1 = pn1.placeToLeftRightTransitions;
			map2 = pn2.placeToLeftRightTransitions;
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*存储一个petri网的所有变迁*/
		List<Transition> taskSet1 = (List<Transition>) pn1.getTransitions();
		List<Transition> taskSet2 = (List<Transition>) pn2.getTransitions();

		//初始化流程的库所和变迁映射所及其相似度
		List<Context>  openpairs =  initializePlaceMapping(map1,map2); 
		List<Context>  openpairs2 = AlgorithnmUtil.initializeTaskMapping(taskSet1,taskSet2);
		initializeMapping.addAll(openpairs);
		initializeMapping.addAll(openpairs2);
		/*for(Context c1:initializeMapping)
    		System.out.println(c1.getNode1()+" "+c1.getNode2()+" "+c1.getSimilarity());*/
		return initializeMapping;
	}
	/*找到开始节点*/
	public static Place getStartPlace(PetriNet p)
	{
		List<Place> places = (List<Place>) p.getPlaces();
		Place place = null;
		for(Place p1:places)
		{
			if(p1.getTokens()==1)
				place=p1;	
		}
		return place;


	}
	/*初始换open结构*/
	public static List<map> initializeopen(Place p,List<Context> initializeMapping,List<Flow> edgeSet1,List<Flow> edgeSet2,int nodeSize,int edgeSize)
	{

		List<map> open=new ArrayList<map>();


		for(Context c:initializeMapping)
		{ 
			map map1=new map();

			if(c.getNode1().equals(p.getName())&& c.getSimilarity()>ledcutoff || c.getNode1().equals(p.getName())&&c.getNode2().equals(s))
			{
				List<Context> l1=new ArrayList<Context>();
				l1.add(c);
				map1.setMappingNodes(l1);
				map1.setSimailrity(similarity(map1.getMappingNodes(),edgeSet1,edgeSet2,nodeSize,edgeSize));
				open.add(map1);	
			}

		}		
		return open;

	}
	/*寻找相似对做大的map*/
	public static map Max(List<map> open)
	{
		map map1=new map();
		double maxsim=0;
		int indexof=0;
		for(map m:open)
		{
			if(m.getSimailrity()>maxsim)
			{
				indexof=open.indexOf(m);
				maxsim=m.getSimailrity();
			}


		}
		map1=open.get(indexof);

		return map1;
	}
	/*获得流程一种所有节点的名称集合*/

	public static List<String> getNodeset(String fileName)
	{
		List<String> setnode1=new ArrayList<String>();

		File file = new File(fileName);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (!line.contains("{ END OF FILE }")) {
				if (line.contains("PLACE")) {
					line = br.readLine();
					while (!line.trim().equals(";")) {
						String[] s = line.trim().split(",");
						String placeName = s[0].trim();
						setnode1.add(placeName);
						line = br.readLine();		
					}
				}
				else if(line.contains("TRANSITION")){
					String[] s = line.trim().split(" ");
					String taskName = s[1].trim();
					setnode1.add(taskName);

					line = br.readLine();

				}else {
					line = br.readLine();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		/*显示N1中的节点*/
		/* for(String s:setnode1)
        	System.out.println(s);
        setnode1.remove("");*/
		return setnode1;
	}

	/*从一个map中得到所包含的N1中的节点集合*/
	public static List<String> dom(map m1)
	{
		List<String> setnode1=new ArrayList<String>();
		for(Context c:m1.getMappingNodes())
		{
			setnode1.add(c.getNode1());
		}
		return setnode1;

	}
	/*判断一个map中得到所包含的N2的节点集合*/
	public static List<String> cod(map m1)
	{
		List<String> setnode1=new ArrayList<String>();
		for(Context c:m1.getMappingNodes())
		{
			setnode1.add(c.getNode2());
		}
		return setnode1;

	}

	/*找出替换边的个数*/
	public static int subeSize(List<Context> map,List<Flow> edgeListA,List<Flow> edgeListB,int nodeSize,int edgeSize){

		int subeSize=0;
		for(Context map1:map){
			for(Context map2:map){
				if((AlgorithnmUtil.ifExistEdge(edgeListA,map1.getNode1(),map2.getNode1()))&&(AlgorithnmUtil.ifExistEdge(edgeListB,map1.getNode2(),map2.getNode2()))){
					subeSize++;
				}
			}
		}
		return subeSize;
	}
	/*计算在一个map下流程间的相似对,这里要改*/
	public static double similarity(List<Context> map,List<Flow> edgeListA,List<Flow> edgeListB,int nodeSize,int edgeSize){
		double fskipn,fskipe,fsubn;
		List<Context> map1=new ArrayList<Context>();
		for(Context c:map)
		{
			if(!c.getNode2().equals(s))
				map1.add(c);
		}
		int subeSize=subeSize(map1,edgeListA,edgeListB,nodeSize, edgeSize);	
		fskipn = (nodeSize-map1.size()*2)*1.0/(nodeSize);
		fskipe = (edgeSize-subeSize*2)*1.0/(edgeSize);
		double totalSimilarity =0;
		for(Context context:map1){
			totalSimilarity =totalSimilarity +(1.0-context.getSimilarity());
		}
		fsubn = 2.0*totalSimilarity/(map1.size()*2);
		double similarity=1.0-(wskipn*fskipn+wskipe*fskipe+wsubn*fsubn)/(wskipn+wskipe+wsubn);

		return similarity;
	}

	public static double executeSimilarity(String filePathA,String filePathB)
	{
		PetriNetExtension pn1 = new PetriNetExtension(filePathA);
		PetriNetExtension pn2 = new PetriNetExtension(filePathB);

		List<Transition> taskSet1 = (List<Transition>) pn1.getTransitions();
		List<Transition> taskSet2 = (List<Transition>) pn2.getTransitions();
		if(AlgorithnmUtil.initializeTaskMapping(taskSet1, taskSet2).size()==0)
			return 0;
		else{
			List<Flow> edgeSet1 = (List<Flow>) pn1.getEdges();
			List<Flow> edgeSet2 = (List<Flow>) pn2.getEdges();

			int nodeSize = pn1.nodeSize+pn2.nodeSize;
			int edgeSize=edgeSet1.size()+edgeSet2.size();

			List<Context> initializeMapping=initializeMapping(pn1,pn2);

			List<map> open=new ArrayList<map>();
			Place p=getStartPlace(pn1);

			open=initializeopen(p,initializeMapping,edgeSet1,edgeSet2,nodeSize,edgeSize);
			map mapping=new map();//用来记录所选择的最佳映射节点


			/*用来存放两个流程的节点*/
			List<String> nodset1=getNodeset(filePathA);
			List<String> nodeset2=getNodeset(filePathB);

			nodeset2.add(s);

			while(open.size()!=0)
			{
				//1.在open中找到相似对最高的map
				//2.从open中删除该map
				//3.如果dom(map)=N1则返回map
				//4.从N1中选择一个不属于dom(map)的点n1
				//5.对于N2中的每个不在cod(map)中的点，并且sim(n1,n2)>ledcutoff,则将(n1,n2,sim(n1,n2))加入到map里，或者将((n1,ε，0))加入到map里，形成新的map',然后加入到open中
				//转到1，直到程序退出
				mapping=Max(open);
				List<String> dommap=dom(mapping);
				List<String> codmap=cod(mapping); 		
				open.remove(mapping);   		
				if(AlgorithnmUtil.compare(nodset1,dom(mapping)))
				{
					open.removeAll(open);
				}
				else
				{
					String s5=null;
					for(String s3:nodset1)
					{
						if(!dommap.contains(s3))
						{
							s5=s3;
							break;
						}
					}

					for(String s4:nodeset2)
					{
						if(!codmap.contains(s4) && !s4.equals(s))
						{
							List<Context> l2=new ArrayList<Context>();
							/*这边也可以直接换成计算两个节点之间的相似度，不用查询*/
							for(Context c1:initializeMapping)
							{
								if(c1.getNode1().equals(s5) && c1.getNode2().equals(s4) && c1.getSimilarity()>ledcutoff)
								{
									l2.add(c1);
									break;
								}
							}
							for(Context c2:l2)
							{
								map mapping2=new map();

								List<Context> currentMapping=new ArrayList<Context>();
								currentMapping.addAll(mapping.getMappingNodes());
								currentMapping.add(c2);
								mapping2.setMappingNodes(currentMapping);
								double similarity=similarity(currentMapping,edgeSet1,edgeSet2,nodeSize,edgeSize);
								mapping2.setSimailrity(similarity);
								open.add(mapping2);

							}
						}
						else if(s4.equals(s))//是否要考虑加一个!dom(maping).contains(s4)
						{
							Context c=new Context();
							c.setNode1(s5);
							c.setNode2(s);
							c.setSimilarity(0);
							map mapping2=new map();

							List<Context> currentMapping=new ArrayList<Context>();
							currentMapping.addAll(mapping.getMappingNodes());
							currentMapping.add(c);
							mapping2.setMappingNodes(currentMapping);
							double similarity=similarity(currentMapping,edgeSet1,edgeSet2,nodeSize,edgeSize);
							mapping2.setSimailrity(similarity);
							open.add(mapping2);
						}
					}
				}
		}
		
		return mapping.getSimailrity();
        }
	}
	
	static final String searchProcessID = "base";
	static final int startProcessID = 1,endProcessID=10; //相关流程ID范围
	static final int relateProcessSize = 10; //相关流程数目
	public static void main(String[] args)  throws Exception{
		Map<Integer,Double> map = new TreeMap<Integer,Double>();
		//String filePathA="./Benchmark1210/"+searchProcessID+".lola";
		String filePathA="./varyOneFixTwo/"+searchProcessID+".lola";

		for (int i = 1; i <= 100; i++) {
			String filePathB="./Benchmark1210/"+i+".lola";
			double similarity =executeSimilarity(filePathA,filePathB);
			System.out.println(i+"的相似度为："+similarity);
			map.put(i, similarity);
		}

		List<Map.Entry<Integer, Double>> list = new ArrayList<Map.Entry<Integer, Double>>(map.entrySet()); 		
		System.out.println("-----------------排序前------------------------");
		AlgorithnmUtil.logListContent(list);
		AlgorithnmUtil.sortList(list);
		System.out.println("-----------------排序后------------------------");
		AlgorithnmUtil.logListContent(list);
		AlgorithnmUtil.storeResult(list,searchProcessID,startProcessID,endProcessID,relateProcessSize);
	}
}
