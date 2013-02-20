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

import edu.ksu.cis.envgen.*;
import edu.ksu.cis.envgen.applinfo.*;
import edu.ksu.cis.envgen.codegen.*;
import edu.ksu.cis.envgen.codegen.ast.expr.*;
import edu.ksu.cis.envgen.codegen.vals.*;
import edu.ksu.cis.envgen.util.*;

import soot.*;

import java.util.*;
import java.util.logging.*;

public class TypeChecker {

	ModuleInfo unit;
	
	ModuleInfo env;
	
	Map definitions;


	/**
	 * List that keeps track of propositions used in the specification, handy
	 * for discovering the universe of propositions for ltl translation to an
	 * automaton.
	 */
	// List specPropositions;
	/** Unit class instantiations as defined in a specification. */
	List initProps;


	/** List that keeps ltl and reg expression specifications as ParserNodes. */
	List threads;

	ThreadSpec mainThread;
	
	List stubs;

	UserSpec ast;

	Logger logger = Logger.getLogger("envgen.spec");

	public TypeChecker(UserSpec ast, ApplInfo info) {
		this.ast = ast;
		definitions = ast.getDefinitions();
		threads = ast.getThreads();
		mainThread = ast.getMainThread();
		initProps = ast.getInitProps();
		this.stubs = ast.getStubs();
		
		this.unit = info.getUnit();
		this.env = info.getEnv();
	}

	public boolean typeCheck() {
		// check init section
		
		Proposition prop;
		for (Iterator ii = initProps.iterator(); ii.hasNext();) {
			prop = (Proposition) ii.next();
			checkProposition(prop);
		}

		// check specPropositions
		ThreadSpec threadSpec;
		List threadPropositions;
		for (Iterator it = threads.iterator(); it.hasNext();) {
			threadSpec = (ThreadSpec) it.next();
			threadPropositions = threadSpec.getPropositions();

			for (Iterator inner = threadPropositions.iterator(); inner
					.hasNext();) {
				prop = (Proposition) inner.next();

				if(!checkProposition(prop))
					return false;
			}

		}

		if (mainThread != null) {
			threads.remove(mainThread);
		}
		
		StubSpec stubSpec;
		for(Iterator si = stubs.iterator(); si.hasNext();){
			stubSpec = (StubSpec)si.next();
			if (!checkStubSpec(stubSpec))
				return false;
		}
		
		return true;

	}

	public boolean checkProposition(Proposition prop) {

		JavaExpr expr = prop.getExpression();
		return checkExpression(expr);

	}

	public boolean checkExpression(JavaExpr expr){
		assert expr != null;
		if (expr instanceof AssignExpr) {
			logger.fine("assignment: " + expr);
			return checkAssignment((AssignExpr) expr);
		} else if (expr instanceof TypeDeclExpr) {
			logger.fine("declaration: " + expr);
			return checkDeclaration((TypeDeclExpr) expr);
		} else if (expr instanceof InvokeExpr) {
			logger.info("invoke: " + expr);
			return checkInvoke((InvokeExpr) expr);
		}
		else if(expr instanceof NewExpr){
			logger.fine("new expr: "+expr);
			return checkAlloc((NewExpr)expr);
		}
		else if(expr instanceof StrExpr){
			logger.fine("str expr: "+expr);
			//TODO: any checks?
			return true;
		}
			logger.severe("not covered: " + expr.getClass());
		return false;
	}
	
	public boolean checkInvoke(InvokeExpr expr) {

		// change the 4 attributes of the MethodCall if needed
		String receiver = expr.getReceiver();
		String methodName = expr.getMethodName();
		List args = expr.getArgs();
		// String propCode = mc.getCode();
		// String label = mc.getLabel();

		List argsList;

		// this is not a definition
		List unitMethods = unit.getMethods();
		
		//logger.info("unit methods: "+ unitMethods);
		
		SootMethod tempMethod;
		//TODO: several methods may match, may want to choose
		for (Iterator mi = unitMethods.iterator(); mi.hasNext();) {
			tempMethod = (SootMethod)mi.next();
			if (checkSignature(receiver, methodName, args, tempMethod)) {
				logger.fine("Matched "+expr+" with "+tempMethod);
				expr.setMethod(tempMethod);

				if (receiver == null) {
					receiver = genReceiver(tempMethod);
				}
				expr.setReceiver(receiver);
				argsList = genArgs(args, tempMethod);
				expr.setArgs(argsList);

				// figure out the label, which is the adjusted propCode

				// if (label == null) {
				// //since methodCode itself might contain "" we need
				// //to replace these with something that doesn't conflict
				// if (propCode.indexOf('"') != -1) {
				// label = propCode.replace('"', '`');
				// } else {
				// label = propCode;
				// }
				// }
				// mc.setLabel(label);

				return true;
			}
		}
		
		logger.severe("Illegal Proposition: " + expr);
		return false;
	}


	public boolean checkAssignment(AssignExpr expr) {
		logger.fine("finish checking assignment: "+expr);
		JavaExpr left = expr.getLeft();
		JavaExpr right = expr.getRight();
		
		return checkExpression(left) && checkExpression(right);
		
	}

	/**
	 * Checks the proposition used in the specification file, returns true if
	 * the proposition corresponds to a method invocation of one of the unit
	 * visible methods, returns false otherwise. Records the proposition in the
	 * specPropositions vector and as a proposition of the node.
	 */
	public boolean checkDeclaration(TypeDeclExpr prop) {

		String typeStr = prop.getTypeStr();
		String varName = prop.getName();
		JavaExpr value = prop.getValueExpr();

		// check type
		if (isPrimitiveType(typeStr)) {

		} else {

			Type type = checkType(typeStr);
			if(type == null){
				logger.severe("Unknown decl type: "+typeStr);
				return false;
			}
			typeStr = type.toString();
			prop.setTypeStr(typeStr);
			prop.setType(type);


			if (value == null) {
				SootClass declClass = ((RefType)type).getSootClass();
				SootMethod initMethod = Util.getConstructor(declClass);
				//logger.info("init method: "+initMethod);
				if (initMethod == null){
					// value = new default constructor
					value = new NewExpr(typeStr, new ArrayList());
					prop.setValueExpr(value);
				}
				else{
					// value = new constructor of init
					//TODO: fill the args in the codegenerator?
					List paramTypes = initMethod.getParameterTypes();
					value =  new NewExpr(typeStr, CodeGenUtil.genDefaultArgs(paramTypes));
					prop.setValueExpr(value);
				}
			}
			else
				checkExpression(value);
		}
		return true;
	}
	
	//TODO: coordinate with the decl type
	public boolean checkAlloc(NewExpr expr){
		boolean result = true;
		String typeStr = expr.getTypeStr();
		List args = expr.getArgs();
		
		Type type = checkType(typeStr);
		if(type == null){
			logger.severe("Unknown alloc type: "+typeStr);
			return false;
		}
		typeStr = type.toString();
		expr.setTypeStr(typeStr);
		expr.setType(type);
		
		//TODO: check args
		
		return result;
	}
	
	public Type checkType(String type){
		Set classNames = unit.getClassNames();
		String name;
		SootClass sc;
		for(Iterator ni = classNames.iterator(); ni.hasNext();){
			name = (String)ni.next();
			if(name.equals(type) || JavaPrinter.getShortName(name).equals(type)){
				sc = unit.getClass(name);
				return sc.getType();
			}
				
		}
		return null;
	}

	public boolean checkArgType(JavaExpr arg, Type type) {
		// check instantiations for arg

		String name;

		if (type instanceof PrimType) {
			// TODO finish checking

			// if arg is refType
			if (arg instanceof StrExpr) {
				name = ((StrExpr) arg).getValue();
				if (ast.containsVar(name))
					return false;
			}
			return true;
		}

		if (type instanceof RefType) {

			if (arg instanceof StrExpr) {
				name = ((StrExpr) arg).getValue();
				if (ast.containsVar(name, type))
					return true;
			}
		}

		if (type instanceof ArrayType) {
			// TODO finish checking
			return true;
		}

		return true;

	}

	public boolean checkSignature(String receiver, String methodName, List args, SootMethod sm) {
		
		if (!methodName.equals(sm.getName()))
			return false;

		if (receiver == null && args.isEmpty()) {
			// the user didn't specify anything
			return true;

		} 
		
		if(receiver != null){
			//check the type of the receiver
			Type declType = sm.getDeclaringClass().getType();
			if(!ast.containsVar(receiver, declType))
				return false;
		}
		
		if(!args.isEmpty()){

			int argsCount = args.size();
			List paramTypes = sm.getParameterTypes();
			if (argsCount != paramTypes.size())
				return false;

			
			JavaExpr arg;
			Type type;
			int i = 0;

			for (Iterator ai = args.iterator(); ai.hasNext(); i++) {
				type = (Type) paramTypes.get(i);
				arg = (JavaExpr) ai.next();

				if (arg instanceof StrExpr)
					if (((StrExpr) arg).getValue().equals("top"))
						continue;
					else
						// check the type
						if (!checkArgType(arg, type))
							return false;
			}
		}
			
		return true;
	}

	/**
	 * If receiver is not supplied, figures out the receiver object.
	 */
	//TODO: move to the code generation phase?
	public String genReceiver(SootMethod internalMethod) {
		String receiver = "";
		SootClass declClass = internalMethod.getDeclaringClass();
		String declClassName = declClass.getName();
		// if no instantiations specified
		// use the default instantiations
		if (initProps.isEmpty()) {

			if (Modifier.isAbstract(declClass.getModifiers())) {

				if (!internalMethod.isStatic())
					receiver = "concrete" + declClassName.toLowerCase() + "0";
				else
					receiver = "Concrete" + declClassName;
			} else {

				if (!internalMethod.isStatic())
					receiver = declClassName.toLowerCase() + "0";
				else
					receiver = declClassName;
			}
		} else {

			if (!internalMethod.isStatic()) {

				receiver = ast.getVar(declClassName);

			} else {
				receiver = declClassName;
			}
		}
		return receiver;
	}
	//TODO: move to code generation phase?
	public List genArgs(List args, SootMethod sm) {

		List paramTypes = sm.getParameterTypes();
		List result = new ArrayList();

		Type type;
		JavaExpr argVal;
		if (args.isEmpty()) {
			// figure out new args

			if (!paramTypes.isEmpty()) {
				result = CodeGenUtil.genDefaultArgs(paramTypes);
			}
			// return empty list
		} else {

			int i = 0;
			JavaExpr arg;
			for (Iterator ai = args.iterator(); ai.hasNext(); i++) {
				// TODO: check for array out of bounds
				type = (Type) paramTypes.get(i);
				arg = (JavaExpr) ai.next();

				if (arg instanceof StrExpr) {
					String val = ((StrExpr) arg).getValue();
					if (val.equals("top"))
						argVal = new ValueExpr(new TopValue(type));
					else
						argVal = new StrExpr(val);
					result.add(argVal);
				}

			}

		}
		return result;

	}

	public boolean isPrimitiveType(String type) {

		if (type.equals("boolean"))
			return true;
		if (type.equals("int"))
			return true;
	    if (type.equals("char"))
			return true;
		if (type.equals("byte"))
			return true;
		if (type.equals("short"))
			return true;
		if (type.equals("long"))
			return true;
		if (type.equals("double"))
			return true;
		return false;
	}

	public String isTopToken(String arg) {

		if (arg.equals("TOP_INT"))
			return "Abstraction.TOP_INT";
		if (arg.equals("TOP_BYTE"))
			return "Abstraction.TOP_BYTE";
		if (arg.equals("TOP_SHORT"))
			return "Abstraction.TOP_SHORT";
		if (arg.equals("TOP_DOUBLE"))
			return "Abstraction.TOP_DOUBLE";
		if (arg.equals("TOP_FLOAT"))
			return "Abstraction.TOP_FLOAT";
		if (arg.equals("TOP_LONG"))
			return "Abstraction.TOP_LONG";
		if (arg.equals("TOP_CHAR"))
			return "Abstraction.TOP_CHAR";
		if (arg.equals("TOP_BOOL"))
			return "Abstraction.TOP_BOOL";
		if (arg.equals("TOP_OBJ"))
			return "Abstraction.TOP_OBJ";

		return arg;

	}

	/**
	 * In case of LTL, we need to intersect specification propositions with
	 * default propositions, in this case default propositions may need to be
	 * updated with label information and specific receiver information.
	 */
	protected List updateDefaultMethods(List defaultPublicMethods) {
		logger.fine("\n------------- updateDefaultMethods");

		// 4 attributes of the MethodCall
		Proposition mc;

		// Sring receiver;
		String call;
		String label;
		SootMethod internalMethod;
		logger.fine("----defaultPublicMethods: " + defaultPublicMethods);
		Iterator di;
		Proposition temp = null;
		InvokeExpr tempInvoke = null;
		
		// String tempReceiver;
		String tempCall;

		for (Iterator it = definitions.keySet().iterator(); it.hasNext();) {
			label = (String) it.next();
			mc = (Proposition) definitions.get(label);
			
			call = mc.getCode();
			di = defaultPublicMethods.iterator();

			while (di.hasNext()) {
				temp = (Proposition) di.next();
				tempInvoke = (InvokeExpr) mc.getExpression();

				tempCall = temp.getCode();

				// add a label if there is one
				if (tempCall.equals(call)) {
					temp.setLabel(label);
				}

				// correct a receiver if there is one
				if (!initProps.isEmpty()) {
					internalMethod = tempInvoke.getMethod();
					// System.out.println("------need to figure out the
					// receiver");
					tempInvoke.setReceiver(genReceiver(internalMethod));
				}
			}
		}
		return defaultPublicMethods;
	}
	
	/**
	 * Checks the type of the object instantiated in the "instantiations"
	 * section of the specification file, returns true if the type is found in
	 * the default instance classes, returns false otherwise.
	 */
	/*
	public Type getType(String type) {
		Iterator ii = defaultInitPropositions.iterator();
		Proposition temp;
		String tempType;
		while (ii.hasNext()) {
			temp = (Proposition) ii.next();
			tempType = temp.getType();
			if (tempType.equals(type)
					|| JavaPrinter.getShortName(tempType).equals(type))
				return temp.getDeclType();
		}

		// what about environment classes (?)
		// should allow instantiation of env classes

		// what about package includes (?)

		logger.severe("getType: unknown type: " + type);
		return null;

	}
	*/
	
	
	   public boolean checkStubSpec(StubSpec stubSpec){
		   
		   String className = stubSpec.getClassName();
		   String methodName = stubSpec.getMethodName();
		  
			if(env == null)
			    logger.severe("no env table");

			if(!env.containsClass(className)){
			    logger.severe("Class "+className+" not in env");
			    return false;
			}
			SootClass sc = env.getClass(className);
			if(!sc.declaresMethodByName(methodName)){
			    logger.severe("Class "+className+" doesn't declare method "+methodName);
			    return false;
			}
			SootMethod sm = sc.getMethodByName(methodName);
			stubSpec.setMethod(sm);
			
			
			Body body = stubSpec.getBody();
			//TODO: type check the body
            body.setMethod(sm);
            
            sm.setActiveBody(body);
			return true;

	}
}
