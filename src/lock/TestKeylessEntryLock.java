package lock;

public class TestKeylessEntryLock {

    public static void main(String[] args) {
        // Assumptions this tester follows:
        // - KeylessEntryLock(int key) seeds the 6-digit master code (e.g., 123456).
        // - To ADD / DELETE / CHANGE codes, you must first enter the current master code.
        // - pushButton(char) accepts '0'..'9'. Unlock occurs after the 6th digit of a valid code.

        KeylessEntryLock lock = new KeylessEntryLock(123456);

        char[] master    = {'1','2','3','4','5','6'};
        char[] user      = {'6','5','4','3','2','1'};
        char[] newMaster = {'0','1','2','3','4','5'};

        // 1) Wrong code should NOT unlock
        char[] wrong = {'9','9','9','9','9','9'};
        for (char d : wrong) lock.pushButton(d);
        if (!lock.isLocked()) {
            System.out.println("Wrong code unlocked the lock — this should fail");
        }

        // 2) Correct master SHOULD unlock
        lock.lock();
        for (char d : master) lock.pushButton(d);
        if (lock.isLocked()) {
            System.out.println("Correct master code did not unlock — this should work");
        }

        // 3) ADD a user code (654321) — enter master first, then program
        lock.lock();
        for (char d : master) lock.pushButton(d);  // enter admin mode
        if (!lock.addedUserCode()) {
            System.out.println("addedUserCode() returned false — expected true");
        }
        for (char d : user) lock.pushButton(d);    // set new user code

        // Verify user code unlocks
        lock.lock();
        for (char d : user) lock.pushButton(d);
        if (lock.isLocked()) {
            System.out.println("User code 654321 did not unlock — this should work");
        }

        // 4) DELETE that user code — enter master first, then delete
        lock.lock();
        for (char d : master) lock.pushButton(d);
        if (!lock.deletedUserCode()) {
            System.out.println("deletedUserCode() returned false — expected true");
        }
        for (char d : user) lock.pushButton(d); // specify which user code to delete

        // Try user again: should FAIL now
        lock.lock();
        for (char d : user) lock.pushButton(d);
        if (!lock.isLocked()) {
            System.out.println("Deleted user code still unlocks — this should fail");
        }
        // Master should still work
        lock.lock();
        for (char d : master) lock.pushButton(d);
        if (lock.isLocked()) {
            System.out.println("Master code should still unlock after deleting a user code");
        }

        // 5) ADD user again, then DELETE ALL users — master should still work
        lock.lock();
        for (char d : master) lock.pushButton(d);
        if (!lock.addedUserCode()) {
            System.out.println("addedUserCode() returned false when re-adding — expected true");
        }
        for (char d : user) lock.pushButton(d); // re-add 654321

        lock.lock();
        for (char d : master) lock.pushButton(d);
        if (!lock.deletedAllUserCodes()) {
            System.out.println("deletedAllUserCodes() returned false — expected true");
        }

        // User should now FAIL
        lock.lock();
        for (char d : user) lock.pushButton(d);
        if (!lock.isLocked()) {
            System.out.println("After deletedAllUserCodes, user code still unlocks — this should fail");
        }
        // Master should still WORK
        lock.lock();
        for (char d : master) lock.pushButton(d);
        if (lock.isLocked()) {
            System.out.println("After deletedAllUserCodes, master code should still unlock");
        }

        // 6) CHANGE MASTER to 012345 — enter current master first, then change
        lock.lock();
        for (char d : master) lock.pushButton(d);
        if (!lock.changedMasterCode()) {
            System.out.println("changedMasterCode() returned false — expected true");
        }
        for (char d : newMaster) lock.pushButton(d); // set new master

        // Old master should now FAIL
        lock.lock();
        for (char d : master) lock.pushButton(d);
        if (!lock.isLocked()) {
            System.out.println("Old master code still unlocks after change — this should fail");
        }
        // New master should WORK
        lock.lock();
        for (char d : newMaster) lock.pushButton(d);
        if (lock.isLocked()) {
            System.out.println("New master code did not unlock — this should work");
        }
    }
}

