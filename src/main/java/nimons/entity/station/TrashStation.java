package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.Plate;

/**
 * TrashStation berfungsi untuk membuang bahan atau makanan yang gagal/tidak diinginkan.
 * - Jika membawa bahan/makanan: Item hilang.
 * - Jika membawa alat masak/piring: Hanya isinya yang hilang.
 */
public class TrashStation extends Station {

    public TrashStation(String name, Position position) {
        super(name, position);
    }

    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;

        Item itemHand = chef.getInventory();

        // Jika tangan kosong, tidak ada yang bisa dibuang
        if (itemHand == null) {
            System.out.println("[INFO] Tidak ada item untuk dibuang.");
            return;
        }

        // KASUS 1: Membawa Piring (Plate)
        if (itemHand instanceof Plate) {
            Plate plate = (Plate) itemHand;
            if (plate.getDish() != null) {
                System.out.println("[ACTION] Membuang makanan " + plate.getDish().getName() + " dari piring.");
                plate.setDish(null); // Hapus makanannya
                plate.setClean(false); // Piring jadi kotor (kena sisa makanan)
            } else {
                System.out.println("[INFO] Piring sudah kosong.");
            }
            return;
        }

        // KASUS 2: Membawa Alat Masak (Panci/Wajan/Oven)
        if (itemHand instanceof KitchenUtensil) {
            KitchenUtensil utensil = (KitchenUtensil) itemHand;
            if (utensil.getContents() != null && !utensil.getContents().isEmpty()) {
                System.out.println("[ACTION] Mengosongkan isi " + utensil.getName());
                utensil.setContents(null); // Kosongkan isi panci
            } else {
                System.out.println("[INFO] " + utensil.getName() + " sudah kosong.");
            }
            return;
        }

        // KASUS 3: Membawa Bahan/Masakan (Ingredient/Dish) tanpa wadah
        // Langsung buang itemnya dari tangan Chef
        System.out.println("[ACTION] Membuang " + itemHand.getName() + " ke tempat sampah.");
        chef.setInventory(null);
    }
}