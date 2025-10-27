package lock;

import java.util.Random;

public class ComboLock implements Lock {
    public static final int COMBO_LENGTH = 3;
    public static final int MAX_TICKS = 39;

    private static final Random RNG = new Random();

    private int[] combination;
    private int[] attempt;
    private boolean isLocked;
    private boolean isReset;
    private int Inum = 0;               // 0: expect R to #1, 1: expect L to #2, 2: expect R to #3, 3: done
    private int currentPosition = 0;    // Start at 0
    private int fullRightTurns = 0;

    public ComboLock() {
        combination = new int[COMBO_LENGTH];
        attempt = new int[COMBO_LENGTH];

        // Random combo in range [0, MAX_TICKS]
        int n = num();
        combination[0] = RNG.nextInt(n);
        combination[1] = RNG.nextInt(n);
        combination[2] = RNG.nextInt(n);

        isLocked = true;
        reset();
    }

    private int num() {
    	return MAX_TICKS + 1;
    } 
    private void clearAttempt() {
        for (int i = 0; i < COMBO_LENGTH; i++) {
        	attempt[i] = 0;
        }
    }

    private void moveRight(int ticks) {
        fullRightTurns += (ticks / num());          // count full rotations to the right
        int movingRight = ticks % num();
        currentPosition = (currentPosition + movingRight) % num();
        isReset = false;
        reset();                                    
    }

    private void moveLeft(int ticks) {
        int movingLeft = ticks % num();
        currentPosition = (currentPosition + movingLeft) % num(); 
        isReset = false;
        fullRightTurns = 0; 
    }

    public boolean turnRight(int ticks) {
        moveRight(ticks);

        if (Inum == 0) {
            if (currentPosition == combination[0]) {
                attempt[0] = currentPosition;
                Inum = 1;
                return true;
            }
            Inum = 0; clearAttempt(); return false;

        } else if (Inum == 2) {
            if (currentPosition == combination[2]) {
                attempt[2] = currentPosition;
                Inum = 3;
                isLocked = false; 
                return true;
            }
            Inum = 0; clearAttempt(); 
            return false;

        } else {
            Inum = 0; clearAttempt(); 
            return false; 
        }
    }

    public boolean turnLeft(int ticks) {
        moveLeft(ticks);

        if (Inum == 1) {
            if (currentPosition == combination[1]) {
                attempt[1] = currentPosition;
                Inum = 2;
                return true;
            }
            Inum = 0; clearAttempt(); 
            return false;

        } else {
            Inum = 0; clearAttempt();
            return false;
        }
    }

    // Reset is performed with 3 right turns in a row
    public void reset() {
        if (fullRightTurns >= 3) {
            currentPosition = 0;
            Inum = 0;
            isReset = true;
            fullRightTurns = 0;
            clearAttempt();
        }
    }

    public boolean isReset() { 
    	return isReset; 
    	}

    public int[] getCombination() {
        int[] copy = new int[COMBO_LENGTH];
        for (int i = 0; i < COMBO_LENGTH; i++) copy[i] = combination[i];
        return copy;
    }

    @Override
    public boolean lock() {
        isLocked = true;
        reset();
        return true;
    }

    @Override
    public boolean unlock(){
    	return !isLocked; 
    }

    @Override
    public boolean isLocked(){ 
    	return isLocked;
    }
}
