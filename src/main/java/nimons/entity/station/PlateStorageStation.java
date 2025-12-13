package nimons.entity.station;

import java.util.Stack;

import nimons.core.GameConfig;
import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;

public class PlateStorageStation extends Station {

    
    private static PlateStorageStation instance;
    
    private Stack<Plate> plates; 

    public PlateStorageStation(String name, Position position) {
        super(name, position);
        
        
        if (instance == null) {
            instance = this;
        } else {
            System.err.println("WARNING: Multiple PlateStorageStation created! Using the first instance.");
        }
        
        this.plates = new Stack<>();
        
        
        for (int i = 0; i < GameConfig.INITIAL_PLATE_STOCK; i++) {
            plates.push(new Plate()); 
        }
        log("INFO", "INITIAL STOCK: " + GameConfig.INITIAL_PLATE_STOCK + " clean plates available.");
    }

    public static PlateStorageStation getInstance() {
        return instance;
    }
    
    
        
    public static void resetInstance() {
        if (instance != null) {
            System.out.println("[PlateStorageStation] Resetting singleton instance");
            instance = null;
        }
    }

    
    @Override
        
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        
        if (itemHand == null) {
            if (plates.isEmpty()) {
                log("FAIL", "STORAGE EMPTY: No plates available!");
                return;
            }

            
            Plate topPlate = plates.peek();
            log("DEBUG", "Top plate is: " + (topPlate.isClean() ? "CLEAN" : "DIRTY"));

            
            
            if (topPlate.isClean()) {
                
                chef.setInventory(plates.pop());
                
                
                long cleanCount = plates.stream().filter(Plate::isClean).count();
                log("ACTION", "TAKEN: Clean Plate from top. Remaining Clean: " + cleanCount + ". Total: " + plates.size());
                return;
            } else {
                
                chef.setInventory(plates.pop()); 
                
                
                long totalCount = plates.size();
                long dirtyCount = plates.stream().filter(p -> !p.isClean()).count();
                long cleanCount = totalCount - dirtyCount;
                
                log("ACTION", "TAKEN: Dirty Plate from top (blocking clean plates). Remaining Dirty: " + dirtyCount + ". Remaining Clean: " + cleanCount + ". Total: " + totalCount);
                return;
            }
        }

        
        log("FAIL", "DROP REJECTED: Items cannot be dropped directly onto Plate Storage.");
    }
    
    
        
    public void addPlateToStack(Plate p) {
        if (p != null) {
            
            if (plates.size() >= GameConfig.INITIAL_PLATE_STOCK) {
                log("WARNING", "REJECTED: Cannot add plate. Storage at maximum capacity (" + GameConfig.INITIAL_PLATE_STOCK + " plates).");
                return;
            }
            
            String status = p.isClean() ? "Clean" : "Dirty";
            
            plates.push(p);
            
            
            long dirtyCount = plates.stream().filter(plate -> !plate.isClean()).count();
            long cleanCount = plates.stream().filter(Plate::isClean).count();
            
            
            log("INFO", "RETURNED: " + status + " plate added to TOP of stack. Total: " + plates.size() + " (Dirty: " + dirtyCount + ", Clean: " + cleanCount + ")");
            
            
            if (plates.size() > 0) {
                StringBuilder topPlates = new StringBuilder("Top 3 plates: ");
                int count = Math.min(3, plates.size());
                for (int i = plates.size() - 1; i >= plates.size() - count; i--) {
                    Plate plate = plates.get(i);
                    topPlates.append(plate.isClean() ? "C" : "D").append(" ");
                }
                log("DEBUG", topPlates.toString());
            }
        }
    }
    
    
    public int getPlateCount() {
        return plates.size();
    }
}