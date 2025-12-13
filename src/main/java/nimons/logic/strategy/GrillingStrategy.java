package nimons.logic.strategy;

import nimons.core.GameConfig;
import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class GrillingStrategy implements CookingStrategy {
    
    private final long cookingDuration;
    private final long burningThreshold;
    
    
    public GrillingStrategy() {
        
        this.cookingDuration = (long) (GameConfig.TIME_TO_COOK_MS * 0.8);
        
        this.burningThreshold = (long) (cookingDuration + GameConfig.TIME_TO_BURN_MS * 0.6);
    }
    
    
    public GrillingStrategy(long cookingDuration, long burningThreshold) {
        this.cookingDuration = cookingDuration;
        this.burningThreshold = burningThreshold;
    }
    
    @Override
        
    public boolean cook(Ingredient ingredient, long elapsedTime) {
        IngredientState currentState = ingredient.getState();
        
        
        if (elapsedTime >= burningThreshold) {
            
            if (currentState != IngredientState.BURNED) {
                ingredient.setState(IngredientState.BURNED);
                System.out.println("[GrillingStrategy] Ingredient burned on grill!");
            }
            return false;
            
        } else if (elapsedTime >= cookingDuration) {
            
            if (currentState != IngredientState.COOKED) {
                ingredient.setState(IngredientState.COOKED);
                System.out.println("[GrillingStrategy] Ingredient grilled perfectly!");
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
