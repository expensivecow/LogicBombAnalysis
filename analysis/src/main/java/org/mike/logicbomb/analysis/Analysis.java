package org.mike.logicbomb.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.JastAddJ.IfStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.Ref;
import soot.jimple.UnopExpr;
import soot.options.Options;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;

public class Analysis {
	private static int time;
	private static ArrayList<Method> methods = new ArrayList<>();
	private static String name;
	private static String version = "1.2";

	/**
	 * Main body of the analysis
	 * @param args
	 */
	public static void main(String[] args) {

		//GET THE NAME FROM THE ARG LIST
		List<String> argList = Arrays.asList(args);
		String fullPath = argList.get(argList.indexOf("-process-dir")+1);
		String[] dirs = fullPath.split("/");
		name = dirs[dirs.length -1].replace(".apk","");

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
			//if(b.getMethod().getName().equals("handleOperation")) {

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
	private static CBlock exploreCondition(IfStmt stmt, Block start, BlockGraph blocks, Body body, int id){

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
	private static <T> List<T> intersection(List<T> list1, List<T> list2) {
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
	private static void printOutput(){

		String outputLocation = "";

		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
			Date date = new Date();

			String fileName = "analysis_"+dateFormat.format(date)+".txt";

			outputLocation = "sootOutput/"+name+"/"+fileName;

			/*File dir = new File("sootOutput/"+name);
			dir.mkdirs();*/

			File file = new File(outputLocation);
			file.getParentFile().mkdirs();


			if (file.createNewFile()){
				System.out.println("New output file created : " + outputLocation);
			}else{
				System.out.println("Output File " + outputLocation + " already exists. Content will be overwritten, sorry if this wasn't planned");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputLocation))) {

			String title = "Logic Bomb Condition Analysis\nVersion +" + version + "\n";

			bw.write(title);
			bw.write("IF statement encountered : " + time + "\n");
			bw.write("APK Stats : " + MethodsWithConditionUsed() + " Methods with used conditions (" + (double)MethodsWithConditionUsed()/methods.size()+")");
			bw.write(" for a total of " + BlocksWithConditionUsed() + " Blocks with condition used (" + (double)BlocksWithConditionUsed()/totalBlocks()+")\n");
			bw.write("The methods are : \n"+ methodListWithConditionUse() + "\n");
			bw.write("exploration : branch [# of units, # of cond. var. used, # of vars][defs,use,cond/tot,cond/use]\n");
			int i = 0;
			for(Method m : methods) {
				if(!Objects.isNull(m)) {
					i++;
					bw.write("=========================================\n");
					bw.write(m.toString());
				}
			}

			System.out.println("Done, output file : " + outputLocation);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static String methodListWithConditionUse(){
		StringBuilder sb = new StringBuilder();

		for(Method m : methods){
			if(!Objects.isNull(m) && m.getNbBlocksWithUsedCondition() > 0){
				sb.append(m.getName() + " (" + m.getNbBlocksWithUsedCondition() + ")\n");
			}
		}

		return sb.toString();
	}

	private static int MethodsWithConditionUsed(){
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

	private static int BlocksWithConditionUsed(){
		int i = 0;
		for(Method m : methods){
			if(!Objects.isNull(m)) {
				i += m.getNbBlocksWithUsedCondition();
			}
		}
		return i;
	}

	private static int totalBlocks(){
		int i = 0;
		for(Method m : methods){
			if(!Objects.isNull(m)) {
				i += m.getTotalBlocks();
			}
		}
		return i;
	}

    private static Local addTmpRef(Body body)
    {
        Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
        body.getLocals().add(tmpRef);
        return tmpRef;
    }
    
    private static Local addTmpString(Body body)
    {
        Local tmpString = Jimple.v().newLocal("tmpString", RefType.v("java.lang.String")); 
        body.getLocals().add(tmpString);
        return tmpString;
    }

	/**
	 * A conditional block
	 */
	public static class CBlock{
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

	public static class Condition {
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

	public static class Truple{
		private int a,b,c;

		public Truple(int a, int b, int c){
			this.a = a;
			this.b = b;
			this.c = c;
		}

		public int a(){
			return a;
		}

		public int b(){
			return b;
		}

		public int c(){
			return c;
		}

		public void setA(int v){
			this.a = v;
		}

		public void setB(int v){
			this.b = v;
		}

		public void setC(int v){
			this.c = v;
		}

		public void incA(){
			this.a ++;
		}

		public void incB(){
			this.b ++;
		}

		public void incC(){
			this.c ++;
		}
	}

	/**
	 * Abstraction of a Branch
	 */
	public static class Branch {
		private final List<Condition> predicates;
		private final HashMap<Condition, List<Usage>> variableUses;
		private final HashMap<Value, Truple> variables;
		private final Block start;
		private final Set<Block> blocks;
		private final Set<Value> conditionVariables;
		private final String name;
		private int totalUnits;
		private int assignments;

		public Branch(String name, Block start, List<Condition> predicates){
			this.name = name;
			this.start = start;
			this.totalUnits = 0;
			this.variableUses = new HashMap<>();
			this.predicates = new ArrayList<>(predicates);
			this.blocks = new HashSet<>();
			this.variables = new HashMap<>();
			this.assignments = 0;
			this.conditionVariables = new HashSet<>();
		}

		public Branch(String name, Block start){
			this(name, start,new ArrayList<>());
		}

		private void addCondUsage(Condition c, Usage u){
			if(!variables.keySet().contains(c)){
				variableUses.put(c,new ArrayList<>());
			}
			variableUses.get(c).add(u);
		}

		public void explore(){

			initCondVariables();
			int totalUnitNumber = 0;
			for(Block b : blocks){
				for(Unit u : b){

					//System.out.println("UNIT : "+ u + "\nDEF : " + u.getDefBoxes() + "\nUSE : " + u.getUseBoxes() +"\n");

					totalUnitNumber ++;
					//Need to consider every possible type

					//Definition Stmt also includes IdentityStmt and AssignStmt
					if(u instanceof DefinitionStmt){
						Value left = ((soot.jimple.DefinitionStmt)u).getLeftOp();
						Value right = ((soot.jimple.DefinitionStmt)u).getRightOp();

						//If the left side was never used, we have a new variable
						boolean leftIsCondVar = false;
						for(Condition c : predicates){
							if(c.isInExpr(left)){
								leftIsCondVar = true;
							}
						}

						if(!variables.keySet().contains(left) && !leftIsCondVar) {
							variables.put(left, new Truple(0,0,0));
						}
						else{
							assignments ++;
						}

						//Right is a local i.e a single variable
						if(right instanceof Local){
							exploreHelperOneValue(right);
						}
						//Right is a Binop. Expression, need to analyse both sides
						else if(right instanceof BinopExpr){
							BinopExpr expr = (BinopExpr)right;
							exploreHelperOneValue(expr.getOp1());
							exploreHelperOneValue(expr.getOp2());
						}
						//Right is a cast, only one op to check
						else if(right instanceof CastExpr){
							exploreHelperOneValue(((CastExpr)right).getOp());
						}
						//Right is a Unop, only one op to check
						else if(right instanceof UnopExpr){
							exploreHelperOneValue(((UnopExpr)right).getOp());
						}
						//Right is an Invoke, check the arguments of the invoke
						else if(right instanceof InvokeExpr){
							for(Value v : ((InvokeExpr)right).getArgs()){
								exploreHelperOneValue(v);
							}
						}
						//Right is a Reference, check if the reference is in the list of variables
						else if(right instanceof Ref){
							exploreHelperOneValue(right);
						}

					}
					//Invoke stmt -> Method call
					else if(u instanceof InvokeStmt){
						InvokeExpr ie = ((InvokeStmt)u).getInvokeExpr();
						List<Value> arguments = ie.getArgs();

						for(Value v : arguments) {
							for (Condition c : predicates) {
								if (c.isInExpr(v)) {
									Usage usage = new Usage(c, v);
									if(!variables.keySet().contains(c)){
										variableUses.put(c,new ArrayList<>());
									}
									variableUses.get(c).add(usage);
								}
							}
							for (Value k : variables.keySet()) {
								if (v.equivTo(k)) {
									variables.get(k).incB();
								}
							}
						}
					}
				}
			}

			this.totalUnits = totalUnitNumber;
		}

		private void exploreHelperOneValue(Value val){
			for(Condition c : predicates){
				if(c.isInExpr(val)){
					Usage usage = new Usage(c,val);
					addCondUsage(c, usage);
				}
			}

			boolean found = false;
			for(Value v : variables.keySet()){
				if(val.equivTo(v)){
					variables.get(v).incB();
					found = true;
				}
			}
			if(!found){
				variables.put(val, new Truple(1,0,0));
			}
		}

		public void addBlock(Block b){
			this.blocks.add(b);
		}

		public boolean containsBlock(Block b){
			return blocks.contains(b);
		}

		public int getTotalUnits(){
			return this.totalUnits;
		}

		public Set<Block> getBlocks() {
			return blocks;
		}

		public void removeBlock(Block block){
			blocks.remove(block);
		}

		public void initCondVariables(){
			for(Condition c : predicates){
				conditionVariables.addAll(c.extractVariables());
			}
		}

		public Set<Value> getConditionVariables(){
			return this.conditionVariables;
		}

		public int getAssignments(){
			return assignments;
		}

		public double getTotalUse(){
			return getCondVariableUses() + getNonCondVariableUses();
		}

		public Block getStart(){
			return start;
		}

		public double getCondVariableUses(){
			int size = 0;
			for(List<Usage> uses : variableUses.values()){
				size += uses.size();
			}
			return (double)size;
		}

		public int getNonCondVariableUses(){
			int c = 0;
			for(Truple i : variables.values()){
				c += i.b();
			}
			return c;
		}

		public int getNbNonCondVariables(){
			return variables.keySet().size();
		}

		public Set<Value> getNonCondVariables(){
			return variables.keySet();
		}

		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("Branch " + name + " [");
			sb.append(this.getTotalUnits() + " & ");
			sb.append(this.getCondVariableUses() + " & ");
			sb.append(this.getNbNonCondVariables() + " & ");
			sb.append(this.assignments+" & ");
			sb.append(this.getNonCondVariableUses() + this.getCondVariableUses() + " & ");
			sb.append(this.getCondVariableUses()/((this.getTotalUnits() == 0)? 1 : this.getTotalUnits())+ " & ");
			sb.append(this.getCondVariableUses()/((this.getTotalUse()==0)? 1 : this.getTotalUse())+" \\ ]\n");

			return sb.toString();
		}

	}

	public static class Usage {
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

	public static class Method {
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

}
