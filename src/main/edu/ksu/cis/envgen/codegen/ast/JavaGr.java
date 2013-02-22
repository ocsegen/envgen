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

import edu.ksu.cis.envgen.codegen.ast.expr.AssignExpr;
import edu.ksu.cis.envgen.codegen.ast.expr.InvokeExpr;
import edu.ksu.cis.envgen.codegen.ast.expr.JavaExpr;
import edu.ksu.cis.envgen.codegen.ast.expr.NewExpr;
import edu.ksu.cis.envgen.codegen.ast.expr.StrExpr;
import edu.ksu.cis.envgen.codegen.ast.expr.TypeDeclExpr;
import edu.ksu.cis.envgen.codegen.ast.expr.ValueExpr;
import edu.ksu.cis.envgen.codegen.ast.stmt.CaseStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.CatchStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.DoWhileStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.EmptyStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.ExprStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.ForStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.IfStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.JavaStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.ReturnStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.SeqStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.SwitchStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.ThrowStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.TryStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.VDeclStmt;
import edu.ksu.cis.envgen.codegen.ast.stmt.WhileStmt;
import edu.ksu.cis.envgen.codegen.vals.*;

/**
 * Data structure for creation of Java grammar components such as statements and
 * expressions.
 */
//TODO: get rid of this class
public class JavaGr {
	
	public static JavaStmt newEmptyStmt() {
		return new EmptyStmt();
	}

	public static JavaStmt newReturnVoidStmt() {
		return new ReturnStmt();
	}

	public static JavaStmt newReturnStmt(JavaExpr op) {
		return new ReturnStmt(op);
	}

	public static JavaStmt newIfStmt(JavaExpr cond, JavaStmt body) {
		return new IfStmt(cond, body, null);
	}

	public static JavaStmt newIfElseStmt(JavaExpr cond, JavaStmt thenpart,
			JavaStmt elsepart) {
		return new IfStmt(cond, thenpart, elsepart);
	}

	public static JavaStmt newWhileStmt(JavaExpr cond, JavaStmt body) {
		return new WhileStmt(cond, body);
	}

	public static JavaStmt newDoWhileStmt(JavaExpr cond, JavaStmt body) {
		return new DoWhileStmt(cond, body);
	}

	public static JavaStmt newSequenceStmt(JavaStmt first, JavaStmt second) {
		return new SeqStmt(first, second);
	}

	public static JavaStmt newExprStmt(JavaExpr expr) {
		return new ExprStmt(expr);
	}

	public static JavaStmt newTryStmt(JavaStmt body) {
		return new TryStmt(body);
	}

	public static JavaStmt newCatchStmt(JavaStmt exptn, JavaStmt body) {
		return new CatchStmt(exptn, body);
	}

	public static JavaStmt newThrowStmt(JavaExpr name) {
		return new ThrowStmt(name);
	}

	public static JavaStmt newVDeclStmt(JavaExpr type, JavaStmt name) {
		return new VDeclStmt(type, name);
	}

	public static JavaStmt newSwitchStmt(JavaExpr choice, JavaStmt cases) {
		return new SwitchStmt(choice, cases);
	}

	public static JavaStmt newCaseStmt(JavaExpr value, JavaStmt body,
			JavaStmt next) {
		return new CaseStmt(value, body, next);
	}

	public static JavaStmt newForStmt(JavaExpr cond, JavaStmt body) {
		return new ForStmt(cond, body);

	}

	// -----------------------------------------------------------------

	public static JavaExpr newStrExpr(String name) {
		return new StrExpr(name);
	}

	public static JavaExpr newMethodCallExpr(String receiver, String m,
			List args) {
		return new InvokeExpr(receiver, m, args);
	}

	public static JavaExpr newAssignExpr(JavaExpr left, String op, JavaExpr right) {
		return new AssignExpr(left, op, right);
	}

	
	public static JavaExpr newNewExpr(String type, List args) {
		return new NewExpr(type,  args);
	}
	
	public static JavaExpr newValueExpr(SymLocValue value) {
		return new ValueExpr(value);
	}
	
	public static JavaExpr newDeclExpr(String type, String name, JavaExpr value) {
		return new TypeDeclExpr(type, name, value);
	}
	
}
