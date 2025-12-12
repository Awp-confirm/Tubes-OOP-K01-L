package nimons.logic.strategy;

import nimons.core.GameConfig;
import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

/**
 * STRATEGY PATTERN - Concrete Strategy: Grilling
 * 
 * Strategi untuk memasak dengan cara memanggang (grilling).
 * Cocok untuk: Fish, vegetables, dll
 * 
 * Grilling cepat tapi mudah burn jika tidak diperhatikan
 */
public class GrillingStrategy implements CookingStrategy {
    
    private final long cookingDuration;
    private final long burningThreshold;
    
    /**
     * Constructor - grilling is fast but burns quickly
     */
    public GrillingStrategy() {
        // Grilling is faster (0.8x normal time)
        this.cookingDuration = (long) (GameConfig.TIME_TO_COOK_MS * 0.8);
        // But burns quickly (shorter burn threshold)
        this.burningThreshold = (long) (cookingDuration + GameConfig.TIME_TO_BURN_MS * 0.6);
    }
    
    /**
     * Constructor dengan custom timing
     */
    public GrillingStrategy(long cookingDuration, long burningThreshold) {
        this.cookingDuration = cookingDuration;
        this.burningThreshold = burningThreshold;
    }
    
    @Override
    public boolean cook(Ingredient ingredient, long elapsedTime) {
        IngredientState currentState = ingredient.getState();
        
        // Handle state transitions
        if (elapsedTime >= burningThreshold) {
            // Grilled too long - burned!
            if (currentState != IngredientState.BURNED) {
                ingredient.setState(IngredientState.BURNED);
                System.out.println("[GrillingStrategy] Ingredient burned on grill!");
            }
            return false;
            
        } else if (elapsedTime >= cookingDuration) {
            // Perfectly grilled!
            if (currentState != IngredientState.COOKED) {
                ingredient.setState(IngredientState.COOKED);
                System.out.println("[GrillingStrategy] Ingredient grilled perfectly!");
                return true;
            }
            
        } else {
            // Still grilling
            if (currentState != IngredientState.COOKING) {
                ingredient.setState(IngredientState.COOKING);
            }
        }
        
        return false;
    }
    
    @Override
    public boolean canApply(Ingredient ingredient) {
        return ingredient != null && 
               ingredient.canBeCooked() &&
               (ingredient.getState() == IngredientState.RAW || 
                ingredient.getState() == IngredientState.COOKING);
    }
    
    @Override
    public String getStrategyName() {
        return "Grilling";
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
        if (ingredient.getState() == IngredientState.RAW) {
            ingredient.setState(IngredientState.COOKING);
            System.out.println("[GrillingStrategy] Starting to grill ingredient...");
        }
    }
}
