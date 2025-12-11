package nimons.entity.station;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Dish;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import nimons.logic.order.OrderManager;

/**
 * ServingStation (S): Menangani penyajian Dish kepada pelanggan.
 * Logika utama: Validasi Order, mencatat skor, dan menunda pengembalian piring kotor (delay 10 detik).
 */
public class ServingStation extends Station {

    private OrderManager orderManager;
    
    /** Inner class untuk menahan piring yang sedang dalam antrean pengembalian (delay) */
    private class PendingPlate {
        Plate plate;
        float timer;
        private final float RETURN_DELAY_MS = 10000; // 10 Detik delay
        
        public PendingPlate(Plate p) { 
            this.plate = p; 
            this.timer = RETURN_DELAY_MS; 
        } 
    }
    
    private List<PendingPlate> pendingReturns; // List antrean piring kotor yang tertunda pengembaliannya.

    public ServingStation(String name, Position position) {
        super(name, position);
        // OrderManager adalah Singleton (diasumsikan)
        this.orderManager = OrderManager.getInstance(); 
        this.pendingReturns = new ArrayList<>();
    }

    /**
     * Update loop: Memajukan timer dan menangani pengembalian piring kotor setelah delay selesai.
     */
    @Override
    public void update(long deltaTime) {
        // --- Ambil PlateStorage dari Singleton ---
        PlateStorageStation plateStorage = PlateStorageStation.getInstance();
        
        if (plateStorage == null || pendingReturns.isEmpty()) return;

        Iterator<PendingPlate> it = pendingReturns.iterator();
        while (it.hasNext()) {
            PendingPlate pp = it.next();
            pp.timer -= deltaTime;
            
            // Cek jika 10 detik telah berlalu
            if (pp.timer <= 0) {
                // Kembalikan ke storage menggunakan method internal di PSS
                plateStorage.addPlateToStack(pp.plate); 
                it.remove();
            }
        }
    }

    /**
     * Menangani interaksi Chef (Menyajikan Dish).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // Scenario: Chef menyerahkan Plate
        if (itemHand instanceof Plate) {
            Plate piring = (Plate) itemHand;
            Dish masakan = piring.getFood(); 

            // Validasi 1: Piring Kosong?
            if (masakan == null) {
                log("FAIL", "REJECTED: Plate is empty. Only plated dishes can be served.");
                return;
            }
            
            log("ACTION", "SERVING: Presenting " + masakan.getName() + " to customer...");

            // Validasi 2: Cocok dengan Order?
            boolean orderMatch = orderManager.validateOrder(masakan);

            if (orderMatch) {
                log("SUCCESS", "ORDER CORRECT! Score recorded.");
            } else {
                log("FAIL", "ORDER MISMATCH! Dish does not match customer order.");
            }
            
            // --- LOGIKA PENGEMBALIAN PIRING KOTOR (DELAY) ---
            
            // 1. Bersihkan Dish dari piring (Plate.removeDish() membersihkan dish dan set status kotor)
            piring.removeDish(); 

            // 2. Hapus dari tangan Chef
            chef.setInventory(null); 

            // --- Ambil PlateStorage dari Singleton ---
            PlateStorageStation plateStorage = PlateStorageStation.getInstance();

            // 3. Masukkan ke antrean delay 10 detik
            if (plateStorage != null) {
                pendingReturns.add(new PendingPlate(piring));
                log("INFO", "DIRTY PLATE RETURN: Plate added to return queue (Total: " + (pendingReturns.size()) + "). Will return in 10 seconds.");
            } else {
                // Log error jika PlateStorage belum dibuat
                log("ERROR", "Plate Storage not initialized! Dirty plate lost."); 
            }
        } else {
            log("INFO", "Only plated items can be served here.");
        }
    }
}