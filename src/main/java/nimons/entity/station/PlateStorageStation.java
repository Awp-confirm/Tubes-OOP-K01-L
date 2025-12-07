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
            plates.push(new Plate()); // Plate default bersih
        }
        log("INFO", "Stok awal: 5 Piring bersih tersedia.");
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
                log("FAIL", "Piring habis!");
                return;
            }

            // Cek piring paling atas TANPA mengambilnya
            Plate topPlate = plates.peek();

            if (topPlate.isClean()) {
                // KONDISI A: Piring bersih di atas (Aman diambil)
                chef.setInventory(plates.pop());
                log("ACTION", "Mengambil Piring Bersih. Sisa: " + plates.size());
            } else {
                // KONDISI B: Piring kotor di atas (RESTRIKSI GDD)
                // Chef dipaksa mengambil piring kotor itu untuk dicuci.
                chef.setInventory(plates.pop());
                log("ACTION", "Mengambil Piring Kotor untuk dicuci.");
            }
            return;
        }

        // SCENARIO 2: MENARUH PIRING (Drop Manual)
        // Seharusnya Chef tidak bisa drop di PlateStorage, tapi pertahankan logika addPlate.
        if (itemHand instanceof Plate) {
            addReturnedPlate((Plate) itemHand);
            chef.setInventory(null);
            log("INFO", "Piring ditaruh manual ke tumpukan.");
            return;
        }
        
        log("INFO", "Tangan penuh, tidak bisa berinteraksi.");
    }

    /**
     * Digunakan oleh ServingStation/WashingStation untuk mengembalikan piring ke tumpukan.
     * Piring yang dikembalikan masuk paling atas (LIFO).
     */
    public void addReturnedPlate(Plate p) {
        if (p != null) {
            // Piring kotor (dari Serving) atau bersih (dari Washing) selalu masuk paling atas.
            plates.push(p);
        }
    }
    
    // Method khusus untuk Serving Station (Karena ada delay 10 detik, kita beri nama spesifik)
    public void addDirtyPlate(Plate p) {
        addReturnedPlate(p);
        log("INFO", "Piring kotor dikembalikan ke tumpukan.");
    }
    
    /**
     * Helper untuk debug (Digunakan oleh ServingStation).
     */
    public int getPlateCount() {
        return plates.size();
    }
}