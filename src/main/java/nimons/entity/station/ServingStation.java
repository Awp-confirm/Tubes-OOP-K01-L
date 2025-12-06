package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;

/**
 * ServingStation adalah tempat menyajikan hidangan kepada pelanggan.
 * Syarat: Chef harus membawa Plate yang berisi Dish.
 */
public class ServingStation extends Station {

    public ServingStation(String name, Position position) {
        super(name, position);
    }

    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;

        Item itemHand = chef.getInventory();

        // Validasi: Harus membawa Piring
        if (itemHand instanceof Plate) {
            Plate plate = (Plate) itemHand;

            // Validasi: Piring harus ada isinya (Dish)
            if (plate.getDish() != null) {
                serveDish(chef, plate);
            } else {
                System.out.println("[FAIL] Tidak bisa menyajikan piring kosong!");
            }
        } else {
            System.out.println("[FAIL] Anda harus menyajikan makanan di atas piring!");
        }
    }

    /**
     * Proses penyajian makanan.
     */
    private void serveDish(Chef chef, Plate plate) {
        String dishName = plate.getDish().getName();
        
        System.out.println("[ACTION] Menyajikan " + dishName + " ke pelanggan!");

        // TODO: Integrasi dengan Order System (Cek apakah pesanan sesuai?)
        // boolean isOrderValid = OrderManager.check(dishName);
        
        // Simulasi Order Berhasil
        System.out.println("[SUCCESS] Pesanan diterima! (Skor +?)");

        // Hapus piring dari tangan Chef 
        // (Secara logika game: Piring kotor akan teleport ke PlateStorage)
        chef.setInventory(null);
        
        // Catatan untuk Integrasi Nanti:
        // plate.setDish(null);
        // plate.setClean(false);
        // PlateStorage.addDirtyPlate(plate);
    }
}