package dev.lst.cc.sre.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import dev.lst.cc.sre.provider.ProviderStatus;

/**
 * Provider Registry. Keeps a list of providers available. Is implemented as a singleton since we should always use only
 * one instance.
 */
public enum ProviderRegistry {
    INSTANCE;

    private static final int MAX_PROVIDERS = 10;
    private static final int HEARTBEAT_PERIOD_SECONDS = 30;
    private final List<ProviderRegistryItem> providerRegistryItems;
    private final ReentrantLock lock = new ReentrantLock();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(MAX_PROVIDERS);

    ProviderRegistry() {
        providerRegistryItems = new ArrayList<>();
    }

    /**
     * registers a provider. is thread safe so that checks are still ok in a multi-threaded scenario.
     * This is implemented with a reentrant lock and not synchronized so that the remove function can also be blocked.
     *
     * @param item to be registered.
     * @throws RegistryFullException is thrown if there are already MAX_PROVIDERS registered.
     */
    public void registerProvider(ProviderRegistryItem item) throws RegistryFullException {
        lock.lock();
        try {
            if (providerRegistryItems.size() >= MAX_PROVIDERS) {
                throw new RegistryFullException("already the max amount of providers registered.");
            }

            if (!providerRegistryItems.contains(item)) {
                providerRegistryItems.add(item);
                scheduleHeartbeatCheck(item);
            }
        } finally {
            lock.unlock();
        }
    }

    private void scheduleHeartbeatCheck(ProviderRegistryItem providerRegistryItem) {
        Runnable task = providerRegistryItem::healthCheck;
        executor.scheduleAtFixedRate(task, 5, HEARTBEAT_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * removes a provider if it is part of the registered providers. Is thread-safe also with regards to
     * adding providers.
     *
     * @param uid Provider to be removed.
     */
    public void removeProvider(String uid) {
        Optional<ProviderRegistryItem> foundItemOpt = providerRegistryItems.stream().filter(p -> p.getProviderUid().equals(uid)).findAny();

        foundItemOpt.ifPresent(foundItem -> {
            lock.lock();
            try {
                providerRegistryItems.remove(foundItem);
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * Get currently active providers (in status ok).
     *
     * @return list of providers.
     */
    public List<ProviderRegistryItem> getActiveProviders() {
        return providerRegistryItems.stream().filter(registryItem -> ProviderStatus.OK.equals(registryItem.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Excludes a provider based on the given UID.
     *
     * @param providerUid Provider to exclude
     */
    public void exclude(String providerUid) {
        Optional<ProviderRegistryItem> providerOpt = providerRegistryItems.stream()
                .filter(p -> p.getProviderUid().equals(providerUid)).findAny();
        providerOpt.ifPresent(p -> p.setStatus(ProviderStatus.EXCLUDED));
    }

    /**
     * Provider that should be included. This is a direct include without going to a PENDING state.
     *
     * @param providerUid Provider to include.
     */
    public void include(String providerUid) {
        Optional<ProviderRegistryItem> providerOpt = providerRegistryItems.stream()
                .filter(p -> p.getProviderUid().equals(providerUid)).findAny();
        if (providerOpt.isPresent() && (ProviderStatus.EXCLUDED.equals(providerOpt.get().getStatus()) ||
                ProviderStatus.PENDING.equals(providerOpt.get().getStatus()))) {
            providerOpt.get().include();
        }
    }

    /**
     * Removes all providers.
     */
    public void resetProviders() {
        lock.lock();
        try {
            providerRegistryItems.clear();
        } finally {
            lock.unlock();
        }
    }
}
