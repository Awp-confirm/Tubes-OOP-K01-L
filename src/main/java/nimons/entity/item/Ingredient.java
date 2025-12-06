package nimons.entity.item;

import nimons.entity.item.interfaces.Preparable;

public abstract class Ingredient extends Item implements Preparable {

    private IngredientState state;

    public Ingredient() {}

    public Ingredient(String id, String name, IngredientState state) {
        super(id, name, true);
        this.state = state;
    }

    // getters & setters
    public IngredientState getState() { 
        return state; 
    }

    public void setState(IngredientState state) { 
        this.state = state; 
    }

    // abstract methods
    @Override
    public abstract boolean canBeChopped();

    @Override
    public abstract boolean canBeCooked();

    @Override
    public abstract boolean canBePlacedOnPlate();

    @Override
    public abstract void chop();

    @Override
    public abstract void cook();
}
