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
package edu.ksu.cis.envgen;

import java.util.*;

/**
 *
 * Combines environment assumptions that may come from different sources. 
 * 
 */
public abstract class AssumptionsCombiner extends EnvGenModule {
	
	public abstract Assumptions generalize(List<Assumptions> assumptionsList);
}
