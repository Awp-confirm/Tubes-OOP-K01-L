package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.interfaces.CookingDevice;

/**
 * TrashStation (T): Menangani pembuangan Item dan pembersihan Kitchen Utensil.
 * Logic utama: Menghapus item dari inventory Chef atau mereset Utensil.
 */
public class TrashStation extends Station {

    public TrashStation(String name, Position position) {
        super(name, position);
    }

    /**
     * Menangani interaksi Chef (Membuang Item/Membersihkan Utensil).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // Hanya beraksi jika Chef memegang Item
        if (itemHand != null) {
            
            // Scenario 1: Utensil (Panci, Wajan, dll.)
            if (itemHand instanceof KitchenUtensil) {
                KitchenUtensil utensil = (KitchenUtensil) itemHand;
                
                // Cek apakah Utensil berisi Item (kotor)
                if (utensil.getContents() != null && !utensil.getContents().isEmpty()) { 
                    utensil.getContents().clear(); // Hapus semua isi (bahan/dish)
                    
                    // Reset Status Masak (mematikan timer, dsb.)
                    if (utensil instanceof CookingDevice) {
                        ((CookingDevice) utensil).reset();
                    }
                    // Log representatif: Isi dibuang dan Utensil di-reset
                    log("SUCCESS", "CLEANED: Contents of " + utensil.getName() + " discarded and reset.");
                } else {
                    // Log representatif: Utensil sudah bersih
                    log("INFO", utensil.getName() + " is already clean.");
                }
                return; // Utensil tetap di tangan Chef setelah dibersihkan
            }

            // Scenario 2: Item Biasa (Ingredient, Dish, Plate Kotor)
            String itemName = itemHand.getName();
            chef.setInventory(null);
            // Log representatif: Item dibuang
            log("SUCCESS", "DISCARDED: Item " + itemName + " thrown away.");
        } else {
            // Log representatif: Tangan kosong
            log("INFO", "HAND EMPTY: No item to discard.");
        }
    }
}