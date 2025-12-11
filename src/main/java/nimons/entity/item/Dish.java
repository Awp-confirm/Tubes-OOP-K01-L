package nimons.entity.item;

import java.util.List;

import nimons.entity.item.interfaces.Preparable;

public class Dish extends Item {

    private List<Preparable> components;

    public Dish() {}

    public Dish(String id, String name, List<Preparable> components) {
        super(id, name, true);
        this.components = components;
    }

    // getters & setters (tetap sama)
    public List<Preparable> getComponents() { 
        return components; 
    }

    public void setComponents(List<Preparable> components) { 
        this.components = components; 
    }

    // --- PERBAIKAN KRITIS: Menggunakan Setter Item Super Class ---
    public void setName(String newName) {
        // PENTING: Panggil setter dari super class (Item)
        super.setName(newName); 
    }
    // ------------------------------------------------


    /**
     * Mengecek apakah Ingredient dapat ditambahkan ke Dish yang sudah ada.
     */
    public boolean canAddIngredient(Ingredient ingredient) {
        return ingredient != null && ingredient.canBePlacedOnPlate();
    }
    
    /**
     * Menambahkan Ingredient baru ke Dish yang sedang dirakit.
     */
    public void addIngredient(Ingredient ingredient) {
        if (canAddIngredient(ingredient)) {
            addComponent(ingredient);
        }
    }
    
    // utility methods (tetap sama)
    public void addComponent(Preparable ingredient) {
        if (components != null && ingredient != null && ingredient.canBePlacedOnPlate()) {
            components.add(ingredient);
        }
    }

    public boolean removeComponent(Preparable ingredient) {
        return components != null && components.remove(ingredient);
    }

    public int getComponentCount() {
        return components != null ? components.size() : 0;
    }

    public boolean isEmpty() {
        return components == null || components.isEmpty();
    }

    public boolean isComplete() {
        return components != null && !components.isEmpty();
    }
}