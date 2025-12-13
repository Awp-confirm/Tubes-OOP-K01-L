package nimons.entity.order.builder;

import java.util.ArrayList;
import java.util.List;

import nimons.entity.item.IngredientState;
import nimons.entity.order.IngredientRequirement;
import nimons.entity.order.Recipe;

public class RecipeBuilder {
    
    
    private String name;
    
    
    private List<IngredientRequirement> requirements;
    private int preparationTime = 60; 
    private int difficulty = 1; 
    private int scoreValue = 100; 
    
    
    public RecipeBuilder() {
        this.requirements = new ArrayList<>();
    }
    
    
    public RecipeBuilder setName(String name) {
        this.name = name;
        return this;
    }
    
    
        
    public RecipeBuilder addRequirement(IngredientRequirement requirement) {
        if (requirement != null) {
            this.requirements.add(requirement);
        }
        return this;
    }
    
    
        
    public RecipeBuilder addIngredient(Class<? extends nimons.entity.item.Ingredient> ingredientType, IngredientState requiredState) {
        IngredientRequirement requirement = new IngredientRequirement(
            ingredientType, 
            requiredState
        );
        this.requirements.add(requirement);
        return this;
    }
    
    
    @SafeVarargs
        
    public final RecipeBuilder addIngredients(IngredientState requiredState, Class<? extends nimons.entity.item.Ingredient>... ingredientTypes) {
        for (Class<? extends nimons.entity.item.Ingredient> type : ingredientTypes) {
            addIngredient(type, requiredState);
        }
        return this;
    }
    
    
    public RecipeBuilder setPreparationTime(int seconds) {
        this.preparationTime = seconds;
        return this;
    }
    
    
    public RecipeBuilder setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(5, difficulty)); 
        return this;
    }
    
    
    public RecipeBuilder setScoreValue(int score) {
        this.scoreValue = score;
        return this;
    }
    
    
        
    public Recipe build() {
        
        validate();
        
        
        Recipe recipe = new Recipe(name, requirements);
        
        
        
        
        
        System.out.println("[RecipeBuilder] Built recipe: " + name + 
                         " with " + requirements.size() + " ingredients");
        
        return recipe;
    }
    
    
        
    private void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Recipe name is required");
        }
        
        if (requirements.isEmpty()) {
            throw new IllegalStateException("Recipe must have at least one ingredient requirement");
        }
    }
    
    
        
    public RecipeBuilder reset() {
        this.name = null;
        this.requirements = new ArrayList<>();
        this.preparationTime = 60;
        this.difficulty = 1;
        this.scoreValue = 100;
        return this;
    }
    
    
        
    public RecipeBuilder fromRecipe(Recipe recipe) {
        this.name = recipe.getName();
        this.requirements = new ArrayList<>(recipe.getRequirements());
        return this;
    }
    
    
    @Override
        
    public String toString() {
        return String.format("RecipeBuilder[name=%s, ingredients=%d, difficulty=%d, score=%d]",
            name, requirements.size(), difficulty, scoreValue);
    }
}
