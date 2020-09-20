package dev.lst.cc.sre.provider;

/**
 * Implements a Provider.
 */
public class InMemoryProvider implements Provider {
    private final String uid;

    public InMemoryProvider(String uid) {
        this.uid = uid;
    }

    public String get() {
        return uid;
    }

    public ProviderHealthCheckStatus healthCheck() {
        return ProviderHealthCheckStatus.OK;
    }

    @Override
    public String getUid() {
        return this.uid;
    }
}
