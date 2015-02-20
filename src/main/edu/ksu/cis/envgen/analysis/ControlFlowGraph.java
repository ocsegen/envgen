//this class is mostly taken from soot 
//TODO: remove 
package edu.ksu.cis.envgen.analysis;

import java.util.*;

import soot.toolkits.graph.*;
import soot.*;


public class ControlFlowGraph implements DirectedGraph {
	List heads;
	List tails;

	protected Map unitToSuccs;
	protected Map unitToPreds;
	//protected SootMethod method;
	//protected Body body;
	//protected Chain unitChain;
	Collection<Unit> unitChain;
	

	/**
	 *   Constructs  a graph for the units found in the provided
	 *   Body instance. Each node in the graph corresponds to
	 *   a unit. The edges are derived from the control flow.
	 *   
	 *   @param body               The underlying body we want to make a
	 *                             graph for.
	 *   @param addExceptionEdges  If true then the control flow edges associated with
	 *                             exceptions are added.
	 *   @param dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock This was added for Dava.
	 *                             If true, edges are not added from statement before area of
	 *                             protection to catch. If false, edges ARE added. For Dava,
	 *                             it should be true. For flow analyses, it should be false.
	 *   @see Body
	 *   @see Unit
	 */
	public ControlFlowGraph(
		Collection unitChain /*,
		boolean addExceptionEdges,
		boolean dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock */) {

		//body = unitBody;
		//unitChain = body.getUnits();
		this.unitChain = unitChain;
		int size = unitChain.size();
		//method = getBody().getMethod();

		// Build successors
		{
			unitToSuccs = new HashMap(size * 2 + 1, 0.7f);

			// Add regular successors
			{
				Iterator<Unit> unitIt = unitChain.iterator();
				Unit currentUnit, nextUnit;

				nextUnit = unitIt.hasNext() ?  unitIt.next() : null;

				while (nextUnit != null) {
					currentUnit = nextUnit;
					nextUnit = unitIt.hasNext() ? unitIt.next() : null;

					List successors = new ArrayList();

					// Put the next statement as the successor
					if (currentUnit.fallsThrough()) {
						if (nextUnit != null)
							successors.add(nextUnit);
					}

					if (currentUnit.branches()) {
						Iterator targetIt =
							currentUnit.getUnitBoxes().iterator();
						while (targetIt.hasNext()) {
							successors.add(
								((UnitBox) targetIt.next()).getUnit());
						}
					}

					// Store away successors
					unitToSuccs.put(currentUnit, successors);
				}
			}

			// Add exception based successors
			/*
			if (addExceptionEdges) {
				Map beginToHandler = new HashMap();

				Iterator trapIt = body.getTraps().iterator();

				while (trapIt.hasNext()) {
					Trap trap = (Trap) trapIt.next();

					Unit beginUnit = (Unit) trap.getBeginUnit();
					Unit handlerUnit = (Unit) trap.getHandlerUnit();
					Unit endUnit = (Unit) trap.getEndUnit();
					Iterator unitIt = unitChain.iterator(beginUnit);

					List handlersStartingHere =
						(List) beginToHandler.get(beginUnit);
					if (handlersStartingHere == null) {
						handlersStartingHere = new LinkedList();
						beginToHandler.put(beginUnit, handlersStartingHere);
					}
					handlersStartingHere.add(handlerUnit);

					for (Unit u = (Unit) unitIt.next();
						u != endUnit;
						u = (Unit) unitIt.next())
						 ((List) unitToSuccs.get(u)).add(handlerUnit);
				}
				
				// Add edges from the predecessors of begin statements directly to the handlers
				// This is necessary because sometimes the first statement of try block
				// is not even fully executed before an exception is thrown
				// WARNING: double negative!
				if (!dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock) {
					Iterator unitIt = body.getUnits().iterator();

					while (unitIt.hasNext()) {
						Unit u = (Unit) unitIt.next();

						List succs = ((List) unitToSuccs.get(u));

						List succsClone = new ArrayList();
						succsClone.addAll(succs);
						// need to clone it because you are potentially 
						// modifying it

						Iterator succIt = succsClone.iterator();

						while (succIt.hasNext()) {
							Unit succ = (Unit) succIt.next();

							List handlers = (List) beginToHandler.get(succ);
							if (handlers != null) {
								// Add an edge from u to each of succ's handlers.
								Iterator handlerIt = handlers.iterator();
								while (handlerIt.hasNext()) {
									Unit handler = (Unit) handlerIt.next();

									if (!succs.contains(handler))
										succs.add(handler);
								}
							}
						}
					}
				}
			}
			*/
			// Make successors unmodifiable
			/*
			{
				Iterator unitIt = unitChain.iterator();
				while (unitIt.hasNext()) {
					Unit s = (Unit) unitIt.next();

					unitToSuccs.put(
						s,
						Collections.unmodifiableList(
							(List) unitToSuccs.get(s)));
				}
			}
			*/
		}

		// Build predecessors
		{
			unitToPreds = new HashMap(size * 2 + 1, 0.7f);

			// initialize the pred sets to empty
			{
				Iterator<Unit> unitIt = unitChain.iterator();

				while (unitIt.hasNext()) {
					unitToPreds.put(unitIt.next(), new ArrayList());
				}
			}

			{
				Iterator<Unit> unitIt = unitChain.iterator();

				while (unitIt.hasNext()) {
					Unit s = (Unit) unitIt.next();

					// Modify preds set for each successor for this statement
					Iterator<Unit> succIt = ((List) unitToSuccs.get(s)).iterator();

					while (succIt.hasNext()) {
						Unit successor = (Unit) succIt.next();
						List predList = (List) unitToPreds.get(successor);
						try {
							predList.add(s);
						} catch (NullPointerException e) {
							G.v().out.println(s + "successor: " + successor);
							throw e;
						}
					}
				}
			}

			// Make pred lists unmodifiable.
			/*
			{
				Iterator unitIt = unitChain.iterator();

				while (unitIt.hasNext()) {
					Unit s = (Unit) unitIt.next();

					List predList = (List) unitToPreds.get(s);
					unitToPreds.put(s, Collections.unmodifiableList(predList));
				}
			}
			*/

		}

		// Build heads and tails
		{
			//List tailList = new ArrayList();
			//List headList = new ArrayList();
			tails = new ArrayList<Unit>();
			heads = new ArrayList<Unit>();

			// Build the sets
			{
				Iterator<Unit> unitIt = unitChain.iterator();

				while (unitIt.hasNext()) {
					Unit s = unitIt.next();

					List succs = (List) unitToSuccs.get(s);
					if (succs.size() == 0)
						tails.add(s);

					List preds = (List) unitToPreds.get(s);
					if (preds.size() == 0)
						heads.add(s);
				}
			}

			//tails = Collections.unmodifiableList(tailList);
			//heads = Collections.unmodifiableList(headList);
		}

	}

	/**
	 *   @return The underlying body instance this UnitGraph was built
	 *           from.
	 *
	 *  @see UnitGraph
	 *  @see Body
	 */
	//public Body getBody()
	//{
	//	return body;
	//}

	/**
	 *  Look for a path in graph,  from def to use. 
	 *  This path has to lie inside an extended basic block 
	 *  (and this property implies uniqueness.). The path returned 
	 *   includes from and to.
	 *
	 *  @param from start point for the path.
	 *  @param to   end point for the path. 
	 *  @return null if there is no such path.
	 */
	public List getExtendedBasicBlockPathBetween(Unit from, Unit to) {
		ControlFlowGraph g = this;

		// if this holds, we're doomed to failure!!!
		if (g.getPredsOf(to).size() > 1)
			return null;

		// pathStack := list of succs lists
		// pathStackIndex := last visited index in pathStack
		LinkedList pathStack = new LinkedList();
		LinkedList pathStackIndex = new LinkedList();

		pathStack.add(from);
		pathStackIndex.add(new Integer(0));

		int psiMax = (g.getSuccsOf((Unit) pathStack.get(0))).size();
		int level = 0;
		while (((Integer) pathStackIndex.get(0)).intValue() != psiMax) {
			int p = ((Integer) (pathStackIndex.get(level))).intValue();

			List succs = g.getSuccsOf((Unit) (pathStack.get(level)));
			if (p >= succs.size()) {
				// no more succs - backtrack to previous level.

				pathStack.remove(level);
				pathStackIndex.remove(level);

				level--;
				int q = ((Integer) pathStackIndex.get(level)).intValue();
				pathStackIndex.set(level, new Integer(q + 1));
				continue;
			}

			Unit betweenUnit = (Unit) (succs.get(p));

			// we win!
			if (betweenUnit == to) {
				pathStack.add(to);
				return pathStack;
			}

			// check preds of betweenUnit to see if we should visit its kids.
			if (g.getPredsOf(betweenUnit).size() > 1) {
				pathStackIndex.set(level, new Integer(p + 1));
				continue;
			}

			// visit kids of betweenUnit.
			level++;
			pathStackIndex.add(new Integer(0));
			pathStack.add(betweenUnit);
		}
		return null;
	}

	/* DirectedGraph implementation */
	public List getHeads() {
		return heads;
	}

	public List getTails() {
		return tails;
	}

	public List getPredsOf(Object s) {
		if (!unitToPreds.containsKey(s))
			throw new RuntimeException("Invalid stmt" + s);

		return (List) unitToPreds.get(s);
	}

	public List getSuccsOf(Object s) {
		if (!unitToSuccs.containsKey(s))
			throw new RuntimeException("Invalid stmt" + s);

		return (List) unitToSuccs.get(s);
	}
	
	public void removeEdge(Unit from, Unit to){
		if(unitToSuccs.containsKey(from)){
			List succs = (List)unitToSuccs.get(from);
			succs.remove(to);
		}
		else
			System.out.println("SEVERE: ControlFlowGraph, removeEdge, no stmt: "+from);
	}
	

	public int size() {
		return unitChain.size();
	}

	public Iterator iterator() {
		return unitChain.iterator();
	}

	public String toString() {
		Iterator<Unit> it = unitChain.iterator();
		StringBuffer buf = new StringBuffer();
		while (it.hasNext()) {
			Unit u = it.next();

			List l = new ArrayList();
			l.addAll(getPredsOf(u));
			buf.append("// preds " + l + "\n");
			buf.append(u.toString() + '\n');
			l = new ArrayList();
			l.addAll(getSuccsOf(u));
			buf.append("// succs " + l + "\n");
		}

		return buf.toString();
	}

}
