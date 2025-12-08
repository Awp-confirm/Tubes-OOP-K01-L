package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Dish;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import nimons.logic.order.OrderManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ServingStation (S) menangani penyajian Dish kepada pelanggan.
 * Logika utama: Validasi Order, mencatat skor, dan menunda pengembalian piring kotor selama 10 detik (GDD).
 */
public class ServingStation extends Station {

    private OrderManager orderManager;
    private PlateStorageStation plateStorage; 
    
    /** Inner class untuk menahan piring yang sedang dalam antrean pengembalian (delay) */
    private class PendingPlate {
        Plate plate;
        float timer;
        // 10 Detik delay
        public PendingPlate(Plate p) { this.plate = p; this.timer = 10000; } 
    }
    
    private List<PendingPlate> pendingReturns; // List antrean piring kotor yang tertunda

    public ServingStation(String name, Position position) {
        super(name, position);
        this.orderManager = OrderManager.getInstance();
        this.pendingReturns = new ArrayList<>();
    }

    /**
     * Setter untuk menghubungkan Plate Storage (Wajib dipanggil di initGame).
     */
    public void setPlateStorage(PlateStorageStation storage) {
        this.plateStorage = storage;
    }

    /**
     * Update loop: Menangani timer pengembalian piring kotor (10 detik delay).
     */
    @Override
    public void update(long deltaTime) {
        if (plateStorage == null || pendingReturns.isEmpty()) return;

        Iterator<PendingPlate> it = pendingReturns.iterator();
        while (it.hasNext()) {
            PendingPlate pp = it.next();
            pp.timer -= deltaTime;
            
            // Check if 10 seconds has passed
            if (pp.timer <= 0) {
                // Kembalikan ke storage dan hapus dari antrean
                plateStorage.addDirtyPlate(pp.plate); // Asumsi method ini ada di PlateStorageStation
                log("INFO", "Piring kotor dikembalikan ke Storage.");
                it.remove();
            }
        }
    }

    /**
     * Menangani interaksi Chef (Menyajikan Dish).
     * Trigger: Chef menyerahkan Plate berisi Dish.
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // Scenario: Chef menyerahkan Plate
        if (itemHand instanceof Plate) {
            Plate piring = (Plate) itemHand;
            Dish masakan = null; 

            // Cek apakah piring berisi Dish
            if (piring.getDish() instanceof Dish) {
                masakan = (Dish) piring.getDish();
            }
            
            // Validasi 1: Piring Kosong?
            if (masakan == null) {
                log("FAIL", "Piring kosong! Hanya Plate yang berisi Dish yang bisa disajikan.");
                return;
            }
            
            log("ACTION", "Menyajikan " + masakan.getName() + "...");

            // Validasi 2: Cocok dengan Order?
            boolean orderMatch = orderManager.validateOrder(masakan);

            if (orderMatch) {
                log("SUCCESS", "Pesanan Benar! Score bertambah.");
                // Logic Score Update (Jika ada)
            } else {
                log("FAIL", "Salah pesanan! Dish tidak cocok dengan Order.");
                // Makanan hilang 
            }
            
            // --- LOGIKA PENGEMBALIAN PIRING KOTOR (DELAY) ---
            
            // 1. Bersihkan piring dan set status kotor
            piring.setDish(null); 
            piring.setClean(false); 

            // 2. Hapus dari tangan Chef
            chef.setInventory(null); 

            // 3. Masukkan ke antrean delay 10 detik
            if (plateStorage != null) {
                pendingReturns.add(new PendingPlate(piring));
                log("INFO", "Piring akan kembali ke storage dalam 10 detik.");
            } else {
                 log("ERROR", "Plate Storage belum disambungkan!");
            }
        } else {
            log("INFO", "Hanya Plate yang bisa disajikan.");
        }
    }
}