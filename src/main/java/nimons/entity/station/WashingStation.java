package nimons.entity.station;

import java.util.Stack;

import nimons.core.GameConfig;
import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate; 

/**
 * WashingStation (W): Menangani pencucian piring kotor di SINK.
 * Clean plates akan dikirim ke Rack station.
 * Constraint: Menggunakan Busy State (Chef di-freeze selama durasi mencuci).
 */
public class WashingStation extends Station {
    
    private static WashingStation activeWashingStation; // Track which washing station is active
    private Rack outputRackReference; 

    private Stack<Plate> dirtyPlates; 
    
    private Plate plateInWash; 
    private float currentProgress = 0;
    
    // --- FIELD UNTUK BUSY STATE ---
    private Chef currentWasher; 
    // ----------------------------
    
    private float logTimer = 0; // Mengembalikan timer log untuk update progress bar 


    public WashingStation(String name, Position position) {
        super(name, position);
        this.dirtyPlates = new Stack<>();
        
        log("INIT", "WashingStation initialized");
    }
    
    // --- Getters untuk Progress Bar (Progress 0.0 - 1.0) ---
    
    @Override
    public float getProgressRatio() {
        // Mengembalikan progress ratio (0.0 - 1.0) berdasarkan currentProgress
        if (GameConfig.WASHING_REQUIRED_TIME_MS == 0 || plateInWash == null || currentWasher == null) {
            return 0.0f;
        }
        return Math.min(1.0f, currentProgress / GameConfig.WASHING_REQUIRED_TIME_MS);
    }
    
    @Override
    public boolean isActive() {
        // Aktif jika ada progress (bahkan saat paused), belum selesai
        return currentProgress > 0 && currentProgress < GameConfig.WASHING_REQUIRED_TIME_MS;
    }
    // -------------------------------------------------------
    
    public void setOutputRack(Rack rack) {
        this.outputRackReference = rack;
        log("INIT", "Sink linked to Rack at " + rack.getPosition());
    }
    
    /**
     * Update loop: Memajukan timer pencucian hanya jika Chef sedang Busy.
     * Chef dapat bergerak, progress akan pause tapi tidak hilang.
     */
    @Override
    public void update(long deltaTime) {
        // Timer hanya berjalan jika ini adalah active station DAN Chef sedang mencuci DAN busy
        if (this == activeWashingStation && currentWasher != null && currentWasher.isBusy() && plateInWash != null) {
            currentProgress += deltaTime;
            
            // Log progress (optional, bisa dihapus jika tidak ingin ada log per detik)
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
        // Jika chef bergerak (not busy), progress tetap ada tapi tidak bertambah
    }

    /**
     * Mengatur interaksi Chef dengan Washing Station (SINK/RACK).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // SCENARIO 4: PAUSE CUCI (Chef dapat bergerak, progress tetap)
        if (chef.isBusy() && chef == currentWasher) {
            chef.setBusy(false);
            // Clear activeWashingStation so chef can resume at this station later
            if (activeWashingStation == this) {
                activeWashingStation = null;
            }
            // Keep currentWasher to allow resuming
            log("INFO", "PAUSED: Washing paused (Progress kept: " + (int)currentProgress + "ms).");
            return;
        }

        // SCENARIO 1: DROP DIRTY PLATE (Input)
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

        // SCENARIO 3: START/RESUME WASHING (di SINK)
        if (itemHand == null) {
            if (plateInWash == null && !dirtyPlates.isEmpty()) {
                plateInWash = dirtyPlates.pop();
                currentProgress = 0; // Reset progress for new plate 
            }
            
            if (plateInWash != null) {
                // Pastikan outputRackReference sudah diset sebelum mulai
                if (outputRackReference == null) {
                    log("ERROR", "Washing Rack is not connected!");
                    return;
                }
                
                // If switching to a different station, reset the previous station's progress
                if (activeWashingStation != null && activeWashingStation != this) {
                    activeWashingStation.resetWashingProgress();
                }
                activeWashingStation = this;
                
                // Start or Resume washing
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

    /**
     * Menyelesaikan proses cuci: Pindahkan piring ke RACK secara OTOMATIS dan melepas Chef.
     */
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
        
        // Reset state SINK
        this.currentProgress = 0;
        this.plateInWash = null;
        
        // Lepas Chef (Un-freeze)
        if (currentWasher != null) {
            currentWasher.setBusy(false);
            currentWasher = null;
        }
        
        // Clear active station
        if (activeWashingStation == this) {
            activeWashingStation = null;
        }
    }
    
    /** Helper to reset progress when chef switches to another washing station */
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