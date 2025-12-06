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
}
