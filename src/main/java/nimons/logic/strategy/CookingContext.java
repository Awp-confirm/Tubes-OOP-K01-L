package nimons.logic.strategy;

public class CookingContext {
    
    private CookingStrategy strategy;
    
    
    public CookingContext() {
        this.strategy = new FryingStrategy(); 
    }
    
    
    public CookingContext(CookingStrategy strategy) {
        this.strategy = strategy;
    }
    
    
    public void setStrategy(CookingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
        System.out.println("[CookingContext] Strategy changed to: " + strategy.getStrategyName());
    }
    
    
    public CookingStrategy getStrategy() {
        return strategy;
    }
    
    
        
    public boolean cookIngredient(nimons.entity.item.Ingredient ingredient, long elapsedTime) {
        if (!strategy.canApply(ingredient)) {
            System.err.println("[CookingContext] Cannot apply " + strategy.getStrategyName() + 
                             " to ingredient: " + ingredient.getName());
            return false;
        }
        
        return strategy.cook(ingredient, elapsedTime);
    }
    
    
        
    public void prepareIngredient(nimons.entity.item.Ingredient ingredient) {
        strategy.prepareIngredient(ingredient);
    }
    
    
    public String getStrategyInfo() {
        return String.format("Strategy: %s (Cook: %dms, Burn: %dms)",
            strategy.getStrategyName(),
            strategy.getCookingDuration(),
            strategy.getBurningThreshold());
    }
}
