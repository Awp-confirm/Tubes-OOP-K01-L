package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.interfaces.CookingDevice; 
import nimons.entity.item.interfaces.Preparable;   

/**
 * CookingStation merepresentasikan kompor atau oven.
 * Station ini khusus berinteraksi dengan alat masak (KitchenUtensil) 
 * dan menjalankan proses memasak secara otomatis (Concurrency).
 */
public class CookingStation extends Station {

    private KitchenUtensil utensilsOnStation; 

    public CookingStation(String name, Position position) {
        super(name, position);
        this.utensilsOnStation = null;
    }

    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;

        Item itemHand = chef.getInventory();

        // 1. Pick Up Utensil
        if (itemHand == null && utensilsOnStation != null) {
            chef.setInventory(utensilsOnStation);
            System.out.println("[DEBUG] Mengangkat " + utensilsOnStation.getName());
            this.utensilsOnStation = null;
            return;
        }

        // 2. Place Utensil
        if (itemHand instanceof KitchenUtensil && utensilsOnStation == null) {
            placeUtensils((KitchenUtensil) itemHand);
            chef.setInventory(null);
            System.out.println("[DEBUG] Menaruh " + utensilsOnStation.getName() + " di kompor.");
            return;
        }

        // 3. Start Cooking
        if (itemHand != null && utensilsOnStation != null) {
            processAddingIngredient(chef, itemHand);
        }
    }

    private void processAddingIngredient(Chef chef, Item ingredient) {
        // Validasi 1: Apakah alat masak penuh?
        // REVISI: Menggunakan getContents() (return Set) sesuai kode KitchenUtensil temanmu
        if (utensilsOnStation.getContents() != null && !utensilsOnStation.getContents().isEmpty()) {
            System.out.println("[FAIL] Alat masak sudah terisi!");
            return;
        }

        // Validasi 2: Apakah item bisa dimasak (Preparable)?
        if (!(ingredient instanceof Preparable)) {
            System.out.println("[FAIL] Item ini tidak bisa dimasak!");
            return;
        }

        // Validasi 3: Apakah alat masak mendukung fitur memasak (CookingDevice)?
        if (utensilsOnStation instanceof CookingDevice) {
            CookingDevice device = (CookingDevice) utensilsOnStation;

            // Masukkan bahan
            device.addIngredient((Preparable) ingredient);
            chef.setInventory(null);
            System.out.println("[ACTION] Memasukkan " + ingredient.getName() + " ke dalam " + utensilsOnStation.getName());

            // Nyalakan api (Concurrency)
            device.startCooking();
        } else {
            System.out.println("[FAIL] Alat ini bukan Cooking Device (mungkin piring?)");
        }
    }

    public KitchenUtensil getUtensils() {
        return utensilsOnStation;
    }

    public void placeUtensils(KitchenUtensil utensils) {
        this.utensilsOnStation = utensils;
    }
}