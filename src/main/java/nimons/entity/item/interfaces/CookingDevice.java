package nimons.entity.item.interfaces;

import nimons.exceptions.InvalidIngredientStateException;
import nimons.exceptions.StationFullException;

public interface CookingDevice {

    boolean isPortable();

    int capacity();

    boolean canAccept(Preparable ingredient);

    void addIngredient(Preparable ingredient) throws StationFullException, InvalidIngredientStateException;

    void startCooking();

    void update(long deltaTime);

    void reset();
}
