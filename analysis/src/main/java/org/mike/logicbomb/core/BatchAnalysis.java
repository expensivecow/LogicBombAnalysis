package org.mike.logicbomb.core;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.options.Options;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;

public class BatchAnalysis {
	/**
	 * Main body of the analysis
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Hello world");
		
		Analysis analysis = new Analysis();
		analysis.startAnalysis(args);
	}
	
	

}
