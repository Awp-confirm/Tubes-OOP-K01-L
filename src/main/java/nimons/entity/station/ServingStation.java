package nimons.entity.station;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nimons.core.GameConfig;
import nimons.core.SoundManager;
import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Dish;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import nimons.gui.GameScreen;
import nimons.logic.GameState;
import nimons.logic.order.OrderManager;

public class ServingStation extends Station {

    private OrderManager orderManager;
    
    
    private static class PendingPlate {
        final Plate plate;
        float timer;
        
        PendingPlate(Plate plate) { 
            this.plate = plate; 
            this.timer = GameConfig.PLATE_RETURN_DELAY_MS; 
        }
        
        boolean isReadyToReturn() {
            return timer <= 0;
        }
        
        void updateTimer(long deltaTime) {
            timer -= deltaTime;
        }
    }
    
    private List<PendingPlate> pendingReturns; 

    public ServingStation(String name, Position position) {
        super(name, position);
        
        this.orderManager = OrderManager.getInstance();
        this.pendingReturns = new ArrayList<>();
    }
    
    
    private GameState getGameState() {
        GameScreen gameScreen = GameScreen.getInstance();
        return gameScreen != null ? gameScreen.getGameState() : null;
    }

    
    @Override
        
    public void update(long deltaTime) {
        
        PlateStorageStation plateStorage = PlateStorageStation.getInstance();
        
        if (plateStorage == null || pendingReturns.isEmpty()) return;

        Iterator<PendingPlate> it = pendingReturns.iterator();
        while (it.hasNext()) {
            PendingPlate pp = it.next();
            pp.timer -= deltaTime;
            
            
            if (pp.timer <= 0) {
                
                plateStorage.addPlateToStack(pp.plate); 
                it.remove();
            }
        }
    }

    
    @Override
        
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        
        if (itemHand instanceof Plate) {
            Plate piring = (Plate) itemHand;
            Dish masakan = piring.getFood(); 

            
            if (masakan == null) {
                log("FAIL", "REJECTED: Plate is empty. Only plated dishes can be served.");
                
                return;
            }
            
            log("ACTION", "SERVING: Presenting " + masakan.getName() + " to customer...");

            
            nimons.entity.order.Order completedOrder = orderManager.completeOrder(masakan);

            if (completedOrder != null) {
                
                int reward = completedOrder.getReward();
                GameState gameState = getGameState();
                if (gameState != null && gameState.getScore() != null) {
                    gameState.getScore().addScore(reward);
                }
                log("SUCCESS", "ORDER CORRECT! +" + reward + " points. Order removed from queue.");
            } else {
                log("FAIL", "ORDER MISMATCH! Dish '" + masakan.getName() + "' is not in the order list.");
                
                SoundManager.getInstance().playSoundEffect("wrong");
                
                GameState gameState = getGameState();
                if (gameState != null) {
                    System.out.println("[ServingStation] Calling loseLife(). Current lives: " + gameState.getLives());
                    gameState.loseLife();
                    System.out.println("[ServingStation] After loseLife(). Lives now: " + gameState.getLives());
                } else {
                    System.out.println("[ServingStation] ERROR: gameState is null, cannot reduce lives!");
                }
            }
            
            
            
            
            log("DEBUG", "Before removeDish: isClean=" + piring.isClean() + ", hasFood=" + (piring.getFood() != null));
            piring.removeDish(); 
            log("DEBUG", "After removeDish: isClean=" + piring.isClean() + ", hasFood=" + (piring.getFood() != null));

            
            chef.setInventory(null); 
            log("DEBUG", "Chef inventory cleared");

            
            pendingReturns.add(new PendingPlate(piring));
            log("SUCCESS", "DIRTY PLATE QUEUED: Plate added to return queue. Will return in 10 seconds.");
        } else {
            log("INFO", "Only plated items can be served here.");
        }
    }
}