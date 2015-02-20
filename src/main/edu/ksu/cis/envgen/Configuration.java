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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import soot.*;
import soot.options.*;

import edu.ksu.cis.envgen.applinfo.*;
import edu.ksu.cis.envgen.applinfo.domain.*;
import edu.ksu.cis.envgen.codegen.*;
import edu.ksu.cis.envgen.util.*;

public class Configuration {

	protected Properties properties;
	
	protected ModuleInfo unit = new UnitInfo();
	protected ModuleInfo env = new EnvInfo();

	protected ApplInfo applInfo;
	protected InterfaceFinder intf;
	protected AssumptionsAcquirer aa;
	protected CodeGenerator cg;
	protected EnvPrinter ep;

	protected Map<String, Object> checkedInstanceCache = new HashMap<String, Object>();

	protected String specFileName;

	static Logger logger = Logger.getLogger("envgen");

	public Configuration(Properties properties) {
		this.properties = properties;
		initialize();
	}

	
	public void setApplInfo(ApplInfo applInfo){
		this.applInfo = applInfo;
	}
	
	public ApplInfo getApplInfo(){
		return applInfo;
	}
	
	public void setInterfaceFinder(InterfaceFinder intf) {
		this.intf = intf;
	}

	public InterfaceFinder getInterfaceFinder() {
		return intf;
	}

	public void setAssumptionsAcquirer(AssumptionsAcquirer aa) {
		this.aa = aa;
	}

	public AssumptionsAcquirer getAssumptionsAcquirer() {
		return aa;
	}

	public void setCodeGenerator(CodeGenerator cg) {
		this.cg = cg;
	}

	public CodeGenerator getCodeGenerator() {
		return cg;
	}

	public void setEnvPrinter(EnvPrinter ep) {
		this.ep = ep;
	}

	public EnvPrinter getEnvPrinter() {
		return ep;
	}

	public ModuleInfo getUnit() {
		return unit;
	}

	public ModuleInfo getEnv() {
		return env;
	}

	public void initialize() {

		setSootOptions();

		String unitStr = properties.getProperty("unit");
		loadUnit(unitStr);

		String pathNames = properties.getProperty("unitPath");
		loadUnitFromPath(pathNames);
		
		applInfo = (ApplInfo) createInstance(ApplInfo.class);
		if(applInfo == null){
			System.out.println("Instantiating default domain info");
			applInfo = new DefaultInfo();
		}
		
		intf = (InterfaceFinder) createInstance(InterfaceFinder.class);
		assert intf != null;
		intf.setOptions(properties);

		aa = (AssumptionsAcquirer) createInstance(AssumptionsAcquirer.class);

		if (aa != null)
			aa.setOptions(properties);

		cg = (CodeGenerator) createInstance(CodeGenerator.class);
		if(cg != null)
			cg.setOptions(properties);

		ep = (EnvPrinter) createInstance(EnvPrinter.class);
		// for now, we allow to use a default printer
		if (ep == null) {
			System.out.println("Instantiating a default printer");
			ep = new JavaPrinter();
		}
		ep.setOptions(properties);

	}

	protected Object createInstance(Class interfaceClass) {
		String name = interfaceClass.getName();
		System.out.println("Creating instance of: " + name);
		String className = properties.getProperty(name);

		if (className == null) {
			return null;
		} else {
			return createCheckedInstance(interfaceClass, className);
		}
	}

	protected Object createCheckedInstance(Class interfaceClass,
			String className) {
		System.out.println("Using ClassName: " + className);
		try {
			Object result = checkedInstanceCache.get(className);

			if (result != null) {
				if (!interfaceClass.isInstance(result)) {
					logger.severe("interface not implemented (1)");
					return null;
				} else {
					return result;
				}
			}

			result = Class.forName(className).newInstance();

			if (result != null) {
				checkedInstanceCache.put(className, result);
			}

			if (interfaceClass.isInstance(result)) {
				return result;
			} else {
				logger.severe("interface not implemented (2) " + className);
			}
		} catch (ClassNotFoundException cnfe) {
			logger.severe("class not found " + className);
		} catch (IllegalAccessException iae) {
			logger.severe("inaccesible extension module " + className);
		} catch (InstantiationException ie) {
			logger.severe("could not instantiate " + className);
		}

		return null;
	}

	public void loadUnit(String unitStr) {
		if (unitStr == null) {
			logger.warning("No unit provided");
			return;
		}
		
		//get the location of the libs
		String examplePath = getExampleClassPath();
		(Scene.v()).setSootClassPath(examplePath);

		StringTokenizer tokenizer = new StringTokenizer(unitStr);
		int length = tokenizer.countTokens();
		String className = null;
		for (int i = 0; i < length; i++) {
			className = tokenizer.nextToken();

			try {
				(Scene.v()).loadClassAndSupport(className);
			} catch (Exception e) {
				System.out.println(e);
			}
			logger.info("----------done loading class: " + className);
			SootClass internalClass = (Scene.v()).getSootClass(className);
			if (internalClass.isPhantom())
				logger.severe("Class " + className + " is a phantom class");
			internalClass.setApplicationClass();

			// put all loaded classes into one table -- unitTable
			unit.addClass(internalClass);
		}
	}

	public void loadUnitFromPath(String pathNames) {
		logger.info("processing paths:" + pathNames);
		if (pathNames == null) {
			logger.info("No path names provided");
			return;
		}

		StringTokenizer tokenizer = new StringTokenizer(pathNames);
		int length = tokenizer.countTokens();
		String pathName = null;
		for (int i = 0; i < length; i++) {
			pathName = tokenizer.nextToken();

			logger.info("pathName: " + pathName);
			// TODO: use SourceLocator directly
			// SourceLocator sourceLocator = SourceLocator.v();
			// List fileNames = sourceLocator.getClassesUnder(pathName);
			List<String> fileNames = getClassesUnder(pathName);
			logger.info("fileNames: " + fileNames);

			for (Iterator<String> fi = fileNames.iterator(); fi.hasNext();) {
				String fileName = fi.next();

				try {
					(Scene.v()).loadClassAndSupport(fileName);
				} catch (Exception e) {
					System.out.println(e);
				}
				logger.info("----------done loading class: " + fileName);
				SootClass internalClass = (Scene.v()).getSootClass(fileName);
				if (internalClass.isPhantom())
					logger.severe("Class " + fileName + " is a phantom class");
				internalClass.setApplicationClass();

				// put all loaded classes into one table -- unitTable
				unit.addClass(internalClass);
			}
		}
	}

	// TODO: taken from soot.SourceLocator
	public List<String> getClassesUnder(String aPath) {
		List<String> fileNames = new ArrayList<String>();

		if (isJar(aPath)) {
			List<String> inputExtensions = new ArrayList<String>(2);
			inputExtensions.add(".class");
			inputExtensions.add(".jimple");
			inputExtensions.add(".java");

			try {
				ZipFile archive = new ZipFile(aPath);
				for (Enumeration entries = archive.entries(); entries
						.hasMoreElements();) {
					ZipEntry entry = (ZipEntry) entries.nextElement();
					String entryName = entry.getName();
					int extensionIndex = entryName.lastIndexOf('.');
					if (extensionIndex >= 0) {
						String entryExtension = entryName
								.substring(extensionIndex);
						if (inputExtensions.contains(entryExtension)) {
							entryName = entryName.substring(0, extensionIndex);
							entryName = entryName.replace(
									java.io.File.separatorChar, '.');
							fileNames.add(entryName);
						}
					}
				}
			} catch (IOException e) {
				logger.warning("Error reading " + aPath + ": " + e.toString());
				throw new CompilationDeathException(
						CompilationDeathException.COMPILATION_ABORTED);
			}
		} else {
			File file = new File(aPath);

			File[] files = file.listFiles();
			if (files == null) {
				files = new File[1];
				files[0] = file;
			}

			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					List<String> l = getClassesUnder(aPath + File.separatorChar
							+ files[i].getName());
					Iterator<String> it = l.iterator();
					while (it.hasNext()) {
						String s = it.next();
						fileNames.add(files[i].getName() + "." + s);
					}
				} else {
					String fileName = files[i].getName();

					if (fileName.endsWith(".class")) {
						int index = fileName.lastIndexOf(".class");
						fileNames.add(fileName.substring(0, index));
					}

					if (fileName.endsWith(".jimple")) {
						int index = fileName.lastIndexOf(".jimple");
						fileNames.add(fileName.substring(0, index));
					}

					if (fileName.endsWith(".java")) {
						int index = fileName.lastIndexOf(".java");
						fileNames.add(fileName.substring(0, index));
					}
				}
			}
		}
		return fileNames;
	}

	private boolean isJar(String path) {
		File f = new File(path);
		if (f.isFile() && f.canRead()) {
			if (path.endsWith("zip") || path.endsWith("jar")) {
				logger.info("isJar: " + path);
				return true;
			} else {
				logger
						.warning("Warning: the following soot-classpath entry is not a supported archive file (must be .zip or .jar): "
								+ path);
			}
		}
		return false;
	}

	public String getExampleClassPath(){
		
		Properties props = System.getProperties();

		//for (Map.Entry<Object,Object> e : props.entrySet()){
		//	System.out.println(e.getKey() + " = " + e.getValue());
		//}
		
		String examplePath = props.getProperty("java.class.path");
		String libPath = props.getProperty("sun.boot.class.path");
		String sep = File.pathSeparator;
		examplePath = examplePath + sep;
		examplePath= examplePath + libPath;
		
		System.out.println("Example path: "+examplePath);
		return examplePath;
		
	}
	
	public void setSootOptions() {

		Options.v().set_full_resolver(true);

		// Options.v().set_verbose(true);

		String source = properties.getProperty("source");
		if (source != null && source.equals("java")) {
			// force to compile java
			// Options.v().set_src_prec(3);
			Options.v().set_src_prec(Options.src_prec_java);
		}

		List<String> excludedPackages = new ArrayList<String>();
		
		String ignoreAnalyzingStr = properties.getProperty("ignoreAnalyzing");
		if (ignoreAnalyzingStr != null) {
			List<String> ignoreAnalyzingList = Util.getTokenList(ignoreAnalyzingStr);
			excludedPackages.addAll(ignoreAnalyzingList);
		}
		//excludedPackages.add("java.");
		//excludedPackages.add("javax.");
		excludedPackages.add("sun.");
		excludedPackages.add("com.sun.");
		excludedPackages.add("com.ibm.");
		excludedPackages.add("org.xml.");
		excludedPackages.add("org.w3c.");
		//excludedPackages.add("org.apache.");

		Options.v().set_exclude(excludedPackages);

		// SPARK PTA requirements
		// Options.v().set_keep_line_number(true);
		// Options.v().set_whole_program(true);
		// Options.v().setPhaseOption("cg", "verbose:true");

		// CHA call graph requirements
		// Options.v().set_include_all(true);
		// Options.v().set_app(true);

	}

	// private static SootClass loadClass(String name, boolean main) {
	// SootClass c = Scene.v().loadClassAndSupport(name);
	// c.setApplicationClass();
	// if (main) Scene.v().setMainClass(c);
	// return c;
	// }

}
