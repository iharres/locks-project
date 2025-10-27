package lock;

public class KeyLock implements Lock {
	private int key;
	private boolean isLocked;
	private boolean isInserted;
	
	public KeyLock(int key) {
		this.key = key;
		this.isInserted = false;
		this.isLocked = true;
	}
	
	public boolean insertKey(int key) {
		if (key == this.key) {
			isInserted = true;
			return true;
		}
		return false;	
	}

	public boolean removeKey(int key) {
		if(isInserted && key == this.key) {
			isInserted = false;
			return true;
		}
		return false; 
	}
	
	public boolean turn() {
		if(!isInserted) {
			return false;
		}
		isLocked = !isLocked;
		return true;
		
	}

	@Override
	public boolean lock() {
		if (!isInserted) {
			return false;
		}
		if(!isLocked) {
			return false;
		}
		isLocked = true;
		return true;
	}

	@Override
	public boolean unlock() {
		if (!isInserted) {
			return false;
		}
		if(!isLocked) {
			return false;
		}
		isLocked = false;
		return true;
	}

	@Override
	public boolean isLocked() {
		return isLocked;
	}

}
