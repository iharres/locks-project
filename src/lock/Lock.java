package lock;

public interface Lock {
	boolean lock();
	boolean unlock();
	boolean isLocked();
}
