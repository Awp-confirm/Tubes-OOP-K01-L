package nimons.logic.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors; // Tambahkan import Collectors

import nimons.entity.item.Dish;
import nimons.entity.item.Ingredient;
import nimons.entity.item.interfaces.Preparable;
import nimons.entity.order.IngredientRequirement;
import nimons.entity.order.Recipe;
import nimons.entity.order.recipes.EbiMaki;
import nimons.entity.order.recipes.FishCucumberRoll;
import nimons.entity.order.recipes.KappaMaki;
import nimons.entity.order.recipes.SakanaMaki;

public class RecipeManager {

    private static final List<Recipe> ALL_RECIPES = Arrays.asList(
        new KappaMaki(),
        new SakanaMaki(),
        new EbiMaki(),
        new FishCucumberRoll()
    );


    /**
     * Mencari Dish final dari list komponen yang sudah terkumpul di Plate.
     * Dipanggil oleh AssemblyStation setelah setiap penambahan Ingredient (untuk penamaan Dish).
     * * @param components List of Preparable (Ingredient) yang ada di Plate.
     * @return Dish yang cocok jika resep lengkap terpenuhi, atau null jika tidak.
     */
    public static Dish findMatch(List<Preparable> components) {
        if (components == null || components.isEmpty()) {
            return null;
        }

        // Konversi Preparable (components) menjadi List<Ingredient> untuk validasi
        List<Ingredient> ingredients = components.stream()
            .filter(p -> p instanceof Ingredient)
            .map(p -> (Ingredient) p)
            .collect(Collectors.toList());

        // Cek setiap resep
        for (Recipe recipe : ALL_RECIPES) {
            if (matchesRecipe(ingredients, recipe)) {
                // Buat Dish baru DENGAN NAMA FINAL resep
                List<Preparable> finalComponents = new ArrayList<>(components);
                return new Dish(
                    recipe.getName().toLowerCase().replace(" ", "_"),
                    recipe.getName(),
                    finalComponents
                );
            }
        }
        
        // --- PENTING: JANGAN KEMBALIKAN "Mixed Dish" DI SINI ---
        // Jika tidak ada resep yang cocok, biarkan AssemblyStation mempertahankan nama Dish yang lama.
        return null;
    }
    

    /**
     * Mengecek apakah ingredients yang diberikan cocok dengan resep. (Logika ini tetap benar)
     */
    private static boolean matchesRecipe(List<Ingredient> ingredients, Recipe recipe) {
        List<IngredientRequirement> requirements = recipe.getRequirements();
        
        // 1. Jumlah ingredient harus sama dengan requirement
        if (ingredients.size() != requirements.size()) {
            return false;
        }
        
        // 2. Clone list untuk tracking requirement yang sudah dipenuhi
        List<IngredientRequirement> remainingRequirements = new ArrayList<>(requirements);
        
        // Cek setiap ingredient cocok dengan salah satu requirement
        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            
            for (int i = 0; i < remainingRequirements.size(); i++) {
                IngredientRequirement req = remainingRequirements.get(i);
                
                if (req.matches(ingredient)) {
                    remainingRequirements.remove(i);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                return false; // Ingredient tidak cocok dengan requirement manapun
            }
        }
        
        // 3. Semua requirement harus terpenuhi
        return remainingRequirements.isEmpty();
    }

    /**
     * Mendapatkan semua resep yang tersedia.
     */
    public static List<Recipe> getAllRecipes() {
        return new ArrayList<>(ALL_RECIPES);
    }

    /**
     * Mencari resep berdasarkan nama.
     */
    public static Recipe getRecipeByName(String name) {
        for (Recipe recipe : ALL_RECIPES) {
            if (recipe.getName().equalsIgnoreCase(name)) {
                return recipe;
            }
        }
        return null;
    }
}