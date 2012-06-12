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
package de.uni_potsdam.hpi.bpt.promnicat.util.analysis.metricAnalyses;

import java.util.Collection;
import java.util.Map;

import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;
import de.uni_potsdam.hpi.bpt.promnicat.util.analysis.AnalysisProcessModel;

/**
 * @author Tobias Metzke
 *
 */
public class RelativeDifferenceAnalysis extends DifferenceAnalysis {

	public RelativeDifferenceAnalysis(Map<String, AnalysisProcessModel> modelsToAnalyze, Collection<METRICS> metrics) {
		super(modelsToAnalyze, metrics);
	}
	
	@Override
	protected double calculateDifference(METRICS metric, double actualValue, double oldValue) {
		double divisor = oldValue == 0 ? actualValue : oldValue;
		if (divisor == 0) divisor = 1;
		int factor = 100;
		double difference = (actualValue - oldValue) * factor / divisor;
		return difference;
	}

}
