package org.mike.logicbomb.helpers;

import org.mike.logicbomb.units.Condition;

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