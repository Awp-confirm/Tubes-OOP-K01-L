package nimons.logic.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors; 

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

    
        
    public static Dish findMatch(List<Preparable> components) {
        if (components == null || components.isEmpty()) {
            return null;
        }

        
        List<Ingredient> ingredients = components.stream()
            .filter(p -> p instanceof Ingredient)
            .map(p -> (Ingredient) p)
            .collect(Collectors.toList());

        
        for (Recipe recipe : ALL_RECIPES) {
            if (matchesRecipe(ingredients, recipe)) {
                
                List<Preparable> finalComponents = new ArrayList<>(components);
                return new Dish(
                    recipe.getName().toLowerCase().replace(" ", "_"),
                    recipe.getName(),
                    finalComponents
                );
            }
        }
        
        
        
        return null;
    }
    

    
        
    private static boolean matchesRecipe(List<Ingredient> ingredients, Recipe recipe) {
        List<IngredientRequirement> requirements = recipe.getRequirements();
        
        
        if (ingredients.size() != requirements.size()) {
            return false;
        }
        
        
        List<IngredientRequirement> remainingRequirements = new ArrayList<>(requirements);
        
        
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
                return false; 
            }
        }
        
        
        return remainingRequirements.isEmpty();
    }

    
    public static List<Recipe> getAllRecipes() {
        return new ArrayList<>(ALL_RECIPES);
    }

    
    public static Recipe getRecipeByName(String name) {
        for (Recipe recipe : ALL_RECIPES) {
            if (recipe.getName().equalsIgnoreCase(name)) {
                return recipe;
            }
        }
        return null;
    }
}