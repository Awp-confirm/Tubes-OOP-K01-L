package nimons.entity.item.factory;

import nimons.entity.item.Ingredient;
import nimons.entity.item.ingredient.Cucumber;
import nimons.entity.item.ingredient.Fish;
import nimons.entity.item.ingredient.Nori;
import nimons.entity.item.ingredient.Rice;
import nimons.entity.item.ingredient.Shrimp;

public class IngredientFactory {
    
    
    public enum IngredientType {
        FISH,
        SHRIMP,
        CUCUMBER,
        RICE,
        NORI
    }
    
    
        
    public static Ingredient createIngredient(IngredientType type) {
        switch (type) {
            case FISH:
                return new Fish();
            case SHRIMP:
                return new Shrimp();
            case CUCUMBER:
                return new Cucumber();
            case RICE:
                return new Rice();
            case NORI:
                return new Nori();
            default:
                throw new IllegalArgumentException("Unknown ingredient type: " + type);
        }
    }
    
    
        
    public static Ingredient createIngredient(String typeName) {
        try {
            IngredientType type = IngredientType.valueOf(typeName.toUpperCase());
            return createIngredient(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ingredient name: " + typeName);
        }
    }
    
    
    public static IngredientType[] getAvailableTypes() {
        return IngredientType.values();
    }
}
