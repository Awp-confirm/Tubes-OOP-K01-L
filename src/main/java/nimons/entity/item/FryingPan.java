package nimons.entity.item;

import nimons.entity.item.interfaces.CookingDevice;
import nimons.entity.item.interfaces.Preparable;
import nimons.entity.item.IngredientState;
import java.util.HashSet;

public class FryingPan extends KitchenUtensil implements CookingDevice {

    private boolean isCooking = false;
    private float timer = 0;
    private final float COOK_TIME = 12000; 
    private final float BURN_TIME = 24000; 

    // FIX: Pastikan ada constructor default
    public FryingPan() {
        this("", 1);
    }

    public FryingPan(String id, int capacity) {
        super(id, "Frying Pan", true, new HashSet<>(), capacity);
    }
    // ... (sisa method sama) ...

    @Override
    public void update(long deltaTime) {
        if (isCooking && !getContents().isEmpty()) {
            timer += deltaTime;
            if (timer % 1000 < 100) System.out.println(">> Frying Pan: " + (int)(timer/1000) + " detik...");
            
            if (timer >= COOK_TIME && timer < BURN_TIME) {
                 for (Preparable p : getContents()) p.cook();
            }
        }
    }

    @Override
    public void reset() {
        this.isCooking = false;
        this.timer = 0;
        System.out.println(">> Frying Pan di-reset.");
    }

    @Override
    public boolean isCooking() { return isCooking; }

    @Override
    public boolean canAccept(Preparable ingredient) {
        // GDD: Frying Pan hanya untuk Ingredient CHOPPED
        if (ingredient.getState() == IngredientState.CHOPPED) {
            this.isCooking = true;
            return true;
        }
        return false;
    }
}