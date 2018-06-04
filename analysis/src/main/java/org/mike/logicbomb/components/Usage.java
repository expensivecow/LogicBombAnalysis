package org.mike.logicbomb.components;

import org.mike.logicbomb.core.Analysis;

import soot.Value;

public class Usage {
	private final Condition condition;
	private final Value usedBy;

	public Usage(Condition condition, Value usedBy){
		this.condition = condition;
		this.usedBy = usedBy;
	}

	public Value getUsedBy() {
		return usedBy;
	}

	@Override
	public String toString(){
		return "[" + this.condition + " -> " + this.usedBy + "]";
	}
}