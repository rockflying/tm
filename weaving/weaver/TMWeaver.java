/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
 * Copyright (C) 2005 Pavel Avgustinov
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

package abc.tm.weaving.weaver;

import abc.weaving.weaver.Weaver;
import abc.main.Debug;
import abc.tm.weaving.aspectinfo.*;
import abc.tm.weaving.matching.StateMachine;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.dynainst.ShadowCountManager;

import java.util.*;

/** 
 * Modified weaver to implement TraceMatching
 *
 *  @author Julian Tibble
 *  @author Pavel Avgustinov
 *  @author Eric Bodden
 */
public class TMWeaver extends Weaver
{
	
    /**
     * set to true before the last (re)weaving pass
     */
    protected boolean nowLastWeavingPass = false;
    
    public void removeDeclareWarnings() {
    	nowLastWeavingPass = true;
    	super.removeDeclareWarnings();
    }

    // TODO: Add a dedicated flag for tracematch codegen debugging
    private void debug(String message)
    { if (abc.main.Debug.v().weaverDriver)
        System.err.println("WEAVER DRIVER (TM) ***** " + message);
    }

    
    public void weaveGenerateAspectMethods() {
    	
        // Generate methods inside aspects needed for code gen and bodies of
        //   methods not filled in by front-end (i.e. aspectOf())
        super.weaveGenerateAspectMethods();
        // also generate the code needed for tracematches, i.e. fill in the
        // advice bodies corresponding to each symbol being matched, and the
        // bodies for the different kinds of 'some' advice.
        debug("Generating code for tracematches");
        TraceMatchCodeGen tmcg = new TraceMatchCodeGen();
        List<TraceMatch> traceMatches = ((TMGlobalAspectInfo)abc.main.Main.v().getAbcExtension().getGlobalAspectInfo()).getTraceMatches();
		Iterator it = traceMatches.iterator();
        while(it.hasNext()) {
            TraceMatch tm = (TraceMatch)it.next();
            tmcg.fillInTraceMatch(tm);
        }
        
        
        System.err.println("So far, there are: "+abc.tm.ast.Regex_c.tmCount+" state machines");
 //---------------------------------------------------------------------------------------------------------------------------------------------       
        for (TraceMatch tm : traceMatches){
        	TMStateMachine tmStateMachine = (TMStateMachine) tm.getStateMachine();
			System.err.println(tmStateMachine);
        }

        
        for (TraceMatch tm : traceMatches) {
        	for(TraceMatch tm2: traceMatches){
        		if(tm.equals(tm2) || traceMatches.indexOf(tm2)< traceMatches.indexOf(tm))
        			continue;
        		Set<String> symbols1 = tm.getSymbols();
        		Set<String> symbols2 = tm2.getSymbols();
        		
        		boolean intersection = false;
        		
        		for(String sym : symbols1)
        			intersection = intersection || symbols2.contains(sym);
        		
        		if(intersection){
        			TMStateMachine SM1 = (TMStateMachine) tm.getStateMachine();
        			TMStateMachine SM2 = (TMStateMachine) tm2.getStateMachine();
        			
        			TMStateMachine mergedSM = SM1.merge(SM2, symbols1, symbols2);
        			System.err.println("Merged SM:" + "\n" + mergedSM.toString());
        			//mergedSM.toString();
        				
        				//what to do with merged TMSM once its been created??
        		}
        		
			
        	}
		}
        
        
        
//---------------------------------------------------------------------------------------------------------------------------------------------         
    }

    public void weaveAdvice()
    {
        if(Debug.v().shadowCount && nowLastWeavingPass) {
            //conjoin all residues with a residue for counting shadows
            ShadowCountManager.setCountResidues();
        }

        super.weaveAdvice();

        Iterator i = ((TMGlobalAspectInfo)
                        abc.main.Main.v().getAbcExtension()
                                         .getGlobalAspectInfo())
                                         .getTraceMatches().iterator();

        while (i.hasNext()) {
            TraceMatch tm = (TraceMatch) i.next();
            CodeGenHelper helper = tm.getCodeGenHelper();

            if (tm.hasITDAnalysisResults()) {
                System.out.println(tm.getITDAnalysisResults());

                if (tm.getITDAnalysisResults().canOptimise())
                    tm.doITDOptimisation();
            }

            helper.extractBodyMethod();
            helper.transformRealBodyMethod();
            helper.genRunSolutions();
        }
    }
}
