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
package edu.ksu.cis.envgen.codegen.ast.stmt;

import java.util.logging.Logger;
import java.io.*;

import soot.*;

import edu.ksu.cis.envgen.codegen.*;

/**
 * Representation of the Java statement.
 */

public abstract class JavaStmt extends AbstractUnit 
{
	Logger logger = Logger.getLogger("envgen.codegen.javagrammar");

	void printf(FileWriter outStream, String string) {
		for (int i = 0; i < string.length(); i++) {
			try {
				outStream.write(string.charAt(i));
			} catch (Exception e) {
			}
		}
	}
	
	//public abstract void printToFile(FileWriter file, int shift);
	
	public abstract void printToFile(FileWriter file, JavaPrinter printer, int shift);

	//TODO: check dummy methods, needed for compatibility with soot.Unit
	public boolean fallsThrough(){return true;} 
	public boolean branches(){return true;}
	public void toString( UnitPrinter up ){return;}
}
