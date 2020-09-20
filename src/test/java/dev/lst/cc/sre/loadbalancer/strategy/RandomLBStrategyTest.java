package dev.lst.cc.sre.loadbalancer.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.lst.cc.sre.loadbalancer.ServiceUnavailableException;
import dev.lst.cc.sre.provider.InMemoryProvider;
import dev.lst.cc.sre.registry.ProviderRegistry;
import dev.lst.cc.sre.registry.RegistryFullException;
import dev.lst.cc.sre.registry.ProviderRegistryItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RandomLBStrategyTest {
    ProviderRegistry registry = ProviderRegistry.INSTANCE;

    @BeforeEach
    public void setup() throws RegistryFullException {
        registry.resetProviders();
    }

    // this test is not perfect. there's a chance that this test will fail so we would need to do more iterations or think about a better way to test randomness.
    @Test
    public void getNext_shouldBeOk() throws ServiceUnavailableException, RegistryFullException {
        // given
        List<String> registeredIds = registerItems(10);

        // when
        boolean allMatch = true;
        for (int i = 0; i < 10; i++) {
            if (!registeredIds.get(i).equals(RandomLBStrategy.INSTANCE.getNext().getProviderUid())) {
                allMatch = false;
            }
        }

        // then
        assertThat(allMatch).isFalse();
    }

    @Test
    public void getNext_noProvidersShouldThrowServiceUnavailableException() {
        // given
        // nothing

        // when
        Assertions.assertThrows(ServiceUnavailableException.class, () -> {
            RandomLBStrategy.INSTANCE.getNext();
        });
    }

    private List<String> registerItems(int amount) throws RegistryFullException {
        List<String> providerIds = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            String providerUid = UUID.randomUUID().toString();
            registry.registerProvider(new ProviderRegistryItem(new InMemoryProvider(providerUid)));
            providerIds.add(providerUid);
        }
        return providerIds;
    }

}