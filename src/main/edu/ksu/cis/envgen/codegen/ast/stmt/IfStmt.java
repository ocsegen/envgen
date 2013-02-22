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

public class IfStmt extends JavaStmt {
	JavaExpr cond;
	JavaStmt first;
	JavaStmt second;

	public IfStmt(JavaExpr cond, JavaStmt thenpart, JavaStmt elsepart) {
		this.cond = cond;
		this.first = thenpart;
		this.second = elsepart;
	}
	
	public void printToFile(FileWriter file, JavaPrinter printer, int shift) {
		for (int i = 0; i < shift; i++)
			printf(file, "  ");
		if (second == null) {
			printf(file, "if(");
			cond.printToFile(file, printer, 0);
			printf(file, "){\n");
			first.printToFile(file, printer, shift + 1);
			for (int i = 0; i < shift; i++)
				printf(file, "  ");
			printf(file, "}\n");
		} else {
			printf(file, "if(");
			cond.printToFile(file, printer, 0);
			printf(file, "){\n");
			first.printToFile(file, printer,  shift + 1);
			for (int i = 0; i < shift; i++)
				printf(file, "  ");
			printf(file, "}\n");
			for (int i = 0; i < shift; i++)
				printf(file, "  ");
			printf(file, "else{\n");
			second.printToFile(file, printer, shift + 1);
			for (int i = 0; i < shift; i++)
				printf(file, "  ");
			printf(file, "}\n");
		}
	}
}
