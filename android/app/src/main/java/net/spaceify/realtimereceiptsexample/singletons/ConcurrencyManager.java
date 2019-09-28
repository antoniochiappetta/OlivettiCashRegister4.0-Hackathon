package net.spaceify.realtimereceiptsexample.singletons;

public class ConcurrencyManager {

    // MARK: - Singleton

    private static final ConcurrencyManager ourInstance = new ConcurrencyManager();

    static ConcurrencyManager getInstance() {
        return ourInstance;
    }

    private ConcurrencyManager() {
    }

    // MARK: - Properties
    public static final Object sharedLock = new Object();

}
