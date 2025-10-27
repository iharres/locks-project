package lock;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class KeylessEntryLock extends KeyLock {

    public static final int MAX_NUM_USER_CODES = 10;
    public static final int USER_CODE_LENGTH   = 4;
    public static final int MASTER_CODE_LENGTH = 6;

    private boolean isNewUserCode;
    private boolean isDeletedUserCode;
    private boolean isChangedMasterCode;
    private boolean areAllUserCodesDeleted;

    private boolean awaitingOp;            // after master + '*'
    private boolean addingUserFirst;       // new user code: first 4 digits
    private boolean addingUserConfirm;     // new user code: confirm 4 digits
    private boolean deletingUserFirst;     // delete one: first 4 digits
    private boolean deletingUserConfirm;   // delete one: confirm 4 digits
    private boolean changingMasterFirst;   // change master: first 6 digits
    private boolean changingMasterConfirm; // change master: confirm 6 digits
    private boolean deletingAllConfirm;    // delete all: re-enter master 6 digits

    private int[]   masterCode;                
    private int[][] userCodes;

    private final List<Integer> attemptBuf = new ArrayList<>();

    private int[] newUserCodeFirstEntry;     // add-user: first 4 digits
    private int[] userCodeToDelete;          // delete-one: the 4-digit code
    private int   userCodeDeleteIndex;       // delete-one: row index
    private int[] newMasterCodeFirstEntry;   // change-master: first 6 digits
    
    
    private void pushDigit(int d) {
        if (d < 0 || d > 9) return;
        attemptBuf.add(d);
    }

    private void clearAttempt() {
        attemptBuf.clear();
    }

    private int[] toIntArray(List<Integer> src) {
        int[] result = new int[src.size()];
        for (int i = 0; i < src.size(); i++) result[i] = src.get(i);
        return result;
    }

    private boolean codesMatch(int[] a, int[] b){
        if (a == null || b==null){
        	return false;
        }
        if (a.length != b.length){
        	return false;
        }
        for (int i=0; i<a.length; i++) if (a[i] != b[i]){
        	return false;
        }
        return true;
    }

    private boolean isMasterCodeAttempt() {
        if (attemptBuf.size() != MASTER_CODE_LENGTH) return false;
        return codesMatch(toIntArray(attemptBuf), masterCode);
    }

    private boolean isEmptySlot(int[] row) {
        for (int v: row) if (v != -1) return false;
        return true;
    }

    private int findFreeUserSlot() {
        for (int i = 0; i < MAX_NUM_USER_CODES; i++) {
            if (isEmptySlot(userCodes[i])) return i;
        }
        return -1;
    }

    private void clearUserSlot (int row) {
        if (row < 0 || row >= MAX_NUM_USER_CODES) return;
        Arrays.fill(userCodes[row], -1);
    }

    private boolean isUserAttempt() {
        if (attemptBuf.size() != USER_CODE_LENGTH) return false;
        int[] g = toIntArray(attemptBuf);
        for (int i = 0; i < MAX_NUM_USER_CODES; i++) {
            if (!isEmptySlot(userCodes[i]) && codesMatch(g, userCodes[i])) return true;
        }
        return false;
    }

    	public KeylessEntryLock(int keyValue) {
    		super(keyValue);

    		//setting master code to 123456
    		masterCode = new int[MASTER_CODE_LENGTH];
    		for (int i = 0; i < MASTER_CODE_LENGTH; i++) {
    			masterCode[i] = i + 1;
    		}
    		//fills each available slot up with -1 if it is empty
    		userCodes = new int[MAX_NUM_USER_CODES][USER_CODE_LENGTH];
    		for (int i = 0; i < MAX_NUM_USER_CODES; i++) {
    			Arrays.fill(userCodes[i], -1);
    		}

    		resetPhases();
    		clearAttempt();
    		isNewUserCode = false;
        	isDeletedUserCode = false;
        	isChangedMasterCode = false;
        	areAllUserCodesDeleted = false;
    	}

    public boolean pushButton(char button) {
        if (button >= '0' && button <= '9') {
            int d = button - '0';
            pushDigit(d);

           
            if (addingUserFirst) {
            	return handleAddUserFirst();
            }
            if (addingUserConfirm){
            	return handleAddUserConfirm();
            }
            if (deletingUserFirst){
            	return handleDelUserFirst();
            }
            if (deletingUserConfirm){
            	return handleDelUserConfirm();
            }
            if (changingMasterFirst){
            	return handleChgMasterFirst();
            }
            if (changingMasterConfirm){
            	return handleChgMasterConfirm();
            }
            if (deletingAllConfirm){
            	return handleDelAllConfirm();
            }
            if (awaitingOp) {
                return handleAwaitOpDigit(d);
            }

            int n = attemptBuf.size();

            if (n == USER_CODE_LENGTH) {
                boolean ok = isUserAttempt();
                clearAttempt();
                if (ok){
                	unlock(); 
                	return true; 
                }
                return false;
            }

            if (n == MASTER_CODE_LENGTH) {
                boolean ok = isMasterCodeAttempt();
                clearAttempt();
                if (ok){
                	unlock(); 
                	return true;
                }
                return false;
            }

            if (n > MASTER_CODE_LENGTH){
                clearAttempt();
                return false;
            }

            return false;
        }

        if (button == '*') {
            if (!awaitingOp && !addingUserFirst && !addingUserConfirm && !deletingUserFirst && !deletingUserConfirm && !changingMasterFirst && !changingMasterConfirm && !deletingAllConfirm && isMasterCodeAttempt()) {
                awaitingOp = true;
                clearAttempt();
                return false;
            }
            resetPhases();
            return false;
        }
        // ignore others
        return false;
    }

    public boolean addedUserCode(){
    	boolean was = isNewUserCode; 
    	isNewUserCode = false;
    	return was;
    }
    public boolean deletedUserCode(){
    	boolean was = isDeletedUserCode; 
    	isDeletedUserCode = false;
    	return was;
    }
    public boolean deletedAllUserCodes(){ 
    	boolean was = areAllUserCodesDeleted; 
    	areAllUserCodesDeleted = false; 
    	return was; 
    	}
    public boolean changedMasterCode(){
    	boolean was = isChangedMasterCode;  
    	isChangedMasterCode = false;  
    	return was; 
    	}

    public int[] getMasterCode() {
        int[] out = new int[MASTER_CODE_LENGTH];
        System.arraycopy(masterCode, 0, out, 0, MASTER_CODE_LENGTH);
        return out;
    }


    private boolean handleAwaitOpDigit(int d) {
        if (d == 1){
        	awaitingOp = false; 
        	addingUserFirst = true; 
        	clearAttempt(); 
        	return false; 
        }
        if (d == 2){
        	awaitingOp = false; 
        	deletingUserFirst = true; 
        	clearAttempt(); return false; 
        }
        if (d == 3){
        	awaitingOp = false; 
        	changingMasterFirst = true; 
        	clearAttempt(); return false; 
        }
        if (d == 6){ awaitingOp = false; 
        deletingAllConfirm = true; 
        clearAttempt(); 
        return false; 
        }
        resetPhases();
        return false;
    }
    	//adding first user
    	private boolean handleAddUserFirst() {
        int n = attemptBuf.size();
        if (n > USER_CODE_LENGTH){ 
        	resetPhases(); return false; 
        }
        if (n < USER_CODE_LENGTH){
        	return false; 
        }
        newUserCodeFirstEntry = toIntArray(attemptBuf);
        clearAttempt();
        addingUserFirst = false;
        addingUserConfirm = true;
        return false;
    }

    //confirm 4 and save
    private boolean handleAddUserConfirm() {
        int n = attemptBuf.size();
        if (n > USER_CODE_LENGTH){ 
        	newUserCodeFirstEntry = null;
        	resetPhases(); 
        	return false; 
        	}
        if (n < USER_CODE_LENGTH){ 
        	return false; 
        	}

        int[] confirm = toIntArray(attemptBuf);
        if (!codesMatch(confirm, newUserCodeFirstEntry)){
        	newUserCodeFirstEntry = null; 
        	resetPhases(); 
        	return false; 
        }

        for (int i = 0; i < MAX_NUM_USER_CODES; i++) {
            if (!isEmptySlot(userCodes[i]) && codesMatch(confirm, userCodes[i])){
            	newUserCodeFirstEntry = null; 
            	resetPhases(); return false; 
            }
        }

        int slot = findFreeUserSlot();
        if (slot == -1){
        	newUserCodeFirstEntry = null; 
        	resetPhases();
        	return false; 
        }

        System.arraycopy(confirm, 0, userCodes[slot], 0, USER_CODE_LENGTH);
        isNewUserCode = true;
        newUserCodeFirstEntry = null;
        resetPhases();
        return true;
    }

    // DELETE ONE: first 4 (find row)
    private boolean handleDelUserFirst() {
        int n = attemptBuf.size();
        if (n > USER_CODE_LENGTH){ 
        	resetPhases(); 
        	return false; 
        }
        if (n < USER_CODE_LENGTH){
        	return false; 
        }

        int[] cand = toIntArray(attemptBuf);
        int m = -1;
        for (int i = 0; i < MAX_NUM_USER_CODES; i++) {
            if (!isEmptySlot(userCodes[i]) && codesMatch(cand, userCodes[i])){ 
            	m = i; break; }
        }
        if (m == -1) { resetPhases(); return false; }

        userCodeToDelete = cand;
        userCodeDeleteIndex = m;
        clearAttempt();
        deletingUserFirst = false;
        deletingUserConfirm = true;
        return false;
    }

    // DELETE ONE: confirm 4 then clear row
    private boolean handleDelUserConfirm() {
        int n = attemptBuf.size();
        if (n > USER_CODE_LENGTH){ 
        	userCodeToDelete = null;
        	resetPhases();
        	return false; 
        }
        if (n < USER_CODE_LENGTH){
        	return false; 
        }

        int[] confirm = toIntArray(attemptBuf);
        if (!codesMatch(confirm, userCodeToDelete)){
        	userCodeToDelete = null; 
        	resetPhases(); 
        	return false;
        }

        clearUserSlot(userCodeDeleteIndex);
        isDeletedUserCode = true;
        userCodeToDelete = null;
        resetPhases();
        return true;
    }

    // CHANGE MASTER: first 6
    private boolean handleChgMasterFirst() {
        int n = attemptBuf.size();
        if (n > MASTER_CODE_LENGTH){
        	resetPhases(); 
        	return false; 
        }
        if (n < MASTER_CODE_LENGTH){
        	return false; 
        }
        newMasterCodeFirstEntry = toIntArray(attemptBuf);
        clearAttempt();
        changingMasterFirst = false;
        changingMasterConfirm = true;
        return false;
    }

    // CHANGE MASTER: confirm 6 then save
    private boolean handleChgMasterConfirm() {
        int n = attemptBuf.size();
        if (n > MASTER_CODE_LENGTH){
        	newMasterCodeFirstEntry = null; 
        	resetPhases(); return false; 
        }
        if (n < MASTER_CODE_LENGTH){
        	return false; 
        }
        int[] confirm = toIntArray(attemptBuf);
        if (!codesMatch(confirm, newMasterCodeFirstEntry)){
        	newMasterCodeFirstEntry = null;
        	resetPhases();
        	return false;
        }
        System.arraycopy(confirm, 0, masterCode, 0, MASTER_CODE_LENGTH);
        isChangedMasterCode = true;
        newMasterCodeFirstEntry = null;
        resetPhases();
        return true;
    }

    // DELETE ALL: re-enter master (6), then clear all
    private boolean handleDelAllConfirm() {
        int n = attemptBuf.size();
        if (n > MASTER_CODE_LENGTH){ 
        	resetPhases(); 
        	return false; 
        }
        if (n < MASTER_CODE_LENGTH){ 
        	return false; 
        }
        if (!isMasterCodeAttempt()){
        	resetPhases();
        	return false; 
        }
        for (int i = 0; i < MAX_NUM_USER_CODES; i++) {
        	clearUserSlot(i);
        }
        areAllUserCodesDeleted = true;
        resetPhases();
        return true;
    }

    private void resetPhases() {
        awaitingOp = false;
        addingUserFirst = false;
        addingUserConfirm = false;
        deletingUserFirst = false;
        deletingUserConfirm = false;
        changingMasterFirst = false;
        changingMasterConfirm = false;
        deletingAllConfirm = false;
        clearAttempt();
    }
      
    
}