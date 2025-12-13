package nimons.entity.order.recipes;

import java.util.Arrays;
import java.util.List;

import nimons.entity.item.IngredientState;
import nimons.entity.item.ingredient.Fish;
import nimons.entity.item.ingredient.Nori;
import nimons.entity.item.ingredient.Rice;
import nimons.entity.order.IngredientRequirement;
import nimons.entity.order.Recipe;

public class SakanaMaki extends Recipe {

    public SakanaMaki() {
        super("Sakana Maki", createRequirements());
    }

        
    private static List<IngredientRequirement> createRequirements() {
        return Arrays.asList(
            new IngredientRequirement(Nori.class, IngredientState.RAW),
            new IngredientRequirement(Rice.class, IngredientState.COOKED),
            new IngredientRequirement(Fish.class, IngredientState.CHOPPED)
        );
    }
}
