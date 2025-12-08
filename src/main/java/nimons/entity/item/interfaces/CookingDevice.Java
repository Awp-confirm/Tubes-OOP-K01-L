package nimons.entity.item.interfaces;

public interface CookingDevice {

    boolean isPortable();

    int capacity();

    boolean canAccept(Preparable ingredient);

    void addIngredient(Preparable ingredient);

    void startCooking();

    void update(float deltaTime);

    void reset();
}
