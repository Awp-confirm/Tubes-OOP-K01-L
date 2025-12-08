package nimons.entity.item.ingredient;

import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;

public class Fish extends Ingredient {
    public Fish() {
        super("I-Fish", "Ikan", IngredientState.RAW);
    }

    @Override public boolean canBeChopped() { return true; }
    @Override public boolean canBeCooked() { return false; } // Di resep Ikan (Raw)
    @Override public boolean canBePlacedOnPlate() { return true; }

    @Override public void chop() {
        if (getState() == IngredientState.RAW) {
            // Secara teknis masih RAW, tapi bentuk fisik berubah
            // Kita anggap CHOPPED state mewakili "Sliced Sashimi"
            this.setState(IngredientState.CHOPPED); 
            this.setName("Ikan (Raw/Chopped)");
        }
    }

    @Override public void cook() {}
}