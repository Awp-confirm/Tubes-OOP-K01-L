package nimons.entity.item;

import nimons.core.GameConfig;
import nimons.entity.item.interfaces.Preparable;

public abstract class Ingredient extends Item implements Preparable {

    private IngredientState state;
    
    // --- FIELD TIMER (Diasumsikan sudah benar) ---
    protected long currentCookTime = 0; // Waktu yang sudah berlalu (ms)
    protected long lastLogTime = 0;
    // ------------------------------------

    public Ingredient() {}

    public Ingredient(String id, String name, IngredientState state) {
        super(id, name, true);
        this.state = state;
    }

    // getters & setters
    public IngredientState getState() { 
        return state; 
    }

    public void setState(IngredientState state) { 
        this.state = state; 
    }
    
    // --- TAMBAHAN KRITIS UNTUK PROGRESS BAR (Perbaikan Compile Error CookingStation) ---
    
    /**
     * Mengembalikan total waktu yang dibutuhkan untuk mencapai state COOKED (ms).
     */
    public float getRequiredCookingTime() {
        return (float) GameConfig.TIME_TO_COOK_MS;
    }
    
    /**
     * Mengembalikan waktu yang sudah berlalu dalam fase COOKING (ms).
     */
    public float getCurrentCookingTime() {
        // Hanya kembalikan waktu dalam fase COOKING (sampai 5000ms)
        return (float) Math.min(currentCookTime, GameConfig.TIME_TO_COOK_MS); 
    }
    
    /**
     * Mengembalikan waktu total cooking yang sebenarnya (termasuk burn phase).
     * Digunakan untuk progress bar burn phase.
     */
    public float getTotalCookingTime() {
        return (float) currentCookTime;
    }
    
    // ----------------------------------------------------------------------------------
    
    /**
     * Dipanggil oleh CookingStation.update() untuk memajukan timer memasak.
     * Mengembalikan String jika ada log progress atau transisi state yang terjadi.
     * * REVISI LOG: Log lebih representatif.
     */
    public String updateCooking(long deltaTime) { 
        String logMessage = null; 
        
        if (state == IngredientState.COOKING || state == IngredientState.COOKED) {
            currentCookTime += deltaTime;
            
            // --- LOGIKA PENCETAKAN PROGRESS (Setiap 1000ms/1 detik) ---
            if (currentCookTime - lastLogTime >= 1000) { 
                
                long totalTimeBase = GameConfig.TIME_TO_COOK_MS; 
                long totalTimeBurn = GameConfig.TIME_TO_BURN_MS; 
                
                long totalSeconds;
                long secondsElapsed = currentCookTime / 1000;

                // Tentukan total waktu untuk logging/progress display
                if (state == IngredientState.COOKING) {
                    totalSeconds = totalTimeBase / 1000; // 5s
                } else { 
                    // Log progres BURNED relatif terhadap total waktu (15s)
                    totalSeconds = (totalTimeBase + totalTimeBurn) / 1000; // 15s
                }
                
                // Pastikan kita mencetak log hanya jika detik baru terlewati
                if (secondsElapsed * 1000 > lastLogTime) {
                    // Log representatif: [Rice] COOKING [X/Y s]
                    logMessage = String.format("[%s] %s [%d/%d s]", 
                                             getName(), 
                                             state.name(), 
                                             secondsElapsed, 
                                             totalSeconds);
                                             
                    lastLogTime = secondsElapsed * 1000;
                }
            }
            // ----------------------------------------------------------

            // Transisi State: COOKING -> COOKED
            if (state == IngredientState.COOKING && currentCookTime >= GameConfig.TIME_TO_COOK_MS) {
                setState(IngredientState.COOKED);
                
                // lastLogTime direset agar log progress COOKED segera dimulai pada detik ke-6
                lastLogTime = 0; 
                // Log representatif:
                logMessage = String.format("PROCESS COMPLETE: %s is now COOKED.", getName()); 
            } 
            
            // Transisi State: COOKED -> BURNED
            else if (state == IngredientState.COOKED && currentCookTime >= (GameConfig.TIME_TO_COOK_MS + GameConfig.TIME_TO_BURN_MS)) {
                setState(IngredientState.BURNED);
                // Log representatif:
                logMessage = String.format("PROCESS FAILURE: %s is BURNED.", getName()); 
            }
        }
        return logMessage; 
    }

    // abstract methods (tidak diubah)
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