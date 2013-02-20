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
package edu.ksu.cis.envgen.analysis.pta;

import edu.ksu.cis.envgen.analysis.MethodSummary;
import edu.ksu.cis.envgen.util.MultiSet;

public class PTMethodSummary extends MethodSummary{

	PTAnalysisTF aliases;
	MultiSet newLocations;
	MultiSet returnLocations;
	
	public PTMethodSummary(){
		
	}
	
	public PTAnalysisTF getAliases() {
		return aliases;
	}
	

	public MultiSet getNewLocations(){
		return newLocations;
	}

	public MultiSet getReturnLocations(){
		return returnLocations;
	}
}
