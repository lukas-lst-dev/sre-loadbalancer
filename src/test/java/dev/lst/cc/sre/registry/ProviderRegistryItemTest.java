package dev.lst.cc.sre.registry;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import dev.lst.cc.sre.provider.InMemoryProvider;
import dev.lst.cc.sre.provider.ProviderHealthCheckStatus;
import org.junit.jupiter.api.Test;

import static dev.lst.cc.sre.provider.ProviderStatus.BUSY;
import static dev.lst.cc.sre.provider.ProviderStatus.EXCLUDED;
import static dev.lst.cc.sre.provider.ProviderStatus.OK;
import static dev.lst.cc.sre.provider.ProviderStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

class ProviderRegistryItemTest {

    @Test
    void healthCheck_shouldPutToStatusExcluded() {
        // given
        String uid = UUID.randomUUID().toString();
        ProviderMock blockingProvider = new ProviderMock(uid);
        ProviderRegistryItem item = new ProviderRegistryItem(blockingProvider);
        blockingProvider.setStatus(ProviderHealthCheckStatus.ERROR);

        // when
        item.healthCheck();

        // then
        assertThat(item.getStatus()).isEqualTo(EXCLUDED);
    }

    @Test
    void healthCheck_shouldPutToStatusExcludedThenPendingThenOk() {
        // given
        String uid = UUID.randomUUID().toString();
        ProviderMock blockingProvider = new ProviderMock(uid);
        ProviderRegistryItem item = new ProviderRegistryItem(blockingProvider);
        blockingProvider.setStatus(ProviderHealthCheckStatus.ERROR);

        // when
        item.healthCheck();

        // then
        assertThat(item.getStatus()).isEqualTo(EXCLUDED);

        // when
        blockingProvider.setStatus(ProviderHealthCheckStatus.OK);
        item.healthCheck();

        // then
        assertThat(item.getStatus()).isEqualTo(PENDING);

        // when (second healthcheck that is ok, should go back to ok
        item.healthCheck();

        // then
        assertThat(item.getStatus()).isEqualTo(OK);

    }

    @Test
    void executeGet_shouldIncreaseAndDecreaseOngoingCounter() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        String uid = UUID.randomUUID().toString();
        ProviderMock blockingProvider = new ProviderMock(uid);
        ProviderRegistryItem item = new ProviderRegistryItem(blockingProvider);

        // when
        assertThat(item.getOngoingRequests()).isEqualTo(0);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<String> future = executor.submit(item::executeGet);

        // then
        while (!blockingProvider.isStarted()) {
            Thread.sleep(20);
        }
        assertThat(item.getOngoingRequests()).isEqualTo(1);
        blockingProvider.setFinish(true);
        String response = future.get(1, TimeUnit.SECONDS);
        assertThat(response).isEqualTo(uid);
        assertThat(item.getOngoingRequests()).isEqualTo(0);
    }

    @Test
    void executeGet_having10RunningThreadsShouldChangeStatusToBusy() throws Exception {
        // given
        String uid = UUID.randomUUID().toString();
        ProviderMock blockingProvider = new ProviderMock(uid);
        blockingProvider.setCountdownRequests(10);
        ProviderRegistryItem item = new ProviderRegistryItem(blockingProvider);

        // when
        assertThat(item.getOngoingRequests()).isEqualTo(0);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Future<String> future = null;
        for (int i = 0; i < 10; i++) {
            future = executor.submit(item::executeGet);
        }

        // then
        while (!blockingProvider.isStarted()) {
            Thread.sleep(20);
        }

        // ok, here we have it. we have 10 ongoing requests and status is busy.
        assertThat(item.getOngoingRequests()).isEqualTo(10);
        assertThat(item.getStatus()).isEqualTo(BUSY);


        blockingProvider.setFinish(true);
        String response = future.get(1, TimeUnit.SECONDS);
        assertThat(response).isEqualTo(uid);
        assertThat(item.getOngoingRequests()).isEqualTo(0);
    }


    @Test
    void include_shouldInclude() {
        // given
        String uid = UUID.randomUUID().toString();
        ProviderRegistryItem item = new ProviderRegistryItem(new InMemoryProvider(uid));
        item.setStatus(EXCLUDED);

        // when
        item.include();

        // then
        assertThat(item.getStatus()).isEqualTo(OK);
    }
}