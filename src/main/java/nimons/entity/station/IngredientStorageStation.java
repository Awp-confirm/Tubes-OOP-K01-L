package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import nimons.entity.item.ingredient.Cucumber; 
import nimons.entity.item.ingredient.Fish;
import nimons.entity.item.ingredient.Nori;
import nimons.entity.item.ingredient.Rice;
import nimons.entity.item.ingredient.Shrimp;

/**
 * IngredientStorageStation (I) bertindak sebagai dispenser bahan mentah tak terbatas.
 * Juga memungkinkan Plating langsung dari storage jika Chef memegang Plate.
 */
public class IngredientStorageStation extends Station {
    
    private Item storedItem; // Dummy item (untuk display GUI)

    public IngredientStorageStation(String name, Position position) {
        super(name, position);
        // Setup dummy item berdasarkan nama crate (diperlukan untuk GUI)
        if (name.contains("Rice")) storedItem = new Rice();
        else if (name.contains("Nori")) storedItem = new Nori();
        else if (name.contains("Cucumber")) storedItem = new Cucumber();
        else if (name.contains("Shrimp")) storedItem = new Shrimp();
        else if (name.contains("Fish")) storedItem = new Fish();
    }

    /**
     * Menangani interaksi Chef (Mengambil Bahan atau Plating).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // SCENARIO 1: PLATING LANGSUNG DARI STORAGE (Hand: Plate)
        if (itemHand instanceof Plate) {
            Plate p = (Plate) itemHand;
            
            // Validasi: Hanya jika piring kosong (belum ada Dish).
            if (p.getDish() == null) {
                Item bahanBaru = spawnItem(); // Ambil bahan baru (Stok tak terbatas)
                
                // Menggunakan Helper Plating (Wrapper Ingredient -> Dish)
                processPlating(p, bahanBaru); 
                // Item tetap di tangan Chef (Plate) setelah plating.
            } else {
                 log("FAIL", "Piring sudah berisi Dish.");
            }
            return;
        }

        // SCENARIO 2: AMBIL BAHAN (Hand: Kosong)
        if (itemHand == null) {
            Item bahanBaru = spawnItem();
            if (bahanBaru != null) {
                chef.setInventory(bahanBaru);
                log("ACTION", "Mengambil " + bahanBaru.getName());
            } else {
                log("FAIL", "Tipe bahan tidak teridentifikasi.");
            }
            return;
        }
        
        // SCENARIO 3: DROP ITEM
        // Jika tangan penuh dan Chef mencoba interact (GDD: Storage bisa untuk menaruh),
        // tapi dalam konteks ini, kita prioritaskan sebagai SOURCE, bukan tempat taruh.
        if (itemHand != null) {
            log("INFO", "Tangan penuh. Gunakan Assembly Station untuk menaruh item.");
        }
    }
    
    /**
     * Helper method untuk membuat instance baru dari Ingredient yang disimpan.
     * Stok dianggap tak terbatas (infinite spawn).
     */
    private Item spawnItem() {
        // Mengembalikan instance baru (cloning)
        if (name.contains("Rice")) return new Rice();
        if (name.contains("Nori")) return new Nori();
        if (name.contains("Cucumber")) return new Cucumber();
        if (name.contains("Shrimp")) return new Shrimp();
        if (name.contains("Fish")) return new Fish();
        return null;
    }

    /**
     * Getter untuk item dummy (Keperluan display GUI).
     */
    public Item getPlacedItem() { return storedItem; }
}