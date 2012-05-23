/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.extractor.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jbpt.pm.ProcessModel;
import org.junit.Test;

import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.TestModelBuilder;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.extractor.BpmnConformanceLevelCheckerUnit;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitData;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataClassification;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataClassification;

/**
 * Test class for {@link BpmnConformanceLevelCheckerUnit}
 * 
 * @author Tobias Hoppe
 */
public class BpmnConformanceLevelCheckerUnitTest {

	private static BpmnConformanceLevelCheckerUnit unit = new BpmnConformanceLevelCheckerUnit();
	
	@Test
	public void testGetName(){
		assertTrue(unit.getName().equals("BpmnConformanceLevelCheckerUnit"));
	}
	
	@Test
	public void testGetInputType(){
		assertEquals(ProcessModel.class, unit.getInputType());
	}
	
	@Test
	public void testGetOutputType(){
		assertEquals(ProcessModel.class, unit.getOutputType());
	}
	
	@Test
	public void testOneMetricCalculation() {
		IUnitData<Object> unitData = new UnitDataClassification<Object>(TestModelBuilder.getConnectedBpmnModel());
		try{
			IUnitDataClassification<Object> result = (IUnitDataClassification<Object>) unit.execute(unitData);
			assertSame(unitData, result);
			assertFalse(result.getDescriptiveModelingConformance());
			assertTrue(result.getAnalyticModelingConformance());
			assertTrue(result.getCommonExecutableModelingConformance());
			//TODO check further properties later on
		} catch (Exception e) {
			fail("got unexpected error with message: " + e.getMessage() + "\n" + e.getStackTrace().toString());
		}
	}
}
