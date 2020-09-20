package dev.lst.cc.sre.loadbalancer.strategy;

import java.util.List;

import dev.lst.cc.sre.loadbalancer.ServiceUnavailableException;
import dev.lst.cc.sre.registry.ProviderRegistry;
import dev.lst.cc.sre.registry.ProviderRegistryItem;

/**
 * Load Balancing strategy as round robin.
 */
public enum RoundRobinLBStrategy implements LBStrategy {
    INSTANCE;

    private int providerIndex = 0;
    private ProviderRegistry registry = ProviderRegistry.INSTANCE;

    @Override
    public ProviderRegistryItem getNext() throws ServiceUnavailableException {
        List<ProviderRegistryItem> activeProviderRegistryItems = registry.getActiveProviders();

        if (activeProviderRegistryItems.size() == 0) {
            throw new ServiceUnavailableException("no providers currently available");
        }

        return activeProviderRegistryItems.get(incrementAndGetProviderPosition(activeProviderRegistryItems.size()));
    }

    // on first view it looks like this could be done with an atomicInteger but that's not the case
    // since we need two operations on it. Once to check if it is larger than activeProviderSize
    // and if it is, then to set it to zero. So it's not always just an increment.
    private synchronized int incrementAndGetProviderPosition(int activeProviderSize) {
        providerIndex++;
        if (providerIndex >= activeProviderSize) {
            providerIndex = 0;
        }
        return providerIndex;
    }
}
