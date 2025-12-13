package nimons.logic.strategy;

import nimons.core.GameConfig;
import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class FryingStrategy implements CookingStrategy {
    
    private final long cookingDuration;
    private final long burningThreshold;
    
    
    public FryingStrategy() {
        this.cookingDuration = GameConfig.TIME_TO_COOK_MS;
        this.burningThreshold = GameConfig.TIME_TO_COOK_MS + GameConfig.TIME_TO_BURN_MS;
    }
    
    
    public FryingStrategy(long cookingDuration, long burningThreshold) {
        this.cookingDuration = cookingDuration;
        this.burningThreshold = burningThreshold;
    }
    
    @Override
        
    public boolean cook(Ingredient ingredient, long elapsedTime) {
        IngredientState currentState = ingredient.getState();
        
        
        if (elapsedTime >= burningThreshold) {
            
            if (currentState != IngredientState.BURNED) {
                ingredient.setState(IngredientState.BURNED);
                System.out.println("[FryingStrategy] Ingredient burned!");
            }
            return false; 
            
        } else if (elapsedTime >= cookingDuration) {
            
            if (currentState != IngredientState.COOKED) {
                ingredient.setState(IngredientState.COOKED);
                System.out.println("[FryingStrategy] Ingredient cooked perfectly!");
                return true; 
            }
            
        } else {
            
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
        
        if (ingredient.getState() == IngredientState.RAW) {
            ingredient.setState(IngredientState.COOKING);
        }
    }
}
