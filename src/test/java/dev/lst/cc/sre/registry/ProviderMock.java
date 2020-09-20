package dev.lst.cc.sre.registry;

import dev.lst.cc.sre.provider.Provider;
import dev.lst.cc.sre.provider.ProviderHealthCheckStatus;

/**
 * This is a Provider mock that allows to block the get() request. It also allows to block multiple threads.
 * <p>
 *     <ul>
 *         <li>getFinish() allows you to control if the requests should be executed and therefore finished</li>
 *         <li>getStarted() will allow you to check in another thread if all the requests have been started.
 *         through setting setCountdownRequests() you can control after how many threads it should set started to true</li>
 *     </ul>
 * </p>
 */
class ProviderMock implements Provider {

    private final String uid;
    private boolean finish = false;
    private boolean started = false;
    private int countdownRequests = 1;
    private ProviderHealthCheckStatus status = ProviderHealthCheckStatus.OK;

    public ProviderMock(String uid) {
        this.uid = uid;
    }

    @Override
    public String get() {
        countdownRequests--;
        if (countdownRequests == 0) {
            // so we have the amount of wanted requests waiting here now and can inform through
            // started that we are waiting.
            started = true;
        }

        while (!finish) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return uid;
    }

    @Override
    public ProviderHealthCheckStatus healthCheck() {
        return status;
    }

    @Override
    public String getUid() {
        return uid;
    }

    public ProviderHealthCheckStatus getStatus() {
        return status;
    }

    public void setStatus(ProviderHealthCheckStatus status) {
        this.status = status;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public boolean isStarted() {
        return started;
    }

    public int getCountdownRequests() {
        return countdownRequests;
    }

    public void setCountdownRequests(int countdownRequests) {
        this.countdownRequests = countdownRequests;
    }
}
