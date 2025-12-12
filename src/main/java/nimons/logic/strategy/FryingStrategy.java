package nimons.logic.strategy;

import nimons.core.GameConfig;
import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

/**
 * STRATEGY PATTERN - Concrete Strategy: Frying
 * 
 * Strategi untuk memasak dengan cara menggoreng (frying).
 * Cocok untuk: Shrimp, Fish (dalam beberapa recipe)
 */
public class FryingStrategy implements CookingStrategy {
    
    private final long cookingDuration;
    private final long burningThreshold;
    
    /**
     * Constructor dengan default timing dari GameConfig
     */
    public FryingStrategy() {
        this.cookingDuration = GameConfig.TIME_TO_COOK_MS;
        this.burningThreshold = GameConfig.TIME_TO_COOK_MS + GameConfig.TIME_TO_BURN_MS;
    }
    
    /**
     * Constructor dengan custom timing
     */
    public FryingStrategy(long cookingDuration, long burningThreshold) {
        this.cookingDuration = cookingDuration;
        this.burningThreshold = burningThreshold;
    }
    
    @Override
    public boolean cook(Ingredient ingredient, long elapsedTime) {
        IngredientState currentState = ingredient.getState();
        
        // Handle state transitions based on elapsed time
        if (elapsedTime >= burningThreshold) {
            // Burned!
            if (currentState != IngredientState.BURNED) {
                ingredient.setState(IngredientState.BURNED);
                System.out.println("[FryingStrategy] Ingredient burned!");
            }
            return false; // Cooking failed - burned
            
        } else if (elapsedTime >= cookingDuration) {
            // Perfectly cooked!
            if (currentState != IngredientState.COOKED) {
                ingredient.setState(IngredientState.COOKED);
                System.out.println("[FryingStrategy] Ingredient cooked perfectly!");
                return true; // Cooking complete
            }
            
        } else {
            // Still cooking
            if (currentState != IngredientState.COOKING) {
                ingredient.setState(IngredientState.COOKING);
            }
        }
        
        return false; // Cooking in progress
    }
    
    @Override
    public boolean canApply(Ingredient ingredient) {
        // Frying dapat diterapkan pada ingredient RAW yang mendukung cooking
        return ingredient != null && 
               ingredient.canBeCooked() &&
               (ingredient.getState() == IngredientState.RAW || 
                ingredient.getState() == IngredientState.COOKING);
    }
    
    @Override
    public String getStrategyName() {
        return "Frying";
    }
    
    @Override
    public long getCookingDuration() {
        return cookingDuration;
    }
    
    @Override
    public long getBurningThreshold() {
        return burningThreshold;
    }
    
    @Override
    public void prepareIngredient(Ingredient ingredient) {
        // Set ke COOKING state jika masih RAW
        if (ingredient.getState() == IngredientState.RAW) {
            ingredient.setState(IngredientState.COOKING);
        }
    }
}
