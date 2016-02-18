package com.structure;
import java.util.List;
import java.util.Set;

import de.hpi.bpt.process.petri.Flow;


public class PetriNet_AStar {
	
	Set<String> taskSet = null;
	Set<String> placeSet = null;
	List<Flow> edges = null;
	public Set<String> getTaskSet() {
		return taskSet;
	}
	public void setTaskSet(Set<String> taskSet) {
		this.taskSet = taskSet;
	}
	public Set<String> getPlaceSet() {
		return placeSet;
	}
	public void setPlaceSet(Set<String> placeSet) {
		this.placeSet = placeSet;
	}

	
	public List<Flow> getEdges() {
		return edges;
	}
	public void setEdges(List<Flow> edges) {
		this.edges = edges;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
