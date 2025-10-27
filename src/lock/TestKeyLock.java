package lock;

public class TestKeyLock {
		
	public static void main(String[] args) {
		//Test keylock 
        KeyLock keyLock = new KeyLock(4);
        boolean inserted = keyLock.insertKey(5); 
        if(inserted) {
			System.out.println("Inserted wrong key, this should fail");
		}
		
		//second test insert correct key
		inserted = keyLock.insertKey(4);
		if (inserted) {
			System.out.println("Insertrd correct key, this shoud work");
		}
		//test turn
		keyLock = new KeyLock(4);
		if(keyLock.turn()) {
			System.out.println("key not inserted, should fail");
		}
		
		keyLock.insertKey(5);
		if(keyLock.turn()) {
			System.out.println("Wrong key inserted, turn should fail");
		}
		
	}

}
