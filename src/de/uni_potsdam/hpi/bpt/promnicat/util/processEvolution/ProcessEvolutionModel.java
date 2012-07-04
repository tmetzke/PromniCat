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

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Tobias Metzke
 *
 */
public class ProcessEvolutionModel {

	private static final String MODELS_NOT_EQUAL_EXCEPTION_MESSAGE = "Models do not have the same name and are therefore not equal, can not be merged.\n";
	private SortedMap<Integer, ProcessEvolutionModelRevision> revisions = new TreeMap<>();
	private String name;
	private boolean growing = true;
	private int numberOfCMRIterations = 0;
	private int numberOfAlteringRevisions = 0;
	private int numberOfAdditions = 0;
	private int numberOfDeletions = 0;
	private int numberOfMovedOrResizedElements = 0;

	public ProcessEvolutionModel(String name) {
		setName(name);
	}
	
	/**
	 * merge constructor that combines two models into one
	 * @param model1
	 * @param model2
	 */
	public ProcessEvolutionModel(ProcessEvolutionModel model1, ProcessEvolutionModel model2) {
		if (model1.getName() != model2.getName())
			throw new RuntimeException(MODELS_NOT_EQUAL_EXCEPTION_MESSAGE + "Model 1: " + model1.getName() + ", " + "Model 2: " + model2.getName());
		else {
			setName(model1.getName());
			setGrowing(model1.isGrowing() && model2.isGrowing());
			setNumberOfAdditions(Math.max(model1.getNumberOfAdditions(),model2.getNumberOfAdditions()));
			setNumberOfAlteringRevisions(Math.max(model1.getNumberOfAlteringRevisions(), model2.getNumberOfAlteringRevisions()));
			setCMRIterations(Math.max(model1.getCMRIterations(), model2.getCMRIterations()));
			setNumberOfDeletions(Math.max(model1.getNumberOfDeletions(), model2.getNumberOfDeletions()));
			setNumberOfMovedOrResizedElements(Math.max(model1.getNumberOfMovedOrResizedElements(), model2.getNumberOfMovedOrResizedElements()));
			mergeRevisions(model1.getRevisions(), model2.getRevisions());
		}
		
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

	public void add(ProcessEvolutionModelRevision revision) {
		revisions.put(revision.getRevisionNumber(), revision);
	}

	public SortedMap<Integer, ProcessEvolutionModelRevision> getRevisions() {
		return revisions;
	}

	public boolean isGrowing() {
		return growing;
	}

	public void setGrowing(boolean growing) {
		this.growing = growing;
	}

	public int getCMRIterations() {
		return numberOfCMRIterations;
	}
	
	public void setCMRIterations(int iterations) {
		this.numberOfCMRIterations = iterations;
	}

	public int getNumberOfAlteringRevisions() {
		return numberOfAlteringRevisions;
	}

	public void setNumberOfAlteringRevisions(int size) {
		this.numberOfAlteringRevisions  = size;
	}

	public int getNumberOfLazyRevisions() {
		return revisions.size() - getNumberOfAlteringRevisions();
	}

	public int getNumberOfAdditions() {
		return numberOfAdditions;
	}

	public void setNumberOfAdditions(int numberOfAdditions) {
		this.numberOfAdditions = numberOfAdditions;
	}

	public int getNumberOfDeletions() {
		return numberOfDeletions;
	}

	public void setNumberOfDeletions(int numberOfDeletions) {
		this.numberOfDeletions = numberOfDeletions;
	}

	public int getNumberOfMovedOrResizedElements() {
		return numberOfMovedOrResizedElements;
	}

	public void setNumberOfMovedOrResizedElements(int numberOfMovedOrResizedElements) {
		this.numberOfMovedOrResizedElements = numberOfMovedOrResizedElements;
	}

	private void mergeRevisions(
			SortedMap<Integer, ProcessEvolutionModelRevision> revisions1, SortedMap<Integer, ProcessEvolutionModelRevision> revisions2) {
		for (ProcessEvolutionModelRevision revision1 : revisions1.values()) {
			ProcessEvolutionModelRevision newRevision;
			ProcessEvolutionModelRevision revision2 = revisions2.get(revision1.getRevisionNumber());
			if (revision2 != null)
				newRevision = new ProcessEvolutionModelRevision(revision1, revision2);
			else
				newRevision = revision1;
			add(newRevision);
		}
		for (ProcessEvolutionModelRevision revision2 : revisions2.values())
			if (!getRevisions().containsKey(revision2.getRevisionNumber()))
				add(revision2);
	}
}
