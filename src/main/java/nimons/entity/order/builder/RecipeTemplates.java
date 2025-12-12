package nimons.entity.order.builder;

import nimons.entity.item.IngredientState;
import nimons.entity.item.ingredient.Cucumber;
import nimons.entity.item.ingredient.Fish;
import nimons.entity.item.ingredient.Nori;
import nimons.entity.item.ingredient.Rice;
import nimons.entity.item.ingredient.Shrimp;
import nimons.entity.order.Recipe;

/**
 * BUILDER PATTERN - Predefined Recipe Templates
 * 
 * Class ini menyediakan pre-configured RecipeBuilder untuk common recipes.
 * Ini adalah contoh penggunaan Builder Pattern dengan template approach.
 * 
 * Manfaat:
 * 1. Consistency: Recipe definitions terpusat di satu tempat
 * 2. Reusability: Template dapat digunakan berulang kali
 * 3. Easy Maintenance: Mudah update recipe definitions
 * 4. Type Safety: Compile-time checking untuk recipe creation
 */
public class RecipeTemplates {
    
    /**
     * Template untuk Kappa Maki (Cucumber Roll)
     */
    public static Recipe createKappaMaki() {
        return new RecipeBuilder()
            .setName("Kappa Maki")
            .addIngredient(Cucumber.class, IngredientState.CHOPPED)
            .addIngredient(Rice.class, IngredientState.COOKED)
            .addIngredient(Nori.class, IngredientState.RAW)
            .setDifficulty(1)
            .setScoreValue(100)
            .setPreparationTime(60)
            .build();
    }
    
    /**
     * Template untuk Sakana Maki (Fish Roll)
     */
    public static Recipe createSakanaMaki() {
        return new RecipeBuilder()
            .setName("Sakana Maki")
            .addIngredient(Fish.class, IngredientState.CHOPPED)
            .addIngredient(Rice.class, IngredientState.COOKED)
            .addIngredient(Nori.class, IngredientState.RAW)
            .setDifficulty(2)
            .setScoreValue(150)
            .setPreparationTime(75)
            .build();
    }
    
    /**
     * Template untuk Ebi Maki (Shrimp Roll)
     */
    public static Recipe createEbiMaki() {
        return new RecipeBuilder()
            .setName("Ebi Maki")
            .addIngredient(Shrimp.class, IngredientState.COOKED)
            .addIngredient(Rice.class, IngredientState.COOKED)
            .addIngredient(Nori.class, IngredientState.RAW)
            .setDifficulty(3)
            .setScoreValue(200)
            .setPreparationTime(90)
            .build();
    }
    
    /**
     * Template untuk Fish Cucumber Roll (Advanced)
     */
    public static Recipe createFishCucumberRoll() {
        return new RecipeBuilder()
            .setName("Fish Cucumber Roll")
            .addIngredient(Fish.class, IngredientState.CHOPPED)
            .addIngredient(Cucumber.class, IngredientState.CHOPPED)
            .addIngredient(Rice.class, IngredientState.COOKED)
            .addIngredient(Nori.class, IngredientState.RAW)
            .setDifficulty(4)
            .setScoreValue(250)
            .setPreparationTime(100)
            .build();
    }
    
    /**
     * Custom builder untuk membuat recipe baru
     * Mengembalikan fresh RecipeBuilder untuk customization
     */
    public static RecipeBuilder custom() {
        return new RecipeBuilder();
    }
    
    /**
     * Create recipe berdasarkan nama template
     * Factory-like method untuk dynamic recipe creation
     * 
     * @param templateName Nama template recipe
     * @return Recipe yang sesuai
     * @throws IllegalArgumentException jika template tidak ditemukan
     */
    public static Recipe createFromTemplate(String templateName) {
        switch (templateName.toLowerCase()) {
            case "kappa_maki":
            case "kappamaki":
                return createKappaMaki();
                
            case "sakana_maki":
            case "sakanamaki":
                return createSakanaMaki();
                
            case "ebi_maki":
            case "ebimaki":
                return createEbiMaki();
                
            case "fish_cucumber_roll":
            case "fishcucumberroll":
                return createFishCucumberRoll();
                
            default:
                throw new IllegalArgumentException("Unknown recipe template: " + templateName);
        }
    }
    
    /**
     * Get all available template names
     */
    public static String[] getAvailableTemplates() {
        return new String[] {
            "kappa_maki",
            "sakana_maki", 
            "ebi_maki",
            "fish_cucumber_roll"
        };
    }
}
