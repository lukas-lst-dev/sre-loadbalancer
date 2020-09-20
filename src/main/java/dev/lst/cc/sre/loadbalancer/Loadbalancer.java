package dev.lst.cc.sre.loadbalancer;

import dev.lst.cc.sre.loadbalancer.strategy.LBStrategy;
import dev.lst.cc.sre.registry.ProviderRegistry;
import dev.lst.cc.sre.registry.RegistryFullException;
import dev.lst.cc.sre.registry.ProviderRegistryItem;

/**
 * Loadbalancer implementation.
 */
public class Loadbalancer {

    private final LBStrategy lbStrategy;

    private ProviderRegistry registry = ProviderRegistry.INSTANCE;

    public Loadbalancer(LBStrategy lbStrategy) {
        this.lbStrategy = lbStrategy;
    }

    public String get() throws ServiceUnavailableException {
        return lbStrategy.getNext().executeGet();
    }

    public void registerProvider(ProviderRegistryItem item) throws RegistryFullException {
        registry.registerProvider(item);
    }

    public void excludeProvider(String providerUid) {
        registry.exclude(providerUid);
    }

    public void includeProvider(String providerUid) {
        registry.include(providerUid);
    }
}
