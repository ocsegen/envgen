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

public class CEProcessor {
	public static void main(String[] args) {

		BufferedReader br = null;
		FileWriter file = null;
		File path = new File("cepd");
		String tempStr = "";
		boolean cetrace = false;
		String extStr = null;

		if (args.length == 0) {
			System.out
					.println("Need to supply name of the counterexample file");
			System.exit(0);
		}

		try {
			path.mkdirs();
		} catch (Exception ex) {
		}

		String fileName = "cepf";
		try {
			file = new FileWriter("cepd/cepf");
		} catch (Exception ex) {
		}

		try {
			br = new BufferedReader(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			System.err.println("Could not open " + args[0]);
			System.exit(-1);
		}

		while (tempStr != null) {
			try {
				tempStr = br.readLine();
			} catch (IOException e) {
			}

			// System.out.println(tempStr);

			if (tempStr != null) {
				if (tempStr.lastIndexOf("path to error") != -1){
					System.out.println("found counterexample");
					cetrace = true;
				}
				if (cetrace && tempStr.lastIndexOf("@EnvDriverThread") != -1) {
					// process the string before printing it into the file
					extStr = extractActionName(tempStr);
					printf(file, extStr + "\n");

				}
			}
		}

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
