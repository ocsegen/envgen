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

import java.io.FileWriter;

import edu.ksu.cis.envgen.codegen.*;

public class SeqStmt extends JavaStmt {
	JavaStmt first;
	JavaStmt second;
	
	public SeqStmt(JavaStmt first, JavaStmt second){
		this.first = first;
		this.second = second;
	}

	public void printToFile(FileWriter file, JavaPrinter printer, int shift) {
		if (shift > 0)
			first.printToFile(file, printer, shift);
		else
			first.printToFile(file, printer, 0);
		second.printToFile(file, printer, shift);
	}
}
