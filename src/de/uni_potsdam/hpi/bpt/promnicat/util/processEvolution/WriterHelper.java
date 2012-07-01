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
package de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import de.uni_potsdam.hpi.bpt.promnicat.util.processEvolution.api.IAnalysis;

/**
 * @author Tobias Metzke
 *
 */
public class WriterHelper {

	/**
	 * Write the result into a CSV file
	 * @param filePath
	 * @param relativeDifference
	 * @throws IOException
	 */
	public static void writeToCSVFile(String filePath, IAnalysis relativeDifference) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		writer.write(relativeDifference.toResultCSVString());
		writer.close();
	}
}
