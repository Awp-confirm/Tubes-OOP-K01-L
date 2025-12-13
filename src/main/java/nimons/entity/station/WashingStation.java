package nimons.entity.station;

import java.util.Stack;

import nimons.core.GameConfig;
import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate; 

public class WashingStation extends Station {
    
    private static WashingStation activeWashingStation; 
    private Rack outputRackReference; 

    private Stack<Plate> dirtyPlates; 
    
    private Plate plateInWash; 
    private float currentProgress = 0;
    
    
    private Chef currentWasher; 
    
    
    private float logTimer = 0; 

    public WashingStation(String name, Position position) {
        super(name, position);
        this.dirtyPlates = new Stack<>();
        
        log("INIT", "WashingStation initialized");
    }
    
    
    
    @Override
    public float getProgressRatio() {
        
        if (GameConfig.WASHING_REQUIRED_TIME_MS == 0 || plateInWash == null || currentWasher == null) {
            return 0.0f;
        }
        return Math.min(1.0f, currentProgress / GameConfig.WASHING_REQUIRED_TIME_MS);
    }
    
    @Override
        
    public boolean isActive() {
        
        return currentProgress > 0 && currentProgress < GameConfig.WASHING_REQUIRED_TIME_MS;
    }
    
    
    public void setOutputRack(Rack rack) {
        this.outputRackReference = rack;
        log("INIT", "Sink linked to Rack at " + rack.getPosition());
    }
    
    
    @Override
        
    public void update(long deltaTime) {
        
        if (this == activeWashingStation && currentWasher != null && currentWasher.isBusy() && plateInWash != null) {
            currentProgress += deltaTime;
            
            
            if (logTimer >= GameConfig.WASHING_LOG_INTERVAL_MS) {
                int percentage = (int)(getProgressRatio() * 100);
                log("TIMER", "WASHING: Progress " + percentage + "%.");
                logTimer = 0;
            } else {
                logTimer += deltaTime;
            }
            
            if (currentProgress >= GameConfig.WASHING_REQUIRED_TIME_MS) {
                finishWashing();
            }
        }
        
    }

    
    @Override
        
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        
        if (chef.isBusy() && chef == currentWasher) {
            chef.setBusy(false);
            
            if (activeWashingStation == this) {
                activeWashingStation = null;
            }
            
            log("INFO", "PAUSED: Washing paused (Progress kept: " + (int)currentProgress + "ms).");
            return;
        }

        
        if (itemHand instanceof Plate) {
            Plate p = (Plate) itemHand;
            if (!p.isClean()) {
                dirtyPlates.push(p); 
                chef.setInventory(null);
                log("ACTION", "DROPPED: Dirty Plate into SINK (Queue: " + dirtyPlates.size() + ").");
            } else {
                log("FAIL", "WASH REJECTED: Plate is already clean.");
            }
            return;
        }

        
        if (itemHand == null) {
            if (plateInWash == null && !dirtyPlates.isEmpty()) {
                plateInWash = dirtyPlates.pop();
                currentProgress = 0; 
            }
            
            if (plateInWash != null) {
                
                if (outputRackReference == null) {
                    log("ERROR", "Washing Rack is not connected!");
                    return;
                }
                
                
                if (activeWashingStation != null && activeWashingStation != this) {
                    activeWashingStation.resetWashingProgress();
                }
                activeWashingStation = this;
                
                
                String action = (currentProgress > 0) ? "RESUMED" : "STARTED";
                this.currentWasher = chef;
                chef.setBusy(true); 
                log("ACTION", action + " WASHING: Chef cleaning plate (Progress: " + (int)currentProgress + "ms).");
                return;
            }
        }
        
        if (itemHand == null) {
            log("INFO", "No dirty plates available to start washing.");
        }
    }

    
        
    private void finishWashing() {
        if (plateInWash != null) {
            plateInWash.setClean(true); 
            
            if (outputRackReference != null) {
                outputRackReference.addCleanPlate(plateInWash); 
                log("SUCCESS", "CLEANED: Plate is clean and sent to RACK.");
            } else {
                log("ERROR", "CLEAN PLATE LOST: Output Rack is missing.");
            }
        }
        
        
        this.currentProgress = 0;
        this.plateInWash = null;
        
        
        if (currentWasher != null) {
            currentWasher.setBusy(false);
            currentWasher = null;
        }
        
        
        if (activeWashingStation == this) {
            activeWashingStation = null;
        }
    }
    
    
        
    private void resetWashingProgress() {
        if (currentWasher != null) {
            currentWasher.setBusy(false);
            currentWasher = null;
        }
        currentProgress = 0;
        plateInWash = null;
        log("INFO", "WASHING RESET: Progress cleared due to station switch.");
    }
    
}