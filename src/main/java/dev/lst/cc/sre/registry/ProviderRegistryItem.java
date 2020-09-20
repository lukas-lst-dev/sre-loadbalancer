package dev.lst.cc.sre.registry;

import java.util.concurrent.atomic.AtomicInteger;

import dev.lst.cc.sre.provider.Provider;
import dev.lst.cc.sre.provider.ProviderHealthCheckStatus;
import dev.lst.cc.sre.provider.ProviderStatus;

import static dev.lst.cc.sre.provider.ProviderStatus.BUSY;
import static dev.lst.cc.sre.provider.ProviderStatus.EXCLUDED;
import static dev.lst.cc.sre.provider.ProviderStatus.OK;
import static dev.lst.cc.sre.provider.ProviderStatus.PENDING;

/**
 * A registered Provider Item in the registry.
 */
public class ProviderRegistryItem {

    private static final int MAX_ONGOING_CALLS = 10;

    private final Provider provider;

    private String providerUid;

    private ProviderStatus status;

    private AtomicInteger ongoingRequests = new AtomicInteger(0);

    public ProviderRegistryItem(Provider provider) {
        this.providerUid = provider.getUid();
        this.provider = provider;
        this.status = OK;
    }

    public String getProviderUid() {
        return this.providerUid;
    }

    public ProviderStatus getStatus() {
        return status;
    }

    public void setStatus(ProviderStatus status) {
        this.status = status;
    }

    public int getOngoingRequests() {
        return ongoingRequests.get();
    }


    /**
     * Executes the get command against the provider. Takes care of Circuit Breaker.
     * @return providers answer.
     */
    public String executeGet() {
        // implementation simulating calling get() on the provider.
        addOngoingCall();
        String retVal = provider.get();
        removeOngoingCall();
        return retVal;
    }

    private synchronized void removeOngoingCall() {
        ongoingRequests.decrementAndGet();
        updateProviderStatus();
    }

    private void updateProviderStatus() {
        if (ongoingRequests.get() >= MAX_ONGOING_CALLS) {
            setStatus(BUSY);
        } else {
            setStatus(OK);
        }
    }

    private synchronized void addOngoingCall() {
        ongoingRequests.incrementAndGet();
        updateProviderStatus();
    }

    /**
     * Includes the provider.
     */
    public void include() {
        ongoingRequests.set(0);
        setStatus(OK);
    }

    /**
     * runs a healthCheck call against the provider and sets appropriate state.
     * if the old status was excluded but the provider is now ok, it will go first into a pending state
     * and if it is still ok after the next healthcheck, it will be included again.
     */
    public void healthCheck() {
        ProviderHealthCheckStatus health = provider.healthCheck();

        if (health == ProviderHealthCheckStatus.ERROR) {
            setStatus(EXCLUDED);
        } else {
            if (EXCLUDED.equals(getStatus())) {
                setStatus(PENDING);
            } else if (PENDING.equals(getStatus())) {
                include();
            }
        }
        System.out.println(" - provider " + getProviderUid() + " healthcheck executed. healt is " + health + ", overall status is " + getStatus());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProviderRegistryItem that = (ProviderRegistryItem) o;

        return providerUid.equals(that.providerUid);
    }

    @Override
    public int hashCode() {
        return providerUid.hashCode();
    }

    @Override
    public String toString() {
        return "ProviderRegistryItem{" +
                "providerUid='" + providerUid + '\'' +
                ", status=" + status +
                '}';
    }
}
