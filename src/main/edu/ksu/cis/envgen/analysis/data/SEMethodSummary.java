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
package edu.ksu.cis.envgen.analysis.data;

import edu.ksu.cis.envgen.analysis.*;
import edu.ksu.cis.envgen.analysis.pta.*;
import edu.ksu.cis.envgen.util.MultiSet;

//TODO: not currently used
//refactor Must and Return SE into separate classes?
public class SEMethodSummary extends MethodSummary{
	PTMethodSummary ptSummary; 
	DataFlowSet sideEffects;
	DataFlowSet mustSideEffects;
	MultiSet returnSideEffects;

	public SEMethodSummary(PTMethodSummary ptSummary){
		this.ptSummary = ptSummary;
	}
	
	public DataFlowSet getSideEffects(){
		return sideEffects;
	}

	public DataFlowSet getMustSideEffects() {
		return mustSideEffects;
	}

	public MultiSet getReturnSideEffects() {
		return returnSideEffects;
	}
}
