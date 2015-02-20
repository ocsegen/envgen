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
package edu.ksu.cis.envgen;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import edu.ksu.cis.envgen.EnvPrinter;
import edu.ksu.cis.envgen.applinfo.*;

/** Main class, takes a configuration.  */
public class EnvGenerator {

	static Logger logger = Logger.getLogger("envgen");
	
	public static void main(String[] args){
		
		//TODO: read default config
		/*
	    File defaultConfigFile = new File("envgen-default.properties");
		Properties defaultProperties = new Properties();
		try{
			FileInputStream fis = new FileInputStream(defaultConfigFile);
			defaultProperties.load(fis);

		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Supply envgen-default.properties file");
			return;
			
		}
		*/
		
		long start = System.currentTimeMillis();
		
		
		Properties properties = processUserInput(null, args); 

		Configuration configuration = new Configuration(properties);
		

		ModuleInfo unit = configuration.getUnit();
		ModuleInfo env = configuration.getEnv();
		ApplInfo applInfo = configuration.getApplInfo();
		applInfo.setUnit(unit);
		applInfo.setEnv(env);
		
		InterfaceFinder intf = configuration.getInterfaceFinder();
		
		long myStart = System.currentTimeMillis();
		
		//TODO: document that info is filled here
		ApplInfo info = intf.findInterface(applInfo);
		
		long myAnalysisStart = System.currentTimeMillis();
		
		
		AssumptionsAcquirer aa = configuration.getAssumptionsAcquirer();
		Assumptions assumptions = null;
		if(aa != null){
			assumptions = aa.acquireAssumptions(info);
		}
		//TODO: may combine different approaches supplied as a list (?)

		long myAnalysisFinish = System.currentTimeMillis();
		
		CodeGenerator cg = configuration.getCodeGenerator();
		
		// discover the interface of the unit
		// construct concrete classes that extend abstract classes of the unit
		// construct concrete classes that implement interfaces in the unit
		

		//TODO: document that genCode modifies env
		//code gen optional
		if(cg!=null){
			cg.genCode(info, assumptions);
		
		
			EnvPrinter printer = configuration.getEnvPrinter();
		
			printer.printfEnv(info);
		}
		
		long finish = System.currentTimeMillis();
		long execTime = finish - start;
		long myExecTime = finish - myStart;
		long findInterfaceTime = myAnalysisStart - myStart;
		//long analysisTime = myAnalysisFinish - myStart;
		long analysisTime = myAnalysisFinish - myAnalysisStart;
        logger.info("\nWhole program exec time: " + execTime);
		logger.info("\nOCSEGen exec time: " + myExecTime);
		logger.info("\nFind Interface time: " + findInterfaceTime);
		logger.info("\nAnalysis time: " + analysisTime);
		
		logger.info("----------------------Done");
		
	}
	
	/**
	 * Processes command line input.
	 */
	//TODO: extend to read key=value options from command line
	public static Properties processUserInput(Properties defaultProperties, String[] args) {
		String configFileName = null;
		if (args.length == 0) {
			
			printHelp(defaultProperties);
			
		}
		if (args.length == 1
			&& (args[0].equals("-h") || args[0].equals("-help"))) {
			printHelp(defaultProperties);
			
		}
		String arg = args[0];

		if (arg.equals("-c") || arg.equals("-config")) {
			if (args.length == 1)
				logger.severe("Please supply config file");
			configFileName = args[1];

			
		} else{
			
			printHelp(defaultProperties);
			
		}
		
		//read user config
        File configFile = new File(configFileName);
		Properties properties = new Properties();
		
		try{
			FileInputStream fis = new FileInputStream(configFile);
			properties.load(fis);

		}catch(Exception e){
			e.printStackTrace();
			logger.severe("Please supply config file");
			System.exit(0);
		}
		
		
		//TODO: combine default props and user props
		
		return properties;
	}
	

	
	public static void showUsage() {
		    System.out.println("Usage: java [<vm-option>..] edu.ksu.cis.envgen.EnvGenerator [<envgen-option>..]");
		    System.out.println("       <envgen-option> : ");
		    System.out.println("       -c <config-file>  : name of config properties file");
		    System.out.println("       -help  : print usage information");
		    //System.out.println("       | +<key>=<value>  : add or override key/value pair to config dictionary (not supported yet)");
		  }
	
	
	
	public static void printHelp(Properties properties){
		showUsage();
		/*
		System.out.println("default envgen options:");
		
		String key;
		String value;
		//String desc;
		for(Enumeration keys = properties.propertyNames(); keys.hasMoreElements();){
			key = (String)keys.nextElement();
			value = properties.getProperty(key);
			//desc = null;
			//desc = properties.getProperty(key+".desc");
			System.out.println(key+" = "+value);
			//if(desc != null)
			//	System.out.println("  -"+desc);
			
		}
		*/
		System.out.println("\nFor info about the options, look into envgen-default.properties");
		System.exit(0);
	}
}
