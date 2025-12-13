package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Shrimp extends Ingredient {

    public Shrimp() {
        super("shrimp", "Shrimp", IngredientState.RAW);
    }

    @Override
        
    public boolean canBeChopped() {
        return getState() == IngredientState.RAW; 
    }

    @Override
        
    public boolean canBeCooked() {
        return getState() == IngredientState.CHOPPED; 
    }

    @Override
        
    public boolean canBePlacedOnPlate() {
        return getState() == IngredientState.COOKED; 
    }

    @Override
        
    public void chop() {
        if (getState() == IngredientState.RAW) {
            setState(IngredientState.CHOPPED);
        }
    }

    @Override
        
    public void cook() {
        if (getState() == IngredientState.CHOPPED) {
            
            setState(IngredientState.COOKING);
            
            this.currentCookTime = 0; 
        } 
        
    }
}
