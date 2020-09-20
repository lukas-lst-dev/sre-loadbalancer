package dev.lst.cc.sre.loadbalancer;

/**
 * Exception indicationg that the service is not available.
 */
public class ServiceUnavailableException extends Exception {
    public ServiceUnavailableException(String message) {
    }
}
