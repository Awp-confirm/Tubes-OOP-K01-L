package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import java.util.Stack;

/**
 * PlateStorageStation (P) menangani tumpukan Plate bersih dan kotor (Stack LIFO).
 * Logic utama adalah manajemen stack dan restriksi pengambilan Plate bersih.
 */
public class PlateStorageStation extends Station {

    private Stack<Plate> plates; // Tumpukan piring

    public PlateStorageStation(String name, Position position) {
        super(name, position);
        this.plates = new Stack<>();
        
        // Isi stok awal piring (5 piring bersih)
        for (int i = 0; i < 5; i++) {
            // Asumsi: Constructor Plate() default menghasilkan piring bersih (isClean=true)
            plates.push(new Plate()); 
        }
        log("INFO", "Stok awal: 5 Piring bersih tersedia.");
    }

    /**
     * Menangani interaksi Chef (Mengambil Piring atau Menaruh Piring).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // SCENARIO 1: AMBIL PIRING (Hand: Kosong)
        if (itemHand == null) {
            if (plates.isEmpty()) {
                log("FAIL", "Piring habis!");
                return;
            }

            // Cek piring paling atas TANPA mengambilnya
            Plate topPlate = plates.peek();

            // Piring diambil, terlepas dari statusnya (Dirty atau Clean)
            chef.setInventory(plates.pop());
            
            if (topPlate.isClean()) {
                // KONDISI A: Piring bersih di atas (Aman diambil)
                log("ACTION", "Mengambil Piring Bersih. Sisa: " + plates.size());
            } else {
                // KONDISI B: Piring kotor di atas (RESTRIKSI GDD)
                log("ACTION", "Mengambil Piring Kotor untuk dicuci. Sisa: " + plates.size());
            }
            return;
        }

        // SCENARIO 2: MENARUH PIRING (Drop Manual/Pengembalian)
        if (itemHand instanceof Plate) {
            Plate p = (Plate) itemHand;
            
            // Piring kotor (dari Serving) atau bersih (dari Washing) selalu masuk paling atas.
            plates.push(p);
            chef.setInventory(null);
            
            // Logging disesuaikan berdasarkan status piring yang ditaruh
            String status = p.isClean() ? "Bersih" : "Kotor";
            log("INFO", "Piring " + status + " ditaruh manual ke tumpukan. Total: " + plates.size());
            return;
        }
        
        log("INFO", "Tangan penuh dengan item non-Plate, tidak bisa berinteraksi.");
    }
    
    // Method yang dipanggil dari ServingStation / WashingStation (Pengembalian piring)
    /**
     * Digunakan oleh ServingStation/WashingStation untuk mengembalikan piring kotor/bersih ke tumpukan.
     * Piring yang dikembalikan masuk paling atas (LIFO).
     */
    public void addDirtyPlate(Plate p) {
        if (p != null) {
            plates.push(p);
        }
    }
    
    /**
     * Helper untuk debug (Digunakan oleh ServingStation/GUI).
     */
    public int getPlateCount() {
        return plates.size();
    }
}