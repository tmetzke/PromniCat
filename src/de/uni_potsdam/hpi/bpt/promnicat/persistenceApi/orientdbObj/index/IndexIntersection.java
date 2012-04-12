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
package de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.orientdbObj.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.list.TreeList;

import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.AbstractPojo;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.orientdbObj.PersistenceApiOrientDbObj;

public class IndexIntersection<V extends AbstractPojo> {

	@SuppressWarnings("rawtypes")
	ArrayList<AbstractIndex> indices = new ArrayList<AbstractIndex>();
	PersistenceApiOrientDbObj papi = null;
	
	public IndexIntersection(PersistenceApiOrientDbObj papi) {
		this.papi = papi;
	}
	
	@SuppressWarnings("rawtypes")
	public void add(AbstractIndex index) {
		indices.add(index);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<IndexCollectionElement<V>> load() {
		
		//load dbIds only and sort by result set size
		TreeList rawResults = new TreeList(); //no generics possible
		int maxSize = 0;
		for(AbstractIndex index : indices) {
			ResultSet<V> oneResultSet = new ResultSet<V>(index.loadIdsOnly(), index.getName());
			rawResults.add(oneResultSet);
			maxSize = Math.max(maxSize,oneResultSet.getSize());
		}
		
		//create a list of intersecting dbIds, start with smallest set
		HashSet<String> intersectingDbIds = new HashSet<String>(maxSize);
		for(Object r : rawResults) {
			ResultSet<V> aResult = (ResultSet<V>) r;
			
			if(intersectingDbIds.isEmpty()) {
				intersectingDbIds.addAll(aResult.getDbIds());
			}
			else {
				intersectingDbIds.retainAll(aResult.getDbIds());
			}
			
			if(intersectingDbIds.isEmpty()) {
				break;
			}
		}
		
		//create Map of IndexElements each
		HashMap<String,IndexCollectionElement<V>> finalElements = new HashMap<String,IndexCollectionElement<V>>(indices.size());
		for(Object r : rawResults) {
			ResultSet<V> aResult = (ResultSet<V>) r;
			for(IndexElement indexElement : aResult.getList()) {
				String currentString = indexElement.getDbId();
				if(intersectingDbIds.contains(currentString)) {
					if( !finalElements.containsKey(currentString) ) {
						finalElements.put(currentString, new IndexCollectionElement<V>(currentString));
					}
					finalElements.get(currentString).addIndexElements(indexElement);
				}
			}
		}
		
		//load pojos
		for(IndexCollectionElement<V> collectionElement : finalElements.values()) {
			collectionElement.loadPojo(papi);
		}
		
		return finalElements.values();
	}
}

@SuppressWarnings("rawtypes")
class ResultSet<V extends AbstractPojo> implements Comparable<ResultSet>{
	List<IndexElement> resultSet;
	Integer size = null;
	String indexName = "";
	
	public ResultSet(List<IndexElement> aList, String name) {
		setList(aList);
		this.indexName = name;
	}
	public Collection<String> getDbIds() {
		HashSet<String> dbIds = new HashSet<String>();
		for(IndexElement e : resultSet) {
			dbIds.add(e.getDbId());
		}
		return dbIds;
	}
	
	/**
	 * @return the indexName
	 */
	public String getIndexName() {
		return indexName;
	}
	/**
	 * @param indexName the indexName to set
	 */
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	/**
	 * @return the list
	 */
	public List<IndexElement> getList() {
		return resultSet;
	}
	/**
	 * @param aList the list to set
	 */
	public void setList(List<IndexElement> aList) {
		this.resultSet = aList;
		this.size = aList.size();
	}
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}
	
	@Override
	public int compareTo(ResultSet other) {
		return size.compareTo(other.size);
	}
}
