package nimons.entity.station;

import java.util.Stack;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;

public class Rack extends Station {
    
    private final Stack<Plate> cleanPlates;

    public Rack(String name, Position position) {
        super(name, position);
        this.cleanPlates = new Stack<>();
        log("INIT", "Rack initialized");
    }

    @Override
        
    public void onInteract(Chef chef) {
        Item heldItem = chef.getInventory();
        
        if (heldItem == null) {
            
            if (!cleanPlates.isEmpty()) {
                Plate p = cleanPlates.pop();
                chef.setInventory(p);
                log("TAKE", "Chef took clean plate. Remaining: " + cleanPlates.size());
            }
        }
        
    }

    
        
    public void addCleanPlate(Plate p) {
        if (p != null && p.isClean()) {
            cleanPlates.push(p);
            log("STORE", "Clean plate added. Total: " + cleanPlates.size());
        } else {
            System.err.println("ERROR: Attempted to store non-clean plate in Rack.");
        }
    }

    
    public int getCleanPlateCount() {
        return cleanPlates.size();
    }
}
