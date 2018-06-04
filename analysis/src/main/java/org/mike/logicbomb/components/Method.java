package org.mike.logicbomb.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mike.logicbomb.core.Analysis;
import org.mike.logicbomb.core.Analysis.CBlock;

import soot.Body;
import soot.Value;

public class Method {
	private final ArrayList<CBlock> conditions;
	private final String name;
	private final Body b;

	public Method(Body b){
		this.name = b.getMethod().getDeclaringClass().getName() + "." + b.getMethod().getName();
		this.conditions = new ArrayList<>();
		this.b = b;
	}

	public void addCBlock(CBlock b){
		this.conditions.add(b);
	}

	public Iterator<CBlock> conditionsIterator(){
		return this.conditions.iterator();
	}

	public int getNbBlocksWithUsedCondition(){
		int i = 0;
		for(CBlock c : conditions){
			if(c.getCondUse() > 0){
				i++;
			}
		}
		return i;
	}

	public int getAllCondVar(){
		Set<Value> vars = new HashSet<>();
		for(CBlock c : conditions){
			vars.addAll(c.getCondVariables());
		}
		return vars.size();
	}

	public String getName() {
		return name;
	}

	public int getTotalBlocks(){
		return conditions.size();
	}


	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.getName() + "\n");
		sb.append("["+this.b.getLocals().size()+","+getAllCondVar()+","+this.getNbBlocksWithUsedCondition()+"]\n");
		int j = 0;

		for (CBlock c : this.conditions) {
			j++;
			sb.append("\nExploring block "  + j + "\n");
			sb.append(c + "\n");
		}

		String s = sb.toString();
		return s;
	}
}