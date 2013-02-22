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
package edu.ksu.cis.envgen.codegen.vals;

import java.util.logging.Logger;

import soot.*;

import edu.ksu.cis.envgen.codegen.JavaPrinter;

/**
 * 
 * Representation of the field or array access expression.
 * 
 */
public class Accessor {

	SootField field;
	int accessIndex;
	Root inner;

	Type type;
	Type castType;

	Logger logger = Logger.getLogger("envgen.analysis.stat.locs");
	
	public Accessor(SootField f) {
		field = f;
		accessIndex = -1;
	}

	public Accessor(SootField f, Type t) {
		field = f;
		accessIndex = -1;
		type = t;
	}

	public Accessor(int i) {
		accessIndex = i;
	}
	
	public Accessor(Root inner){
		this.inner = inner;
	}

	public Accessor(int i, Type t) {
		accessIndex = i;
		type = t;
	}

	public boolean isField(){
		return field != null;
	}
	
	public boolean isInner(){
		return inner != null;		
	}
	
	public boolean isArrayAccess(){
		return field == null && inner == null;
	}
	
	public void setField(SootField f) {
		field = f;
	}

	public SootField getField() {
		return field;
	}

	public Root getInner(){
		return inner;
	}
	
	public String getName(){
		if(field != null)
			return field.getName();
		if(inner != null)
			return inner.toString();
		return "array acces "+accessIndex;
	}
	
	public void setAccessIndex(int i) {
		accessIndex = i;
	}

	public int getAccessIndex() {
		return accessIndex;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setCastType(Type t) {
		castType = t;
	}

	public Type getCastType() {
		return castType;
	}

	public String getCode(JavaPrinter printer) {
		if (field != null)
			return ("." + field.getName());
		else if (inner!=null)
			return "."+inner.getCode(printer);
		else if (accessIndex >= 0)
			return "[Abstraction.TOP_INT]";

		else
			logger.severe("Accessor, getCode: unhandled case");
		return "no code";
	}

	public String toString() {
		if (field != null)
			return "."+field.getName();
		else if (inner != null)
			return "."+inner.toString();
		else if (accessIndex >= 0)
			return "[i]";
	
		else
			logger.severe("Accessor, toString: unhandled case");
		return "no string";
	}
}
