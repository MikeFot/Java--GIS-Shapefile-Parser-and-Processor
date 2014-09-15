package com.michaelfotiadis.shpparser.containers.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for Maps containing metadata objects
 * Implements Metadata interface
 * @author Michael Fotiadis
 *
 */
public abstract class MetadataStore implements Metadata {
	private final Map<String , String> stringMap;
	private final Map<String , Integer> integerMap;
	private final Map<String , Double> doubleMap;
	
	public MetadataStore(){
		stringMap = new HashMap<String, String>();
		integerMap = new HashMap<String, Integer>();
		doubleMap = new HashMap<String, Double>();
	}
	
	@Override
	public void putString(String key, String value){
		stringMap.put(key, value);
	}
	
	@Override
	public String getString(String key){
		return stringMap.get(key);
	}
	
	public String[] getStringKeys(){
		return stringMap.keySet().toArray(new String[1]);
	}
	
	@Override
	public void putInteger(String key, Integer value){
		integerMap.put(key, value);
	}
	
	@Override
	public Integer getInteger(String key){
		return integerMap.get(key);
	}
	
	@Override
	public String[] getIntegerKeys(){
		return integerMap.keySet().toArray(new String[1]);
	}
	
	@Override
	public void putDouble(String key, Double value){
		doubleMap.put(key, value);
	}
	
	@Override
	public Double getDouble(String key){
		return doubleMap.get(key);
	}
	
	@Override
	public String[] getDoubleKeys(){
		return doubleMap.keySet().toArray(new String[1]);
	}
	
}
