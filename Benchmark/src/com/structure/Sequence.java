package com.structure;

import de.hpi.bpt.process.petri.Transition;

public class Sequence {

	Transition from ;
	Transition to ;
	public Transition getFrom() {
		return from;
	}
	public void setFrom(Transition from) {
		this.from = from;
	}
	public Transition getTo() {
		return to;
	}
	public void setTo(Transition to) {
		this.to = to;
	}
	
}
