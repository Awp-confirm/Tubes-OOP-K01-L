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

    // getters & setters
    public List<Preparable> getComponents() { 
        return components; 
    }

    public void setComponents(List<Preparable> components) { 
        this.components = components; 
    }

    // utility methods
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
        // Dish dianggap complete jika memiliki minimal 1 component
        return components != null && !components.isEmpty();
    }
}
