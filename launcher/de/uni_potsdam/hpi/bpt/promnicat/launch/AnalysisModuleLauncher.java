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
package de.uni_potsdam.hpi.bpt.promnicat.launch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import com.jdotsoft.jarloader.JarClassLoader;

/**
 * Launcher class for analysis modules.
 * @author Tobias Hoppe
 *
 */
public class AnalysisModuleLauncher {

	/**
	 * Starts the analysis module given as first parameter in args. The name must contain the
	 * complete path starting from analysis module package.<br\>
	 * e.g. "example.Analysis" for de.uni_potsdam.hpi.bpt.promnicat.analysisModules.example.Analysis
	 * @param args first parameter is the analysis module being started.
	 * Any further parameters are passed on to the analysis module's main method.
	 */
	public static void main(String[] args) {
		JarClassLoader jcl = new JarClassLoader();
		String moduleName = args[0];
		Collection<String> arguments = new ArrayList<String>();
		for (int i = 1; i < args.length; i++) {
			arguments.add(args[i]);
		}
		try {
			jcl.invokeMain("de.uni_potsdam.hpi.bpt.promnicat.analysisModules." + moduleName, arguments.toArray(new String[0]));
		} catch (Throwable e) {
			Logger.getLogger(AnalysisModuleLauncher.class.getName()).
				severe("Failed to start analysis module " + moduleName + ". Got error message: " + e.getMessage());
		}
	}
}
