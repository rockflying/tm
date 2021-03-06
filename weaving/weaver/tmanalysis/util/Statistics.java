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
package abc.tm.weaving.weaver.tmanalysis.util;

import abc.main.Debug;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;

/**
 * Prints statistics for whole-program optimizations on tracematches.
 *
 * @author Eric Bodden
 */
public class Statistics {
	
	public static volatile String lastStageCompleted = "not set";
	
	public static volatile boolean errorOccured = false;
	
	public static void print(String label, Object value) {
		if(Debug.v().tmShadowStatistics) {
			if(Debug.v().csv) {
				System.err.println("\":::::\",\""+label+"\",\""+value+"\",");
			} else {
				System.err.println(label+" = "+value);
			}
		}
	}

	public static void printFinalStatistics() {
		print("last-stage-completed",lastStageCompleted);
		print("full-shadow-count",ShadowRegistry.v().allShadows().size()+"");
		print("remaining-shadow-count",ShadowRegistry.v().enabledShadows().size()+"");
		print("error",errorOccured+"");
	}	

}
