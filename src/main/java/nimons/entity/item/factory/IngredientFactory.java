package nimons.entity.item.factory;

import nimons.entity.item.Ingredient;
import nimons.entity.item.ingredient.Cucumber;
import nimons.entity.item.ingredient.Fish;
import nimons.entity.item.ingredient.Nori;
import nimons.entity.item.ingredient.Rice;
import nimons.entity.item.ingredient.Shrimp;

/**
 * FACTORY PATTERN
 * 
 * Factory Pattern digunakan untuk menciptakan objek tanpa mengekspos logika pembuatan
 * kepada klien dan mengacu pada objek yang baru dibuat menggunakan interface umum.
 * 
 * Manfaat:
 * 1. Encapsulation: Logika pembuatan ingredient tersembunyi
 * 2. Flexibility: Mudah menambah jenis ingredient baru tanpa mengubah kode klien
 * 3. Single Responsibility: Satu tempat untuk mengelola pembuatan ingredient
 * 4. Loose Coupling: Klien tidak perlu tahu detail implementasi setiap ingredient
 */
public class IngredientFactory {
    
    /**
     * Enum untuk mendefinisikan tipe-tipe ingredient yang tersedia
     */
    public enum IngredientType {
        FISH,
        SHRIMP,
        CUCUMBER,
        RICE,
        NORI
    }
    
    /**
     * Factory method untuk membuat ingredient berdasarkan tipe
     * 
     * @param type Tipe ingredient yang ingin dibuat
     * @return Instance dari ingredient yang sesuai
     * @throws IllegalArgumentException jika tipe tidak dikenali
     */
    public static Ingredient createIngredient(IngredientType type) {
        switch (type) {
            case FISH:
                return new Fish();
            case SHRIMP:
                return new Shrimp();
            case CUCUMBER:
                return new Cucumber();
            case RICE:
                return new Rice();
            case NORI:
                return new Nori();
            default:
                throw new IllegalArgumentException("Unknown ingredient type: " + type);
        }
    }
    
    /**
     * Overloaded factory method yang menerima String
     * Berguna untuk dynamic creation dari input string
     * 
     * @param typeName Nama tipe ingredient (case-insensitive)
     * @return Instance dari ingredient yang sesuai
     */
    public static Ingredient createIngredient(String typeName) {
        try {
            IngredientType type = IngredientType.valueOf(typeName.toUpperCase());
            return createIngredient(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ingredient name: " + typeName);
        }
    }
    
    /**
     * Method untuk mendapatkan semua jenis ingredient yang tersedia
     * Berguna untuk UI atau debugging
     * 
     * @return Array dari semua tipe ingredient
     */
    public static IngredientType[] getAvailableTypes() {
        return IngredientType.values();
    }
}
