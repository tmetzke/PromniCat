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
package de.uni_potsdam.hpi.bpt.promnicat.util.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

import de.uni_potsdam.hpi.bpt.promnicat.analysisModules.metrics.ProcessMetrics;
import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants.METRICS;

/**
 * @author Tobias Metzke
 *
 */
public class WriterHelper {

	/**
	 * split element for CSV file values
	 */
	private static final String ITEMSEPARATOR = ";";	
	
	/**
	 * Write the result into a CSV file
	 * @param resultSet the collected result of the chain execution
	 * @throws IOException if file can't be read or written
	 */
	public static void writeToFile(String filePath, Map<String, AnalysisProcessModel> models, String... method) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		boolean add_delete_method = method.length != 0 && method[0] == AnalysisConstant.ADD_DELETE.getDescription();
		Collection<? extends Enum> processModelMetrics;
		if (add_delete_method)
			processModelMetrics = AnalysisHelper.getIndividualMetrics();
		else	
			processModelMetrics = AnalysisHelper.getProcessModelMetrics();
		StringBuilder resultStringBuilder = new StringBuilder(addHeader(processModelMetrics));
		// collect result from each model
		for (AnalysisProcessModel model : models.values())
			resultStringBuilder.append(toCsvString(model,processModelMetrics));
		writer.write(resultStringBuilder.toString());
		writer.close();
	}

	/**
	 * add the specific header to the analysis files that includes
	 * the model path, revision and the metrics of this revision
	 * @return a String representation of the process model metrics
	 * separated by the {@link ProcessMetrics#ITEMSEPARATOR}.
	 */
	private static String addHeader(Collection<? extends Enum> metrics) {
		StringBuilder builder = new StringBuilder()
			.append("Process Model" + ITEMSEPARATOR)
			.append("Revision" + ITEMSEPARATOR);
		for (Enum metric : metrics)
			if (metric instanceof METRICS)
				builder.append(metric.name() + ITEMSEPARATOR);
			else if (metric instanceof AnalysisConstant) {
				builder
					.append(((AnalysisConstant) metric).getDescription() + AnalysisConstant.ADDITIONS.getDescription() + ITEMSEPARATOR)
					.append(((AnalysisConstant) metric).getDescription() + AnalysisConstant.DELETIONS.getDescription() + ITEMSEPARATOR);
			}
		builder.append("grows continuously?");
		return builder.toString();
	}
	
	/**
	 * helper method that puts the real data of the metrics into format.
	 * every model revision is put into a new line with the according metrics
	 * @param model that should be displayed in CSV format
	 * @return a String representation of the formatted model results
	 */
	private static String toCsvString(AnalysisProcessModel model, Collection<? extends Enum> metrics) {
		// collect all information from the revisions
		// display each revision in a separate line
		StringBuilder builder = new StringBuilder();
		for (AnalysisModelRevision revision : model.getRevisions().values()) {
			builder
				.append("\n")
				.append(model.getName())
				.append(ITEMSEPARATOR + revision.getRevisionNumber());
			for (Enum metric : metrics) {
				if (metric instanceof METRICS) 
					builder.append(ITEMSEPARATOR + revision.get((METRICS)metric).intValue());
				else if (metric instanceof AnalysisConstant) {
					String metricName = ((AnalysisConstant) metric).getDescription();
					builder
						.append(ITEMSEPARATOR + revision.get(metricName + AnalysisConstant.ADDITIONS.getDescription()).intValue())
						.append(ITEMSEPARATOR + revision.get(metricName + AnalysisConstant.DELETIONS.getDescription()).intValue());
				}
					
			}
		}
		builder.append(ITEMSEPARATOR + model.isGrowing());
		return builder.toString();
	}
	
	

	/**
	 * write the analysis into a new file
	 * @param features
	 * @throws IOException
	 */
	public static void writeAnalysisWith(String filePath, Map<String, Integer> features)
			throws IOException {
		// write growing models
		StringBuilder resultBuilder = new StringBuilder()
			.append(AnalysisConstant.NUM_MODELS.getDescription() + ITEMSEPARATOR)
			.append(AnalysisConstant.NUM_GROWING.getDescription() + ITEMSEPARATOR)
			.append(AnalysisConstant.NUM_NOT_GROWING.getDescription())
			.append("\n")
			.append(features.get(AnalysisConstant.NUM_MODELS.getDescription()) + ITEMSEPARATOR)
			.append(features.get(AnalysisConstant.NUM_GROWING.getDescription()) + ITEMSEPARATOR)
			.append(features.get(AnalysisConstant.NUM_NOT_GROWING.getDescription()))
			.append("\n\n");			
		
		// write high/low/same analysis
		Collection<METRICS> processModelMetrics = AnalysisHelper.getProcessModelMetrics();
		for (METRICS metric : processModelMetrics)
			resultBuilder.append(ITEMSEPARATOR + metric);
		String[] measures = {AnalysisConstant.HIGHER.getDescription(),AnalysisConstant.SAME.getDescription(),AnalysisConstant.LOWER.getDescription()};
		
		for (String measure : measures) {
			resultBuilder
				.append("\n")
				.append(measure);
			for (METRICS metric : processModelMetrics)
				resultBuilder.append(ITEMSEPARATOR + features.get(metric.name() + measure));
		}
		
		// write lazy revisions
		resultBuilder
			.append("\n\n")
			.append(AnalysisConstant.NUM_REVISIONS.getDescription() + ITEMSEPARATOR)
			.append(AnalysisConstant.ALTERING_REVISIONS.getDescription() + ITEMSEPARATOR)
			.append(AnalysisConstant.UNALTERING_REVISIONS.getDescription() +ITEMSEPARATOR)
			.append("\n")
			.append(features.get(AnalysisConstant.NUM_REVISIONS.getDescription()) +ITEMSEPARATOR)
			.append(features.get(AnalysisConstant.ALTERING_REVISIONS.getDescription()) +ITEMSEPARATOR)
			.append(features.get(AnalysisConstant.UNALTERING_REVISIONS.getDescription()) +ITEMSEPARATOR);
		
		// write high level model language analysis
		resultBuilder.append("\n\n");
		for (AnalysisConstant languageConstant : AnalysisHelper.getModelLanguageMetrics())
			if (features.containsKey(languageConstant.getDescription()))
					resultBuilder.append(languageConstant.getDescription() + ITEMSEPARATOR);
		resultBuilder.append("\n");
		for (AnalysisConstant languageConstant : AnalysisHelper.getModelLanguageMetrics())
			if (features.containsKey(languageConstant.getDescription()))
				resultBuilder.append(features.get(languageConstant.getDescription()) + ITEMSEPARATOR);
		
		// write to file
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		writer.write(resultBuilder.toString());
		writer.close();
	}

	public static void writeModelLanguage(String filePath,
			Map<String, AnalysisProcessModel> analyzedModels) throws IOException {
		// header
		StringBuilder resultBuilder = new StringBuilder()
			.append("Process Model" + ITEMSEPARATOR)
			.append("Revision" + ITEMSEPARATOR)
			.append("Model Language");
		// language elements for every revision
		for (AnalysisProcessModel model : analyzedModels.values())
			for (AnalysisModelRevision revision : model.getRevisions().values()) {
				resultBuilder
				.append("\n")
				.append(model.getName())
				.append(ITEMSEPARATOR + revision.getRevisionNumber());
				int count = 0;
				for (String languageElement : revision.getMetrics().keySet()) {
					if (count > 0) resultBuilder.append(",");
					else resultBuilder.append(ITEMSEPARATOR);
					resultBuilder.append(languageElement);
					count++;
				}
			}
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		writer.write(resultBuilder.toString());
		writer.close();
	}
}
