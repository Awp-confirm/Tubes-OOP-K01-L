package nimons.entity.item;

import nimons.entity.item.interfaces.CookingDevice;
import nimons.entity.item.interfaces.Preparable;
import nimons.entity.item.IngredientState; // Harus ada import IngredientState
import java.util.HashSet;

public class BoilingPot extends KitchenUtensil implements CookingDevice {

    private boolean isCooking = false;
    private float timer = 0;
    private final float COOK_TIME = 12000;
    private final float BURN_TIME = 24000;

    // FIX #2: Tambahkan constructor default
    public BoilingPot() {
        this("", 1);
    }
    
    // FIX #3: Hapus typo 'u' pada constructor
    public BoilingPot(String id, int capacity) {
        super(id, "Boiling Pot", true, new HashSet<>(), capacity); 
    }

    @Override
    public void update(long deltaTime) {
        if (isCooking && !getContents().isEmpty()) {
            timer += deltaTime;
            
            // Debug Log Timer
            if (timer % 1000 < 100) System.out.println(">> Boiling Pot: " + (int)(timer/1000) + " detik...");

            if (timer >= BURN_TIME) {
                 // Logika Gosong
                 for (Preparable p : getContents()) {
                     // Set state BURNED
                 }
                 System.out.println(">> [ALERT] MAKANAN GOSONG!");
            } else if (timer >= COOK_TIME) {
                 // Logika Matang
                 for (Preparable p : getContents()) {
                     p.cook(); // Ubah jadi COOKED
                 }
                 if (timer < COOK_TIME + 100) System.out.println(">> [TING!] Makanan MATANG!");
            }
        }
    }

    @Override
    public void reset() {
        this.isCooking = false;
        this.timer = 0;
        System.out.println(">> Boiling Pot di-reset.");
    }

    @Override
    public boolean isCooking() {
        return isCooking;
    }

    @Override
    public boolean canAccept(Preparable ingredient) {
        if (isCooking || getContents().size() >= getCapacity()) return false; 
        
        String name = ((Item)ingredient).getName().toUpperCase();
        
        if (name.contains("RICE") || name.contains("BERAS") || name.contains("PASTA")) {
            this.isCooking = true;
            return true;
        }
        return false;
    }
}