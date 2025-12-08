package nimons.logic.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nimons.entity.item.Dish;
import nimons.entity.item.Ingredient;
import nimons.entity.item.Item;
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
     * Mencari resep yang cocok dengan kombinasi item yang diberikan.
     * @param item1 Item pertama
     * @param item2 Item kedua
     * @return Dish jika resep cocok, null jika tidak
     */
    public static Dish findMatch(Item item1, Item item2) {
        // Kumpulkan semua ingredient dari kedua item
        List<Ingredient> ingredients = new ArrayList<>();
        
        if (item1 instanceof Ingredient) {
            ingredients.add((Ingredient) item1);
        } else if (item1 instanceof Dish) {
            Dish dish = (Dish) item1;
            for (Preparable p : dish.getComponents()) {
                if (p instanceof Ingredient) {
                    ingredients.add((Ingredient) p);
                }
            }
        }
        
        if (item2 instanceof Ingredient) {
            ingredients.add((Ingredient) item2);
        } else if (item2 instanceof Dish) {
            Dish dish = (Dish) item2;
            for (Preparable p : dish.getComponents()) {
                if (p instanceof Ingredient) {
                    ingredients.add((Ingredient) p);
                }
            }
        }
        
        // Cek setiap resep
        for (Recipe recipe : ALL_RECIPES) {
            if (matchesRecipe(ingredients, recipe)) {
                // Buat dish baru dengan ingredients yang cocok
                List<Preparable> components = new ArrayList<>(ingredients);
                return new Dish(
                    recipe.getName().toLowerCase().replace(" ", "_"),
                    recipe.getName(),
                    components
                );
            }
        }
        
        // Jika tidak ada resep yang cocok, return Dish dengan kombinasi ingredients
        if (!ingredients.isEmpty()) {
            List<Preparable> components = new ArrayList<>(ingredients);
            return new Dish("mixed_dish", "Mixed Dish", components);
        }
        
        return null;
    }

    /**
     * Mengecek apakah dua item bisa dikombinasikan.
     */
    public static boolean isValidCombination(Item item1, Item item2) {
        if (item1 == null || item2 == null) return false;
        
        // Setidaknya salah satu harus ingredient atau dish
        boolean hasIngredient = (item1 instanceof Ingredient || item1 instanceof Dish) &&
                                (item2 instanceof Ingredient || item2 instanceof Dish);
        
        return hasIngredient;
    }

    /**
     * Mengecek apakah ingredients yang diberikan cocok dengan resep.
     */
    private static boolean matchesRecipe(List<Ingredient> ingredients, Recipe recipe) {
        List<IngredientRequirement> requirements = recipe.getRequirements();
        
        // Jumlah ingredient harus sama dengan requirement
        if (ingredients.size() != requirements.size()) {
            return false;
        }
        
        // Clone list untuk tracking requirement yang sudah dipenuhi
        List<IngredientRequirement> remainingRequirements = new ArrayList<>(requirements);
        List<Ingredient> remainingIngredients = new ArrayList<>(ingredients);
        
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
        
        // Semua requirement harus terpenuhi
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
