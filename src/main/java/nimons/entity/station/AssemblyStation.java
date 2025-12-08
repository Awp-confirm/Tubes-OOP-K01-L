package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Dish;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.Plate;
import nimons.logic.recipe.RecipeManager;

/**
 * AssemblyStation (A) atau Counter menangani perakitan hidangan,
 * berfungsi sebagai meja kerja untuk Plating, Pick Up, Drop, dan Kombinasi Resep.
 */
public class AssemblyStation extends Station {

    private Item placedItem; // Item yang diletakkan di permukaan Assembly Station

    public AssemblyStation(String name, Position position) {
        super(name, position);
    }

    /**
     * Menangani interaksi Chef (Menaruh, Mengambil, Plating, atau Merakit).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();
        Item itemTable = this.placedItem;

        // SCENARIO 1: PLATING DARI UTENSIL KE PIRING (Hand: Plate, Table: Utensil)
        if (itemHand instanceof Plate && itemTable instanceof KitchenUtensil) {
            KitchenUtensil panci = (KitchenUtensil) itemTable;
            
            if (panci.getContents() != null && !panci.getContents().isEmpty()) {
                // Item isi diambil (sebagai Preparable/Item)
                Item isi = (Item) panci.getContents().iterator().next();
                
                // Menggunakan Helper Plating (Wrapper Ingredient -> Dish)
                processPlating((Plate) itemHand, isi);
                
                // Jika plating sukses, kosongkan Utensil (Menggunakan getFood() alias dari Plate.java)
                if (((Plate)itemHand).getFood() != null) {
                    panci.getContents().clear();
                    log("INFO", "Isi Utensil dikosongkan setelah plating ke piring.");
                }
            } else {
                log("INFO", panci.getName() + " kosong, tidak ada yang bisa di-plating.");
            }
            return;
        }

        // SCENARIO 2: PICK UP ITEM (Hand: Empty, Table: Item)
        if (itemHand == null && itemTable != null) {
            log("ACTION", "Mengambil " + itemTable.getName());
            chef.setInventory(itemTable);
            this.placedItem = null;
            return;
        }

        // SCENARIO 3: DROP ITEM (Hand: Item, Table: Empty)
        if (itemHand != null && itemTable == null) {
            log("ACTION", "Menaruh " + itemHand.getName());
            this.placedItem = itemHand;
            chef.setInventory(null);
            return;
        }

        // SCENARIO 4: ASSEMBLY / KOMBINASI RESEP (Hand: Item, Table: Plate)
        if (itemHand != null && itemTable instanceof Plate) {
            Plate piring = (Plate) itemTable;
            Item isiPiring = piring.getFood(); // Menggunakan getFood() alias

            // Pastikan piring bersih sebelum Assembly (Opsional, tapi aman)
            if (!piring.isClean()) {
                log("FAIL", "Piring kotor, tidak bisa digunakan untuk Assembly!");
                return;
            }

            // 4a. Plating Manual Pertama (Piring di meja kosong)
            if (isiPiring == null) {
                processPlating(piring, itemHand);
                // Jika plating berhasil, item di tangan Chef akan kosong
                if (piring.getFood() != null) chef.setInventory(null); 
                return;
            }

            // 4b. Kombinasi Lanjutan (Piring sudah ada isinya)
            
            // VALIDASI: Hanya item yang valid yang bisa dikombinasikan
            if (!RecipeManager.isValidCombination(itemHand, isiPiring)) {
                log("FAIL", "Kombinasi " + itemHand.getName() + " dan " + isiPiring.getName() + " tidak valid.");
                return;
            }
            
            // Cari hasil (bisa Dish bernama, atau Mixed Dish, atau null jika tidak ada)
            Dish hasil = RecipeManager.findMatch(itemHand, isiPiring);
            
            if (hasil != null) {
                piring.setFood(hasil); // Menggunakan setFood() alias
                chef.setInventory(null); 
                log("SUCCESS", "Assembly Berhasil: " + hasil.getName());
            } else {
                // Gagal Assembly: Seharusnya tidak terjadi jika isValidCombination true,
                // tapi log ini bisa menangkap kasus Mixed Dish jika RecipeManager mengembalikan null
                log("FAIL", "Gagal Assembly: Item di tangan tidak ditambahkan.");
                // Catatan: Item di tangan Chef TIDAK hilang agar Chef bisa membuangnya.
            }
        }
    }

    /**
     * Getter untuk item yang diletakkan di atas Assembly Station (Keperluan GUI).
     */
    public Item getPlacedItem() { return placedItem; }
}