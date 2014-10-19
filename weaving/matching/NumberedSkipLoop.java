package abc.tm.weaving.matching;
import java.util.*;


/**
 * author: Caitlin Phillips
 */
public class NumberedSkipLoop extends SkipLoop {
	
	protected Set<Integer> TMList;
	
	
	/**
	 * Creates a new numbered skip loop which skips label <code>labelToSkip</code>
	 * at state <code>state</code>, for the tracematches in TMList.
	 * @param state the state to be set as source and target of the edge
	 * @param labelToSkip the label for this skip loop
	 * @param TMnum the Tracematch to which this skiploop corresponds
	 */
	public NumberedSkipLoop(SMNode state, String labelToSkip, int TMnum) {
		super(state, labelToSkip);
		this.TMList = new HashSet<Integer>();
		TMList.add(new Integer(TMnum));
		
	}
	
	/**
	 * Constructor which accepts a set of tracematch numbers rather than just one.
	 * @param state the state to be set as source and target of the edge
	 * @param labelToSkip the label for this skip loop
	 * @param TMs the set of Tracematch numbers to which this skiploop corresponds
	 */
	public NumberedSkipLoop(SMNode state, String labelToSkip, Set<Integer> TMs) {
		super(state, labelToSkip);
		this.TMList = new HashSet<Integer>();
		TMList.addAll(TMs);
		
	}
	/**
	 * @return TMList: the list of tracematches to which this skip-loop belongs
	 */
	public Set<Integer> getTMList(){
		return TMList;
	}
	
	/**
	 * Adds a new tracematch to TMList
	 * @param newTMnum: the number of the tracematch which is to be added
	 */
	public void addTMNumber(int newTMnum){
		TMList.add(new Integer(newTMnum));
	}
	/**
	 * Adds a set of tracematch numbers to TMList
	 * @param TMnums
	 */
	public void addAllNumbers(Set<Integer> TMnums){
		TMList.addAll(TMnums);
	}
	
	/**
	 * Tells whether this skip loop belongs to tracematch TMnum
	 * @param TMnum
	 * @return <code>true</code> if TMnum is in TMList, <code>false</code> otherwise 
	 */
	public boolean belongsTo(int TMnum){
		return(TMList.contains(new Integer(TMnum)));
	}
	/**
	 * 
     * Tells whether this loop is a numbered loop.
     * @return <code>true</code>
     */
	public boolean isNumbered(){
		return true;
	}
	/**
	 * @see abc.tm.weaving.matching.SkipLoop#toString()
	 */
	public String toString() {
		return ""+super.toString()+", "+ getTMList()+"";
	}

}
