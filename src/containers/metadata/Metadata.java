package containers.metadata;

/**
 * 
 * @author Mike
 *
 */
interface Metadata {
	
	public abstract Double getDouble(String key);
	public abstract String[] getDoubleKeys();

	public abstract Integer getInteger(String key);
	public abstract String[] getIntegerKeys();

	public abstract String getString(String key);
	public abstract String[] getStringKeys();

	public abstract void putDouble(String key, Double value);
	public abstract void putInteger(String key, Integer value);
	public abstract void putString(String key, String value);
	
	
	
}
