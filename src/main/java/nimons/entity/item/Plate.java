package nimons.entity.item;

import nimons.entity.item.interfaces.Preparable;
import java.util.HashSet;
import java.util.Set;

public class Plate extends KitchenUtensil {

    private boolean clean;
    private Dish dish;

    public Plate() {
        // FIX #4: Panggil constructor yang sudah ada dengan 5 argumen untuk KitchenUtensil
        // Plate ID, Nama, Portable, Contents (null/empty), Capacity
        super("PLATE_01", "Plate", true, new HashSet<>(), 1); 
        this.clean = true; // Default bersih
        this.dish = null;
    }

    // Plate sekarang harus punya constructor yang sesuai dengan KitchenUtensil
    public Plate(String id, boolean clean, Dish dish) {
        // Panggil constructor default KitchenUtensil (yang kita buat di langkah 1)
        this(); 
        this.clean = clean;
        this.dish = dish;
    }

    public boolean isClean() { return clean; }
    public void setClean(boolean clean) { this.clean = clean; }
    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }

    // --- JEMBATAN (ADAPTER) AGAR BISA DIBACA ASSEMBLY STATION ---
    public Item getFood() {
        return this.getDish();
    }

    public void setFood(Item item) {
        if (item == null) {
            this.setDish(null);
        } else if (item instanceof Dish) {
            this.setDish((Dish) item);
        } else {
            System.out.println("[ERROR] Plate hanya menerima Dish!");
        }
    }
}