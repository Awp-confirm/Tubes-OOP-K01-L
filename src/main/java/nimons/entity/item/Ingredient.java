package nimons.entity.item;

import nimons.core.GameConfig;
import nimons.entity.item.interfaces.Preparable;

public abstract class Ingredient extends Item implements Preparable {

    private IngredientState state;
    
    protected long currentCookTime = 0;
    protected long lastLogTime = 0;

    public Ingredient() {}

    public Ingredient(String id, String name, IngredientState state) {
        super(id, name, true);
        this.state = state;
    }

    public IngredientState getState() { 
        return state; 
    }

    public void setState(IngredientState state) { 
        this.state = state; 
    }
    
    /**
     * Mengembalikan total waktu yang dibutuhkan untuk memasak
     */
    public float getRequiredCookingTime() {
        return (float) GameConfig.TIME_TO_COOK_MS;
    }
    
    /**
     * Mengembalikan waktu yang sudah berlalu saat memasak
     */
    public float getCurrentCookingTime() {
        return (float) Math.min(currentCookTime, GameConfig.TIME_TO_COOK_MS); 
    }
    
    /**
     * Mengembalikan waktu total memasak termasuk fase burn
     */
    public float getTotalCookingTime() {
        return (float) currentCookTime;
    }
    
    /**
     * Update timer memasak dan kelola transisi state (COOKING -> COOKED -> BURNED)
     */
    public String updateCooking(long deltaTime) { 
        String logMessage = null; 
        
        if (state == IngredientState.COOKING || state == IngredientState.COOKED) {
            currentCookTime += deltaTime;
            
            // Proses logging progress setiap 1 detik
            if (currentCookTime - lastLogTime >= 1000) { 
                
                long totalTimeBase = GameConfig.TIME_TO_COOK_MS; 
                long totalTimeBurn = GameConfig.TIME_TO_BURN_MS; 
                
                long totalSeconds;
                long secondsElapsed = currentCookTime / 1000;

                if (state == IngredientState.COOKING) {
                    totalSeconds = totalTimeBase / 1000;
                } else {
                    totalSeconds = (totalTimeBase + totalTimeBurn) / 1000;
                }
                
                if (secondsElapsed * 1000 > lastLogTime) {
                    logMessage = String.format("[%s] %s [%d/%d s]", getName(), state.name(), secondsElapsed, totalSeconds);   
                    lastLogTime = secondsElapsed * 1000;
                }
            }

            // Proses transisi: COOKING -> COOKED
            if (state == IngredientState.COOKING && currentCookTime >= GameConfig.TIME_TO_COOK_MS) {
                setState(IngredientState.COOKED);
                lastLogTime = 0;
                logMessage = String.format("PROCESS COMPLETE: %s is now COOKED.", getName()); 
            } 
            
            // Proses transisi: COOKED -> BURNED
            else if (state == IngredientState.COOKED && currentCookTime >= (GameConfig.TIME_TO_COOK_MS + GameConfig.TIME_TO_BURN_MS)) {
                setState(IngredientState.BURNED);
                logMessage = String.format("PROCESS FAILURE: %s is BURNED.", getName()); 
            }
        }
        return logMessage; 
    }

    @Override
    public abstract boolean canBeChopped();

    @Override
    public abstract boolean canBeCooked();

    @Override
    public abstract boolean canBePlacedOnPlate();

    @Override
    public abstract void chop();

    @Override
    public abstract void cook();
}