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
package de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.extractor.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jbpt.petri.PetriNet;
import org.jbpt.pm.epc.Epc;
import org.junit.Test;

import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.TestModelBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.modelConverter.ModelToPetriNetConverter;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.extractor.PetriNetAnalyzerUnit;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataClassification;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataClassification;

/**
 * Test class for {@link PetriNetAnalyzerUnit}.
 * 
 * @author Tobias Hoppe
 */
public class PetriNetAnalyzerUnitTest {

	private static final PetriNetAnalyzerUnit unit = new PetriNetAnalyzerUnit();
	
	@Test
	public void testGetName(){
		assertTrue(unit.getName().equals("PetriNetAnalyzerUnit"));
	}
	
	@Test
	public void testGetInputType(){
		assertEquals(PetriNet.class, unit.getInputType());
	}
	
	@Test
	public void testGetOutputType(){
		assertEquals(PetriNet.class, unit.getOutputType());
	}
	
	@Test
	public void testExecute(){
		IUnitDataClassification<Object> unitData = new UnitDataClassification<Object>();
		PetriNet petriNet = null;
		try {
			petriNet = new ModelToPetriNetConverter().convertToPetriNet(TestModelBuilder.getSequence(5, Epc.class));
			unitData.setValue(petriNet);
			unit.execute(unitData);
		} catch (Exception e) {
			fail("Model to PetriNet convertion failed with: " + e.getMessage());
		}
		//TODO check all attributes coming from LoLA Soundness checker
		assertTrue(unitData.getSoundnessResults().isClassicalSound());
		assertFalse(unitData.isCyclic());
		assertTrue(unitData.isFreeChoice());
		assertTrue(unitData.isExtendedFreeChoice());
		assertTrue(unitData.isSNet());
		assertTrue(unitData.isTnet());
		assertTrue(unitData.isWorkflowNet());
	}
}
