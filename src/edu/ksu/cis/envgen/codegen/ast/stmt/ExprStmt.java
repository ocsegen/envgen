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
import edu.ksu.cis.envgen.codegen.ast.expr.JavaExpr;

public class ExprStmt extends JavaStmt {
	JavaExpr expr;
	
	public ExprStmt(JavaExpr e) {
		this.expr = e;
	}

	public void printToFile(FileWriter file, JavaPrinter printer, int shift) {

		for (int i = 0; i < shift; i++)
			printf(file, "  ");
		expr.printToFile(file, printer, 0);
		if (shift > 0)
			printf(file, ";\n");
		if (shift == 0)
			printf(file, ", ");
		if (shift < 0)
			printf(file, " ");
	}
}
