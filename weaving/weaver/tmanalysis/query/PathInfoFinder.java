/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.StateMachine;
import abc.tm.weaving.weaver.tmanalysis.ds.Bag;
import abc.tm.weaving.weaver.tmanalysis.ds.HashBag;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.tm.weaving.weaver.tmanalysis.util.Naming;

/**
 * Computes the labels of all edges that dominate a final state in the state machine.
 * This means that all those events must occur in some order to lead to a match.
 * 
 * @author Eric Bodden
 */
public class PathInfoFinder {
	
	/**
	 * A predicate over a tracematch state machine state.
	 * @author Eric Bodden
	 */
	public interface StatePredicate {
		
		boolean match(State s);
		
	}

	protected Set<PathInfo> pathInfos;
	
	/**
	 * Constructs a new analysis for the given state machine.
	 * @param traceMatch a tracematch state machine.
	 */
	public PathInfoFinder(TraceMatch traceMatch) {
		this(traceMatch, new StatePredicate() {
			public boolean match(State s) {
				//treat only final states as final
				return s.isFinalNode();
			}			
		});
	}
	
	/**
	 * Constructs a new analysis for the given state machine.
	 * @param traceMatch a tracematch state machine.
	 * @param pred a predicate that decides which states to treat as a final state
	 */
	public PathInfoFinder(TraceMatch traceMatch, StatePredicate pred) {
		pathInfos = new HashSet<PathInfo>();
		
		StateMachine sm = traceMatch.getStateMachine();
		
		Set<List<SMEdge>> allPaths = findAllPaths(sm,pred);

		for (List<SMEdge> path : allPaths) {
			Bag<String> labSet = new HashBag<String>();
			Set<String> skipLoopLabelSet = new HashSet<String>();
			for (Iterator<SMEdge> edgeIter = path.iterator(); edgeIter.hasNext();) {
				SMEdge edge = edgeIter.next();
				String lab = Naming.getSymbolShortName(edge.getLabel());
				labSet.add(lab);
				SMNode tgt = edge.getTarget();
				for (Iterator<SMEdge> outIter = tgt.getOutEdgeIterator(); outIter.hasNext();) {
					SMEdge outEdge = outIter.next();
					if(outEdge.isSkipEdge()) {
						skipLoopLabelSet.add(outEdge.getLabel());
					}
				}
			}
			skipLoopLabelSet = Collections.unmodifiableSet(skipLoopLabelSet);			
			pathInfos.add(new PathInfo(labSet,skipLoopLabelSet));
		}

		pathInfos = Collections.unmodifiableSet(pathInfos);
		
		allPaths = null;
	}

	/**
	 * Computes all paths through the given state machine that end in a node
	 * matched by the given predicate. Loops are not taken into account, i.e.
	 * back-jumps are not followed.
	 * @param sm any tracematch state machine
	 * @param pred any state predicate
	 * @return a set of paths, where each path is stored as a list of edges,
	 * stored in the order induced by the automaton structure
	 */
	private Set<List<SMEdge>> findAllPaths(StateMachine sm, StatePredicate pred) {
		//first build a worklist of dummy edges, pointing to the initial states
		//of the automaton
		Set<List<SMEdge>> worklist = new HashSet<List<SMEdge>>(); 
		for (Iterator<State> stateIter = sm.getStateIterator(); stateIter.hasNext();) {
			SMNode s = (SMNode) stateIter.next();
			if(s.isInitialNode()) {
				LinkedList<SMEdge> path = new LinkedList<SMEdge>();
				path.add(new SMEdge(null,s,null));
				worklist.add(path);
			}
		}
		
		Set<List<SMEdge>> completePaths = new HashSet<List<SMEdge>>(); 		
		boolean changed = false;
		//no do a fixed point iteration...
		do {
			Set<List<SMEdge>> newWorklist = new HashSet<List<SMEdge>>();
			//for each partial path currently in the list, see if we can make it longer
			//by adding another edge to it...
			for (List<SMEdge> path : worklist) {
				//get last edge on path
				SMEdge inEdge = path.get(path.size()-1);
				//get the last node on the path
				SMNode s = inEdge.getTarget();
				//if the predicate matches, the partial path is indeed complete...
				if(pred.match(s)) {
					//cut off artificial start edge
					List<SMEdge> realPath = new LinkedList<SMEdge>(path.subList(1, path.size()));
					//store result
					completePaths.add(realPath);
				} else {
					//else, it is incomplete; look at all outgoing edges
					for (Iterator<SMEdge> iterator = s.getOutEdgeIterator(); iterator.hasNext();) {
						SMEdge outEdge = iterator.next();
						SMNode target = outEdge.getTarget();
						//if the target node is not yet part of the path (i.e. no cycle),
						//add the edge to the path and then add the new path to the new worklist 
						if(!containsState(path, target)) {
							List<SMEdge> newPath = new LinkedList<SMEdge>(path);
							newPath.add(outEdge);
							newWorklist.add(newPath);
						}
					}
				}
			}
			//have to iterate if the worklist changed
			changed = !worklist.equals(newWorklist);
			worklist = newWorklist;
		} while(changed);

		return completePaths;
	}
	
	/**
	 * Returns true if the given path contains the given state.
	 * @param path any list of edges
	 * @param s any state
	 * @return <code>true</code> if s is source or target state of any edge in path 
	 */
	protected boolean containsState(List<SMEdge> path, State s) {
		for (SMEdge edge : path) {
			if((edge.getSource()!=null && edge.getSource().equals(s))
			|| (edge.getTarget()!=null && edge.getTarget().equals(s))) {
				return true;
			}
		} 
		return false;
	}

	/**
	 * @return all path infos cumputed for the given tracematch
	 */
	public Set<PathInfo> getPathInfos() {
		return pathInfos;
	}
	
	/**
	 * A path info holds a bag of dominating labels and a set of skip-labels.
	 * The dominating labels have to be visited (as often as they are contained in the bag)
	 * in order to reach a final state. Skip loops with the given skip loop labels could interfere
	 * with a match on the same path to the final state.
	 * @author Eric Bodden
	 */
	public static class PathInfo {
		
		protected Bag<String> dominatingLabels;
		
		protected Set<String> skipLoopLabels;

		public PathInfo(Bag dominatingLabels, Set skipLoopLabels) {
			this.dominatingLabels = dominatingLabels;
			this.skipLoopLabels = skipLoopLabels;
		}

		/**
		 * @return the dominating labels
		 */
		public Bag<String> getDominatingLabels() {
			return new HashBag(dominatingLabels);
		}

		/**
		 * @return the skip loop labels
		 */
		public Set<String> getSkipLoopLabels() {
			return new HashSet(skipLoopLabels);
		}
		
		/**
		 * Returns the length of this path info, i.e.
		 * the number of labels that have to be taken
		 * in order to reach a final state along this path.
		 */
		public int length() {
			return getDominatingLabels().size();
		}
		
		/**
		 * Returns <code>true</code>, if the path info can be satisfied by the given
		 * set of shadows, i.e. if the set of shadows holds at least one shadow
		 * with the same label as in the bag of dominating labels for this path info.
		 * Note that multiplicity is not taken into account, i.e. a single shadow
		 * with label <i>l</i> can satisfy a path info with a bag of
		 * dominating labels <i>[l,l]</i>. 
		 * @param shadows a set of symbol shadows
		 */
		public boolean isSatisfiedByShadowSet(Set<? extends ISymbolShadow> shadows) {
			Set<String> containedLabels = new HashSet<String>();
			for (ISymbolShadow symbolShadow : shadows) {
				containedLabels.add(symbolShadow.getSymbolName());
			}
			return containedLabels.containsAll(dominatingLabels);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			return "<dom-labels="+dominatingLabels+",skip-labels="+skipLoopLabels+">";
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((dominatingLabels == null) ? 0 : dominatingLabels
							.hashCode());
			result = prime
					* result
					+ ((skipLoopLabels == null) ? 0 : skipLoopLabels.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final PathInfo other = (PathInfo) obj;
			if (dominatingLabels == null) {
				if (other.dominatingLabels != null)
					return false;
			} else if (!dominatingLabels.equals(other.dominatingLabels))
				return false;
			if (skipLoopLabels == null) {
				if (other.skipLoopLabels != null)
					return false;
			} else if (!skipLoopLabels.equals(other.skipLoopLabels))
				return false;
			return true;
		}
		
	}

}
 