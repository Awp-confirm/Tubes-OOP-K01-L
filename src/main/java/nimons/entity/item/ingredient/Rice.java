package nimons.entity.item.ingredient;

import nimons.entity.item.Dish;
import nimons.entity.item.IngredientState; 
import nimons.entity.item.interfaces.Preparable;
import java.util.ArrayList;

public class Rice extends Dish implements Preparable {
    
    private IngredientState state; 

    public Rice() {
        this("Rice");
    }

    public Rice(String name) {
        super("RICE_01", name, new ArrayList<>()); 
        this.state = IngredientState.RAW; 
    }

    // --- IMPLEMENTASI ACTIONS ---

    @Override
    public void cook() {
        this.state = IngredientState.COOKED;
    }

    @Override
    public void chop() {
        // Walaupun logic game mungkin tidak membolehkan nasi dipotong,
        // method ini tetap harus ada (bisa dikosongkan atau ubah state)
        this.state = IngredientState.CHOPPED; 
    }

    @Override
    public IngredientState getState() { 
        return state; 
    }

    // --- IMPLEMENTASI CHECKS (VALIDATOR) ---

    @Override
    public boolean canBePlacedOnPlate() {
        return true; 
    }

    @Override
    public boolean canBeCooked() {
        // Hanya bisa dimasak kalau masih mentah
        return state == IngredientState.RAW;
    }

    // --- PERBAIKAN: METHOD YANG HILANG (canBeChopped) ---
    @Override
    public boolean canBeChopped() {
        // Logic Game: Apakah nasi boleh dipotong?
        // Biasanya Nasi itu DI-REBUS (Boil), bukan DI-POTONG.
        // Jadi kita return FALSE agar CuttingStation menolak Rice.
        return false; 
    }
    
    // --- Setter & Getter Lainnya ---

    public void setState(IngredientState state) {
        this.state = state;
    }

    @Override
    public String getName() {
        return super.getName() + " (" + state + ")";
    }
}