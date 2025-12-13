package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Rice extends Ingredient {

    public Rice() {
        super("rice", "Rice", IngredientState.RAW);
    }

    @Override
        
    public boolean canBeChopped() {
        return false; 
    }

    @Override
        
    public boolean canBeCooked() {
        return getState() == IngredientState.RAW;
    }

    @Override
        
    public boolean canBePlacedOnPlate() {
        return getState() == IngredientState.COOKED;
    }

    @Override
        
    public void chop() {
        
    }

    @Override
            
    public void cook() {
            if (getState() == IngredientState.RAW) {
                
                setState(IngredientState.COOKING);
                
            } 
        }
}