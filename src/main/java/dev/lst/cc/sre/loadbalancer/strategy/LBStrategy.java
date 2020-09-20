package dev.lst.cc.sre.loadbalancer.strategy;

import dev.lst.cc.sre.loadbalancer.ServiceUnavailableException;
import dev.lst.cc.sre.registry.ProviderRegistryItem;

/**
 * Loadbalancing strategy interface.
 */
public interface LBStrategy {

    /**
     * Returns the next provider based on the strategy. If no provider is available, it throws
     * a ServiceUnavailableException.
     *
     * @return next provider.
     * @throws ServiceUnavailableException if no provider is available.
     */
    ProviderRegistryItem getNext() throws ServiceUnavailableException;
}
