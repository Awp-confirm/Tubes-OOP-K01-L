package nimons.entity.item;

import nimons.entity.item.interfaces.CookingDevice;
import nimons.entity.item.interfaces.Preparable;
import java.util.HashSet;

public class Oven extends KitchenUtensil implements CookingDevice {

    private boolean isCooking = false;
    private float timer = 0;
    
    public Oven() {
        this("", 1);
    }

    public Oven(String id, int capacity) {
        super(id, "Oven", true, new HashSet<>(), capacity); 
    }
    // ... (sisa method sama) ...

    @Override
    public void update(long deltaTime) {
        if (isCooking && !getContents().isEmpty()) {
            timer += deltaTime;
            if (timer % 1000 < 100) System.out.println(">> Oven: " + (int)(timer/1000) + " detik...");
            // Logic masak sama...
        }
    }

    @Override
    public void reset() {
        this.isCooking = false;
        this.timer = 0;
    }

    @Override
    public boolean isCooking() { return isCooking; }

    @Override
    public boolean canAccept(Preparable ingredient) {
        // Logic Oven: Terima apa saja dulu untuk sekarang (misal Adonan Pizza)
        this.isCooking = true;
        return true;
    }
}