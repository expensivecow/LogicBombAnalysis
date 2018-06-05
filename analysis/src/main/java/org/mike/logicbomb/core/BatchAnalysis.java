package org.mike.logicbomb.core;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.mysql.jdbc.Connection;

public class BatchAnalysis {
	private static String apkPoolDir = "/home/mike/Dev/School/Research/LogicBombs/apkPool";
	private static String androidJarPath = "/home/mike/Dev/Git/LogicBombAnalysis/android";
	private static Connection conn = null;
	
	/**
	 * Main body of the analysis
	 * @param args
	 */
	public static void main(String[] args) {
		exploreDirectoryRecursively(new File(apkPoolDir), new File(androidJarPath));
		
		//"jdbc:mysql://localhost/test?" + "user=minty&password=greatsqldb"
		Analysis analysis = new Analysis("/home/mike/Dev/Git/Logic-Bomb-Condition-Analysis/apk/CalcA.apk", "/home/mike/Dev/Git/LogicBombAnalysis/android",
				"jdbc:mysql://localhost:3306/LOGIC_BOMB_ANALYSIS_DB?", "user=root&password=Winstonia132!", 0);
		analysis.startAnalysis(args);
	}
	
	private static void exploreDirectoryRecursively(File dir, File androidJarPath) {
		if (!androidJarPath.exists()) {
			System.out.println("ANDROID JAR PATH DOES NOT EXIST");
		}
		
		if (dir.exists()) {
		    for (final File file : dir.listFiles()) {
		        if (file.isDirectory()) {
		        	exploreDirectoryRecursively(file, androidJarPath);
		        } else {
		        	String fileName = file.getName();
		        	
		        	if (FilenameUtils.getExtension(fileName).equals("apk")) {
		        		//System.out.println(file.getAbsolutePath());	
		        	}
		        }
		    }
		}
		else {
			
		}
	}
	

}
