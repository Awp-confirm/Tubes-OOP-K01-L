package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;

/**
 * CookingStation merepresentasikan kompor atau oven.
 * Station ini khusus berinteraksi dengan alat masak (Utensils) 
 * dan menjalankan proses memasak secara otomatis (Concurrency).
 */
public class CookingStation extends Station {

    private KitchenUtensil utensilsOnStation; // Menyimpan alat masak (Panci/Wajan) yang sedang berada di atas kompor

    public CookingStation(String name, Position position) {
        super(name, position);
        this.utensilsOnStation = null;
    }

    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;

        Item itemHand = chef.getInventory();

        // Skenario 1: Mengambil alat masak dari kompor (Pick Up)
        if (itemHand == null && utensilsOnStation != null) {
            chef.setInventory(utensilsOnStation);
            System.out.println("[DEBUG] Mengangkat " + utensilsOnStation.getName());
            
            this.utensilsOnStation = null;
            return;
        }

        // Skenario 2: Menaruh alat masak ke kompor (Place Utensil)
        if (itemHand instanceof KitchenUtensil && utensilsOnStation == null) {
            placeUtensils((KitchenUtensil) itemHand);
            chef.setInventory(null);
            
            System.out.println("[DEBUG] Menaruh " + utensilsOnStation.getName() + " di kompor.");
            return;
        }

        // Skenario 3: Memasukkan bahan masakan (Start Cooking)
        if (itemHand != null && utensilsOnStation != null) {
            processAddingIngredient(chef, itemHand);
        }
    }

    /**
     * Logika memasukkan bahan dan memicu thread memasak.
     */
    private void processAddingIngredient(Chef chef, Item ingredient) {
        // Validasi: Pastikan panci kosong sebelum memasak
        if (utensilsOnStation.getContents() != null && !utensilsOnStation.getContents().isEmpty()) {
            System.out.println("[FAIL] Alat masak sudah terisi!");
            return;
        }

        // Validasi: Item yang dimasukkan bukan alat masak lain
        if (ingredient instanceof KitchenUtensil) {
            System.out.println("[FAIL] Tidak bisa menumpuk alat masak!");
            return;
        }

        // Proses memasukkan bahan
        // TODO: Implement addIngredient method in KitchenUtensil or use setContents
        // utensilsOnStation.addIngredient(ingredient);
        chef.setInventory(null); 
        
        System.out.println("[ACTION] Memasukkan " + ingredient.getName() + " ke dalam " + utensilsOnStation.getName());

        // Pemicu Concurrency: Menjalankan thread memasak di background
        // TODO: Implement startCooking method in KitchenUtensil
        // utensilsOnStation.startCooking();
    }

    /**
     * Mengambil referensi alat masak yang ada di station.
     */
    public KitchenUtensil getUtensils() {
        return utensilsOnStation;
    }

    /**
     * Menempatkan alat masak baru di station.
     */
    public void placeUtensils(KitchenUtensil utensils) {
        this.utensilsOnStation = utensils;
    }
}