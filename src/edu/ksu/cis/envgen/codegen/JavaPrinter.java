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
package edu.ksu.cis.envgen.codegen;

import java.util.*;
import java.util.logging.*;
import java.io.*;

import soot.*;
import soot.util.*;

import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.applinfo.*;
import edu.ksu.cis.envgen.codegen.ast.*;
import edu.ksu.cis.envgen.codegen.ast.expr.*;
import edu.ksu.cis.envgen.codegen.vals.*;
import edu.ksu.cis.envgen.util.Util;

/** 
 * Class used for printing of
 * environment classes into their separate files.
 *
 */
public class JavaPrinter extends EnvPrinter{

	ModuleInfo unit;
	
	ModuleInfo env;
	
	EnvHierarchy envHierarchy;

	String packageName = "";
	String outputDirName = "";

	/** Set to use identify the framework to be used with the generated program (OCSEGen: Open Components and Systems or JPF).  By default, JPF is used. */
	String context = "Verify";
	
	/**  Forces to generate TOP values as method calls, to support 
	 * symbolic execution tools that can't see final static fields TOP. */
	boolean supportSymValues;
	
	/** Prints choice modeling primitives, supported by model checking */
	boolean supportChoiceValues;
	
	//boolean supportSingletonValues = false; //default
	
	boolean unitAnalysis;
	
	String notModeled;
	
	Logger logger = Logger.getLogger("envgen.codegen");
	
	public JavaPrinter(){
		
	}

	
	public void setOptions(Properties properties){
		//TODO: recheck/finish options
		
		String contextStr = properties.getProperty("context");
		if(contextStr != null)
			context = contextStr;
		
		String supportSymValuesStr = properties.getProperty("supportSymValues");
		String supportChoiceValuesStr = properties.getProperty("supportChoiceValues");
		//String supportSingletonValuesStr = properties.getProperty("supportSingletonValues");
		
		if(supportSymValuesStr != null)
			supportSymValues = Boolean.valueOf(supportSymValuesStr);
		if(supportChoiceValuesStr != null)
			supportChoiceValues = Boolean.valueOf(supportChoiceValuesStr);
		//if(supportSingletonValuesStr != null)
		//	supportSingletonValues = Boolean.valueOf(supportSingletonValuesStr);

		
		String unitAnalysisStr = properties.getProperty("unitAnalysis");
		if(unitAnalysisStr != null)
			unitAnalysis = Boolean.valueOf(unitAnalysisStr);

		String notModeledStr = properties.getProperty("ignoreModeling");
		if(notModeledStr != null)
			notModeled = notModeledStr;
		
		String outputDirNameStr = properties.getProperty("outputDirName");
		if(outputDirNameStr != null)
			outputDirName = outputDirNameStr;
		//adjust the dir name	
        if(outputDirName != "") 
			if (!outputDirName.endsWith("/"))
				outputDirName = outputDirName + File.separator;
	    //logger.info("output dir name:"+outputDirName);

        //adjust the package name	 
		String packageNameStr = properties.getProperty("packageName");
		if(packageNameStr != null)
			packageName = packageNameStr;
       if(packageName != "") 
       		if (!packageName.endsWith("."))
       			packageName = packageName + ".";
		
	   logger.fine("finish implementation");
	}
	
	protected void initialize(ApplInfo info) {
		assert info!=null;
		this.unit = info.getUnit();
		this.env = info.getEnv();
		this.envHierarchy = info.getEnvHierarchy();
	}

	public void printfEnv(ApplInfo info){
		//assert(env!=null)
		initialize(info);
		printfTable(env.getClasses());
	}
	
	/**
	 *                                                         
	 * Prints soot classes of <code>markedTable</code> to their files.                                
	 *                                                         
	 */
	public void printfTable(Collection classes) {
		assert(classes != null);
		SootClass markedClass;
		String className;

		FileWriter file = null;
		File path = null;
		for (Iterator it = classes.iterator(); it.hasNext();) {

			markedClass = (SootClass) it.next();

			className = getActualName(markedClass.getName());

			//List methodsList = markedClass.getMethods();
			//Iterator mi = methodsList.iterator();

			path = new File(getFullPathName(className));
			try {
				path.mkdirs();
			} catch (Exception e) {
				logger.severe("Could not create directories");
				e.printStackTrace();
				
			}
			String fileName = outputDirName + (packageName + className).replace(".", File.separator);
			
			try {
				file = new FileWriter(fileName + ".java");
			} catch (Exception e) {
				logger.severe("Could not create file");
				e.printStackTrace();
			}
			if (!markedClass.isInterface())
				printClass(file, markedClass);
			else
				printInterface(file, markedClass);
			try {
				file.close();
			} catch (Exception e) {
				logger.severe("could not close file");
				e.printStackTrace();
			}
			logger.info("Generated: "+fileName +".java");

		}
		logger.info("Num classes generated: "+ classes.size());
	}

	/**
	 *                                                         
	 * Prints string to a file.                          
	 *                                                         
	 */
	protected void printf(FileWriter outStream, String string) {
		for (int i = 0; i < string.length(); i++) {
			try {
				outStream.write(string.charAt(i));
			} catch (Exception e) {
				logger.severe("problems printing into a file");
				e.printStackTrace();
			}
		}
	}

	/**
	 *                                                         
	 * Prints <code>sc</code> class to a <code>file</code>.                                    
	 *                                                         
	 */
	//this is for classes which are not interfaces
	protected void printClass(FileWriter file, SootClass sc) {

		String className = getActualName(sc.getName());

		if (getPathName(className).length() != 0)
			printf(file, "package " + getPackageName(className) + ";\n");

		printImports(file);

		if (sc.getModifiers() != 0)
			printf(file, Modifier.toString(sc.getModifiers()) + " ");
		printf(file, "class ");
		printf(file, getShortName(className) + " ");
		if (sc.hasSuperclass()) {
			SootClass parent = sc.getSuperclass();
			String parentName = getActualName(parent.getName());
			if (!parentName.equals("java.lang.Object"))
				printf(
					file,
					"extends " + getPackageAdjustedType(parentName) + " ");

		}
		if (sc.getInterfaceCount() > 0) {
			Chain interfaces = sc.getInterfaces();

			Iterator ii = interfaces.iterator();
			SootClass interfc;
			String interfcName;

			for (int i = 0; ii.hasNext();) {
				interfc = (SootClass) ii.next();
				interfcName = getActualName(interfc.getName());

				if (!unit.containsClass(interfcName)
					&& !env.containsClass(interfcName))
					continue;
				if (i != 0)
					printf(file, ",");
				else
					printf(file, "implements ");
				printf(file, getPackageAdjustedType(interfcName) + " ");
				i++;
			}
		}
		printf(file, "{");
		//print body of the class
		printFields(file, sc);
		printf(file, "\n");
		printMethods(file, sc);
		printf(file, "\n}");
	}

	/**
	 *                                                         
	 * Prints information about interfaces of <code>sc</code> to a file.              
	 *                                                         
	 */
	protected void printInterface(FileWriter file, SootClass sc) {
		SootClass interfc;
		
		String className = getActualName(sc.getName());
		String interfcName = null;

		if (getPathName(className).length() != 0)
			printf(file, "package " + getPackageName(className) + ";\n");
		
		printImports(file);
		
		printf(file, Modifier.toString(sc.getModifiers()) + " ");
		printf(file, getShortName(className) + " ");
		if (sc.getInterfaceCount() > 0) {
			Chain interfaces = sc.getInterfaces();
			Iterator ii = interfaces.iterator();
			for (int i = 0; ii.hasNext();) {
				interfc = (SootClass) ii.next();
				interfcName = getActualName(interfc.getName());
				if (!unit.containsClass(interfcName)
					&& !env.containsClass(interfcName))
					continue;
				if (i != 0)
					printf(file, ",");
				else
					printf(file, "extends ");
				interfcName = getActualName(interfc.getName());
				printf(file, getPackageAdjustedType(interfcName) + " ");
				i++;
			}

		}
		printf(file, "{");
		//print body of the class
		printFields(file, sc);
		printf(file, "\n");
		printMethods(file, sc);
		printf(file, "\n}");
	}
	
	protected void printImports(FileWriter file) {
		//if(sc.getName().startsWith("EnvDriverThread")){
		//any env class may use Verify or OCSEGen: Open Components and Systems choose methods
		if (context.equals("Verify"))
			printf(file, "import gov.nasa.jpf.jvm.Verify;\n");
		else
			//printf(file, "import edu.ksu.cis.OCSEGen: Open Components and Systems.OCSEGen: Open Components and Systems;\n\n");
			printf(file, "import edu.ksu.cis.OCSEGen: Open Components and Systems.Verify;\n");
		// }

		//if(!className.equals("EnvDriver"))
		//by default include in all the environment files
		//(?) may want to filter out and omit in files
		//where Abstraction class is not used
		printf(file, "import edu.ksu.cis.OCSEGen: Open Components and Systems.Abstraction;\n\n");
	}
	

	/**
	 *                                                         
	 * Prints fields of <code>sc</code> to a file.                                 
	 *                                                         
	 */

	protected void printFields(FileWriter file, SootClass sc) {
		SootField markedField;
		//if (debug > 1)
		//	System.out.println("printFields");
		Chain fields = sc.getFields();
		Iterator fi = fields.iterator();
		Type fieldType;
		String fieldTypeName;
		String fieldName;
		while (fi.hasNext()) {
			markedField = (SootField) fi.next();
			fieldType = markedField.getType();
			fieldTypeName = getPackageAdjustedType(fieldType);
			fieldName = markedField.getName();
			printf(file, "\n");

			printf(
				file,
				"  " + Modifier.toString(markedField.getModifiers()) + " ");
			//if(fieldType instanceof RefType)
			//  printf(file, dirName);
			//printf(file, fieldType + " "+fieldName);
			printf(file, fieldTypeName + " " + fieldName);
			
			if(markedField.isFinal()){
				logger.info("printing constant:"+markedField);
				//get its const value
				//String fieldSig = markedField.getSignature();
				Value constVal = env.getConstantValue(markedField);
				if(constVal != null){
					printf(file, " = " + constVal.toString());
					printf(file, ";");
					continue;
				}
				
				//logger.warning("Didn't find const val/source for: "+fieldSig);				
			}

			if (markedField.isStatic()) {
				if (fieldName.equals("TOP")) {
					//TODO: invoking a default constructor
					SootMethod constructor = Util.getConstructor(sc);
					if (constructor != null) {

						//  SootMethod constructor = sc.getMethodByName("<init>");
						String params =
							printTopArgs(
								constructor.getParameterTypes());
						printf(file, " = new " + fieldTypeName + params);

					} else {
						printf(file, " = new " + fieldTypeName + "()");
					}

				} else
					printf(file, " = " + printTopValue(fieldType));
			}
			printf(file, ";");
		}
	}


	/**
	 *                                                         
	 * Prints methods of <code>sc</code> to  <code>file</code>.                                  
	 *                                                         
	 */
	protected void printMethods(FileWriter file, SootClass sc) {
		SootMethod markedMethod;
		String methodName;
		List methods = sc.getMethods();
		Iterator mi = methods.iterator();
		while (mi.hasNext()) {
			markedMethod = (SootMethod) mi.next();
			methodName = markedMethod.getName();

			if (methodName.equals("<clinit>"))
				continue;

			if (methodName.equals("<init>")) {
				printf(
					file,
					"\n\n  "
						+ Modifier.toString(markedMethod.getModifiers())
						+ " ");
				printf(file, getShortName(getActualName(sc.getName())) + "(");
				printParams(file, markedMethod);
				printf(file, ")");

				printThrowsExceptions(file, markedMethod);
				
				printf(file, "{\n");
				
				if (markedMethod.hasActiveBody()) {
					//check it, we should always have an active body!!!
					JavaBody jb = (JavaBody) markedMethod.getActiveBody();
					jb.printToFile(file, this);
				}
				printf(file, "  }");
				continue;
			}
			printMethod(file, markedMethod);
		}
	}

	/**
	 *                                                         
	 * Prints method of <code>sc</code> to  <code>file</code>.                                  
	 *                                                         
	 */
	protected void printMethod(FileWriter file, SootMethod markedMethod) {
		//String methodName = markedMethod.getName();
		printf(
			file,
			"\n\n  " + Modifier.toString(markedMethod.getModifiers()) + " ");

		//print out the method
		Type returnType = markedMethod.getReturnType();
		printf(file, getPackageAdjustedType(returnType));
		printf(file, " " + markedMethod.getName());
		printf(file, "(");
		printParams(file, markedMethod);
		printf(file, ")");

		printThrowsExceptions(file, markedMethod);

		if (!markedMethod.isConcrete()
			|| Modifier.isAbstract(markedMethod.getModifiers()))
			//do nothing, no body
			printf(file, ";");
		else {
			printf(file, "{\n");
			if (markedMethod.hasActiveBody()) {
				JavaBody jb = (JavaBody) markedMethod.getActiveBody();
				jb.printToFile(file, this);
			}
			printf(file, "  }");
		}
	}

	/**
	 *                                                         
	 * Prints parameters of <code>markedMethod</code> to <code>file</code>.                                   
	 *                                                         
	 */
	protected void printParams(FileWriter file, SootMethod markedMethod) {
		int numParams = markedMethod.getParameterCount();
		for (int i = 0; i < numParams; i++) {
			if (i != 0)
				printf(file, ", ");
			Type paramType = markedMethod.getParameterType(i);
			printf(file, getPackageAdjustedType(paramType));
			printf(file, " param" + i);
		}
	}

	/**
	 * 
	 * @param file
	 * @param markedMethod
	 */
	protected void printThrowsExceptions(
		FileWriter file,
		SootMethod markedMethod) {

		if (!markedMethod.getExceptions().isEmpty()) {
			java.util.List exceptions = markedMethod.getExceptions();
			Iterator ei = exceptions.iterator();
			SootClass except = null;
			String exceptName;

			for (int i = 0; ei.hasNext(); i++) {
				except = (SootClass) ei.next();
				exceptName = getActualName(except.getName());

				//if (!unitTable.containsKey(exceptName)
				//	&& !envTable.containsKey(exceptName))
				//	continue;
				if (i != 0)
					printf(file, ",");
				else
					printf(file, " throws ");
				printf(
					file, getPackageAdjustedType(exceptName) + " ");

			}
		}
	}
	
	/**
	 *                                                         
	 * Prints top values based on their type.
	 *
	 */
	public String printTopValue(Type type) {
		
		if(supportSymValues)
			return printTopValueAsSym(type);
		if(supportChoiceValues)
			//support choice primitives
			return printTopValueAsChoice(type);
		//support simple testing	
		return printTopValueAsSingleton(type);
	}
	
	
	/**
	 *                                                         
	 * Prints top values based on their type.
	 *
	 */
	public String printTopValueAsSingleton(Type type) {
		if (type instanceof IntType)
			return "Abstraction.TOP_INT";
		if (type instanceof ByteType)
			return "Abstraction.TOP_BYTE";
		if (type instanceof ShortType)
			return "Abstraction.TOP_SHORT";
		if (type instanceof DoubleType)
			return "Abstraction.TOP_DOUBLE";
		if (type instanceof FloatType)
			return "Abstraction.TOP_FLOAT";
		if (type instanceof LongType)
			return "Abstraction.TOP_LONG";
		if (type instanceof CharType)
			return "Abstraction.TOP_CHAR";
		if (type instanceof BooleanType)
			return "Abstraction.TOP_BOOL";
		
		String typeName = type.toString();
		
		if(typeName.equals("java.lang.String")){
			return "Abstraction.TOP_STRING";
		}
		if(typeName.equals("java.lang.Object")){
			return "Abstraction.TOP_OBJ";
		}
		
		if(typeName.startsWith("java.lang")){
			//no modeling for java.lang
			//may want to refine to model some classes from java.lang
			return getRandomObjectCall(type);
		}
		
		//no TOP vals for unit classes
		if(!unitAnalysis && unit.containsClass(typeName)){
			
			return getRandomObjectCall(type);
		}
		if(unitAnalysis && !unit.containsClass(typeName)){
			logger.info("generating randomObject for: "+typeName);
			return getRandomObjectCall(type);
		}
		
		if (type instanceof RefType) {

			//return Type.TOP
			SootClass sc = ((RefType)type).getSootClass();
			if(sc.isInterface()){
				if(envHierarchy!=null)
					return printTopInterfaceValue(sc);
				else{
					logger.warning("no implementor for: "+type);
					return getRandomObjectCall(type);
				}
			}
			else{
				typeName = getPackageAdjustedType(type);
				return typeName+".TOP";
			}

		}

		if (type instanceof ArrayType) {
			logger.fine(
				"Printer: getTopValName from type: " + type.toString());
			return getRandomObjectCall(type);
		}

		if (type instanceof VoidType)
			return "";
		logger.severe("Uncovered type " + type);
		return "Uncovered type";
	}
	
	
	/**
	 *                                                         
	 * Prints top values based on their type.
	 *
	 */
	public String printTopValueAsSym(Type type) {
		if (type instanceof IntType)
			return "Abstraction.getTopInt()";
		if (type instanceof ByteType)
			return "Abstraction.getTopByte()";
		if (type instanceof ShortType)
			return "Abstraction.getTopShort()";
		if (type instanceof DoubleType)
			return "Abstraction.getTopDouble()";
		if (type instanceof FloatType)
			return "Abstraction.getTopFloat()";
		if (type instanceof LongType)
			return "Abstraction.getTopLong()";
		if (type instanceof CharType)
			return "Abstraction.getTopChar()";
		if (type instanceof BooleanType)
			return "Abstraction.getTopBool()";
		
		String typeName = type.toString();
		
		if(typeName.equals("java.lang.String")){
			return "Abstraction.getTopString()";
		}
		if(typeName.equals("java.lang.Object")){
			return "Abstraction.getTopObject()";
		}
		
		if(typeName.startsWith("java.lang")){
			//no modeling for java.lang
			//may want to refine to model some classes from java.lang
			return getRandomObjectCall((type));
		}
		
		if (type instanceof RefType) {
			
			return getTopObjectCall((type));
			
		}

		if (type instanceof ArrayType) {
			logger.info(
				"Printer: getTopValName from type: " + type.toString());
			return getTopObjectCall((type));
		}

		if (type instanceof VoidType)
			return "";
		logger.severe("Uncovered type " + type);
		return "Uncovered type";
	}
	

	
	/**
	 *                                                         
	 * Prints top values based on their type.
	 *
	 */
	public String printTopValueAsChoice(Type type) {

		if (type instanceof IntType)
			return "Abstraction.TOP_INT";
		if (type instanceof ByteType)
			return "Abstraction.TOP_BYTE";
		if (type instanceof ShortType)
			return "Abstraction.TOP_SHORT";
		if (type instanceof DoubleType)
			return "Abstraction.TOP_DOUBLE";
		if (type instanceof FloatType)
			return "Abstraction.TOP_FLOAT";
		if (type instanceof LongType)
			return "Abstraction.TOP_LONG";
		if (type instanceof CharType)
			return "Abstraction.TOP_CHAR";
		if (type instanceof BooleanType)
			return "Abstraction.TOP_BOOL";
		
		String typeName = type.toString();
		
		if(typeName.equals("java.lang.String")){
			return "Abstraction.TOP_STRING";
		}
		if(typeName.equals("java.lang.Object")){
			return "Abstraction.TOP_OBJ";
		}	
		if(typeName.startsWith("java.lang")){
			//no modeling for java.lang
			//may want to refine to model some classes from java.lang
			return getRandomObjectCall((type));
		}
		if (type instanceof RefType) {
			return getRandomObjectCall((type));
		}
		if (type instanceof ArrayType) {
			logger.info(
				"Printer: getTopValName from type: " + type.toString());
			return getRandomObjectCall((type));
		}
		if (type instanceof VoidType)
			return "";
		logger.severe("Uncovered type " + type);
		return "Uncovered type";
	}
	
	
	public String printTopInterfaceValue(SootClass sc) {
	
		//find implementors
		//List implementors = envHierarchy.getDirImplementorsOf(sc);
		List implementors = envHierarchy.getImplementorsOf(sc);
		if(implementors == null){
			logger.warning("No implementors for: "+sc);
			return getRandomObjectCall(sc.getName());
		}
		else if(implementors.isEmpty()){
			logger.warning("Implementors empty for: "+sc);
			return getRandomObjectCall(sc.getName());
		}
		else if(implementors.size() == 1){
			
			SootClass implementor = (SootClass)implementors.get(0);
			//logger.fine("One implementor: "+implementor);
			String typeName = JavaPrinter.getActualName(implementor.getName());
			
			return getPackageAdjustedType(typeName) +".TOP";
		}
		//else many implementors
		//logger.info("Many implementors for: "+sc+": "+implementors);
			
		//TODO: for now, we pick one 
		//may want to refine as a nondeterministic choice over all
		SootClass implementor = (SootClass)implementors.get(0);
		String typeName = JavaPrinter.getActualName(implementor.getName());
		return getPackageAdjustedType(typeName) +".TOP";
			
		//Type type = sc.getType();
		//return getRandomObjectCall((type));
		
	}


	/** 
	  * Generates a list of top values for a list of <code>types</code>. 
	  */	
	public String printTopArgs(List types) {

		assert(types != null);
		
		String result = "";
		Type argType;
		int count = 0;
		for (Iterator ti = types.iterator(); ti.hasNext();) {
			argType = (Type) ti.next();
			if (count != 0)
				result = result + ", ";
			result = result + printTopValue(argType);
			count++;
		}
		return "(" + result + ")";
	}
	
	/** 
	  * Generates a list of top values for a list of <code>types</code>. 
	  */
	
	public String printArgs(List args) {
		//assert(args != null);
		if(args == null)
			return "null";
	
		String result = "";
		int count = 0;
		JavaExpr val;
		for (Iterator ai = args.iterator(); ai.hasNext();) {
			//argType = (Type) di.next();
			val = (JavaExpr)ai.next();
			if (count != 0)
				result = result + ", ";
			result = result + val.getCode(this);
			count++;
		}
		return "(" + result + ")";
	}
	
	public String printDefaultValue(Type type) {

		if (type
			instanceof IntType | type
			instanceof ByteType | type
			instanceof ShortType | type
			instanceof DoubleType | type
			instanceof FloatType | type
			instanceof LongType)
			return "0";
		if (type instanceof BooleanType)
			return "false";
		if (type instanceof CharType)
			return "\'0\'";
		if (type instanceof RefType || type instanceof ArrayType)
			return "null";
		if (type instanceof VoidType)
			return "";
		
		logger.severe("unacconted type: "+type);
		return "unaccounted type value";
	}



	/*-------------------------------------------------------*/
	/*       Auxiliary methods                               */
	/*-------------------------------------------------------*/
	
	/**
	 *                                                         
	 * Returns the name of the path by replacing "." with "/".
	 *
	 */
	protected String getFullPathName(String name) {
		String temp = "";
		String result;
		int index = name.lastIndexOf(".");
		if (index >= 0)
			temp = name.substring(0, index);
		result = outputDirName + (packageName + temp).replace(".", File.separator);;

		//if (debug > 1)
		//	System.out.println("getPath String: " + result);
		logger.fine("getPath String: " + result);
		//return result.replace('.', '/');
	
		
		return result;				
	}

	/**
	 *                                                         
	 * Returns the name of the path by replacing "." with "/".
	 *
	 */
	protected String getPathName(String name) {
		String temp = "";
		String result;
		int index = name.lastIndexOf(".");
		if (index >= 0)
			temp = name.substring(0, index);
		result = packageName + temp;

		//if (debug > 1)
		//	System.out.println("getPath String: " + result);
		logger.fine("getPath String: " + result);
		return result.replace('.', '/');
	}

	/**
	 *                                                         
	 * Returns the name of the package from <code>name</code>.
	 *
	 */
	protected String getPackageName(String name) {
		String temp = "";

		int index = name.lastIndexOf(".");
		if (index >= 0)
			temp = name.substring(0, index);

		if (temp.length() == 0)
			return packageName.substring(0, packageName.length() - 1);

		return packageName + temp;

	}
	
	//all type adjustments should happen in this class
	public String getPackageAdjustedType(String name) {
		if(notModeled!=null)
			if (name.startsWith(notModeled))
				return name;
		
		//only environment types get prefixed
		if(unit.containsClass(name))
			return name;
		
		return packageName + name;
	}

	public String getPackageAdjustedType(Type type) {
		if(type == null)	
			return "null";
		String name = type.toString();
		if (type instanceof RefType)
			return getPackageAdjustedType(name);
		else if (type instanceof ArrayType) {
			Type bType = ((ArrayType) type).baseType;
			if (bType instanceof RefType)
				return getPackageAdjustedType(name);
		}
		return name;
	}

	public static String getBeginAtomicCall() {
		//return context + ".beginAtomic()";
		return "Verify.beginAtomic()";
	}

	public static String getEndAtomicCall() {
		//return context + ".endAtomic()";
		return "Verify.endAtomic()";
	}

	public static String getRandomBoolCall() {
		//if (context.equals("OCSEGen: Open Components and Systems"))
		//	return context + ".choose()";
		//else
		//	return context + ".randomBool()";
		return "Verify.randomBool()";
	}

	public static String getRandomIntCall(int n) {
		//if (context.equals("OCSEGen: Open Components and Systems"))
		//	return "OCSEGen: Open Components and Systems.random(" + n + ")";
		//else
		return "Verify.random(" + n + ")";

	}

	public String getRandomObjectCall(Type type) {
		String call =  "Verify.randomObject(";

		String typeName = getPackageAdjustedType(type);
		if (typeName.equals("java.lang.Object"))
			return call + "\"" + typeName + "\")";
		
		return "((" + typeName + ")" + call + "\"" + typeName + "\"))";
	}
	
	public String getRandomObjectCall(String type) {
		String call =  "Verify.randomObject(";

		String typeName = getPackageAdjustedType(type);
		if (typeName.equals("java.lang.Object"))
			return call + "\"" + typeName + "\")";
		
		
		return "((" + typeName + ")" + call + "\"" + typeName + "\"))";
	}

	public String getRandomReachableCall(Type type, String from) {
		String call = "Verify.randomReachable(";
		String typeName = getPackageAdjustedType(type);

		if (typeName.equals("java.lang.Object"))
			return call + "\"" + typeName + "\", " + from + ")";
		
		return "(("
			+ typeName
			+ ")"
			+ call
			+ "\""
			+ typeName
			+ "\","
			+ from
			+ " ))";
	}

	
	public String getTopObjectCall(Type type) {
		String call =  "Abstraction.getTopObject(";

		String typeName = getPackageAdjustedType(type);
		if (typeName.equals("java.lang.Object"))
			return call + "\"" + typeName + "\")";
		
		return "((" + typeName + ")" + call + "\"" + typeName + "\"))";
	}	

	/**
	 * Removes the prefix, reveals the real name of a class.
	 */

	public static String getActualName(String name) {
		if (name.startsWith("$")) {

			//String result = new String(name.substring(1));
			//System.out.println("EnvPrinter, getName: "+ result);
			return name.substring(1);
		}
		return name;

	}

	/**
	 * Appends a dummy prefix to a name, 
	 * Scene doesn't allow multiple classes with the same name,
	 * therefore stubs have special prefixed names.
	 */
	public static String getPrefixedName(String name) {
		return "$" + name;
	}


	/**
	 *                                                         
	 * Returns a name of the class <code>name</code> excluding the package name.
	 *
	 */
	public static String getShortName(String name) {
		int index = name.lastIndexOf(".");
		//String result = new String(name.substring(index + 1, name.length()));
		return name.substring(index + 1, name.length());
	}

	
	/*
	//works for basic types, returns nulls for RefType 
	public static Value getValue(Type type)
	{
	if (type instanceof IntType ||
	type instanceof ByteType ||
	type instanceof ShortType ||
	type instanceof BooleanType ||
	type instanceof CharType)
	  	  return IntConstant.v(0);
	
	if(type instanceof DoubleType)
	  return DoubleConstant.v(0);
	
	if (type instanceof FloatType)
	  return FloatConstant.v(0);
	
	if (type instanceof LongType)
	  return LongConstant.v(0);
	
	if (type instanceof RefType)
	   return NullConstant.v();
	if (type instanceof ArrayType)
	  return NullConstant.v();
	else
	{
	  //just to see if we missed anything
	  error("Uncovered type: "+type.toString());
	  return NullConstant.v();
	}
	}
	
	
	// return top value for any type
	public static Value getTopValue(Type type)
	{
	return TopValue.v();
	}
	
	//Returns the name of <code>type</code> as a string.
	public static String typeName(Type type)
	{
	if (type instanceof IntType)
	  return "int";
	if (type instanceof ByteType)
	  return "byte";
	if (type instanceof ShortType)
	  return "short";
	if (type instanceof BooleanType)
	  return "bool";
	if (type instanceof CharType)
	  return "char";
	if(type instanceof DoubleType)
	  return "double";
	if (type instanceof FloatType)
	  return "float";
	if (type instanceof LongType)
	  return "long";
	if (type instanceof RefType)
	  return "ref";
	if (type instanceof ArrayType)
	  return "array";
	if (type instanceof VoidType)
	  return "void";
	error("Printer, typeName: unknown type");
	return "unknown";
	}
	*/
	
}
