/* OCSEGen: Open Components and Systems Environment Generator
 * Copyright (c) <2002-2008> Oksana Tkachuk, Kansas State University.
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * For questions about the license, copyright, and software, contact 
 * Oksana Tkachuk at oksana.tkachuk@gmail.com
 */ 
package edu.ksu.cis.envgen.counterexample;

import java.util.*;
import java.io.*;

public class CoverageProcessor {
	public static void main(String[] args) {

		BufferedReader br = null;
		FileWriter file = null;
		File path = new File("cpd");
		String tempStr = "";
		boolean cetrace = false;
		String extStr = null;

		if (args.length < 3) {
			System.out
					.println("Need to supply name of the coverage file, thread id, and class name");
			System.exit(0);
		}

		try {
			path.mkdirs();
		} catch (Exception ex) {
		}

		String fileName = "cpf";
		try {
			file = new FileWriter("cpd/cpf");
		} catch (Exception ex) {
		}

		try {
			br = new BufferedReader(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			System.err.println("Could not open " + args[0]);
			System.exit(-1);
		}

		int threadId = 0;
		
		try{
		  threadId = Integer.parseInt(args[1]);
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		
		String className = args[2];
		System.out.println("Identifying coverage for thread : "+threadId+", for class: "+className);
		int totalBranches = 0;
		int coveredBranches = 0;
		
		while (tempStr != null) {
			try {
				tempStr = br.readLine();
			} catch (IOException e) {
			}

			// System.out.println(tempStr);

			if (tempStr != null) {
				if(tempStr.startsWith("     [java] Thread "+threadId)){
					if (tempStr.lastIndexOf(className) != -1){
						totalBranches++;
						if (tempStr.endsWith(": 0")) {
							//uncovered branch
						}
						else
							coveredBranches++;
						
						printf(file, tempStr + "\n");

					}
				}
			}
		}
		
		printf(file, "\nCovered branches: "+coveredBranches);
		printf(file, "\nTotal branches: "+totalBranches);

		try {
			file.close();
		} catch (Exception ex) {
		}
	}

	private static String extractActionName(String str) {
		StringTokenizer strTokenizer = new StringTokenizer(str);
		String first = strTokenizer.nextToken("@");
		String second = strTokenizer.nextToken(":");
		String extracted = strTokenizer.nextToken("\"");
		extracted = extracted.substring(2, extracted.length());
		return extracted;
	}

	private static void printf(FileWriter outStream, String string) {
		for (int i = 0; i < string.length(); i++) {
			try {
				outStream.write(string.charAt(i));
			} catch (Exception e) {
			}
		}
	}
}
