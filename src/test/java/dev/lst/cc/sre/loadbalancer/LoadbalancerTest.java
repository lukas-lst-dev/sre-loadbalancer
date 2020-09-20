package dev.lst.cc.sre.loadbalancer;

import java.util.List;
import java.util.UUID;

import dev.lst.cc.sre.loadbalancer.strategy.RandomLBStrategy;
import dev.lst.cc.sre.provider.InMemoryProvider;
import dev.lst.cc.sre.registry.ProviderRegistry;
import dev.lst.cc.sre.registry.RegistryFullException;
import dev.lst.cc.sre.registry.ProviderRegistryItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class LoadbalancerTest {

    Loadbalancer lb = new Loadbalancer(RandomLBStrategy.INSTANCE);
    ProviderRegistry registry = ProviderRegistry.INSTANCE;

    @BeforeEach
    public void setup() {
        registry.resetProviders();
    }

    @Test
    public void get_shouldAnswer() throws RegistryFullException, ServiceUnavailableException {
        // given
        String providerUid = UUID.randomUUID().toString();
        registry.registerProvider(new ProviderRegistryItem(new InMemoryProvider(providerUid)));

        // then
        assertThat(lb.get()).isEqualTo(providerUid);
    }

    @Test
    public void excludeProvider_ShouldExcludeProvider() throws RegistryFullException {
        // given
        String providerUid = UUID.randomUUID().toString();
        registry.registerProvider(new ProviderRegistryItem(new InMemoryProvider(providerUid)));

        // when
        lb.excludeProvider(providerUid);

        // then
        assertThat(registry.getActiveProviders().size()).isEqualTo(0);
    }

    @Test
    public void includeProvider_ShouldExcludeProvider() throws RegistryFullException {
        // given
        String providerUid = UUID.randomUUID().toString();
        registry.registerProvider(new ProviderRegistryItem(new InMemoryProvider(providerUid)));

        // when
        lb.excludeProvider(providerUid);
        lb.includeProvider(providerUid);

        // then
        List<ProviderRegistryItem> activeProviders = registry.getActiveProviders();
        System.out.println(activeProviders.size());
        assertThat(activeProviders.size()).isEqualTo(1);
    }
}