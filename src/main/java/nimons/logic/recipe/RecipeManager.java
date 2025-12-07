package nimons.logic.recipe;

import nimons.entity.item.Dish;
import nimons.entity.item.Item;
import nimons.entity.item.interfaces.Preparable;
import java.util.ArrayList;
import java.util.List;

/**
 * RecipeManager bertanggung jawab untuk mengidentifikasi kombinasi Item yang valid
 * dan menentukan nama hidangan (Dish) yang dihasilkan.
 */
public class RecipeManager {

    /**
     * Mencari kecocokan resep dari dua Item yang digabungkan (Item A + Item B).
     * Jika tidak ada resep bernama, akan mengembalikan Dish generik (Fallback).
     */
    public static Dish findMatch(Item item1, Item item2) {
        if (item1 == null || item2 == null) return null;

        String n1 = item1.getName();
        String n2 = item2.getName();
        
        // --- TAHAP 1: CEK RESEP SPESIAL (NAMED DISH) ---
        
        // 1. SUSHI BASE (Nasi + Nori + Cooked)
        boolean hasNori = check(n1, n2, "Nori");
        boolean hasRice = check(n1, n2, "Rice") || check(n1, n2, "Nasi"); 
        boolean isCooked = check(n1, n2, "Cooked");

        if (hasNori && hasRice && isCooked) {
            return createDish("Sushi Base", item1, item2);
        }

        // 2. VARIAN MAKI (Sushi Base + Isi)
        if (check(n1, n2, "Sushi Base")) {
            // Kappa Maki: Base + Timun Chopped
            if (check(n1, n2, "Timun", "Chopped") || check(n1, n2, "Cucumber", "Chopped")) {
                return createDish("Kappa Maki", item1, item2);
            }
            // Sakana Maki: Base + Ikan Chopped
            if (check(n1, n2, "Ikan", "Chopped") || check(n1, n2, "Fish", "Chopped")) {
                return createDish("Sakana Maki", item1, item2);
            }
            // Ebi Maki: Base + Udang Cooked
            if (check(n1, n2, "Udang", "Cooked") || check(n1, n2, "Shrimp", "Cooked")) {
                return createDish("Ebi Maki", item1, item2);
            }
        }
        
        // 3. SPECIAL ROLL (Maki + Topping)
        // Fish Cucumber Roll: Sakana Maki + Timun Chopped
        if (check(n1, n2, "Sakana Maki") && check(n1, n2, "Timun", "Chopped")) {
             return createDish("Fish Cucumber Roll", item1, item2);
        }

        // --- TAHAP 2: FALLBACK (CUSTOM DISH / VALID COMBINATION) ---
        
        // Kombinasi apapun di piring adalah Dish yang VALID.
        // Kita gabungkan nama item untuk Dish generik.
        String customName = item1.getName() + " + " + item2.getName();
        
        return createDish(customName, item1, item2);
    }

    // --- HELPER METHODS ---

    /**
     * Mengecek apakah salah satu item mengandung kata kunci tertentu (Case-insensitive).
     */
    private static boolean check(String n1, String n2, String keyword) {
        String u1 = n1.toUpperCase();
        String u2 = n2.toUpperCase();
        String key = keyword.toUpperCase();
        return u1.contains(key) || u2.contains(key);
    }

    /**
     * Mengecek apakah salah satu item mengandung nama dan status tertentu (Case-insensitive).
     */
    private static boolean check(String n1, String n2, String name, String state) {
        String u1 = n1.toUpperCase();
        String u2 = n2.toUpperCase();
        String uName = name.toUpperCase();
        String uState = state.toUpperCase();
        boolean match1 = u1.contains(uName) && u1.contains(uState);
        boolean match2 = u2.contains(uName) && u2.contains(uState);
        return match1 || match2;
    }

    /**
     * Membuat objek Dish baru dengan komponen gabungan.
     */
    private static Dish createDish(String dishName, Item source1, Item source2) {
        List<Preparable> components = new ArrayList<>();
        // Rekursif menambahkan komponen dari Dish lama atau item baru
        addComponents(components, source1);
        addComponents(components, source2);

        // Generate ID Unik dari Nama
        String dishId = "D-" + dishName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (dishId.length() > 20) dishId = dishId.substring(0, 20); // Batasi panjang ID

        return new Dish(dishId, dishName, components);
    }

    /**
     * Helper rekursif untuk membongkar komponen dari Dish lama saat digabungkan.
     */
    private static void addComponents(List<Preparable> targetList, Item sourceItem) {
        if (sourceItem instanceof Dish) {
            // Jika Item lama adalah Dish, ambil semua komponennya
            targetList.addAll(((Dish) sourceItem).getComponents());
        } else if (sourceItem instanceof Preparable) {
            // Jika Item lama adalah Ingredient, tambahkan langsung
            targetList.add((Preparable) sourceItem);
        }
    }
}