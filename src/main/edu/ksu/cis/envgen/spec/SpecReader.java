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
package edu.ksu.cis.envgen.spec;

import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Logger;


import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.spec.parser.*;

public class SpecReader extends AssumptionsAcquirer {
	
	/** Object that holds information about various symbol tables
	 * filled by the parser.
	 */
	//UserSpec userSpec;
	
	//ModuleInfo unit;
	
	ApplInfo applInfo;
	
	String specFileName;
	
	StringReader reader;

	Logger logger = Logger.getLogger("envgen.spec");
	
	
	public SpecReader(){
		
	}
	
	public SpecReader(String specFileName, ApplInfo info){
		assert info != null;
		this.specFileName = specFileName;
		//this.unit = info.getUnit();
		applInfo = info;
	}
	
	public SpecReader(StringReader reader, ApplInfo info){
		assert info != null;
		this.reader = reader;
		//this.unit = info.getUnit();
		applInfo = info;
	}

	public void setOptions(Properties properties){
		specFileName = properties.getProperty("specFileName");
		
		//TODO: finish
	}
	
	
	public void setSpecFileName(String specFileName){
		this.specFileName = specFileName;
	}
	
	public Assumptions acquireAssumptions(ApplInfo info){
		
		assert info != null;
		//this.unit = info.getUnit();
		UserSpec userSpec = null;
		boolean typeChecks;
		
		if (specFileName != null) {
			userSpec = readSpec(specFileName);
			TypeChecker typeChecker = new TypeChecker(userSpec, info);
			typeChecks = typeChecker.typeCheck();
			assert typeChecks;
		}
		
		else if (reader != null) {
			userSpec = readSpec(reader );

			TypeChecker typeChecker =
				new TypeChecker(userSpec, info);
			typeChecks = typeChecker.typeCheck();
			assert typeChecks;
		}
		
		else{
			logger.severe("No specification found");
			//TODO: may finish to handle universal/default spec
			//userSpec = new UserSpec();	
		}
		assert userSpec != null;
		return userSpec;
	}
 
	/**
	 * Reads a user specification.  Needs to be done after the tool discovered
	 * the interface of the system, so it can check the validity of propositions.
	 */
	public UserSpec readSpec(String specFileName) {
		//take specification from the user
		UserSpec userSpec = null;
		EnvGenParser parser = null;
		if (specFileName == null) {
			logger.info("Reading from standard input . . .");
			parser = new EnvGenParser(System.in);
		} else {
			logger.info("Reading from file " + specFileName + " . . .");
			try {
				parser =
					new EnvGenParser(new java.io.FileInputStream(specFileName));
				userSpec = EnvGenParser.getUserSpec();
				

			} catch (java.io.FileNotFoundException e) {
				logger.severe(
					"File " + specFileName + " not found.");
				assert false;
			}
		}
		try {
			EnvGenParser.CompilationUnit();

			logger.info("Environment assumptions parsed successfully.");

		} catch (ParseException e) {
			
			e.printStackTrace();
			logger.severe("Encountered errors during parse.");
			assert false;
			
		}
		
		return userSpec;
	}
	
	/**
	 * Reads a user specification.  Needs to be done after the tool discovered
	 * the interface of the system, so it can check the validity of propositions.
	 */
	public UserSpec readSpec(StringReader specReader) {
		//take specification from the user
		UserSpec userSpec = null;
		EnvGenParser parser = null;
		if (specReader == null) {
			System.out.println(
				"EnvGenParser:  Reading from standard input . . .");
			parser = new EnvGenParser(System.in);
		} else {
			logger.info("Reading from file " + specReader + " . . .");
			//try {
			parser = new EnvGenParser(specReader);
			userSpec = EnvGenParser.getUserSpec();
			

			//} catch (java.io.FileNotFoundException e) {
			//	EnvPrinter.error(
			//		"EnvGenParser:  specReader not found.");
			//}
		}
		try {
			EnvGenParser.CompilationUnit();

			logger.info("Environment assumptions parsed successfully.");

		} catch (ParseException e) {
			e.printStackTrace();
			logger.severe("Encountered errors during parse.");
			assert false;
		}
		
		return userSpec;
	}
	
}
