package de.uni_potsdam.hpi.bpt.promnicat.utilityUnits;

import java.util.Collection;

import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants;

public interface IFlexibleUnitChainBuilder extends IUnitChainBuilder {

	/**
	 * Add a {@link IUnit} to the internal {@link IUnitChain}, that is used to calculate a
	 * large set of process model metrics for a jBPT {@link ProcessModel}.
	 * @param handleSubProcesses flag that indicates whether to include 
	 * available sub process in metric calculation or not
	 * @param metricsToCalculate a collection of metrics that shall be evaluated
	 * for each process model
	 * @throws IllegalTypeException if the unit's input and output value classes are not compatible.
	 */
	public void createProcessModelMetricsCalulatorUnit(Collection<ProcessMetricConstants.METRICS> metricsToCalculate, boolean handleSubProcesses) throws IllegalTypeException;
}
