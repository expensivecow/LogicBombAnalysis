package org.mike.logicbomb.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.Value;
import soot.jimple.ConditionExpr;

/**
 * Represents a variable with some atributes to classify them
 */

public class Condition {
	private final ConditionExpr expression;
	private final List<Usage> uses;

	public Condition (ConditionExpr expression){
		this.expression = expression;
		this.uses = new ArrayList<Usage>();
	}

	public void newUse(Condition cond, Value usedBy){
		this.uses.add(new Usage(cond, usedBy));
	}

	public int nbUses(){
		return this.uses.size();
	}

	public boolean isInExpr(soot.Value v){
		if(expression.getOp1().equivTo(v)){
			return true;
		}
		else if(expression.getOp2().equivTo(v)){
			return true;
		}
		else{
			return false;
		}
	}

	public Set<Value> extractVariables(){
		Set<Value> variables = new HashSet<>();
		if(expression.getOp1() instanceof Local){
			variables.add(expression.getOp1());
		}

		if(expression.getOp2() instanceof Local){
			variables.add(expression.getOp2());
		}

		return variables;
	}

	@Override
	public String toString(){
		return this.expression.toString();
	}

}