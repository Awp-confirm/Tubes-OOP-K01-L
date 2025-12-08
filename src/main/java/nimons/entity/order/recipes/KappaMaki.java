package nimons.entity.order.recipes;

import java.util.Arrays;
import java.util.List;

import nimons.entity.item.IngredientState;
import nimons.entity.item.ingredient.Cucumber;
import nimons.entity.item.ingredient.Nori;
import nimons.entity.item.ingredient.Rice;
import nimons.entity.order.IngredientRequirement;
import nimons.entity.order.Recipe;

public class KappaMaki extends Recipe {

    public KappaMaki() {
        super("Kappa Maki", createRequirements());
    }

    private static List<IngredientRequirement> createRequirements() {
        return Arrays.asList(
            new IngredientRequirement(Nori.class, IngredientState.RAW),
            new IngredientRequirement(Rice.class, IngredientState.COOKED),
            new IngredientRequirement(Cucumber.class, IngredientState.CHOPPED)
        );
    }
}
