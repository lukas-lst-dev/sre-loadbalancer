package dev.lst.cc.sre.loadbalancer.strategy;

import java.util.List;
import java.util.Random;

import dev.lst.cc.sre.loadbalancer.ServiceUnavailableException;
import dev.lst.cc.sre.registry.ProviderRegistry;
import dev.lst.cc.sre.registry.ProviderRegistryItem;

/**
 * Implements random load balancer strategy
 */
public enum RandomLBStrategy implements LBStrategy {
    INSTANCE;
    Random random = new Random();
    private ProviderRegistry registry = ProviderRegistry.INSTANCE;

    @Override
    public ProviderRegistryItem getNext() throws ServiceUnavailableException {
        List<ProviderRegistryItem> activeProviderRegistryItems = registry.getActiveProviders();

        if (activeProviderRegistryItems.size() == 0) {
            throw new ServiceUnavailableException("no providers currently available");
        }

        return activeProviderRegistryItems.get(random.nextInt(activeProviderRegistryItems.size()));
    }
}
