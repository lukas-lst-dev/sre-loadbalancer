package dev.lst.cc.sre.provider;

/**
 * Interface defining a Provider.
 */
public interface Provider {
    /**
     * Executes the get operation.
     *
     * @return uid of the provider
     */
    String get();

    /**
     * invokes healthcheck.
     *
     * @return Health status.
     */
    ProviderHealthCheckStatus healthCheck();

    /**
     * Returns the uid - for simplicity here and not to abuse get();
     *
     * @return Uid.
     */
    public String getUid();
}
