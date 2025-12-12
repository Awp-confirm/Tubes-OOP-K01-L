package nimons.entity.order.builder;

import java.util.ArrayList;
import java.util.List;

import nimons.entity.item.IngredientState;
import nimons.entity.order.IngredientRequirement;
import nimons.entity.order.Recipe;

/**
 * BUILDER PATTERN - Recipe Builder
 * 
 * Builder Pattern memisahkan konstruksi objek kompleks dari representasinya,
 * sehingga proses konstruksi yang sama dapat membuat representasi yang berbeda.
 * 
 * Manfaat:
 * 1. Fluent Interface: Chaining methods untuk readability
 * 2. Step-by-step Construction: Membangun objek kompleks secara bertahap
 * 3. Immutability: Dapat membuat immutable objects dengan banyak parameters
 * 4. Named Parameters: Menghindari constructor dengan banyak parameter
 * 5. Validation: Validasi dapat dilakukan saat build()
 * 6. Flexibility: Mudah menambah optional parameters
 * 
 * Contoh Penggunaan:
 * <pre>
 * Recipe sushi = new RecipeBuilder()
 *     .setName("Salmon Sushi")
 *     .addIngredient("fish", IngredientState.CHOPPED)
 *     .addIngredient("rice", IngredientState.COOKED)
 *     .addIngredient("nori", IngredientState.RAW)
 *     .build();
 * </pre>
 */
public class RecipeBuilder {
    
    // Required parameters
    private String name;
    
    // Optional parameters dengan default values
    private List<IngredientRequirement> requirements;
    private int preparationTime = 60; // Default 60 seconds
    private int difficulty = 1; // Default difficulty 1 (easy)
    private int scoreValue = 100; // Default score value
    
    /**
     * Constructor
     */
    public RecipeBuilder() {
        this.requirements = new ArrayList<>();
    }
    
    /**
     * Set nama recipe (required)
     * 
     * @param name Nama recipe
     * @return Builder instance untuk method chaining
     */
    public RecipeBuilder setName(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * Add ingredient requirement menggunakan IngredientRequirement object
     * 
     * @param requirement Requirement yang akan ditambahkan
     * @return Builder instance untuk method chaining
     */
    public RecipeBuilder addRequirement(IngredientRequirement requirement) {
        if (requirement != null) {
            this.requirements.add(requirement);
        }
        return this;
    }
    
    /**
     * Add ingredient requirement menggunakan parameter individual
     * Convenience method untuk readability
     * 
     * @param ingredientType Tipe ingredient class (e.g., Fish.class, Rice.class)
     * @param requiredState State yang dibutuhkan
     * @return Builder instance untuk method chaining
     */
    public RecipeBuilder addIngredient(Class<? extends nimons.entity.item.Ingredient> ingredientType, IngredientState requiredState) {
        IngredientRequirement requirement = new IngredientRequirement(
            ingredientType, 
            requiredState
        );
        this.requirements.add(requirement);
        return this;
    }
    
    /**
     * Add multiple ingredients dengan state yang sama
     * Useful untuk recipe dengan banyak ingredient dengan state sama
     * 
     * @param requiredState State yang dibutuhkan
     * @param ingredientTypes Variadic parameter untuk tipe ingredient classes
     * @return Builder instance untuk method chaining
     */
    @SafeVarargs
    public final RecipeBuilder addIngredients(IngredientState requiredState, Class<? extends nimons.entity.item.Ingredient>... ingredientTypes) {
        for (Class<? extends nimons.entity.item.Ingredient> type : ingredientTypes) {
            addIngredient(type, requiredState);
        }
        return this;
    }
    
    /**
     * Set waktu persiapan recipe (optional)
     * 
     * @param seconds Waktu dalam detik
     * @return Builder instance untuk method chaining
     */
    public RecipeBuilder setPreparationTime(int seconds) {
        this.preparationTime = seconds;
        return this;
    }
    
    /**
     * Set difficulty level (optional)
     * 
     * @param difficulty Level kesulitan (1-5)
     * @return Builder instance untuk method chaining
     */
    public RecipeBuilder setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(5, difficulty)); // Clamp 1-5
        return this;
    }
    
    /**
     * Set score value untuk recipe ini (optional)
     * 
     * @param score Nilai score
     * @return Builder instance untuk method chaining
     */
    public RecipeBuilder setScoreValue(int score) {
        this.scoreValue = score;
        return this;
    }
    
    /**
     * Build Recipe object dengan validasi
     * 
     * @return Recipe yang telah dikonstruksi
     * @throws IllegalStateException jika required fields tidak diset
     */
    public Recipe build() {
        // Validation
        validate();
        
        // Create Recipe object
        Recipe recipe = new Recipe(name, requirements);
        
        // Note: Jika Recipe class memiliki fields tambahan (preparationTime, difficulty, scoreValue),
        // mereka bisa diset di sini. Untuk saat ini, kita hanya gunakan name dan requirements
        // yang sudah ada di Recipe class.
        
        System.out.println("[RecipeBuilder] Built recipe: " + name + 
                         " with " + requirements.size() + " ingredients");
        
        return recipe;
    }
    
    /**
     * Validate required fields
     */
    private void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Recipe name is required");
        }
        
        if (requirements.isEmpty()) {
            throw new IllegalStateException("Recipe must have at least one ingredient requirement");
        }
    }
    
    /**
     * Reset builder ke initial state
     * Useful untuk reuse builder
     * 
     * @return Builder instance
     */
    public RecipeBuilder reset() {
        this.name = null;
        this.requirements = new ArrayList<>();
        this.preparationTime = 60;
        this.difficulty = 1;
        this.scoreValue = 100;
        return this;
    }
    
    /**
     * Create a copy of existing recipe untuk modification
     * 
     * @param recipe Recipe yang akan di-copy
     * @return Builder instance dengan values dari recipe
     */
    public RecipeBuilder fromRecipe(Recipe recipe) {
        this.name = recipe.getName();
        this.requirements = new ArrayList<>(recipe.getRequirements());
        return this;
    }
    
    /**
     * Get current state sebagai String untuk debugging
     */
    @Override
    public String toString() {
        return String.format("RecipeBuilder[name=%s, ingredients=%d, difficulty=%d, score=%d]",
            name, requirements.size(), difficulty, scoreValue);
    }
}
