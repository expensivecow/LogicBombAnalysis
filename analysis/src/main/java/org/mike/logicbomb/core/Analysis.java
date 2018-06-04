package org.mike.logicbomb.core;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.mike.logicbomb.components.Branch;
import org.mike.logicbomb.components.Method;
import org.mike.logicbomb.components.Usage;
import org.mike.logicbomb.core.Analysis.CBlock;

import java.lang.StringBuilder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import soot.*;
import soot.options.Options;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

import soot.jimple.Jimple;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.ConditionExpr;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.UnopExpr;
import soot.jimple.Ref;

//Adapted from the Soot Android Instumentation tutorial
public class Analysis {
	private int time;
	private ArrayList<Method> methods;

	public Analysis() {
		methods = new ArrayList<Method>();
	}
	
	public void startAnalysis(String[] args) {
		//prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);
		
		// resolve the PrintStream and System soot-classes
		Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);

		time = 0;

		//Main transform
		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
					Method m = new Method(b);
					BriefBlockGraph blocks = new BriefBlockGraph(b);
					Iterator<Block> iter = blocks.iterator();

					//The first pass is based on the basic block graph
					while (iter.hasNext()) {
						Block current = iter.next();

						Unit tail = current.getTail();

						//Need to add support for switch statements
						if (tail instanceof IfStmt) {
							time++;

							// 1. Define the boundaries of the blocks and branches (using the basic block graph)
							CBlock cBlock = exploreCondition((IfStmt) tail, current, blocks, b, time);

							// 2. Explore the unit graph of every branch to complete the stats
							cBlock.exploreBranches();

							// 3. Add the conditional block to the collection
							m.addCBlock(cBlock);
						}

					}

					methods.add(m);
				}
			//}
		}));

		// run soot logic
		soot.Main.main(args);

		printOutput();
	}
	
	/**
	 * Helper method to explore a condition
	 * @param stmt - The IfStatement conditioning the condition
	 * @param start - The first block
	 * @param blocks - The block graph of the method
	 * @param body - The body of the method
	 * @param id - The number of the condition
	 */
	private CBlock exploreCondition(IfStmt stmt, Block start, BlockGraph blocks, Body body, int id){

		CBlock cond_block = new CBlock();
		Condition cond = new Condition((ConditionExpr) stmt.getCondition());
		ArrayList<Condition> conditions = new ArrayList<>();
		conditions.add(cond);
		cond_block.addCondition(cond);

		int i = 0;
		for(Block block : blocks.getSuccsOf(start)){
			Branch branch = new Branch(id + "-" + i, block, conditions);
			cond_block.addBranch(branch);
			i++;
		}

		cond_block.explore(blocks,start);

		return cond_block;

	}

	/**
	 * Computes the intersection between two lists
	 * @param list1 - The first list
	 * @param list2 - The second list
	 * @param <T> The type of the list
	 * @return The intersection between the two lists
	 */
	private <T> List<T> intersection(List<T> list1, List<T> list2) {
		List<T> list = new ArrayList<T>();


		for (T t : list1) {
			if(list2.contains(t)) {
				list.add(t);
			}
		}

		return list;
	}

	/**
	 * Print the output in a file
	 * Is haevily based on the toString methods of the different objects
	 */
	private void printOutput(){
		System.out.println("IF statement encountered : " + time);
		System.out.println("APK Stats : " + MethodsWithConditionUsed() + " Methods with used conditions (" + (double)MethodsWithConditionUsed()/methods.size()+")");
		System.out.println(" for a total of " + BlocksWithConditionUsed() + " Blocks with condition used (" + (double)BlocksWithConditionUsed()/totalBlocks()+")");
		System.out.println("The methods are : \n"+ methodListWithConditionUse());
		System.out.println("exploration : branch [# of units, # of cond. var. used, # of vars][defs,use,cond/tot,cond/use]");
		
		for(Method m : methods) {
			if(!Objects.isNull(m)) {
				System.out.println("=========================================");
				System.out.println(m.toString());
			}
		}

		System.out.println("Done");
	}

	private String methodListWithConditionUse(){
		StringBuilder sb = new StringBuilder();

		for(Method m : methods){
			if(!Objects.isNull(m) && m.getNbBlocksWithUsedCondition() > 0){
				sb.append(m.getName() + " (" + m.getNbBlocksWithUsedCondition() + ")\n");
			}
		}

		return sb.toString();
	}

	private int MethodsWithConditionUsed(){
		int i = 0;
		for(Method m : methods){
			if(!Objects.isNull(m)) {
				if (m.getNbBlocksWithUsedCondition() > 0) {
					i++;
				}
			}else{
				System.out.println("NULL");
			}
		}

		return i;
	}

	private int BlocksWithConditionUsed(){
		int i = 0;
		for(Method m : methods){
			if(!Objects.isNull(m)) {
				i += m.getNbBlocksWithUsedCondition();
			}
		}
		return i;
	}

	private int totalBlocks(){
		int i = 0;
		for(Method m : methods){
			if(!Objects.isNull(m)) {
				i += m.getTotalBlocks();
			}
		}
		return i;
	}

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
}