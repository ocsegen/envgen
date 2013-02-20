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
package edu.ksu.cis.envgen.codegen.ast;

import java.util.*;
import java.io.*;

import soot.*;
import soot.util.*;

import edu.ksu.cis.envgen.codegen.*;
import edu.ksu.cis.envgen.codegen.ast.stmt.JavaStmt;

/**
 * Representation of the Java body for a method, similar to JimpleBody, holds
 * Java statements.
 */
public class JavaBody extends Body {

	
	public Object clone() {
		//dummy
		return null;
	}
	
	
	public void printToFile(FileWriter file, JavaPrinter printer) {
		Chain locals = this.getLocals();
		Iterator li = locals.iterator();
		String typeName;
		while (li.hasNext()) {
			Local l = (Local) li.next();
			typeName = l.getType().toString();
			typeName = printer.getPackageAdjustedType(typeName);
			printf(file, "    " + typeName + " " + l.getName()
					+ ";\n");
		}

		PatchingChain units = this.getUnits();
		Iterator ui = units.iterator();
		while (ui.hasNext()) {
			JavaStmt stmt = (JavaStmt) ui.next();
			stmt.printToFile(file, printer, 2); // 2 is the number of shifts
		}
	}

	private void printf(FileWriter outStream, String string) {
		for (int i = 0; i < string.length(); i++) {
			try {
				outStream.write(string.charAt(i));
			} catch (Exception e) {
			}
		}
	}
}
