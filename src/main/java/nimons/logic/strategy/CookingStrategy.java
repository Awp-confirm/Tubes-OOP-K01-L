package nimons.logic.strategy;

import nimons.entity.item.Ingredient;

public interface CookingStrategy {
    
    
    boolean cook(Ingredient ingredient, long elapsedTime);
    
    
    boolean canApply(Ingredient ingredient);
    
    
    String getStrategyName();
    
    
    long getCookingDuration();
    
    
    long getBurningThreshold();
    
    
    default void prepareIngredient(Ingredient ingredient) {
        
    }
}
