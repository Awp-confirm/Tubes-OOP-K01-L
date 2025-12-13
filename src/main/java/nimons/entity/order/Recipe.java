package nimons.entity.order;

import java.util.List;

public class Recipe {

    private String name;
    private List<IngredientRequirement> requirements;

    public Recipe() {}

    public Recipe(String name, List<IngredientRequirement> requirements) {
        this.name = name;
        this.requirements = requirements;
    }

    
    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public List<IngredientRequirement> getRequirements() { 
        return requirements; 
    }

    public void setRequirements(List<IngredientRequirement> requirements) { 
        this.requirements = requirements; 
    }
}
