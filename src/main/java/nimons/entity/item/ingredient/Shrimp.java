package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Shrimp extends Ingredient {
    public Shrimp() {
        super("I-Shrimp", "Udang", IngredientState.RAW);
    }

    @Override public boolean canBeChopped() { return true; } // Harus dikupas/potong dulu
    @Override public boolean canBeCooked() { return true; }  // Baru bisa dimasak

    @Override public boolean canBePlacedOnPlate() { 
        return getState() == IngredientState.COOKED; 
    }

    @Override public void chop() {
        if (getState() == IngredientState.RAW) {
            this.setState(IngredientState.CHOPPED);
            this.setName("Udang (Chopped)");
        }
    }

    @Override public void cook() {
        // Hanya bisa dimasak kalau SUDAH DIPOTONG
        if (getState() == IngredientState.CHOPPED) {
            this.setState(IngredientState.COOKED);
            this.setName("Udang (Cooked)");
        }
    }
}