package com.structure;

import java.util.ArrayList;
import java.util.List;

//记录两个流程中映射的节点对、这两个流程的相似分
public class map {
	private List<Context> mappingNodes = new ArrayList<Context>();
	double simailrity = 0;
	public List<Context> getMappingNodes() {
		return mappingNodes;
	}
	public void setMappingNodes(List<Context> mappingNodes) {
		this.mappingNodes = mappingNodes;
	}
	public double getSimailrity() {
		return simailrity;
	}
	public void setSimailrity(double simailrity) {
		this.simailrity = simailrity;
	}
	
}
