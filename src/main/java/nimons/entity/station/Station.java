package nimons.entity.station;

import java.util.ArrayList;
import java.util.List;

import nimons.entity.chef.Chef; 
import nimons.entity.common.Position;
import nimons.entity.item.Dish;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import nimons.entity.item.interfaces.Preparable;
import nimons.gui.GameScreen; 

public abstract class Station {
    protected String name;
    protected Position position;

    public Station(String name, Position position) {
        this.name = name;
        this.position = position;
    }

    
    public abstract void onInteract(Chef chef);

    
        
    public void update(long deltaTime) {
        
    }

    public String getName() { return name; }
    public Position getPosition() { return position; }

    
        
    protected void log(String level, String message) {
        String stationType = this.getClass().getSimpleName(); 
        String formattedMessage = "[" + stationType + "] [" + level + "] " + message;
        
        System.out.println(formattedMessage);
        
        
        try {
            
            GameScreen.getInstance().addLog(formattedMessage); 
        } catch (Exception e) {
            
            System.err.println("CRITICAL ERROR: Failed to add log to GUI: " + e.getMessage());
        }
    } 
    
    
    public float getProgressRatio() {
        return 0.0f; 
    }
    
    
        
    public boolean isActive() {
        return getProgressRatio() > 0.0f && getProgressRatio() < 1.0f;
    }
    
    
        
    protected boolean processPlating(Plate piring, Item itemToPlate) {
        if (itemToPlate == null) return false;

        
        if (piring.getFood() != null) { 
            log("FAIL", "PLATING REJECTED: Plate is full.");
            return false;
        }

        Dish dishSiapSaji;

        if (itemToPlate instanceof Dish) {
            
            dishSiapSaji = (Dish) itemToPlate;
        } else {
            
            List<Preparable> components = new ArrayList<>();
            if (itemToPlate instanceof Preparable) {
                components.add((Preparable) itemToPlate);
            }
            
            dishSiapSaji = new Dish("D-" + itemToPlate.getName(), itemToPlate.getName(), components); 
        }

        
        piring.setFood(dishSiapSaji); 
        log("SUCCESS", "PLATED: " + dishSiapSaji.getName() + " successfully placed on plate.");
        return true;
    }
}