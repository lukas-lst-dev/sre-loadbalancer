package dev.lst.cc.sre.provider;

/**
 * This is a slow provider...
 */
public class SlowInMemoryProvider extends InMemoryProvider{
    public SlowInMemoryProvider(String uid) {
        super(uid);
    }

    /**
     * Takes 2 seconds to answer.
     * @return uid.
     */
    @Override
    public String get() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return super.getUid();
    }
}
