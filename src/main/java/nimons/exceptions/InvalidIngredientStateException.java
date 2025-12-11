package nimons.exceptions;

/**
 * Exception thrown when an ingredient is in an invalid state for the requested operation.
 */
public class InvalidIngredientStateException extends GameException {
    
    private String ingredientName;
    private String currentState;
    private String requiredState;
    
    public InvalidIngredientStateException(String ingredientName, String currentState, String requiredState) {
        super(String.format("Invalid state for %s: current=%s, required=%s", 
                          ingredientName, currentState, requiredState));
        this.ingredientName = ingredientName;
        this.currentState = currentState;
        this.requiredState = requiredState;
    }
    
    public String getIngredientName() {
        return ingredientName;
    }
    
    public String getCurrentState() {
        return currentState;
    }
    
    public String getRequiredState() {
        return requiredState;
    }
}
