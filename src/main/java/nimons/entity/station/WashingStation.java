package nimons.entity.station;

import java.util.Stack;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate; 

/**
 * WashingStation (W): Menangani pencucian piring kotor.
 * Mode: SINK (Input/Cuci) dan RACK (Output/Simpan Bersih).
 * Constraint: Menggunakan Busy State (Chef di-freeze selama durasi mencuci).
 */
public class WashingStation extends Station {
    
    public enum WashingMode { SINK, RACK }
    private WashingMode mode; 
    private WashingStation outputRackReference; 

    private Stack<Plate> dirtyPlates; 
    private Stack<Plate> cleanPlates; 
    
    private Plate plateInWash; 
    private float currentProgress = 0;
    private final float REQUIRED_TIME = 3000; 
    
    // --- FIELD UNTUK BUSY STATE ---
    private Chef currentWasher; 
    // ----------------------------
    
    private float logTimer = 0; // Mengembalikan timer log untuk update progress bar
    private final float LOG_INTERVAL = 1000; 


    public WashingStation(String name, Position position, WashingMode mode) {
        super(name, position);
        this.mode = mode;
        this.dirtyPlates = new Stack<>();
        this.cleanPlates = new Stack<>();
        
        log("INIT", "Station mode: " + mode.toString());
    }
    
    // --- Getters untuk Progress Bar (Progress 0.0 - 1.0) ---
    
    @Override
    public float getProgressRatio() {
        // Mengembalikan progress ratio (0.0 - 1.0) berdasarkan currentProgress
        if (REQUIRED_TIME == 0 || plateInWash == null || currentWasher == null) {
            return 0.0f;
        }
        return Math.min(1.0f, currentProgress / REQUIRED_TIME);
    }
    
    @Override
    public boolean isActive() {
        // Aktif hanya jika Chef sedang Busy mencuci (progress > 0 dan belum selesai)
        return getProgressRatio() > 0.0f && getProgressRatio() < 1.0f;
    }
    // -------------------------------------------------------
    
    public WashingMode getMode() { return mode; }
    public void setMode(WashingMode newMode) { 
        this.mode = newMode;
        log("INIT", "Mode changed to: " + newMode.toString());
    }
    public void setOutputRack(WashingStation rack) {
        if (this.mode == WashingMode.SINK) {
            this.outputRackReference = rack;
            log("INIT", "Sink linked to Rack at " + rack.getPosition());
        } else {
            System.err.println("ERROR: Only SINK can be linked to Output Rack.");
        }
    }
    
    /**
     * Update loop: Memajukan timer pencucian jika Chef sedang Busy.
     */
    @Override
    public void update(long deltaTime) {
        if (mode != WashingMode.SINK) return;
        
        // Timer hanya berjalan jika Chef sedang mencuci
        if (currentWasher != null && plateInWash != null) {
            currentProgress += deltaTime;
            
            // Log progress (optional, bisa dihapus jika tidak ingin ada log per detik)
            if (logTimer >= LOG_INTERVAL) {
                int percentage = (int)(getProgressRatio() * 100);
                log("TIMER", "WASHING: Progress " + percentage + "%.");
                logTimer = 0;
            } else {
                logTimer += deltaTime;
            }
            
            if (currentProgress >= REQUIRED_TIME) {
                finishWashing();
            }
        }
    }

    /**
     * Mengatur interaksi Chef dengan Washing Station (SINK/RACK).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();
        
        if (mode == WashingMode.RACK) {
            // LOGIKA RACK (Output): Mengambil piring bersih
            if (itemHand == null && !cleanPlates.isEmpty()) { 
                Plate p = cleanPlates.pop();
                chef.setInventory(p);
                log("ACTION", "TAKEN: Clean Plate from RACK (Remaining: " + cleanPlates.size() + ").");
            } else {
                log("INFO", "Rack is empty or Chef's hands are full.");
            }
            return;
        }

        // --- LOGIKA SINK ---

        // SCENARIO 4: BATALKAN/PAUSE CUCI (Hanya jika Chef Busy di SINK ini)
        if (chef.isBusy() && chef == currentWasher) {
            chef.setBusy(false);
            currentWasher = null;
            log("INFO", "PAUSED: Washing stopped (Progress kept: " + (int)currentProgress + "ms).");
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
            }
            
            if (plateInWash != null) {
                // Pastikan outputRackReference sudah diset sebelum mulai
                if (outputRackReference == null) {
                    log("ERROR", "Washing Rack is not connected!");
                    return;
                }
                
                // Mulai Busy State (Chef freeze)
                this.currentWasher = chef;
                chef.setBusy(true); 
                log("ACTION", "START WASHING: Chef starts cleaning plate (Progress: " + (int)currentProgress + "ms).");
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
                outputRackReference.addCleanPlateToRack(plateInWash); 
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
    }
    
    /**
     * Method internal untuk menerima Plate Bersih (Hanya di RACK).
     */
    public void addCleanPlateToRack(Plate p) {
        if (mode == WashingMode.RACK && p.isClean()) {
            cleanPlates.push(p);
        } else {
            System.err.println("ERROR: Attempted to store wrong item type in Washing RACK.");
        }
    }
}