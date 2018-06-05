package org.mike.logicbomb.core;
import java.util.*;

import org.mike.logicbomb.components.Branch;
import org.mike.logicbomb.components.CBlock;
import org.mike.logicbomb.components.Condition;
import org.mike.logicbomb.components.Method;

import java.io.File;
import java.lang.StringBuilder;

import soot.*;
import soot.options.Options;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;
import soot.jimple.IfStmt;
import soot.jimple.ConditionExpr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.CallableStatement;

//Adapted from the Soot Android Instumentation tutorial
public class Analysis {
	private Connection conn;
	private int time;
	private ArrayList<Method> methods;
	private final String apkDir; 
	private final String androidJarDir;
	private final String dbPath;
	private final String dbUserCredentials;
	private final String apkName;
	private int isMalicious;

	public Analysis(String apkDir, String androidJarDir, String dbPath, String dbUserCredentials, int isMalicious) {
		methods = new ArrayList<Method>();
		this.apkDir = apkDir;
		this.androidJarDir = androidJarDir;
		
		this.dbPath = dbPath;
		this.dbUserCredentials = dbUserCredentials;
		
		this.isMalicious = isMalicious;
		
		apkName = (new File(apkDir)).getName();
		
		conn = null;
	}
	
	public void startAnalysis(String[] args) {
		//prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().process_multiple_dex();
		Options.v().allow_phantom_refs();
		Options.v().set_android_jars(androidJarDir);
		
		List<String> dir = new ArrayList<String>();
		dir.add(apkDir);
		Options.v().set_process_dir(dir);
		
		Options.v().setPhaseOption("jb.ne", "enabled:false");
		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().setPhaseOption("jb.lp", "unsplit-original-locals:true");
		Options.v().setPhaseOption("jb.lns", "only-stack-locals:true");
		Options.v().setPhaseOption("jb.ulp", "enabled:false");
		Options.v().setPhaseOption("jb.a", "enabled:false");
		Options.v().setPhaseOption("jb.ls", "enabled:true");
		
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

		//printOutput();
		saveOutput();
	}
	
	/**
	 * Helper method to explore a condition
	 * @param stmt - The IfStatement conditioning the condition
	 * @param start - The first block
	 * @param blocks - The block graph of the method
	 * @param body - The body of the method
	 * @param id - The number of the condition
	 */
	private CBlock exploreCondition(IfStmt stmt, Block start, BlockGraph blocks, Body body, int id) {
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
	 * Print the output in a file
	 * Is heavily based on the toString methods of the different objects
	 */
	private void printOutput() {
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

		System.out.println("Done.");
	}
	
	private void saveOutput() {
		System.out.println("Hello World");
		
		try {
		    conn = DriverManager.getConnection(dbPath + dbUserCredentials);

		    System.out.println("Connection Successful!");

		    // Save Application Row
		    CallableStatement createApplicationRow = conn.prepareCall("{call CREATE_APPLICATION(?, ?, ?, ?, ?, ?)}");
		    createApplicationRow.setString("applicationName", apkName);
		    createApplicationRow.setString("numMethods", Integer.toString(methods.size()));
		    createApplicationRow.setString("numMethodsWithCondUsed", Integer.toString(MethodsWithConditionUsed()));
		    createApplicationRow.setString("numBlocks", Integer.toString(totalBlocks()));
		    createApplicationRow.setString("numBlocksWithCondUsed", Integer.toString(BlocksWithConditionUsed()));
		    createApplicationRow.setString("isMalicious", Integer.toString(isMalicious));
		    
		    createApplicationRow.execute();
		    
		    for (Method m : methods) {
		    	if (!Objects.isNull(m)) {
		    		CallableStatement createMethodRow = conn.prepareCall("{call CREATE_METHOD(?, ?, ?, ?, ?)}");
			    	createMethodRow.setString("applicationName", apkName);
			    	createMethodRow.setString("methodName", m.getName());
			    	createMethodRow.setString("numDeclaredVarInMethod", Integer.toString(m.getDeclaredVariables()));
			    	createMethodRow.setString("numVarUsedInMethod", Integer.toString(m.getAllCondVar()));
			    	createMethodRow.setString("numBlocksWithCondVarUsed", Integer.toString(m.getNbBlocksWithUsedCondition()));
			    	
			    	createMethodRow.execute();
			    	
			    	int i = 1;
			    	for (CBlock c : m.getConditions()) {
				    	CallableStatement createCondBlockRow = conn.prepareCall("{call CREATE_COND_BLOCK(?, ?, ?, ?, ?, ?, ?)}");
				    	createCondBlockRow.setString("methodName", m.getName());
				    	createCondBlockRow.setString("condBlockName", "Block " + i);
				    	createCondBlockRow.setString("conditionName", c.getConditions().toString());
				    	createCondBlockRow.setString("numUnits", Integer.toString(c.getTotalUnits()));
				    	createCondBlockRow.setString("numAssignments", Integer.toString(c.getAssignments()));
				    	createCondBlockRow.setString("numCondVarUsages", Integer.toString((int) c.getCondUse()));
				    	createCondBlockRow.setString("numTotalUsages", Integer.toString(c.getUse()));
				    	
				    	createCondBlockRow.execute();

				    	int j = 1;
				    	for (Branch b : c.getBranches()) {
					    	CallableStatement createBranchRow = conn.prepareCall("{call CREATE_BRANCH(?, ?, ?, ?, ?, ?, ?, ?)}");
					    	createBranchRow.setString("methodName", m.getName());
					    	createBranchRow.setString("condBlockName", "Block " + i);
					    	createBranchRow.setString("branchName", "Branch " + j);
					    	createBranchRow.setString("numUnits", Integer.toString(b.getTotalUnits()));
					    	createBranchRow.setString("numCondVarUsages", Integer.toString((int) b.getCondVariableUses()));
					    	createBranchRow.setString("numNonCondVar", Integer.toString(b.getNbNonCondVariables()));
					    	createBranchRow.setString("numAssignments", Integer.toString(b.getAssignments()));
					    	createBranchRow.setString("numTotalVarUsages", Integer.toString((int) b.getTotalUse()));
					    	
					    	createBranchRow.execute();
				    		j++;
				    	}
			    	}
		    	}
		    }
		    
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
}