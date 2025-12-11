package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.Plate;
import nimons.entity.item.ingredient.Cucumber; 
import nimons.entity.item.ingredient.Fish;
import nimons.entity.item.ingredient.Nori;
import nimons.entity.item.ingredient.Rice;
import nimons.entity.item.ingredient.Shrimp;
import nimons.entity.item.interfaces.Preparable;

/**
 * IngredientStorageStation (I): Bertindak sebagai dispenser bahan mentah tak terbatas.
 * Chef dapat mengambil bahan (Pick Up) atau Plating langsung (Hand: Plate).
 */
public class IngredientStorageStation extends Station {
    
    private Item storedItem; // Dummy item (untuk display GUI dan menentukan tipe bahan yang didispense)

    /**
     * CONSTRUCTOR: Menetapkan tipe bahan berdasarkan koordinat X dan Y di peta.
     */
    public IngredientStorageStation(String name, Position position) {
        super(name, position);
        
        final int x = position.getX();
        final int y = position.getY();

        // --- LOGIC MAPPING POSISI KE ITEM ---
        if (x == 0) { 
            // Kolom Kiri Peta (0)
            if (y == 3) storedItem = new Rice(); 
            else if (y == 4) storedItem = new Nori(); 
            else if (y == 5) storedItem = new Cucumber();
        } 
        else if (x == 13) { 
            // Kolom Kanan Peta (13)
            if (y == 3) storedItem = new Fish(); 
            else if (y == 4) storedItem = new Shrimp(); 
            else if (y == 5) storedItem = new Cucumber();
        } 
        
        // --- LOGIC FALLBACK DAN PENAMAAN AKHIR ---
        String itemName = storedItem != null ? storedItem.getName() : "UNDEFINED";
        
        if (storedItem == null) {
            // Fallback: Jika posisi tidak terdeteksi
            storedItem = new Nori();
            itemName = "Nori_FALLBACK"; 
        }
        
        // Set nama yang benar (misal: Rice Storage) untuk logging
        this.name = storedItem.getName() + " Storage";

        // --- DEBUGGING LOG PENTING (DI TERMINAL) ---
        System.out.println("DEBUG I: Membuat " + itemName + " di Posisi: (" + x + ", " + y + ")");
    }
    
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // --- SCENARIO KRITIS BARU: PLATING UTENSIL $\rightarrow$ PIRING DI TANGAN ---
        // Blokir semua logic Utensil di Storage, karena Storage hanya untuk mengambil bahan.
        if (itemHand instanceof KitchenUtensil && this.storedItem instanceof Preparable) {
            log("FAIL", "Ingredient Storage hanya untuk mengambil bahan mentah.");
            return; 
        }

        // SCENARIO 1: PLATING LANGSUNG DARI STORAGE (Hand: Plate)
        if (itemHand instanceof Plate) {
            Plate p = (Plate) itemHand;
            
            if (p.getFood() == null) { 
                Item bahanBaru = spawnItem(); 
                
                if (bahanBaru != null) {
                    processPlating(p, bahanBaru); // Memanggil Plating dari Base Class
                    log("ACTION", "PLATED: " + bahanBaru.getName() + " langsung ke piring.");
                } else {
                    log("FAIL", "Cannot instantiate item type: " + this.name + ".");
                }
            } else {
                log("FAIL", "Plate is occupied by Dish (" + p.getFood().getName() + ").");
            }
            return; 
        }

        // SCENARIO 2: AMBIL BAHAN (Hand: Kosong)
        if (itemHand == null) {
            Item bahanBaru = spawnItem();
            if (bahanBaru != null) {
                chef.setInventory(bahanBaru);
                log("ACTION", "TAKEN: " + bahanBaru.getName() + " picked up.");
            } else {
                log("FAIL", "Cannot instantiate item type: " + this.name + ".");
            }
            return;
        }
        
        // SCENARIO 3: DROP ITEM (Jika tangan penuh dan bukan Plate)
        if (itemHand != null) {
            log("INFO", "Hand is full. Storage only dispenses ingredients.");
            return;
        }
    }
    
    /** Menginstansiasi objek Item baru dari tipe yang tersimpan (Dispenser). */
    private Item spawnItem() {
        if (storedItem instanceof Rice) return new Rice();
        if (storedItem instanceof Nori) return new Nori();
        if (storedItem instanceof Cucumber) return new Cucumber();
        if (storedItem instanceof Shrimp) return new Shrimp();
        if (storedItem instanceof Fish) return new Fish();
        return null; 
    }

    public Item getPlacedItem() { return storedItem; }
}