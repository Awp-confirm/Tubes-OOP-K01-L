package nimons.exceptions;

import java.util.List;

public class RecipeNotFoundException extends GameException {
    
    private List<String> ingredientNames;
    
    public RecipeNotFoundException(List<String> ingredientNames) {
        super("No recipe found for ingredients: " + String.join(", ", ingredientNames));
        this.ingredientNames = ingredientNames;
    }
    
    public List<String> getIngredientNames() {
        return ingredientNames;
    }
}
