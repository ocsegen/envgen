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
package edu.ksu.cis.envgen.applinfo;

import java.util.*;

import edu.ksu.cis.envgen.codegen.JavaPrinter;

import soot.Modifier;
import soot.PrimType;
import soot.RefLikeType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.util.Chain;
import soot.Value;

public abstract class ModuleInfo {
	/** Table of module classes, allowing to identify classes by their name. 
	 * This is needed for creation of model classes for the actual classes. */
	Map<String, SootClass> classes = new HashMap<String, SootClass>();
	
	/** List of module methods. */
	List<SootMethod> methods = new ArrayList<SootMethod>();
	
	List<SootField> fields;
	
	//	public static fields
	List<SootField> globals;
	
	//	public static final fields
	Map<SootField, Value> constants = new HashMap<SootField, Value>();

	/** Auxiliary map */
	Map attributes = new HashMap();
	
	
	/** Prefix to env classes package to distinguish from actual libs */
	String packageName;
		
	
	public int size(){
		return classes.size();
	}
	
	public boolean containsClass(String className){
		if(classes.containsKey(className))
			return true;
		return false;
	}
	
	public Collection<SootClass> getClasses(){
		return classes.values();
	}

	public Set<String> getClassNames(){
		return classes.keySet();
	}
	
	public Collection<String> getClassNamesCopy(){
		return new ArrayList<String>(classes.keySet());
	}
	
	public SootClass getClass(String className){
		return (SootClass)classes.get(className);
	}
	
	public SootClass getClassByShortName(String type){
		Set<String> classNames = getClassNames();
		String name;
		SootClass sc;
		for(Iterator<String> ni = classNames.iterator(); ni.hasNext();){
			name = ni.next();
			if(name.equals(type) || JavaPrinter.getShortName(name).equals(type)){
				sc = getClass(name);
				return sc;
			}
				
		}
		return null;
	}

	
	/** unit and env have different mechanisms of adding a class.*/
	public abstract SootClass addClass(SootClass sc);
	
	
	public void addClass(String name, SootClass sc){
		classes.put(name, sc);
	}
	
	//public void addClass(String name, EnvClass ec){
	//	classes.put(name, ec);
	//}
	
	public List<SootMethod> getMethods() {
		return methods;
	}
	
	
	public void setMethods(List<SootMethod> envMethods){
		this.methods = envMethods;
	}
	
	public void addMethod(SootMethod sm){
		//TODO: check for dup?
		System.out.println("Adding method to unit: "+ sm);
		
		methods.add(sm);
	}
	
	public abstract void addMethodToClass(
			SootClass sc,
			SootMethod sm);
	
	public abstract void addFieldToClass(SootClass sc, SootField sf);

	
	public void addConstant(SootField sf){
		constants.put(sf, null);
	}
	
	public void addConstantValue(SootField sf, Value value){
		constants.put(sf, value);
	}
	
	public Value getConstantValue(SootField sf){
		return (Value)constants.get(sf);
	}
	
	public Set<SootField> getConstants(){
		return constants.keySet();
	}
	
	public Map getAttributes(){
		return attributes;
	}
	
	public void setPackageName(String packageName){
		this.packageName = packageName;
	}
	
	public String getPackageName(){
		return packageName;
	}
	
	/**
	 * Builds a list of unit static fields (unit globals).
	 */
	//TODO: move to the interface finder
	public List<SootField> findGlobals() {
		List<SootField> globals = new ArrayList<SootField>();
		SootClass internalClass;

		for (Iterator<SootClass> it = getClasses().iterator(); it.hasNext();) {
			internalClass = it.next();
			Chain<SootField> fields = internalClass.getFields();
			Iterator<SootField> fi = fields.iterator();
			SootField sf = null;
			Type type = null;
			while (fi.hasNext()) {
				sf = fi.next();
				if (Modifier.isStatic(sf.getModifiers())
						&& Modifier.isPublic(sf.getModifiers())) {
					type = sf.getType();
					if (type instanceof RefLikeType) {
						if (classes.containsKey(type.toString()))
							globals.add(sf);
					} else if (type instanceof PrimType)
						globals.add(sf);
				}
			}
		}
		return globals;
	}
	
	/**
	 * Checks whether there is a library class in the unit.
	 */
	public boolean includesLibClasses() {

		for (Iterator<String> it = getClassNames().iterator(); it.hasNext();) {
			String key = it.next();
			if (key.startsWith("java") || key.startsWith("sun.")
					|| key.startsWith("org.") || key.startsWith("com.")
					|| key.startsWith("edu.") || key.startsWith("de.")
					|| key.startsWith("gov.") || key.startsWith("scale.")) {
				// if there is library class in unit
				// need to analyze
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Checks whether paramTypeClass is a supertype of any of the unit classes.
	 */
	//TODO: implement a version that considers unit types only
	public boolean isModuleType(SootClass sc) {
		String className = sc.getName();
		//SootClass unitClass;
		if(classes.containsKey(className))
			return true;

		return isModuleSuperType(sc);
	}
	
	
	/**
	 * Checks whether paramTypeClass is a supertype of any of the unit classes.
	 */
	public boolean isModuleSuperType(SootClass sc) {
		SootClass unitClass;

		for (Iterator<SootClass> it = getClasses().iterator(); it.hasNext();) {
			unitClass = it.next();
			if (isSubType(unitClass, sc)) {
				// as soon as we find a parameter
				// with a type that comes from unit
				// then we need to process the call

				return true;
			}
		}
		return false;
	}
	

	/**
	 * Finds whether <code>unitClass</code> class is a subtype of
	 * <code>paramTypeClass</code> class. Used to check whether a unitClass
	 * type can flow into a method through paramTypeClass, or whether a
	 * unitClass inherits from a class in the env, in which case we need to keep
	 * track of side-effects to the fields of paramTypeClass.
	 */
	public boolean isSubType(SootClass unitClass, SootClass paramTypeClass) {
		if (unitClass.getName().equals(paramTypeClass.getName()))
			return true;
		if (unitClass.getName().equals("java.lang.Object"))
			return false;
		return isSubType(unitClass.getSuperclass(), paramTypeClass);
	}
	
	/**
	 * Prints classes and their methods. 
	 */

	public String toString() {
		String result = "";
		SootClass sc;

		for (Iterator<SootClass> it = classes.values().iterator(); it.hasNext();) {
			sc = it.next();
			result = result +(
				"\n Class <"
					+ sc.getName()
					+ "> with methods: "
					+ sc.getMethods()
					+ "\n");
		}
		return result;
	}
}
