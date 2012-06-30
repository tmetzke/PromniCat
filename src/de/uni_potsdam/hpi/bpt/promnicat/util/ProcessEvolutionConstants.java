/**
 * PromniCAT - Collection and Analysis of Business Process Models
 * Copyright (C) 2012 Cindy Fähnrich, Tobias Hoppe, Andrina Mascher
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_potsdam.hpi.bpt.promnicat.util;

import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;

/**
 * @author Tobi
 *
 */
public class ProcessEvolutionConstants {

	public static final String IS_GROWING = "isGrowing";
	public static final String NUM_ITERATIONS = "NumberOfCMRIterations";
	
	public enum PROCESS_EVOLUTION{
		IS_GROWING(ProcessEvolutionConstants.IS_GROWING){
			/** returns true if the model is always growing */
			public int getAttribute(AnalysisProcessModel input){
				return input.isGrowing() ? 1 : 0;
			}
		},
		NUM_ITERATIONS(ProcessEvolutionConstants.NUM_ITERATIONS){
			/** returns the number of CMR iterations the model went through */
			public int getAttribute(AnalysisProcessModel input){
				return input.getCMRIterations();
			}
		};
		
		private String description;
	     
		PROCESS_EVOLUTION(String description) {
			this.description = description;
	    }

		public String toString() {
			return description;
		}
		public abstract int getAttribute(AnalysisProcessModel input);
	};
}
