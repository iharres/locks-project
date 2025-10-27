package lock;

public class testComboLock {

    public static void main(String[] args) {
        int N = ComboLock.MAX_TICKS + 1;

        ComboLock lock = new ComboLock();
        int[] combo = lock.getCombination();
        if (lock.turnRight((combo[0] + 1) % N)) {
            System.out.println("Wrong first number accepted, this should fail");
        }

        lock = new ComboLock();
        combo = lock.getCombination();
        int cp = 0;

        if (!lock.turnRight(combo[0])) {
            System.out.println("Right to " + combo[0] + " should work");
        }
        cp = combo[0];

        int toC1 = (combo[1] - cp + N) % N;
        if (!lock.turnLeft(toC1)) {
            System.out.println("Left to " + combo[1] + " should work");
        }
        cp = combo[1];

        int toC2 = (combo[2] - cp + N) % N;
        if (!lock.turnRight(toC2)) {
            System.out.println("Right to " + combo[2] + " should work");
        }

        if (!lock.unlock() || lock.isLocked()) {
            System.out.println("After the correct combo, unlock() should be true and isLocked should be false");
        }

        // 3) Reset after three full right turns (each full turn = 40 ticks)
        lock = new ComboLock();
        lock.turnRight(N);
        if (lock.isReset()) {
            System.out.println("After 1 full right turn, should NOT be reset");
        }
        lock.turnRight(N);
        if (lock.isReset()) {
            System.out.println("After 2 full right turns, should not be reset");
        }
        lock.turnRight(N);
        if (!lock.isReset()) {
            System.out.println("After 3 full right turns, should be reset");
        }

        // 4) A left turn breaks the reset streak: 2R + L + 1R should NOT reset
        lock = new ComboLock();
        lock.turnRight(N);
        lock.turnRight(N);
        lock.turnLeft(1);
        lock.turnRight(N);  // only 1 in a new streak
        if (lock.isReset()) {
            System.out.println("Left turn should break the reset streak; 2R + L + 1R should NOT reset");
        }
    }
}
