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
package de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.transformer.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.jbpt.petri.PetriNet;
import org.jbpt.pm.ProcessModel;
import org.junit.Test;

import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.TestModelBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.IPersistenceApi;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Representation;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.orientdb.test.RepresentationFactory;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.orientdbObj.PersistenceApiOrientDbObj;
import de.uni_potsdam.hpi.bpt.promnicat.util.Constants;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.transformer.ProcessModelToPetriNetUnit;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataClassification;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataClassification;

/**
 * Test class for {@link ProcessModelToPetriNetUnit}
 * @author Tobias Hoppe
 *
 */
public class ProcessModelToPetriNetUnitTest {
	
	private ProcessModelToPetriNetUnit unit = new ProcessModelToPetriNetUnit();
	private ProcessModel model = TestModelBuilder.getModelWithoutOrGateway();
	private IPersistenceApi persistenceApi = PersistenceApiOrientDbObj.getInstance(Constants.TEST_DB_CONFIG_PATH);

	@Test
	public void testGetName(){
		assertEquals("ProcessModelToPetriNetUnit", unit.getName());
	}
	
	@Test
	public void testGetInputType(){
		assertEquals(ProcessModel.class, unit.getInputType());
	}
	
	@Test
	public void testGetOutputType(){
		assertEquals(PetriNet.class, unit.getOutputType());
	}

	@Test
	public void testExecuteWithoutDbAccess() throws IllegalTypeException {
		IUnitDataClassification<Object> unitData = new UnitDataClassification<Object>(this.model);
		unit.execute(unitData);
		PetriNet pn = unitData.getPetriNet();
		
		//ensure successful parsing
		assertNotNull(pn);
		assertSame(unitData.getValue(), pn);
		assertEquals(1, pn.getSinkNodes().size());
		assertEquals(1, pn.getSourceNodes().size());
		assertEquals(17, pn.getNodes().size());
		assertEquals(18, pn.getFlow().size());
		assertEquals(9, pn.getPlaces().size());
		assertEquals(8, pn.getTransitions().size());
		assertEquals(2, pn.getSilentTransitions().size());
	}
	
	@Test
	public void testExecuteWithDbAccess() throws IllegalTypeException {
		Representation repr = RepresentationFactory.createLightweightRepresentation();
		persistenceApi.savePojo(repr.getModel());
		IUnitDataClassification<Object> unitData = new UnitDataClassification<Object>(this.model);
		unitData.setDbId(repr.getDbId());
		ProcessModelToPetriNetUnit unitPm2Pn = new ProcessModelToPetriNetUnit(this.persistenceApi);
		unitPm2Pn.execute(unitData);
		
		//ensure successful parsing
		PetriNet pn = unitData.getPetriNet();
		assertNotNull(pn);
		assertSame(unitData.getValue(), pn);
		
		//check successful saving in db
		//TODO uncomment if petri net serialization has been implemented
//		Model model = persistenceApi.loadCompleteModelWithDbId(repr.getModel().getDbId());
//		boolean found = false;
//		for(Representation representation : model.getLatestRevision().getRepresentations()) {
//			if(representation.getFormat().equals(Constants.FORMATS.PNML) && representation.getNotation().equals(Constants.NOTATIONS.PETRINET.toString())) {
//				found = true;
//			}
//		}
//		assertTrue("Petri Net has not been saved as expected!", found);
	}
}
