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
package edu.ksu.cis.envgen.applinfo;

import java.util.*;
import java.util.logging.Logger;


import soot.SootClass;
import soot.util.Chain;

import edu.ksu.cis.envgen.codegen.*;


/* Hierarchy for the environment classes, 
 * unlike Hierarchy in Soot, allows updates. */
public class EnvHierarchy {
	/** Keeps track of unit classes. */
	ModuleInfo unit;

	/**
	 * Keeps track of environment components, including only those that get
	 * called inside the unit.
	 */
	ModuleInfo env;

	//Map classToDirSubclasses;

	//Map classToSubclasses;

	Map<String, List> interfaceToSubinterfaces;

	/** To Dir Subinterfaces */
	Map<String, List> interfaceToDirSubinterfaces;

	
	Map<String, List> interfaceToImplementors;

	/** To Dir Implementors */
	public Map<String, List> interfaceToDirImplementors;

	Logger logger = Logger.getLogger("envgen.applinfo");

	public EnvHierarchy(ModuleInfo unit, ModuleInfo env) {
		this.unit = unit;
		this.env = env;

		// TODO: see if other maps are needed
		interfaceToDirSubinterfaces = new HashMap<String, List>();

		interfaceToDirImplementors = new HashMap<String, List>();
		
	}

	public Map<String, List> getDirImplementorsMap(){
		return interfaceToDirImplementors;
	}
	
	public Map<String, List> getDirSubinterfacesMap(){
		return interfaceToDirSubinterfaces;
	}
	
	public List<SootClass> getDirImplementorsOf(String interfcName) {
		return interfaceToDirImplementors.get(interfcName);
	}

	public List<SootClass> getDirImplementorsOf(SootClass sc) {
		String interfcName = sc.getName();
		return interfaceToDirImplementors.get(interfcName);
	}
	
	public List<SootClass> getDirSubinterfacesOf(String interfcName) {
		return interfaceToDirSubinterfaces.get(interfcName);
	}

	public List<SootClass> getDirSubinterfacesOf(SootClass sc) {
		String interfcName = sc.getName();
		return interfaceToDirSubinterfaces.get(interfcName);
	}
	

	public void addDirImplementor(String interfcName, SootClass markedClass) {
		List<SootClass> implementors = interfaceToDirImplementors.get(interfcName);
		if (implementors == null) {
			implementors = new ArrayList<SootClass>();
			interfaceToDirImplementors.put(interfcName, implementors);
		}
		if(!implementors.contains(markedClass))
			implementors.add(markedClass);

	}

	public void addInterface(String interfcName) {
		if(!interfaceToDirImplementors.containsKey(interfcName))
			interfaceToDirImplementors.put(interfcName, new ArrayList<SootClass>());
		
		
		if(!interfaceToDirSubinterfaces.containsKey(interfcName))
			interfaceToDirSubinterfaces.put(interfcName, new ArrayList());

	}
	
	public Collection getInterfaces(){
		return interfaceToDirImplementors.keySet();
	}
	
	public void addDirSubinterface(String interfcName, SootClass markedClass) {
		List<SootClass> implementors = interfaceToDirSubinterfaces.get(interfcName);
		if (implementors == null) {
			implementors = new ArrayList<SootClass>();
			interfaceToDirSubinterfaces.put(interfcName, implementors);
		}
		if(!implementors.contains(markedClass))
			implementors.add(markedClass);

	}
	
	
	public List<SootClass> getImplementorsOf(String interfcName){
		//TODO: to avoid recalculating, store in the map

		String name = JavaPrinter.getActualName(interfcName);
		List<SootClass> implementers = getDirImplementorsOf(name);
	
		
		List<SootClass> subinterfaces = getDirSubinterfacesOf(name);
		if(subinterfaces == null){
			logger.warning("Null Subinterfaces for class : "+name);
			return implementers;
		}

		SootClass sub;
		for(Iterator<SootClass> it = subinterfaces.iterator(); it.hasNext();){
			sub = it.next();
			List<SootClass> subImpl = getImplementorsOf(sub.getName());
			
			
			//implementers.addAll(subImpl);
			for(Iterator<SootClass> it2 = subImpl.iterator(); it2.hasNext();){
				SootClass sub2 = it2.next();
				if(!implementers.contains(sub2))
					implementers.add(sub2);
			}
		}
		
		return implementers;
	}

	public List<SootClass> getImplementorsOf(SootClass sc){
		String interfcName = sc.getName();
		return getImplementorsOf(interfcName);
	}
	
	/**
	 * This method gets interfaces that are directly implements by sc and the
	 * super interfaces of these interfaces.
	 * 
	 * @param sc
	 *            The soot class of which its direct implemented interfaces and
	 *            the super interfaces of these interfaces need to be found.
	 * @return A list of the interfaces that are directly implements by sc and
	 *         the super interfaces of these interfaces.
	 */
	public List<SootClass> getSuperInterfaceClasses(SootClass sc) {
		// the list that will contain the interfaces that are directly
		// implements by sc and the super interfaces of these interfaces
		ArrayList<SootClass> superInterfaceClasses = new ArrayList<SootClass>();

		// get the interfaces that are directly implemented by sc
		Chain directInterfaces = sc.getInterfaces();
		
		// get all the interfaces that are directly implemented by sc
		for (Iterator i = directInterfaces.iterator(); i.hasNext();) {
			// get an interface that is implemented by sc
			SootClass directInterface = (SootClass) (i.next());
			
			if (!superInterfaceClasses.contains(directInterface))
				// add directInterface to the list
				superInterfaceClasses.add(directInterface);
			
			// get the super interfaces of directInterface
			List superInterfaces = getSuperInterfaceClasses(directInterface);
			
			//add the super interfaces of the directInterface to the list
			for (Iterator j = superInterfaces.iterator(); j.hasNext();) {
				SootClass superinterface = (SootClass) (j.next());
				if (!superInterfaceClasses.contains(superinterface))
					superInterfaceClasses.add(superinterface);
			}
		}

		return superInterfaceClasses;
	}

	/**
	 * 
	 * Gets interfaces of the class.
	 * 
	 */
	/*
	public static HashSet getInterfaces(SootClass sc) {
		SootClass interfc;
		// String interfcName;
		HashSet result = new HashSet();
		if (sc.getInterfaceCount() == 0)
			return result;
		Chain interfaces = sc.getInterfaces();
		Iterator ii = interfaces.iterator();
		while (ii.hasNext()) {
			interfc = (SootClass) ii.next();
			// interfcName = interfc.getName();

			result.add(interfc);
			SootClass parent = interfc.getSuperclass();

			if (parent.isInterface())
				result.add(parent);
			// else {
			// Chain upper = getInterfaces(interfc);
			// Iterator ui = upper.iterator();
			// while (ui.hasNext())
			// result.add(ui.next());
			// }
		}
		return result;
	}
	*/


}
