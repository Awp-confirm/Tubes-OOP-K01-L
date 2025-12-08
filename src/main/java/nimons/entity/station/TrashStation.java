package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.interfaces.CookingDevice;

/**
 * TrashStation (T) menangani pembuangan Item dan pembersihan Kitchen Utensil.
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
            
            // Scenario 1: Utensil (Panci, Wajan)
            if (itemHand instanceof KitchenUtensil) {
                KitchenUtensil panci = (KitchenUtensil) itemHand;
                
                // Pastikan contents tidak null sebelum dicek isEmpty()
                if (panci.getContents() != null && !panci.getContents().isEmpty()) { 
                    panci.getContents().clear(); // Hapus semua isi (bahan/dish)
                    
                    // Reset Status Masak (mematikan timer, dsb.)
                    if (panci instanceof CookingDevice) {
                        ((CookingDevice) panci).reset();
                    }
                    log("SUCCESS", "Isi " + panci.getName() + " dibuang dan di-reset.");
                } else {
                    log("INFO", panci.getName() + " sudah bersih.");
                }
                return; // Utensil tetap di tangan Chef setelah dibersihkan
            }

            // Scenario 2: Item Biasa (Ingredient, Dish, Plate Kotor)
            String itemName = itemHand.getName();
            chef.setInventory(null);
            log("SUCCESS", "Item " + itemName + " dibuang.");
        } else {
            log("INFO", "Tidak ada item di tangan untuk dibuang.");
        }
    }
}