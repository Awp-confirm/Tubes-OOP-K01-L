package nimons.entity.item;

public class Plate extends KitchenUtensil {

    private boolean clean;
    private Dish dish;

    public Plate() {}

    public Plate(String id, boolean clean, Dish dish) {
        super(id, "Plate", true, null);
        this.clean = clean;
        this.dish = dish;
    }

    // getters & setters
    public boolean isClean() { 
        return clean; 
    }

    public void setClean(boolean clean) { 
        this.clean = clean; 
    }

    public Dish getDish() { 
        return dish; 
    }

    public void setDish(Dish dish) { 
        this.dish = dish; 
    }
}
