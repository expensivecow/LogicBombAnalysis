package org.mike.logicbomb.core;

import java.awt.List;
import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import com.mysql.jdbc.Connection;

import soot.G;
import soot.PackManager;

public class BatchAnalysis {
	private static String apkPoolDir = "/home/mike/Dev/School/Research/LogicBombs/apkPool";
	private static String androidJarPath = "/home/mike/Dev/Git/LogicBombAnalysis/android";
	private static String dbPath = "jdbc:mysql://localhost:3306/LOGIC_BOMB_ANALYSIS_DB?";
	private static String dbUserCredentials = "user=root&password=testtest";
	private static Connection conn = null;
	private static int numApplications = 0;
	/**
	 * Main body of the analysis
	 * @param args
	 */
	public static void main(String[] args) {
		//exploreDirectoryRecursively(args, new File(apkPoolDir), new File(androidJarPath));
		
		exploreDirectoryRecursively(args, new File(apkPoolDir), new File(androidJarPath));
		System.out.println("Finished Analyzing " + numApplications + " Applications.");
	}
	
	private static void exploreDirectoryRecursively(String[] args, File dir, File androidJarPath) {
		if (!androidJarPath.exists()) {
			System.out.println("ERR: ANDROID JAR PATH DOES NOT EXIST");
			return;
		}
		
		if (dir.exists()) {
		    for (final File file : dir.listFiles()) {
		        if (file.isDirectory()) {
		        	exploreDirectoryRecursively(args, file, androidJarPath);
		        } else {
		        	String fileName = file.getName();
		        	
		        	if (FilenameUtils.getExtension(fileName).equals("apk")) {
		        		System.out.print("Starting analysis on " + file.getAbsolutePath() + "...");
		        		Analysis analysis = new Analysis(file.getAbsolutePath(), androidJarPath.getAbsolutePath(),
		        				dbPath, dbUserCredentials, 1);
		        		
		        		analysis.startAnalysis(args);
		        		System.out.print(" done.\n");
		        		numApplications++;
		        	}
		        }
		    }
		}
		else {
			
		}
	}
	

}
