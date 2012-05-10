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
package de.uni_potsdam.hpi.bpt.promnicat.analysisModules.classification;

import org.jbpt.pm.FlowNode;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.bpmn.BpmnControlFlow;

/**
 * THis class checks a given {@link Bpmn} process model for conformance according to
 * the BPMN Conformance Levels defined in the BPMN specification.
 * @author Tobias Hoppe
 *
 */
public class BpmnConformanceLevelChecker {
	
	private Bpmn<BpmnControlFlow<FlowNode>, FlowNode> model = null;
	
	public BpmnConformanceLevelChecker(Bpmn<BpmnControlFlow<FlowNode>, FlowNode> model) {
		this.model = model;
	}

	/**
	 * @return the model
	 */
	public Bpmn<BpmnControlFlow<FlowNode>, FlowNode> getModel() {
		return model;
	}
	
	public boolean isDescriptiveModelingConform() {
		//TODO implement me
		return false;
	}
	
	public boolean isAnalyticModelingConform() {
		//TODO implement me
		return false;
	}
	
	public boolean isCommonExecutableModelingConform() {
		//TODO implement me
		return false;
	}
}
