package nimons.entity.item.interfaces;

import nimons.entity.item.IngredientState;

public interface Preparable {

    boolean canBeChopped();

    boolean canBeCooked();

    boolean canBePlacedOnPlate();

    void chop();

    void cook();

    IngredientState getState();
}
