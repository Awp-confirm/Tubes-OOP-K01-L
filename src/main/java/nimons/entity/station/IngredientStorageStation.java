package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import nimons.entity.item.ingredient.Cucumber; 
import nimons.entity.item.ingredient.Fish;
import nimons.entity.item.ingredient.Nori;
import nimons.entity.item.ingredient.Rice;
import nimons.entity.item.ingredient.Shrimp;
import nimons.entity.item.interfaces.Preparable;

public class IngredientStorageStation extends Station {
    
    private Item storedItem; 
    private Item placedItem; 

    
    public IngredientStorageStation(String name, Position position) {
        super(name, position);
        
        final int x = position.getX();
        final int y = position.getY();

        
        if (x == 0) { 
            
            if (y == 3) storedItem = new Rice(); 
            else if (y == 4) storedItem = new Nori(); 
            else if (y == 5) storedItem = new Cucumber();
        } 
        else if (x == 13) { 
            
            if (y == 3) storedItem = new Fish(); 
            else if (y == 4) storedItem = new Shrimp(); 
            else if (y == 5) storedItem = new Cucumber();
        } 
        
        
        String itemName = storedItem != null ? storedItem.getName() : "UNDEFINED";
        
        if (storedItem == null) {
            
            storedItem = new Nori();
            itemName = "Nori_FALLBACK"; 
        }
        
        
        this.name = storedItem.getName() + " Storage";

        
        System.out.println("DEBUG I: Membuat " + itemName + " di Posisi: (" + x + ", " + y + ")");
    }
    
    @Override
        
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();
        Item itemTable = this.placedItem;

        
        if (itemHand == null && itemTable != null) {
            chef.setInventory(itemTable);
            this.placedItem = null;
            log("ACTION", "PICKED UP: " + itemTable.getName() + " from table.");
            return;
        }

        
        if (itemHand instanceof Plate && itemTable == null) {
            this.placedItem = itemHand;
            chef.setInventory(null);
            log("ACTION", "PLACED: Plate on table.");
            return;
        }

        
        if (itemHand == null && itemTable instanceof Plate) {
            Plate p = (Plate) itemTable;
            Item bahanBaru = spawnItem();
            
            if (bahanBaru != null && bahanBaru instanceof Preparable) {
                if (processPlating(p, bahanBaru)) {
                    log("ACTION", "ADDED: " + bahanBaru.getName() + " to plate on table.");
                } else {
                    log("FAIL", "Cannot add " + bahanBaru.getName() + " to plate.");
                }
            }
            return;
        }
        
        
        if (itemHand instanceof Preparable && itemTable instanceof Plate) {
            Plate p = (Plate) itemTable;
            if (processPlating(p, itemHand)) {
                chef.setInventory(null);
                log("ACTION", "ASSEMBLY: " + itemHand.getName() + " added to plate on table.");
            } else {
                log("FAIL", "Cannot add " + itemHand.getName() + " to plate.");
            }
            return;
        }

        
        if (itemHand instanceof Plate && itemTable == null) {
            Plate p = (Plate) itemHand;
            
            if (p.getFood() == null) { 
                Item bahanBaru = spawnItem(); 
                
                if (bahanBaru != null) {
                    processPlating(p, bahanBaru); 
                    log("ACTION", "PLATED: " + bahanBaru.getName() + " to plate in hand.");
                } else {
                    log("FAIL", "Cannot instantiate item type: " + this.name + ".");
                }
            } else {
                log("FAIL", "Plate is occupied by Dish (" + p.getFood().getName() + ").");
            }
            return; 
        }

        
        if (itemHand == null && itemTable == null) {
            Item bahanBaru = spawnItem();
            if (bahanBaru != null) {
                chef.setInventory(bahanBaru);
                log("ACTION", "TAKEN: " + bahanBaru.getName() + " picked up.");
            } else {
                log("FAIL", "Cannot instantiate item type: " + this.name + ".");
            }
            return;
        }
        
        
        if (itemHand instanceof Preparable && itemTable == null) {
            this.placedItem = itemHand;
            chef.setInventory(null);
            log("ACTION", "PLACED: " + itemHand.getName() + " on table.");
            return;
        }
        
        
        log("INFO", "Cannot perform action. Hand: " + (itemHand != null ? itemHand.getName() : "empty") + ", Table: " + (itemTable != null ? itemTable.getName() : "empty"));
    }
    
    
        
    private Item spawnItem() {
        if (storedItem instanceof Rice) return new Rice();
        if (storedItem instanceof Nori) return new Nori();
        if (storedItem instanceof Cucumber) return new Cucumber();
        if (storedItem instanceof Shrimp) return new Shrimp();
        if (storedItem instanceof Fish) return new Fish();
        return null; 
    }

    public Item getStoredItem() { return storedItem; }
    
    public Item getPlacedItem() { return placedItem; }
}
