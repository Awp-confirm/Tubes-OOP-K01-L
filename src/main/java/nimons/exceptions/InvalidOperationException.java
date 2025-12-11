package nimons.exceptions;

/**
 * Exception thrown when an invalid operation is attempted.
 */
public class InvalidOperationException extends GameException {
    
    private String operation;
    private String reason;
    
    public InvalidOperationException(String operation, String reason) {
        super(String.format("Invalid operation '%s': %s", operation, reason));
        this.operation = operation;
        this.reason = reason;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getReason() {
        return reason;
    }
}
