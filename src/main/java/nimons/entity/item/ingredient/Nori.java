package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Nori extends Ingredient {
    public Nori() {
        super("I-Nori", "Nori", IngredientState.RAW);
    }

    @Override public boolean canBeChopped() { return false; }
    @Override public boolean canBeCooked() { return false; }
    @Override public boolean canBePlacedOnPlate() { return true; }

    @Override public void chop() {}
    @Override public void cook() {}
}