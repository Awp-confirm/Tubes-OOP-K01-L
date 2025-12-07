package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;

public class IngredientStorageStation extends Station {

    public IngredientStorageStation(String name, Position position) {
        super(name, position);
    }

    @Override
    public void onInteract(Chef chef) {
        // TODO: Implement ingredient storage interaction logic
    }
}