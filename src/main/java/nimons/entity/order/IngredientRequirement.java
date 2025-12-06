package nimons.entity.order;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class IngredientRequirement {

    private Class<? extends Ingredient> ingredientType;
    private IngredientState requiredState;

    public IngredientRequirement() {}

    public IngredientRequirement(Class<? extends Ingredient> ingredientType, IngredientState requiredState) {
        this.ingredientType = ingredientType;
        this.requiredState = requiredState;
    }

    // getters & setters
    public Class<? extends Ingredient> getIngredientType() { 
        return ingredientType; 
    }

    public void setIngredientType(Class<? extends Ingredient> ingredientType) { 
        this.ingredientType = ingredientType; 
    }

    public IngredientState getRequiredState() {
        return requiredState; 
    }

    public void setRequiredState(IngredientState requiredState) { 
        this.requiredState = requiredState; 
    }
}
