package dev.lst.cc.sre.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import dev.lst.cc.sre.provider.InMemoryProvider;
import dev.lst.cc.sre.provider.ProviderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderRegistryTest {
    ProviderRegistry registry = ProviderRegistry.INSTANCE;

    @BeforeEach
    public void setup() throws RegistryFullException {
        registry.resetProviders();
    }

    @Test
    public void registerProvider_shouldAllowOnly10Providers() throws RegistryFullException {
        // given
        registerItems(10);

        // when
        // then
        Assertions.assertThrows(RegistryFullException.class, () -> {
            String providerUid = UUID.randomUUID().toString();
            registry.registerProvider(new ProviderRegistryItem(new InMemoryProvider(providerUid)));
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

    @Test
    public void registerProvider_alreadyContainsItem() throws RegistryFullException {
        // given
        List<String> strings = registerItems(1);

        // when
        registry.registerProvider(new ProviderRegistryItem(new InMemoryProvider(strings.get(0))));

        // then
        assertThat(registry.getActiveProviders().size()).isEqualTo(1);
    }

    @Test
    public void removeProvider_shouldBeOk() throws RegistryFullException {
        // given
        List<String> strings = registerItems(1);

        // when
        registry.removeProvider(strings.get(0));

        // then
        assertThat(registry.getActiveProviders().size()).isEqualTo(0);
    }

    @Test
    public void removeProvider_removingItemThatIsNotThere() throws RegistryFullException {
        // given
        registerItems(1);

        // when
        registry.removeProvider(UUID.randomUUID().toString());

        // then
        assertThat(registry.getActiveProviders().size()).isEqualTo(1);
    }

    @Test
    public void getActiveProviders_shouldContainItem() throws RegistryFullException {
        // when
        List<String> strings = registerItems(1);

        // then
        assertThat(registry.getActiveProviders().get(0).getProviderUid()).isEqualTo(strings.get(0));
    }

    @Test
    public void excludeItem_shouldBeExcluded() throws RegistryFullException {
        // given
        registerItems(5);
        String uid = UUID.randomUUID().toString();
        ProviderRegistryItem providerRegistryItem = new ProviderRegistryItem(new InMemoryProvider(uid));
        registry.registerProvider(providerRegistryItem);

        // when
        registry.exclude(uid);

        // then
        List<ProviderRegistryItem> activeProviders = registry.getActiveProviders();

        assertThat(activeProviders.stream().map(ProviderRegistryItem::getProviderUid).collect(Collectors.toList()))
                .doesNotContain(uid);
        assertThat(activeProviders.size()).isEqualTo(5);
        assertThat(providerRegistryItem.getStatus()).isEqualTo(ProviderStatus.EXCLUDED);
    }

    @Test
    public void includeItem_shouldBeIncluded() throws RegistryFullException {
        // given
        String uid = UUID.randomUUID().toString();
        ProviderRegistryItem providerRegistryItem = new ProviderRegistryItem(new InMemoryProvider(uid));
        registry.registerProvider(providerRegistryItem);
        registry.exclude(uid);

        // when
        registry.include(uid);

        // then
        List<ProviderRegistryItem> activeProviders = registry.getActiveProviders();

        assertThat(activeProviders.size()).isEqualTo(1);
        assertThat(activeProviders.get(0).getProviderUid()).isEqualTo(uid);
        assertThat(providerRegistryItem.getStatus()).isEqualTo(ProviderStatus.OK);
    }

}