package com.structure;
import java.util.ArrayList;
import java.util.List;

public class CurrentMappingNodes {
	
	private List<Context> mappingNodes = new ArrayList<Context>();
	private Context context;
	double simailrity = 0;
	public List<Context> getMappingNodes() {
		return mappingNodes;
	}
	public void setMappingNodes(List<Context> mappingNodes) {
		this.mappingNodes = mappingNodes;
	}
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public double getSimailrity() {
		return simailrity;
	}
	public void setSimailrity(double simailrity) {
		this.simailrity = simailrity;
	}
	

}
