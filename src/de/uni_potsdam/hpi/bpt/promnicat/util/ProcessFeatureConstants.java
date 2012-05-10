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

import java.util.Collection;
import java.util.Iterator;

import org.jbpt.pm.FlowNode;
import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.transformer.ModelToFeatureVectorUnit;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataProcessMetrics;

/**
 *  Class containing all necessary constants regarding the configuration of the 
 * {@link ModelToFeatureVectorUnit}. Inherits from {@link ProcessMetricConstants} 
 * to also include the process metrics
 * 
 * @author Cindy Fähnrich
 *
 */
public class ProcessFeatureConstants extends ProcessMetricConstants {

	public static final String PROCESS_NAME = "ProcessName";
	public static final String FIRST_FLOWNODE_LABEL = "FirstFlowNodeLabel";
	
	public enum PROCESS_LABELS{
		PROCESS_NAME(ProcessFeatureConstants.PROCESS_NAME){
			/** returns the name of the process model */
			public String getAttribute(IUnitDataProcessMetrics<?> input){
				String result = ((ProcessModel)input.getValue()).getName();
				if (result == null){
					return "no title";
				}
				return result;
			}
		},
		FIRST_FLOWNODE_LABEL(ProcessFeatureConstants.FIRST_FLOWNODE_LABEL){
			/** returns the label of the first flownode of the process model */
			public String getAttribute(IUnitDataProcessMetrics<?> input){
				String result = ((ProcessModel)input.getValue()).getName();
				result = "";
				Collection<FlowNode> coll = ((ProcessModel)input.getValue()).getFlowNodes();
				Iterator<FlowNode> it = coll.iterator();
				FlowNode n;
				if ((n = it.next()) != null){
					result = n.getName();
					if (result == ""){
						return "no title";
					}
					return result;
				} else {
					return "no name";
				}
			};
		};
		
		private String description;
	     
		PROCESS_LABELS(String description) {
			this.description = description;
	    }

		public String toString() {
			return description;
		}
		public abstract String getAttribute(IUnitDataProcessMetrics<?> input);
	};
}
