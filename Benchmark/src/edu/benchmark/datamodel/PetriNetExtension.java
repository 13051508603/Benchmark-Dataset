package edu.benchmark.datamodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import de.hpi.bpt.process.petri.Flow;
import de.hpi.bpt.process.petri.Node;
import de.hpi.bpt.process.petri.PetriNet;
import de.hpi.bpt.process.petri.Place;
import de.hpi.bpt.process.petri.Transition;
import edu.benchmark.algorithnm.AlgorithnmUtil;

/**
 * 
 * @author wjx_sky
 * 扩展PetriNet,增加Place左右两边的变迁List
 */
public class PetriNetExtension extends PetriNet {
	//数据结构存放，计算出Place左右两边Transitin List后存放到map中
	public 	Map<String, String[]> placeToLeftRightTransitions = new HashMap<String, String[]>();
	public int nodeSize;

	public PetriNetExtension(String fileName){
		super();
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
							Place place = new Place(placeName);
							this.addNode(place);
							line = br.readLine();		
						}
					}else if(line.contains("MARKING")){
						line = br.readLine();
						String[] s = line.trim().split(":");
						String startPlaceName= s[0].trim();
						Place startPlace = AlgorithnmUtil.findPlace(this,startPlaceName);
						startPlace.setTokens(1);
					}
					else if(line.contains("TRANSITION")){
						String[] s = line.trim().split(" ");
						String taskName = s[1].trim();
						Transition task = new Transition(taskName);
						this.addNode(task);
						line = br.readLine();
						if(line.contains("CONSUME")){
							line = br.readLine();
							while(!line.trim().equals(";") && !line.contains("PRODUCE")&&line.trim().length()!=0){
								String[] t = line.trim().split(":");
								String consume = t[0].trim();
								Place consumePlace = AlgorithnmUtil.findPlace(this,consume);
								
								this.addFlow(consumePlace, task);
								line = br.readLine();
							}
							if(line.contains("PRODUCE")){
								line = br.readLine();
								while(!line.trim().equals(";")&&!line.contains("TRANSITION")&&line.trim().length()!=0){
									String[] t = line.trim().split(":");
									String produce = t[0].trim();
									Place producePlace = AlgorithnmUtil.findPlace(this,produce);
									this.addFlow(task, producePlace);
									line = br.readLine();
								}
							}
						}	
					}else {
						line = br.readLine();
					}
			}
			this.nodeSize = ((List<Place>) this.getPlaces()).size() + ((List<Transition>) this.getTransitions()).size();
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//需要在所有petriNet对应数据都到位后再调用
	public void executePlaceToLeftRightTransitions() throws Exception{
		if(this.placeToLeftRightTransitions.size() >0){
			return;
		}
		Map<String, String[]> answerMap = new HashMap<String, String[]>();
		List<Place> places = (List<Place>) this.getPlaces();
		List<Transition> tasks = (List<Transition>) this.getTransitions();
		List<Flow> edges = (List<Flow>) this.getEdges();

		for (Flow e : edges) {
			Node left = e.getV1();
			Node right = e.getV2();
			if (left.getClass().getName().equals("de.hpi.bpt.process.petri.Place")) {//left是库所,right是变迁
				if (!places.contains(e.getV1())) {
					throw new Exception(left + " is not in the place set!!");
				}
				if (!tasks.contains(e.getV2())) {
					throw new Exception(right + " is not in the task set!!");
				}
				if (!answerMap.containsKey(e.getV1().getName())) {
					//如果没有，新建一个
					String[] transNodes = { "", "" };
					transNodes[1] = e.getV2().getName();
					answerMap.put(e.getV1().getName(), transNodes);
				} else {
					//如果有直接塞进去
					String[] transNodes = answerMap.get(e.getV1().getName());
					transNodes[1] = transNodes[1] + " " + e.getV2().getName();
				}
			} else if (left.getClass().getName().equals("de.hpi.bpt.process.petri.Transition")) {// left是变迁,right是库所

				if (!tasks.contains(e.getV1())) {
					throw new Exception(left + " is not in the task set!!");
				}
				if (!places.contains(e.getV2())) {
					throw new Exception(right + " is not in the place set!!");
				}
				if (!answerMap.containsKey(e.getV2().getName())) {
					String[] transNodes = { "", "" };
					transNodes[0] = e.getV1().getName();
					answerMap.put(e.getV2().getName(), transNodes);
				} else {
					String[] transNodes = answerMap.get(e.getV2().getName());
					transNodes[0] = transNodes[0] + " " + e.getV1().getName();
				}
			} else {
				throw new Exception("Unknown node type!!!");
			}
		}
		this.placeToLeftRightTransitions = answerMap ;
	}
	
	/**
	 * 获取一个place的左边变迁集合
	 * @return
	 */
	public String getLeftTransition(String placeName){
		return this.placeToLeftRightTransitions.get(placeName)[0];
	}
	/**
	 *  获取一个place的右边变迁集合
	 * @return
	 */
	public String getRightTransition(String placeName){
		return this.placeToLeftRightTransitions.get(placeName)[1];
	}
}
