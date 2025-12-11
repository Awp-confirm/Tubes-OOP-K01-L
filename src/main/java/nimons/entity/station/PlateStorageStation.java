package nimons.entity.station;

import java.util.Stack;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;

/**
 * PlateStorageStation (P): Menangani tumpukan Plate bersih dan kotor (Stack LIFO).
 * Menggunakan pola Singleton agar dapat diakses oleh ServingStation tanpa Dependency Injection manual.
 */
public class PlateStorageStation extends Station {

    // --- Singleton Fields ---
    private static PlateStorageStation instance;
    
    private Stack<Plate> plates; 
    private final int INITIAL_STOCK = 5;

    public PlateStorageStation(String name, Position position) {
        super(name, position);
        
        // Memastikan hanya satu instance yang dibuat (Singleton)
        if (instance == null) {
            instance = this;
        } else {
            System.err.println("WARNING: Multiple PlateStorageStation created! Using the first instance.");
        }
        
        this.plates = new Stack<>();
        
        // Mengisi stok awal
        for (int i = 0; i < INITIAL_STOCK; i++) {
            plates.push(new Plate()); 
        }
        log("INFO", "INITIAL STOCK: " + INITIAL_STOCK + " clean plates available.");
    }

    public static PlateStorageStation getInstance() {
        return instance;
    }

    /**
     * Menangani interaksi Chef (Mengambil Piring).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // SCENARIO 1: AMBIL PIRING (Hand: Kosong)
        if (itemHand == null) {
            if (plates.isEmpty()) {
                log("FAIL", "STORAGE EMPTY: No plates available!");
                return;
            }

            // Cek piring paling atas
            Plate topPlate = plates.peek();

            // RESTRIKSI GDD: Plate bersih TIDAK bisa diambil jika Plate di atasnya kotor.
            
            if (topPlate.isClean()) {
                // KASUS A: Ambil Piring Bersih.
                chef.setInventory(plates.pop());
                
                // --- LOGGING AKURAT KASUS A ---
                long cleanCount = plates.stream().filter(Plate::isClean).count();
                log("ACTION", "TAKEN: Clean Plate. Remaining Clean: " + cleanCount + ". Total: " + plates.size());
                return;
            } else {
                // KASUS B: Ambil Plate Kotor yang memblokir.
                chef.setInventory(plates.pop()); 
                
                // --- LOGGING AKURAT KASUS B ---
                long totalCount = plates.size();
                long dirtyCount = plates.stream().filter(p -> !p.isClean()).count();
                long cleanCount = totalCount - dirtyCount;
                
                log("ACTION", "TAKEN: Dirty Plate (for washing). Remaining Dirty: " + dirtyCount + ". Remaining Clean: " + cleanCount + ". Total: " + totalCount);
                return;
            }
        }

        // SCENARIO 2: MENARUH ITEM (Drop Manual)
        log("FAIL", "DROP REJECTED: Items cannot be dropped directly onto Plate Storage.");
    }
    
    /**
     * Digunakan oleh ServingStation/WashingStation untuk mengembalikan piring kotor/bersih ke tumpukan (Internal Add).
     */
    public void addPlateToStack(Plate p) {
        if (p != null) {
            plates.push(p);
            
            String status = p.isClean() ? "Clean" : "Dirty";
            
            // Log Internal
            log("INFO", "RETURNED: " + status + " plate added automatically to stack. Total: " + plates.size());
        }
    }
    
    /**
     * Helper untuk debug.
     */
    public int getPlateCount() {
        return plates.size();
    }
}