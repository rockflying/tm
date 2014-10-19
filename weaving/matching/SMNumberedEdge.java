package abc.tm.weaving.matching;
import java.util.*;
/**
 * @author Caitlin Phillips
 */
public class SMNumberedEdge extends SMEdge {
	protected Set<Integer> TMList;
	
	
	public SMNumberedEdge(SMNode from, SMNode to, String l, int TMnum) {
		super(from, to, l);
		
		this.TMList = new HashSet<Integer>();
		TMList.add(new Integer(TMnum));
	}
	
	public SMNumberedEdge(SMNode from, SMNode to, String l, Set<Integer> TMnums) {
		super(from, to, l);
		
		this.TMList = new HashSet<Integer>();
		TMList.addAll(TMnums);
	}
	
	/**
	 * Returns the list of tracematches to which this edge belongs
	 * @return TMList
	 */
	public Set<Integer> getTMList(){
		return TMList;
	}
	/**
	 * Adds a tracematch to TMList
	 * @param newTMnum: the number of the tracematch being added
	 */
	public void addTMNumber(int newTMnum){
		TMList.add(new Integer(newTMnum));
	}
	/**
	 * Adds a set of tracematches to TMList
	 * @param TMnums: the numbers of the tracematches to be added
	 */
	public void addAllNumbers(Set<Integer> TMnums){
		TMList.addAll(TMnums);
	}
	/**
	 * Removes a tracematch from TMList
	 * @param newTMnum: tracematch number to remove
	 */
	public void removeTMNumber(int newTMnum){
		TMList.remove(new Integer(newTMnum));
	}
	/**
	 * Checks if this edge belongs to traceMatch TMnum
	 * @param TMnum: number of the tracematch 
	 * @return <code>true</code> if TMnum is in TMList, <code>false</code> otherwise.
	 */
	public boolean belongsTo(int TMnum){
		return(TMList.contains(new Integer(TMnum)));
	}
	/**
	 * Checks if the edge is numbered
	 * @return <code>true</code>
	 */
	public boolean isNumbered(){
		return true;
	}
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return ""+super.toString()+", "+ getTMList().toString()+"";
	}

}
