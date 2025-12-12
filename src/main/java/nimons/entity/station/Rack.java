package nimons.entity.station;

import java.util.Stack;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;

/**
 * Rack Station (K): Storage untuk clean plates dari WashingStation.
 * Hanya menerima clean plates dan menyimpannya dalam stack.
 */
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
            // Chef mengambil clean plate dari rack
            if (!cleanPlates.isEmpty()) {
                Plate p = cleanPlates.pop();
                chef.setInventory(p);
                log("TAKE", "Chef took clean plate. Remaining: " + cleanPlates.size());
            }
        }
        // Tidak bisa menaruh item di rack secara manual
    }

    /**
     * Method internal untuk menerima clean plate dari WashingStation.
     */
    public void addCleanPlate(Plate p) {
        if (p != null && p.isClean()) {
            cleanPlates.push(p);
            log("STORE", "Clean plate added. Total: " + cleanPlates.size());
        } else {
            System.err.println("ERROR: Attempted to store non-clean plate in Rack.");
        }
    }

    /**
     * Get clean plate count for visualization.
     */
    public int getCleanPlateCount() {
        return cleanPlates.size();
    }
}
