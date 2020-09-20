package dev.lst.cc.sre;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import dev.lst.cc.sre.loadbalancer.Loadbalancer;
import dev.lst.cc.sre.loadbalancer.ServiceUnavailableException;
import dev.lst.cc.sre.loadbalancer.strategy.RandomLBStrategy;
import dev.lst.cc.sre.loadbalancer.strategy.RoundRobinLBStrategy;
import dev.lst.cc.sre.provider.InMemoryProvider;
import dev.lst.cc.sre.provider.ProviderStatus;
import dev.lst.cc.sre.provider.SlowInMemoryProvider;
import dev.lst.cc.sre.registry.ProviderRegistry;
import dev.lst.cc.sre.registry.ProviderRegistryItem;
import dev.lst.cc.sre.registry.RegistryFullException;

/**
 * Simulates a few things.
 */
public class LoadbalancerApp {

    public static void main(String[] args) throws InterruptedException {

        executeStep1And2And3();

        executeStep4();

        executeStep5();


        executeStep6And7();


        executeStep8();

        System.out.println("the rest from here on now are just healthchecks.");
    }

    private static void executeStep1And2And3() {
        // create a loadbalancer with Random Access Strategy:

        Loadbalancer randomProviderLoadbalancer = new Loadbalancer(RandomLBStrategy.INSTANCE);

        // Step 2
        // create and register some providers
        for (int i = 0; i < 10; i++) {
            ProviderRegistryItem providerRegistryItem = new ProviderRegistryItem(new InMemoryProvider(UUID.randomUUID().toString()));
            try {
                randomProviderLoadbalancer.registerProvider(providerRegistryItem);
            } catch (RegistryFullException e) {
                e.printStackTrace();
            }
        }

        // Step 1 / Step 3
        // execute get call a few times (this also shows that the provider is random but for this, you should look at the tests)
        // also obviously, system.out.println...
        System.out.println("--- Step 1 / 3: get calls and Random LB -");
        for (int i = 0; i < 100; i++) {
            try {
                System.out.println(randomProviderLoadbalancer.get());
            } catch (ServiceUnavailableException e) {
                e.printStackTrace();
            }
        }

        System.out.println("- End Step 1 / 3: get calls and Random LB ---");
    }

    private static void executeStep4() {
        // Step 4
        // RoundRobin LB
        Loadbalancer roundRobinLoadbalancer = new Loadbalancer(RoundRobinLBStrategy.INSTANCE);

        // since the registry is a singleton, we don't need to take care of that again. all the providers are still registered.
        // let's execute a few get calls...
        System.out.println("--- Step 4: RoundRobin LB -");

        for (int i = 0; i < 100; i++) {
            try {
                System.out.println(roundRobinLoadbalancer.get());
            } catch (ServiceUnavailableException e) {
                e.printStackTrace();
            }
        }
        System.out.println("- End Step 4: RoundRobin LB ---");
    }

    private static void executeStep5() {
        // Step 5
        // exclude / include a node
        Loadbalancer roundRobinLoadbalancer = new Loadbalancer(RoundRobinLBStrategy.INSTANCE);
        System.out.println("--- Step 5: Exclude / Include a provider -");
        String providerUid = ProviderRegistry.INSTANCE.getActiveProviders().get(0).getProviderUid();
        System.out.println("We currently have " + ProviderRegistry.INSTANCE.getActiveProviders().size() + " active, excluding provider uid " + providerUid);
        roundRobinLoadbalancer.excludeProvider(providerUid);
        System.out.println("We now have " + ProviderRegistry.INSTANCE.getActiveProviders().size() + " active, going to add it back in...");
        roundRobinLoadbalancer.includeProvider(providerUid);
        System.out.println("We now have " + ProviderRegistry.INSTANCE.getActiveProviders().size() + " active, back to normal");
        System.out.println("- End Step 5: Exclude / Include a provider -");
    }

    private static void executeStep6And7() throws InterruptedException {
        // Step 6 / Step 7
        // provider health check. have a look at the output. you should see something like this regularly pop up:
        // - provider <some-provider-uid> healthcheck executed. healt is <health>, overall status is <overall-state>
        // initial execution is after 5 seconds, after that, every 30 seconds.
        // I will move one to excluded again so we can see it.

        System.out.println("--- Step 6 / Step 7: providerHealthCheck -");
        Loadbalancer roundRobinLoadbalancer = new Loadbalancer(RoundRobinLBStrategy.INSTANCE);
        String providerUid = ProviderRegistry.INSTANCE.getActiveProviders().get(0).getProviderUid();
        roundRobinLoadbalancer.excludeProvider(providerUid);
        // we sleep for 6 seconds so we can see this in the first printout
        Thread.sleep(6000);
        // now you can see that there is one in state pending. this is the case since the healthcheck was successful.
        // After the next check it will be included again (this also shows step 7).
        roundRobinLoadbalancer.includeProvider(providerUid);
        System.out.println("- End Step 6 / Step 7: providerHealthCheck ---");
    }


    private static void executeStep8() {
        // Step 8
        // now, here I create a special provider. This one will take 2 seconds to answer and I will request those calls in a
        // separate thread so we can see the providers slowly disappear and eventually be fully busy.

        System.out.println("--- Step 8: curcuit breaker -");
        Loadbalancer randomProviderLoadbalancer = new Loadbalancer(RandomLBStrategy.INSTANCE);

        ProviderRegistry.INSTANCE.resetProviders();
        List<ProviderRegistryItem> providers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ProviderRegistryItem providerRegistryItem = new ProviderRegistryItem(new SlowInMemoryProvider(UUID.randomUUID().toString()));
            providers.add(providerRegistryItem);
            try {
                randomProviderLoadbalancer.registerProvider(providerRegistryItem);
            } catch (RegistryFullException e) {
                e.printStackTrace();
            }
        }

        // now we're going to execute 10 * 10 calls. With this we will see that all the providers will be busy for a bit.
        ExecutorService executor = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 10; i++) {
            for (int z = 0; z < 10; z++) {
                Future<String> future = executor.submit(randomProviderLoadbalancer::get);
            }
            System.out.print("Current status after " + (i * 10 + 10) + " requests is : ");
            providers.forEach(System.out::print);
            System.out.println();

            if (i == 9) {
                System.out.println("Overall we have " + providers.stream()
                        .filter(p -> p.getStatus().equals(ProviderStatus.BUSY)).collect(Collectors.toList()).size() + " with status busy");

                // now we execute another one and we should get an exception.
                try {
                    randomProviderLoadbalancer.get();
                } catch (ServiceUnavailableException e) {
                    System.out.println("we caught a ServiceUnavailableException which is correct!!!");
                }
            }
        }

        System.out.println("- End Step 8: curcuit breaker ---");
    }

}
