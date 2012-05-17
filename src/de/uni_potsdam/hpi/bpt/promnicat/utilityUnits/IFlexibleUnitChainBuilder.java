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
package de.uni_potsdam.hpi.bpt.promnicat.utilityUnits;

import java.util.Collection;

import org.jbpt.pm.ProcessModel;

import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants;

/**
 * 
 * @author Tobias Metzke
 *
 */
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
