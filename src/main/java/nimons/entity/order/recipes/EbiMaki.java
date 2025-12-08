package nimons.entity.order.recipes;

import java.util.Arrays;
import java.util.List;

import nimons.entity.item.IngredientState;
import nimons.entity.item.ingredient.Nori;
import nimons.entity.item.ingredient.Rice;
import nimons.entity.item.ingredient.Shrimp;
import nimons.entity.order.IngredientRequirement;
import nimons.entity.order.Recipe;

public class EbiMaki extends Recipe {

    public EbiMaki() {
        super("Ebi Maki", createRequirements());
    }

    private static List<IngredientRequirement> createRequirements() {
        return Arrays.asList(
            new IngredientRequirement(Nori.class, IngredientState.RAW),
            new IngredientRequirement(Rice.class, IngredientState.COOKED),
            new IngredientRequirement(Shrimp.class, IngredientState.COOKED)
        );
    }
}
