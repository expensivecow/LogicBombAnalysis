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

import org.mike.logicbomb.units.Branch;
import org.mike.logicbomb.units.CBlock;
import org.mike.logicbomb.units.Condition;
import org.mike.logicbomb.units.Method;

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
}
