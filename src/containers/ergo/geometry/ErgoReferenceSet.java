package containers.ergo.geometry;

import containers.ergo.geometry.ErgoReferenceSystem;

/**
 * Custom Container for Reference System as a Global Variable
 * @author Michael Fotiadis
 *
 */
public class ErgoReferenceSet {

	private ErgoReferenceSystem ergoSourceSystem;
	private ErgoReferenceSystem ergoTargetSystem;
	

	public ErgoReferenceSet(String sourceSystem, String sourceType, String targetSystem, String targetType) {
		this.ergoSourceSystem.setSystem(sourceSystem);
		this.ergoSourceSystem.setType(sourceType);
		
		this.ergoTargetSystem.setSystem(targetSystem);
		this.ergoTargetSystem.setType(targetType);
	}
	
	public ErgoReferenceSet() {
		this.ergoSourceSystem = null;
		this.ergoTargetSystem = null;
	}

	public ErgoReferenceSystem getSourceSystem() {
		return ergoSourceSystem;
	}

	public void setSourceSystem(ErgoReferenceSystem sourceSystem) {
		this.ergoSourceSystem = sourceSystem;
	}

	public ErgoReferenceSystem getTargetSystem() {
		return ergoTargetSystem;
	}

	public void setTargetSystem(ErgoReferenceSystem targetSystem) {
		this.ergoTargetSystem = targetSystem;
	}


	
}
