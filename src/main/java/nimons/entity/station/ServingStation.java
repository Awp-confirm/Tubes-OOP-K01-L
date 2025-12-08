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
        public PendingPlate(Plate p) { 
            // Kita gunakan timer dalam milisekon, 10000ms = 10 detik
            this.plate = p; 
            this.timer = 10000; 
        } 
    }
    
    private List<PendingPlate> pendingReturns; // List antrean piring kotor yang tertunda

    public ServingStation(String name, Position position) {
        super(name, position);
        // OrderManager adalah Singleton (diasumsikan)
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
                // ASUMSI: plateStorage.addDirtyPlate(Plate) adalah method yang benar
                plateStorage.addDirtyPlate(pp.plate); 
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
            
            // Ambil Dish menggunakan getFood() alias dari Plate.java
            Dish masakan = piring.getFood(); 

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
                // TODO: Panggil ScoreManager.addScore(masakan);
            } else {
                log("FAIL", "Salah pesanan! Dish tidak cocok dengan Order.");
            }
            
            // --- LOGIKA PENGEMBALIAN PIRING KOTOR (DELAY) ---
            
            // 1. Bersihkan Dish dari piring (Menghapus makanan)
            piring.setFood(null); 
            // 2. Set status piring menjadi kotor
            piring.setClean(false); 

            // 3. Hapus dari tangan Chef
            chef.setInventory(null); 

            // 4. Masukkan ke antrean delay 10 detik
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