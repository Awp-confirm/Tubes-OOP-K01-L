package nimons.entity.item;

public class Plate extends KitchenUtensil {

    private boolean clean;
    private Dish dish;

    public Plate() {
        this("plate", true, null);
    }

    public Plate(String id, boolean clean, Dish dish) {
        super(id, "Plate", true, null);
        this.clean = clean;
        this.dish = dish;
    }

    
        
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
    
    
    public Dish getFood() {
        return dish;
    }
    
    public void setFood(Dish dish) {
        this.dish = dish;
    }

    
        
    public boolean canPlaceDish() {
        return clean && dish == null;
    }

        
    public void placeDish(Dish newDish) {
        if (canPlaceDish()) {
            this.dish = newDish;
        }
    }

        
    public void washPlate() {
        if (dish == null) {
            this.clean = true;
        }
    }

        
    public Dish removeDish() {
        Dish removedDish = this.dish;
        this.dish = null;
        this.clean = false;
        return removedDish;
    }
}
