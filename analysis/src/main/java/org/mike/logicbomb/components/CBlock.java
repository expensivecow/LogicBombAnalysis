package org.mike.logicbomb.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import soot.Value;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;

/**
 * A conditional block
 */
public class CBlock{
	private final List<Condition> conditions;
	private final Set<Branch> branches;
	private boolean explored = false;

	//CONSTRUCTORS
	public CBlock(List<Condition> conditions, Set<Branch> branches){
		this.branches = branches;
		this.conditions = conditions;
	}

	public CBlock(List<Condition> conditions){
		this(conditions, new HashSet<Branch>());
	}

	public CBlock(){
		this(new ArrayList<Condition>(), new HashSet<Branch>());
	}

	//ADDERS
	public void addCondition(Condition c){
		this.conditions.add(c);
	}

	public void addBranch(Branch b){
		this.branches.add(b);
	}

	//GETTERS
	public int getNbBranches(){
		return branches.size();
	}

	public boolean containsBlock(Block block){

		for(Branch b : branches){
			if(b.containsBlock(block)){
				return true;
			}
		}

		return false;
	}

	public boolean blockInAllBranches(Block block){
		for(Branch b : branches){
			if(!b.containsBlock(block)){
				return false;
			}
		}

		return true;
	}

	public void removeFromAllBranches(Block block){
		for(Branch b : branches){
			b.removeBlock(block);
		}
	}

	public void printAllBranches(){
		for(Branch b : branches){
			System.out.println("\n" + b);
		}
	}

	/**
	 * Main method for exploration
	 * @param blocks
	 * @param conditional
	 */
	public void explore(BlockGraph blocks, Block conditional){

		//Explore the branches
		for(Branch branch : branches){
			branch.addBlock(conditional);
			Stack<Block> stack = new Stack<>();
			stack.push(branch.getStart());

			//DFS for now (might switch to BFS)
			while(!stack.empty()){
				Block current = stack.pop();
				if(!branch.containsBlock(current)){
					branch.addBlock(current);
					for(Block succ : blocks.getSuccsOf(current)){
						stack.push(succ);
					}

				}

			}
		}

		//Clean the blocks appearing in all branches, this happens if we are out of the condition
		for(Block b : blocks){
			if(blockInAllBranches(b)) {
				removeFromAllBranches(b);
			}
		}

		explored = true;
	}

	public void exploreBranches(){
		if(explored) {
			for (Branch b : branches) {
				b.explore();
			}

			//Empty branches are not considered
			branches.removeIf(b -> b.getTotalUnits() == 0);

		}
	}

	public void removeBranch(Branch b){
		this.branches.remove(b);
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Condition : ");
		sb.append(this.conditions);
		sb.append("\n");
		sb.append("Stats : [" + getTotalUnits() + " & " + getAssignments()+ " & " + getCondUse()/getTotalUnits() + " & " + getCondUse()/getUse()+"]");
		sb.append("\n");
		for(Branch b : branches){
			sb.append(b);
		}
		return sb.toString();
	}
	
	public Set<Branch> getBranches() {
		return branches;
	}
	
	public List<Condition> getConditions() {
		return this.conditions;
	}

	public int getAssignments(){
		int asst = 0;
		for(Branch b : branches){
			asst += b.getAssignments();
		}

		return asst;
	}

	public int getUse(){
		int use = 0;
		for(Branch b : branches){
			use += b.getTotalUse();
		}
		return (use==0)? 1: use;
	}

	public double getCondUse(){
		int use = 0;
		for(Branch b : branches){
			use += b.getCondVariableUses();
		}
		return (double)use;
	}

	public int getNonCondUse(){
		int use = 0;
		for(Branch b : branches){
			use += b.getNonCondVariableUses();
		}
		return use;
	}

	public int getTotalUnits(){
		int use = 0;
		for(Branch b : branches){
			use += b.getTotalUnits();
		}
		return (use == 0)? 1: use;
	}

	public Set<Value> getNonCondVariables(){
		Set<Value> values = new HashSet<>();
		for(Branch b : branches){
			values.addAll(b.getNonCondVariables());
		}
		return values;
	}

	public Set<Value> getCondVariables(){
		Set<Value> values = new HashSet<>();
		for(Branch b : branches){
			values.addAll(b.getConditionVariables());
		}
		return values;
	}


}