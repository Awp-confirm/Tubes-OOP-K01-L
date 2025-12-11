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
import nimons.entity.item.interfaces.Preparable;

/**
 * IngredientStorageStation (I): Bertindak sebagai dispenser bahan mentah tak terbatas.
 * Chef dapat mengambil bahan (Pick Up) atau Plating langsung (Hand: Plate).
 * Juga dapat digunakan sebagai assembly station untuk meletakkan plate/ingredients.
 */
public class IngredientStorageStation extends Station {
    
    private Item storedItem; // Dummy item (untuk display GUI dan menentukan tipe bahan yang didispense)
    private Item placedItem; // Item yang ditempatkan di atas meja (untuk assembly)

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
        Item itemTable = this.placedItem;

        // SCENARIO 1: Pick up item from table
        if (itemHand == null && itemTable != null) {
            chef.setInventory(itemTable);
            this.placedItem = null;
            log("ACTION", "PICKED UP: " + itemTable.getName() + " from table.");
            return;
        }

        // SCENARIO 2: Place Plate on table (for assembly)
        if (itemHand instanceof Plate && itemTable == null) {
            this.placedItem = itemHand;
            chef.setInventory(null);
            log("ACTION", "PLACED: Plate on table.");
            return;
        }

        // SCENARIO 3: Add ingredient from storage to plate on table
        if (itemHand == null && itemTable instanceof Plate) {
            Plate p = (Plate) itemTable;
            Item bahanBaru = spawnItem();
            
            if (bahanBaru != null && bahanBaru instanceof Preparable) {
                processPlating(p, bahanBaru);
                log("ACTION", "ADDED: " + bahanBaru.getName() + " to plate on table.");
            }
            return;
        }

        // SCENARIO 4: PLATING LANGSUNG DARI STORAGE (Hand: Plate, Table: Empty)
        if (itemHand instanceof Plate && itemTable == null) {
            Plate p = (Plate) itemHand;
            
            if (p.getFood() == null) { 
                Item bahanBaru = spawnItem(); 
                
                if (bahanBaru != null) {
                    processPlating(p, bahanBaru); // Memanggil Plating dari Base Class
                    log("ACTION", "PLATED: " + bahanBaru.getName() + " to plate in hand.");
                } else {
                    log("FAIL", "Cannot instantiate item type: " + this.name + ".");
                }
            } else {
                log("FAIL", "Plate is occupied by Dish (" + p.getFood().getName() + ").");
            }
            return; 
        }

        // SCENARIO 5: AMBIL BAHAN BARU (Hand: Kosong, Table: Anything)
        if (itemHand == null && itemTable == null) {
            Item bahanBaru = spawnItem();
            if (bahanBaru != null) {
                chef.setInventory(bahanBaru);
                log("ACTION", "TAKEN: " + bahanBaru.getName() + " picked up.");
            } else {
                log("FAIL", "Cannot instantiate item type: " + this.name + ".");
            }
            return;
        }
        
        // SCENARIO 6: Place ingredient on empty table
        if (itemHand instanceof Preparable && itemTable == null) {
            this.placedItem = itemHand;
            chef.setInventory(null);
            log("ACTION", "PLACED: " + itemHand.getName() + " on table.");
            return;
        }
        
        // Default: Hand is full or invalid operation
        log("INFO", "Cannot perform action. Hand: " + (itemHand != null ? itemHand.getName() : "empty") + ", Table: " + (itemTable != null ? itemTable.getName() : "empty"));
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

    public Item getStoredItem() { return storedItem; }
    
    public Item getPlacedItem() { return placedItem; }
}
