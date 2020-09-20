package dev.lst.cc.sre.registry;

/**
 * Exception that is thrown if the registry is currently full.
 */
public class RegistryFullException extends Exception {
    public RegistryFullException(String message) {
    }
}
