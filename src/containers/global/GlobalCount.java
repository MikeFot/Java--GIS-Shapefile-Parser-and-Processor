package containers.global;

/**
 * 
 * @author Mike
 *
 */
public class GlobalCount {

	private int mErgoCount;
	
	public GlobalCount() {
		this.mErgoCount = 0;
	}

	public int getErgoCount() {
		mErgoCount ++ ; 
		return mErgoCount;
	}

	public void setErgoCount(int ergoCount) {
		this.mErgoCount = ergoCount;
	}
	
	
	
	
}
