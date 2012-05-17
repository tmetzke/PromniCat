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

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Tobias Metzke
 *
 */
public class AnalysisProcessModel {

	private SortedMap<Integer, AnalysisModelRevision> revisions = new TreeMap<>();
	private String name;
	private boolean growing = true;

	public AnalysisProcessModel(String name) {
		setName(name);
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void add(AnalysisModelRevision revision) {
		revisions.put(revision.getRevisionNumber(), revision);
	}

	public SortedMap<Integer, AnalysisModelRevision> getRevisions() {
		return revisions;
	}

	public boolean isGrowing() {
		return growing;
	}

	public void setGrowing(boolean growing) {
		this.growing = growing;
	}
}
