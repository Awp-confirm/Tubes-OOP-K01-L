package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Dish;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.Plate;
import nimons.entity.item.interfaces.CookingDevice;

public class TrashStation extends Station {

    public TrashStation(String name, Position position) {
        super(name, position);
    }

    
    @Override
        
        
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        
        if (itemHand != null) {
            
            
            if (itemHand instanceof Plate) {
                Plate plate = (Plate) itemHand;
                
                
                if (plate.getDish() != null) {
                    Dish dish = plate.getDish();
                    
                    if (dish.getComponents() != null && !dish.getComponents().isEmpty()) {
                        dish.getComponents().clear();
                        log("SUCCESS", "CLEARED: All ingredients removed from plate.");
                    }
                    
                    plate.setDish(null);
                    plate.setClean(true);
                    log("SUCCESS", "DISCARDED: Dish removed from plate.");
                } else {
                    log("INFO", "Plate is already empty.");
                }
                return;
            }
            
            
            if (itemHand instanceof KitchenUtensil) {
                KitchenUtensil utensil = (KitchenUtensil) itemHand;
                
                
                if (utensil.getContents() != null && !utensil.getContents().isEmpty()) { 
                    utensil.getContents().clear(); 
                    
                    
                    if (utensil instanceof CookingDevice) {
                        ((CookingDevice) utensil).reset();
                    }
                    
                    log("SUCCESS", "CLEANED: Contents of " + utensil.getName() + " discarded and reset.");
                } else {
                    
                    log("INFO", utensil.getName() + " is already clean.");
                }
                return; 
            }

            
            String itemName = itemHand.getName();
            chef.setInventory(null);
            
            log("SUCCESS", "DISCARDED: Item " + itemName + " thrown away.");
        } else {
            
            log("INFO", "HAND EMPTY: No item to discard.");
        }
    }
}