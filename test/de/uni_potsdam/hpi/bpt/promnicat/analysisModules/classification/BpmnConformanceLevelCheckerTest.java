/**
 * 
 */
package de.uni_potsdam.hpi.bpt.promnicat.analysisModules.classification;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.TestModelBuilder;

/**
 * Test class for {@link BpmnConformanceLevelChecker}
 * 
 * @author Tobias Hoppe
 *
 */
public class BpmnConformanceLevelCheckerTest {
	
	@Test
	public void testDescriptiveModelConformance() {
		BpmnConformanceLevelChecker modelChecker = new BpmnConformanceLevelChecker(TestModelBuilder.createDescriptiveConformBpmnModel());
		assertTrue(modelChecker.isDescriptiveModelingConform());
		assertTrue(modelChecker.isAnalyticModelingConform());
		assertTrue(modelChecker.isCommonExecutableModelingConform());
	}

	@Test
	public void testAnalyticModelConformance() {
		BpmnConformanceLevelChecker modelChecker = new BpmnConformanceLevelChecker(TestModelBuilder.getConnectedBpmnModel());
		assertFalse(modelChecker.isDescriptiveModelingConform());
		assertTrue(modelChecker.isAnalyticModelingConform());
		assertTrue(modelChecker.isCommonExecutableModelingConform());
	}
	
	@Test
	public void testOnlyExecutableModelConformance() {
		BpmnConformanceLevelChecker modelChecker = new BpmnConformanceLevelChecker(TestModelBuilder.createOnlyExecutableConformBpmnModel());
		assertFalse(modelChecker.isDescriptiveModelingConform());
		assertFalse(modelChecker.isAnalyticModelingConform());
		assertTrue(modelChecker.isCommonExecutableModelingConform());
	}

}
