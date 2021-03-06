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

public class SwitchStmt extends JavaStmt {
	JavaExpr expr;
	JavaStmt stmt;
	
	public SwitchStmt(JavaExpr expr, JavaStmt stmt) {
		this.expr = expr;
		this.stmt = stmt;
	}
	
    public Object clone()
    {
        return new SwitchStmt(expr, stmt);
        
    }
	
	public void printToFile(FileWriter file, JavaPrinter printer, int shift) {

		for (int i = 0; i < shift; i++)
			printf(file, "  ");
		printf(file, "switch(");
		expr.printToFile(file, printer, 0);
		printf(file, "){\n");
		stmt.printToFile(file, printer, shift + 2);
		// for(int i=0; i<shift; i++)
		// printf(file, " ");
		printf(file, "}\n");
	}
}
