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

import static org.junit.Assert.*;

import org.jbpt.petri.PetriNet;
import org.jbpt.pm.ProcessModel;
import org.jbpt.utils.TransformationException;
import org.junit.Test;

import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.TestModelBuilder;

/**
 * Test class for {@link ProcessModelToPetriNetConverter}.
 * @author Tobias Hoppe
 *
 */
public class ProcessModelToPetriNetConverterTest {

	private static final String GOT_UNEXPECTED_TRANSFORMATION_EXCEPTION = "Got unexpected transformation exception!";

	@Test
	public void testSequenceConverting() {
		ProcessModel model = TestModelBuilder.getSequence(5);
		ProcessModelToPetriNetConverter converter = new ProcessModelToPetriNetConverter();		
		try {
			PetriNet pn = converter.convertToPetriNet(model);
			
			assertEquals(5, pn.getNodes().size());
			assertEquals(4, pn.getFlow().size());
			assertEquals(3, pn.getPlaces().size());
			assertEquals(2, pn.getTransitions().size());
			assertEquals(1, pn.getSinkNodes().size());
			assertEquals(1, pn.getSourceNodes().size());
		} catch (TransformationException e) {
			fail(GOT_UNEXPECTED_TRANSFORMATION_EXCEPTION);
		}
	}
	
	@Test
	public void testModelWithoutOrgatewayAndAttachedEvent() {
		ProcessModel model = TestModelBuilder.getModelWithoutOrGateway();
		ProcessModelToPetriNetConverter converter = new ProcessModelToPetriNetConverter();		
		try {
			PetriNet pn = converter.convertToPetriNet(model);
			assertEquals(1, pn.getSinkNodes().size());
			assertEquals(1, pn.getSourceNodes().size());
			assertEquals(17, pn.getNodes().size());
			assertEquals(18, pn.getFlow().size());
			assertEquals(9, pn.getPlaces().size());
			assertEquals(8, pn.getTransitions().size());
			assertEquals(2, pn.getSilentTransitions().size());
		} catch (TransformationException e) {
			fail(GOT_UNEXPECTED_TRANSFORMATION_EXCEPTION);
		}
	}
	
}
