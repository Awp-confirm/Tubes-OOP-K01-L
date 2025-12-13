package nimons.entity.station;

import nimons.core.GameConfig;
import nimons.core.SoundManager;
import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.Plate;
import nimons.entity.item.interfaces.Preparable;

public class CuttingStation extends Station {

    private static CuttingStation activeStation; 
    private Item placedItem; 
    private float currentProgress = 0;
    private Chef currentCutter;
    private long lastChoppingSound = 0; 

    public CuttingStation(String name, Position position) {
        super(name, position);
    }

    
    public Item getPlacedItem() { return placedItem; }

    
    @Override
        
    public void update(long deltaTime) {
        
        if ((activeStation == null || this == activeStation) && currentCutter != null && currentCutter.isBusy() && placedItem instanceof Preparable) {
            Preparable p = (Preparable) placedItem;
            
            
            if (p.canBeChopped()) {
                currentProgress += deltaTime;
            } else {
                
                finishCutting();
                return;
            }

            if (currentProgress >= GameConfig.CUTTING_REQUIRED_TIME_MS) {
                
                p.chop();
                
                
                finishCutting();
            }
        }
        
    }
    
    
        
    private void finishCutting() {
        if (currentCutter != null) {
            
            log("SUCCESS", "CUTTING COMPLETE: " + placedItem.getName() + " finished chopping.");
            currentCutter.setBusy(false);
        } else {
            
            log("INFO", "CUTTING STOPPED: Item reached final state.");
        }
        if (activeStation == this) {
            activeStation = null;
        }
        currentCutter = null;
        currentProgress = 0; 
    }
    
    
        
    private void resetProgress() {
        if (currentCutter != null) {
            currentCutter.setBusy(false);
            currentCutter = null;
        }
        currentProgress = 0;
        log("INFO", "CUTTING RESET: Progress cleared due to station switch.");
    }
    
    
    @Override
    public float getProgressRatio() {
        if (currentProgress > 0 && placedItem instanceof Preparable) {
            return Math.min(1.0f, currentProgress / GameConfig.CUTTING_REQUIRED_TIME_MS);
        }
        return 0.0f;
    }
    
    
    @Override
        
    public boolean isActive() {
        return currentProgress > 0 && currentProgress < GameConfig.CUTTING_REQUIRED_TIME_MS;
    }

    
    @Override
        
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        
        if (itemHand instanceof KitchenUtensil && placedItem instanceof Plate) {
            KitchenUtensil utensilHand = (KitchenUtensil) itemHand;
            Plate piringTable = (Plate) placedItem;
            
            
            if (currentCutter != null) { 
                log("INFO", "BLOCKED: Chef is busy chopping.");
                return; 
            }

            if (piringTable.getFood() == null && utensilHand.getContents() != null && !utensilHand.getContents().isEmpty()) {
                Preparable isiPreparable = utensilHand.getContents().iterator().next();
                
                
                if (isiPreparable.getState() == nimons.entity.item.IngredientState.COOKED) {
                    Item isi = (Item) isiPreparable; 
                    
                    processPlating(piringTable, isi);
                    
                    if (piringTable.getFood() != null) { 
                        utensilHand.getContents().clear();
                        
                        log("SUCCESS", "PLATED UTENSIL: Contents successfully plated.");
                    }
                } else {
                    
                    log("INFO", "INGREDIENT CHECK: Content state (" + isiPreparable.getState().name() + ") is not COOKED.");
                }
            } else {
                log("FAIL", "Invalid state: Plate full, Utensil empty, or item mismatch.");
            }
            return;
        }
        
        
        
        if (chef.isBusy() && chef == currentCutter) {
            currentCutter.setBusy(false); 
            
            if (activeStation == this) {
                activeStation = null;
            }
            
            log("INFO", "PAUSED: Chopping paused (Progress kept: " + (int)currentProgress + "ms). Can resume anytime.");
            return; 
        }

        
        if (itemHand instanceof Plate && placedItem != null) {
            processPlating((Plate) itemHand, placedItem);
            if (((Plate)itemHand).getFood() != null) placedItem = null; 
            return;
        }        
        
        if (itemHand instanceof Preparable && placedItem instanceof Plate) {
            Plate plateTable = (Plate) placedItem;
            
            
            if (currentCutter != null) { 
                log("INFO", "BLOCKED: Chef is busy chopping.");
                return; 
            }
            
            if (processPlating(plateTable, itemHand)) {
                chef.setInventory(null);
                log("SUCCESS", "ASSEMBLY: " + itemHand.getName() + " added to plate on table.");
            } else {
                log("FAIL", "Cannot add " + itemHand.getName() + " to plate.");
            }
            return;
        }
        
        if (itemHand != null && placedItem == null) {
            if (currentCutter != null) finishCutting(); 
            
            
            log("ACTION", "DROPPED: " + itemHand.getName() + " placed on station.");
            placedItem = itemHand;
            chef.setInventory(null);
            currentProgress = 0;
            return;
        }
        
        
        if (itemHand == null && placedItem instanceof Preparable) {
            Preparable p = (Preparable) placedItem;
            if (p.canBeChopped()) {
                
                if (activeStation != null && activeStation != this) {
                    activeStation.resetProgress();
                }
                activeStation = this;
                
                this.currentCutter = chef;
                chef.setBusy(true);
                
                
                long currentTime = System.currentTimeMillis();
                if (currentProgress == 0) {
                    
                    
                    if (currentTime - lastChoppingSound >= 3000) {
                        SoundManager.getInstance().playSoundEffect("chopping");
                        lastChoppingSound = currentTime;
                    }
                    log("ACTION", "STARTED: Chopping " + placedItem.getName() + "...");
                } else {
                    log("INFO", "RESUMED: Chopping resumed from " + (int)currentProgress + "ms.");
                }
                return; 
            } else {
                
                log("INFO", placedItem.getName() + " cannot be chopped or is already done.");
            }
        }
        
        
        if (itemHand == null && placedItem != null && currentCutter == null) {
            
            log("ACTION", "TAKEN: " + placedItem.getName() + " picked up.");
            chef.setInventory(placedItem);
            placedItem = null;
            return;
        }

        log("INFO", "Invalid interaction scenario.");
    }
}