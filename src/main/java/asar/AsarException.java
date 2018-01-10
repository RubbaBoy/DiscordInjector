package asar;

/**
 * Thrown when there's an error reading an asar file
 */
public class AsarException extends RuntimeException {
    AsarException(String msg) {
        super(msg);
    }

    AsarException(String msg, Throwable cause) {
        super(msg, cause);
    }
}