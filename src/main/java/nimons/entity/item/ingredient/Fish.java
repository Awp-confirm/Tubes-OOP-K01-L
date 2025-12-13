package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Fish extends Ingredient {

    public Fish() {
        super("fish", "Fish", IngredientState.RAW);
    }

    @Override
    public boolean canBeChopped() {
        return getState() == IngredientState.RAW;
    }

    @Override
    public boolean canBeCooked() {
        return false;
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return getState() == IngredientState.CHOPPED;
    }

    @Override
    public void chop() {
        // Proses pemotongan: ubah state dari RAW ke CHOPPED
        if (getState() == IngredientState.RAW) {
            setState(IngredientState.CHOPPED);
        }
    }

    @Override
    public void cook() {
        // Proses memasak: ubah state ke COOKING untuk memulai timer
        if (getState() == IngredientState.RAW) {
            setState(IngredientState.COOKING);
        }
    }
}