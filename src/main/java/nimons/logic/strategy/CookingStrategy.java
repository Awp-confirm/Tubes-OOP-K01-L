package nimons.logic.strategy;

import nimons.entity.item.Ingredient;

/**
 * STRATEGY PATTERN - Strategy Interface
 * 
 * Strategy Pattern mendefinisikan family of algorithms, encapsulate masing-masing,
 * dan membuatnya interchangeable. Strategy memungkinkan algorithm bervariasi
 * independent dari clients yang menggunakannya.
 * 
 * Manfaat:
 * 1. Open/Closed Principle: Mudah menambah strategi baru tanpa ubah kode existing
 * 2. Eliminates Conditional Logic: Mengganti if-else chains dengan polymorphism
 * 3. Runtime Flexibility: Strategy bisa diganti saat runtime
 * 4. Single Responsibility: Setiap strategy fokus pada satu cara cooking
 * 5. Testability: Setiap strategy bisa ditest secara independent
 * 
 * Interface ini mendefinisikan contract untuk berbagai metode cooking.
 */
public interface CookingStrategy {
    
    /**
     * Apply cooking method ke ingredient
     * 
     * @param ingredient Ingredient yang akan dimasak
     * @param elapsedTime Waktu yang sudah berlalu (ms)
     * @return true jika cooking selesai (reached COOKED state)
     */
    boolean cook(Ingredient ingredient, long elapsedTime);
    
    /**
     * Check apakah ingredient compatible dengan strategi ini
     * 
     * @param ingredient Ingredient yang akan dicek
     * @return true jika ingredient bisa dimasak dengan strategi ini
     */
    boolean canApply(Ingredient ingredient);
    
    /**
     * Get nama strategi cooking ini
     * 
     * @return Nama strategi (e.g., "Frying", "Boiling")
     */
    String getStrategyName();
    
    /**
     * Get waktu cooking yang dibutuhkan (ms)
     * 
     * @return Durasi cooking dalam milliseconds
     */
    long getCookingDuration();
    
    /**
     * Get waktu burning threshold (ms)
     * Setelah waktu ini, ingredient akan menjadi BURNED
     * 
     * @return Durasi sebelum burning dalam milliseconds
     */
    long getBurningThreshold();
    
    /**
     * Reset ingredient state jika perlu sebelum cooking
     * 
     * @param ingredient Ingredient yang akan di-reset
     */
    default void prepareIngredient(Ingredient ingredient) {
        // Default: tidak perlu preparation
    }
}
