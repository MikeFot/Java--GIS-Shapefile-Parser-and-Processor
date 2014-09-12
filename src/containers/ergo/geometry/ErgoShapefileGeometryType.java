package containers.ergo.geometry;

public enum ErgoShapefileGeometryType {
	POINT("Point"), POLYLINE("Polyline"), POLYGON("Polygon"), 
	MULTI_LINE_STRING("MultiLineString"), MULTI_POLYGON("MultiPolygon");
	
	private final String description;
	
	private ErgoShapefileGeometryType(final String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return this.description;
	}
	
}
