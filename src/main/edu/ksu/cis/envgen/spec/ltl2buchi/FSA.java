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
package edu.ksu.cis.envgen.spec.ltl2buchi;

import java.util.*;
import java.util.logging.Logger;

import edu.ksu.cis.envgen.spec.LTLNode;
import edu.ksu.cis.envgen.util.MultiSet;

/**
 * Implementation of a finite state automaton used for code generation of
 * drivers.
 */
public class FSA {

	int maxSize;

	/**
	 * Automaton states that keep track of transitions and what states they lead
	 * to.
	 */
	FSAState[] fsaStates;

	int actualSize;

	int initialState;

	Logger logger = Logger.getLogger("envgen.spec.ltl2buchi");

	/**
	 * Takes a buchi automaton, throws away states that do not confrom to a
	 * single event condition, performs simple optimizations.
	 */
	public FSA(MultiSet buchi, int numActions) {
		int i, j;
		Intgr temp;
		maxSize = (actualSize = buchi.size()) + 1;
		fsaStates = new FSAState[maxSize];
		Node[] nds = new Node[maxSize];
		MultiSet[] propositions = new MultiSet[maxSize];
		Node tempNode;
		MultiSet tempSet;
		int statesCount = 0;

		Iterator bi = buchi.iterator();
		for (i = 1; bi.hasNext() && i < maxSize; i++) {
			tempNode = (Node) bi.next();

			// check whether conforms to single event cond
			// rewrite propositions from & to |
			tempSet = filter(tempNode.propositions, numActions);
			// if the state conforms to a single event condition
			// include it into the fsa
			// if the state doesn't conform
			// exclude it from fsa
			if (!tempSet.isEmpty()) {
				statesCount++;
				nds[i] = tempNode;
				propositions[i] = tempSet;
				fsaStates[i] = new FSAState();
			}
		}

		if (statesCount == 0)
			logger
					.severe("FSA: no finite state automaton that conforms to a single event condition");

		// for(i=0; i<maxSize; i++)
		// fsaStates[i] = new FSAState();

		// create the initial state
		fsaStates[0] = new FSAState();

		for (i = 0; i < maxSize; i++) {
			if (nds[i] != null || i == 0) {
				int currentID = (i == 0) ? 0 : nds[i].id;
				temp = new Intgr(currentID);
				for (j = 1; j < maxSize; j++) {
					if (nds[j] != null && nds[j].incoming.contains(temp)) {
						fsaStates[i].outgoing.add(new Intgr(j));
						fsaStates[j].incoming.add(new Intgr(i));
					}
				}
			}
		}
		logger.fine("******before optimization: " + this);
		while (optimize(propositions)) {
		} // to iterate until no change

		logger.fine("******after optimization:" + this);

		for (i = 1; i < maxSize; i++) {
			if (fsaStates[i] == null)
				continue;
			Iterator oi = fsaStates[i].outgoing.iterator();
			Iterator pi; // propositions
			while (oi.hasNext()) {
				int tempr = ((Intgr) oi.next()).getNum();
				pi = propositions[i].iterator();
				while (pi.hasNext())
					fsaStates[i].transitions.addLast(new Transition(((Intgr) pi
							.next()).getNum(), tempr));
			}
		}

		logger.fine("******without initial:" + this);
		makeInitial();
		logger.fine("******with initial:" + this);
	}

	/**
	 * Check whether propositions set conforms to single event condition and
	 * rewrites a set in terms of || (vs &&).
	 */
	public MultiSet filter(MultiSet propositions, int numActions) {
		int i;
		int index = 0;
		MultiSet result = new MultiSet();
		boolean[] actionsNotAllowed = new boolean[numActions]; // false means
																// action is
																// allowed

		boolean singleEvent = false; // no atomic proposition required

		if (propositions.isEmpty())
			for (i = 0; i < numActions; i++)
				result.add(new Intgr(i));
		else {
			LTLNode temp;
			Iterator pi = propositions.iterator();
			while (pi.hasNext()) {
				temp = (LTLNode) pi.next();
				if (temp.getKind() == 'P')
					if (singleEvent)
						return result; // empty at this point
					else {
						singleEvent = true;
						index = temp.getVal();
					}
				else
					// must be negation
					actionsNotAllowed[temp.getRight().getVal()] = true; // not
																		// allowed
			}
			if (singleEvent)
				result.add(new Intgr(index));
			else {
				for (i = 0; i < numActions; i++)
					if (!actionsNotAllowed[i])
						result.add(new Intgr(i));
			}
		}
		return result;
	}

	/**
	 * Collapses states with similar incoming and similar outgoing transitions.
	 */
	public boolean optimize(MultiSet[] propositions) {
		boolean change = false;
		int i, j;
		Iterator edges;
		//MultiSet temp;
		for (i = 1; i < maxSize; i++) {
			if (fsaStates[i] == null)
				continue;
			for (j = i + 1; j < maxSize; j++) {
				if (fsaStates[j] == null)
					continue;
				if (fsaStates[i].incoming.equals(fsaStates[j].incoming)
						&& fsaStates[i].outgoing.equals(fsaStates[j].outgoing)) {
					// get incoming states of the remaining state and remove
					// deleted state from their outgoing
					edges = fsaStates[i].incoming.iterator();
					while (edges.hasNext()) {
						fsaStates[((Intgr) edges.next()).getNum()].outgoing
								.remove(new Intgr(j));
					}
					edges = fsaStates[i].outgoing.iterator();
					while (edges.hasNext()) {
						fsaStates[((Intgr) edges.next()).getNum()].incoming
								.remove(new Intgr(j));
					}
					propositions[i].merge(propositions[j]);
					fsaStates[j] = null;
					actualSize--;
					change = true;
				}
			}
		}
		return change;
	}

	/**
	 * Makes numbering of states continuous, not implemented yet.
	 */
	public void rearrange(MultiSet[] propositions) {
		FSAState[] tempStates = new FSAState[actualSize];
	}

	/**
	 * Takes one of the initial states, marks it as a new initial state, removes
	 * the rest of the initial states, adding their transitions and propositions
	 * to the new initial state.
	 */
	public void makeInitial() {
		logger.fine("makeInitial: " + this);
		Iterator initStates = fsaStates[0].outgoing.iterator();

		initialState = ((Intgr) (fsaStates[0].outgoing.getFirst())).getNum();
		logger.fine("Initial state: " + initialState);

		Iterator oi = fsaStates[0].outgoing.iterator();

		Iterator ti = null;
		int tempID;

		while (oi.hasNext()) {
			tempID = ((Intgr) (oi.next())).getNum();
			logger.fine("outgoing: " + tempID);

			ti = fsaStates[tempID].transitions.iterator();
			fsaStates[tempID].incoming.remove(new Intgr(0));

			if (tempID != initialState && fsaStates[tempID].incoming.isEmpty()) {
				logger.fine("removing: " + tempID);
				fsaStates[tempID] = null;
			}

			while (ti.hasNext() && tempID != initialState) {
				logger.fine("adding transitions");
				fsaStates[initialState].transitions.add(ti.next());
			}

		}

	}

	public int getInitialState() {
		return initialState;
	}

	public FSAState[] getStates() {
		return fsaStates;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public String toString() {
		int i;
		String result = "maxSize=" + maxSize + ", numStates=" + actualSize
				+ ", initial=" + initialState + "\n";
		for (i = 0; i < maxSize; i++)
			if (fsaStates[i] != null)
				result += "dfsaState" + i + " " + fsaStates[i] + "\n";

		return result;
	}
}
