package util.helper;

/**
 * Exception thrown when feature model merging operations fail.
 */
public class MergerException extends RuntimeException {
    
    public MergerException(String message) {
        super(message);
    }
    
    public MergerException(String message, Throwable cause) {
        super(message, cause);
    }
}
