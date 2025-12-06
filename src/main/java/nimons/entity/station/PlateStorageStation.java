package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;

import java.util.Stack; // Syarat Teknis: Collections

/**
 * PlateStorageStation berfungsi menampung piring bersih dan kotor.
 * Menggunakan struktur data Stack (Tumpukan).
 */
public class PlateStorageStation extends Station {

    private Stack<Plate> plateStack;

    public PlateStorageStation(String name, Position position) {
        super(name, position);
        this.plateStack = new Stack<>();
        initializePlates();
    }

    /**
     * Mengisi tumpukan awal dengan piring bersih.
     * Jumlah piring bisa disesuaikan (misal 5 piring).
     */
    private void initializePlates() {
        for (int i = 0; i < 5; i++) {
            // ID Dummy, Clean = true, Dish = null
            plateStack.push(new Plate("P-" + i, true, null));
        }
    }

    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;

        Item itemHand = chef.getInventory();

        // 1. DROP (Menaruh Item) - DILARANG
        // Spesifikasi: "Tidak dapat melakukan drop item apapun pada station ini"
        if (itemHand != null) {
            System.out.println("[FAIL] Tidak bisa menaruh barang di Plate Storage.");
            return;
        }

        // 2. PICK UP (Mengambil Piring)
        // Syarat: Chef tangan kosong & Tumpukan tidak kosong
        if (itemHand == null && !plateStack.isEmpty()) {
            // Ambil piring teratas (bisa bersih, bisa kotor tergantung tumpukan)
            Plate topPlate = plateStack.pop();
            
            chef.setInventory(topPlate);
            
            String status = topPlate.isClean() ? "Bersih" : "Kotor";
            System.out.println("[DEBUG] Mengambil Piring (" + status + "). Sisa: " + plateStack.size());
        } else {
            System.out.println("[INFO] Storage kosong.");
        }
    }

    /**
     * Method khusus untuk menerima piring kotor dari ServingStation.
     * Piring kotor akan ditaruh di posisi paling atas (Top of Stack).
     */
    public void addDirtyPlate(Plate plate) {
        if (plate != null) {
            plate.setClean(false); // Pastikan statusnya kotor
            plate.setDish(null);   // Pastikan makanannya sudah hilang/dimakan
            
            plateStack.push(plate);
            System.out.println("[INFO] Piring kotor dikembalikan ke storage.");
        }
    }

    public int getPlateCount() {
        return plateStack.size();
    }
}