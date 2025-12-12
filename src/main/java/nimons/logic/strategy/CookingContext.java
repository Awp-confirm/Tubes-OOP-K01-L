package nimons.logic.strategy;

/**
 * STRATEGY PATTERN - Context Class
 * 
 * Context yang menggunakan CookingStrategy.
 * Class ini bertindak sebagai wrapper yang memungkinkan strategy diganti saat runtime.
 * 
 * Contoh penggunaan:
 * <pre>
 * CookingContext context = new CookingContext();
 * 
 * // Use frying for shrimp
 * context.setStrategy(new FryingStrategy());
 * context.cookIngredient(shrimp, elapsedTime);
 * 
 * // Use boiling for rice
 * context.setStrategy(new BoilingStrategy());
 * context.cookIngredient(rice, elapsedTime);
 * </pre>
 */
public class CookingContext {
    
    private CookingStrategy strategy;
    
    /**
     * Constructor dengan default strategy
     */
    public CookingContext() {
        this.strategy = new FryingStrategy(); // Default to frying
    }
    
    /**
     * Constructor dengan specific strategy
     */
    public CookingContext(CookingStrategy strategy) {
        this.strategy = strategy;
    }
    
    /**
     * Set cooking strategy (dapat diubah saat runtime)
     * 
     * @param strategy Strategy baru yang akan digunakan
     */
    public void setStrategy(CookingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
        System.out.println("[CookingContext] Strategy changed to: " + strategy.getStrategyName());
    }
    
    /**
     * Get current strategy
     */
    public CookingStrategy getStrategy() {
        return strategy;
    }
    
    /**
     * Execute cooking menggunakan current strategy
     * 
     * @param ingredient Ingredient yang akan dimasak
     * @param elapsedTime Waktu yang sudah berlalu
     * @return true jika cooking selesai
     */
    public boolean cookIngredient(nimons.entity.item.Ingredient ingredient, long elapsedTime) {
        if (!strategy.canApply(ingredient)) {
            System.err.println("[CookingContext] Cannot apply " + strategy.getStrategyName() + 
                             " to ingredient: " + ingredient.getName());
            return false;
        }
        
        return strategy.cook(ingredient, elapsedTime);
    }
    
    /**
     * Prepare ingredient sebelum cooking
     */
    public void prepareIngredient(nimons.entity.item.Ingredient ingredient) {
        strategy.prepareIngredient(ingredient);
    }
    
    /**
     * Get info tentang current strategy
     */
    public String getStrategyInfo() {
        return String.format("Strategy: %s (Cook: %dms, Burn: %dms)",
            strategy.getStrategyName(),
            strategy.getCookingDuration(),
            strategy.getBurningThreshold());
    }
}
