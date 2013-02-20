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

import java.util.*;

/**
 * Data structure to hold a regular expression.
 */
public class RENode extends SpecNode {

	/**
	 * Kinds of nodes: '|' - choice, 'S' - sequence, '^ num' - times, '^ {num0,
	 * num1}' - range , '*' - Kleene Closure - zero or more, '+' - one or more,
	 * '?' - zero or one, 'P' - proposition.
	 */
	char kind;

	/** End of range value. */
	int val1 = -1;

	/** Operands for choice and sequence. */
	Vector operands;

	public RENode(char k, int v, Vector op) {
		kind = k;
		val = v;
		operands = op;
	}

	public RENode(char k, int v0, int v1, Vector op) {
		kind = k;
		val = v0;
		val1 = v1;
		operands = op;
	}

	public void orCombine() {
		if (kind == '|') {
			Enumeration ope = operands.elements();
			RENode node;
			while (ope.hasMoreElements()) {
				node = (RENode) ope.nextElement();
				if (node.kind == '|') {
					node.orCombine();
					operands.remove(node);
					operands.addAll(node.operands);
				}
			}
		}
	}

	public void seqCombine() {
		if (kind == 'S') {
			Enumeration ope = operands.elements();
			RENode node;
			while (ope.hasMoreElements()) {
				node = (RENode) ope.nextElement();
				if (node.kind == 'S') {
					node.orCombine();
					operands.remove(node);
					operands.addAll(node.operands);
				}
			}
		}
	}

	public char getKind() {
		return kind;
	}

	public int getVal() {
		return val;
	}

	public void setVal(int i) {
		this.val = i;
	}

	public int getSecondVal() {
		return val1;
	}

	public void setSecondVal(int i) {
		val1 = i;
	}

	public Vector getOperands() {
		return operands;
	}

	public void addOperand(RENode spec) {
		operands.add(spec);
	}

	public boolean equals(Object obj) {
		assert (obj != null);
		if (!(obj instanceof RENode))
			return false;
		RENode node = (RENode) obj;
		if (kind != node.kind)
			return false;
		switch (kind) {
		case '|':
			return operands.equals(node.operands);
		case '^':
			return ((operands.equals(node.operands)) && val == node.val);
		case 'S':
			return operands.equals(node.operands);
		case 'P':
			return (val == node.val);
		default:
			logger.severe("RNode, equals, wrong kind of node " + node);
		}
		return false;
	}

	public String toString() {
		String temp = "";
		Iterator ni;
		switch (kind) {
		case '|':
			temp = "";
			ni = operands.iterator();
			if (ni.hasNext())
				temp = "(" + (RENode) (ni.next()) + ")";
			while (ni.hasNext())
				temp = temp + " | " + "(" + (RENode) ni.next() + ")";
			return temp;
		case 'S':
			temp = "";
			ni = operands.iterator();
			if (ni.hasNext())
				temp = "(" + (RENode) (ni.next()) + ")";
			while (ni.hasNext())
				temp = temp + ">" + "(" + (RENode) (ni.next()) + ")";
			return temp;
		case '^':
			temp = "";
			if (val1 == -1)
				temp = "(" + (RENode) ((operands).firstElement()) + ")^" + val;
			else
				temp = "(" + (RENode) ((operands).firstElement()) + ")^" + "{"
						+ val + "," + val1 + "}";
			return temp;

		case '*':
			temp = "(" + (RENode) ((operands).firstElement()) + ")*";
			return temp;

		case '+':
			temp = "(" + (RENode) ((operands).firstElement()) + ")+";
			return temp;

		case '?':
			temp = "(" + (RENode) ((operands).firstElement()) + ")?";
			return temp;

		case 'P': // temp = "p" + val;
					// return temp;
			return proposition.toString();

		default:
			logger.severe("\nRNode: unexpected node kind " + kind);

		}
		return null;
	}
}
