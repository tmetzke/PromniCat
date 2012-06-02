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
package de.uni_potsdam.hpi.bpt.promnicat.modelConverter;

import java.util.HashMap;

import org.jbpt.petri.PetriNet;
import org.jbpt.pm.ProcessModel;
import org.jbpt.pm.bpmn.Bpmn;
import org.jbpt.pm.epc.Epc;
import org.jbpt.utils.TransformationException;

/**
 * @author Tobias Hoppe
 *
 */
public class ModelToPetriNetConverter implements IModelToPetriNetConverter {
	
	/**
	 * Hashmap for delegater converters
	 */
	private HashMap<Class<?>, IModelToPetriNetConverter> delegates;
	/**
	 * Last converted {@link PetriNet}
	 */
	private PetriNet lastPetriNet = null;
	
	public ModelToPetriNetConverter() {
		this.delegates = new HashMap<Class<?>, IModelToPetriNetConverter>();
		
		this.delegates.put(Bpmn.class, new BpmnToPetriNetConverter());
		this.delegates.put(Epc.class, new EpcToPetriNetConverter());
		this.delegates.put(ProcessModel.class, new ProcessModelToPetriNetConverter());
	}

	/* (non-Javadoc)
	 * @see de.uni_potsdam.hpi.bpt.promnicat.modelConverter.IModelToPetriNetConverter#convertToPetriNet(org.jbpt.pm.ProcessModel)
	 */
	@Override
	public PetriNet convertToPetriNet(ProcessModel model) throws TransformationException {
		if (model instanceof Bpmn) {
			this.lastPetriNet = this.delegates.get(Bpmn.class).convertToPetriNet((Bpmn<?, ?>)model);
		} else if (model instanceof Epc) {
			this.lastPetriNet = this.delegates.get(Epc.class).convertToPetriNet((Epc)model);
		} else {
			this.lastPetriNet = this.delegates.get(ProcessModel.class).convertToPetriNet(model);
		}
		return this.lastPetriNet;
	}

	@Override
	public PetriNet getLastPetriNet() {
		return this.lastPetriNet;
	}

}
