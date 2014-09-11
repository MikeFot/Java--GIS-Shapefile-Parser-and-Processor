package containers.ergo.geometry;

import java.util.List;

import containers.metadata.MetadataStore;


/**
 * Custom container class for objects containing 1++ MyVertex objects (stored in a collection). Supports storing metadata in a HashMap.
 * @author Michael Fotiadis
 *
 */
public class ErgoPolyline extends MetadataStore {

	private final List<ErgoVertex> vertexList;
	private final String id;
	private final String type;
	private final int vertexSize;
	private int hasmhapSize = 0;
	private String mReferenceSystem;

	public ErgoPolyline (List<ErgoVertex> VertexList) {
		this.vertexList = VertexList;
		this.id = null;
		this.type = null;
		this.vertexSize = VertexList.size();
	}


	public ErgoPolyline (List<ErgoVertex> VertexList, String ID) {
		this.vertexList = VertexList;
		this.id = ID;
		this.type = null;
		this.vertexSize = VertexList.size();

	}

	public ErgoPolyline (List<ErgoVertex> VertexList, String ID, String Type) {
		this.vertexList = VertexList;
		this.id = ID;
		this.type = Type;
		this.vertexSize = VertexList.size();
	}

	public void setHashMapSize() {
		this.hasmhapSize ++;
	}
	
	public int getHashMapSize() {
		return hasmhapSize;
	}
	
	
	
	public int getSize() {
		return vertexSize;
	}

	public List<ErgoVertex> getVertexList() {
		return vertexList;
	}

	public ErgoVertex getVertex(int Number) {
		ErgoVertex singleVertex = vertexList.get(Number);
		return singleVertex;
	}

	public String getID() {
		return id;
	}


	public String getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ErgoPolyline other = (ErgoPolyline) obj;
		if (vertexList == null) {
			if (other.vertexList  != null)
				return false;
		} else if (!vertexList .equals(other.vertexList ))
			return false;
		return true;
		
	}


	public String getReferenceSystem() {
		return mReferenceSystem;
	}


	public void setReferenceSystem(String mReferenceSystem) {
		this.mReferenceSystem = mReferenceSystem;
	}
}
