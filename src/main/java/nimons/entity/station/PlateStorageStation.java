package nimons.entity.station;

import java.util.Stack;

import nimons.core.GameConfig;
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
        for (int i = 0; i < GameConfig.INITIAL_PLATE_STOCK; i++) {
            plates.push(new Plate()); 
        }
        log("INFO", "INITIAL STOCK: " + GameConfig.INITIAL_PLATE_STOCK + " clean plates available.");
    }

    public static PlateStorageStation getInstance() {
        return instance;
    }
    
    /**
     * Reset singleton instance for game restart
     */
    public static void resetInstance() {
        if (instance != null) {
            System.out.println("[PlateStorageStation] Resetting singleton instance");
            instance = null;
        }
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
            log("DEBUG", "Top plate is: " + (topPlate.isClean() ? "CLEAN" : "DIRTY"));

            // RESTRIKSI GDD: Plate bersih TIDAK bisa diambil jika Plate di atasnya kotor.
            
            if (topPlate.isClean()) {
                // KASUS A: Ambil Piring Bersih.
                chef.setInventory(plates.pop());
                
                // --- LOGGING AKURAT KASUS A ---
                long cleanCount = plates.stream().filter(Plate::isClean).count();
                log("ACTION", "TAKEN: Clean Plate from top. Remaining Clean: " + cleanCount + ". Total: " + plates.size());
                return;
            } else {
                // KASUS B: Ambil Plate Kotor yang memblokir.
                chef.setInventory(plates.pop()); 
                
                // --- LOGGING AKURAT KASUS B ---
                long totalCount = plates.size();
                long dirtyCount = plates.stream().filter(p -> !p.isClean()).count();
                long cleanCount = totalCount - dirtyCount;
                
                log("ACTION", "TAKEN: Dirty Plate from top (blocking clean plates). Remaining Dirty: " + dirtyCount + ". Remaining Clean: " + cleanCount + ". Total: " + totalCount);
                return;
            }
        }

        // SCENARIO 2: MENARUH ITEM (Drop Manual)
        log("FAIL", "DROP REJECTED: Items cannot be dropped directly onto Plate Storage.");
    }
    
    /**
     * Digunakan oleh ServingStation/WashingStation untuk mengembalikan piring kotor/bersih ke tumpukan (Internal Add).
     * Membatasi jumlah plate maksimal sesuai INITIAL_PLATE_STOCK.
     */
    public void addPlateToStack(Plate p) {
        if (p != null) {
            // Jangan tambah jika sudah mencapai jumlah maksimal
            if (plates.size() >= GameConfig.INITIAL_PLATE_STOCK) {
                log("WARNING", "REJECTED: Cannot add plate. Storage at maximum capacity (" + GameConfig.INITIAL_PLATE_STOCK + " plates).");
                return;
            }
            
            String status = p.isClean() ? "Clean" : "Dirty";
            
            plates.push(p);
            
            // Count dirty and clean plates in stack
            long dirtyCount = plates.stream().filter(plate -> !plate.isClean()).count();
            long cleanCount = plates.stream().filter(Plate::isClean).count();
            
            // Detailed log
            log("INFO", "RETURNED: " + status + " plate added to TOP of stack. Total: " + plates.size() + " (Dirty: " + dirtyCount + ", Clean: " + cleanCount + ")");
            
            // Show top 3 plates status for debugging
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
    
    /**
     * Helper untuk debug.
     */
    public int getPlateCount() {
        return plates.size();
    }
}