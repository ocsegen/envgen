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

import edu.ksu.cis.envgen.codegen.JavaPrinter;
import edu.ksu.cis.envgen.codegen.ast.expr.JavaExpr;

public class VDeclStmt extends JavaStmt{
	//TODO: elimininate or move to expr
	JavaExpr type;
	JavaStmt name;
	
	public VDeclStmt(JavaExpr type, JavaStmt name){
		this.type = type;
		this.name = name;
		
	}
	
    public Object clone()
    {
        return new VDeclStmt(type, name);
        
    }
	
	public void printToFile(FileWriter file, JavaPrinter printer, int shift){
		type.printToFile(file, printer, 0);
		printf(file, " ");
		name.printToFile(file, printer, -1);
	}
}
