package nimons.entity.station;

import nimons.core.GameConfig;
import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.Plate;
import nimons.entity.item.interfaces.Preparable;

/**
 * CuttingStation (C): Menangani pemotongan bahan (RAW -> CHOPPED).
 * Stasiun ini berfungsi ganda sebagai Assembly/Holding Station.
 */
public class CuttingStation extends Station {

    private static CuttingStation activeStation; // Track which station is currently being used
    private Item placedItem; 
    private float currentProgress = 0;
    private Chef currentCutter; 

    public CuttingStation(String name, Position position) {
        super(name, position);
    }

    /**
     * Mengembalikan item yang diletakkan di atas stasiun (untuk keperluan GUI).
     */
    public Item getPlacedItem() { return placedItem; }

    /**
     * Memajukan progress timer pemotongan jika ada Chef yang berinteraksi.
     * Chef bisa bergerak, progress akan pause tapi tidak hilang.
     */
    @Override
    public void update(long deltaTime) {
        // Logic Timer Berjalan: Hanya jika ini adalah active station (atau belum ada active) DAN Chef busy
        if ((activeStation == null || this == activeStation) && currentCutter != null && currentCutter.isBusy() && placedItem instanceof Preparable) {
            Preparable p = (Preparable) placedItem;
            
            // Validasi: Pastikan item masih bisa dipotong.
            if (p.canBeChopped()) {
                currentProgress += deltaTime;
            } else {
                // Item sudah selesai atau tidak bisa dipotong lagi, hentikan timer
                finishCutting();
                return;
            }

            if (currentProgress >= GameConfig.CUTTING_REQUIRED_TIME_MS) {
                // 1. Ubah state bahan (RAW -> CHOPPED)
                p.chop();
                
                // 2. Selesaikan proses (Lepaskan Chef)
                finishCutting();
            }
        }
        // Jika chef bergerak (not busy), progress tetap ada tapi tidak bertambah
    }
    
    /** Helper untuk mereset state setelah pemotongan selesai/batal. */
    private void finishCutting() {
        if (currentCutter != null) {
            // LOG DIRAPIKAN
            log("SUCCESS", "CUTTING COMPLETE: " + placedItem.getName() + " finished chopping.");
            currentCutter.setBusy(false);
        } else {
            // LOG DIRAPIKAN
            log("INFO", "CUTTING STOPPED: Item reached final state.");
        }
        if (activeStation == this) {
            activeStation = null;
        }
        currentCutter = null;
        currentProgress = 0; 
    }
    
    /** Helper to reset progress when chef switches to another cutting station */
    private void resetProgress() {
        if (currentCutter != null) {
            currentCutter.setBusy(false);
            currentCutter = null;
        }
        currentProgress = 0;
        log("INFO", "CUTTING RESET: Progress cleared due to station switch.");
    }
    
    /**
     * Return progress ratio for rendering progress bar (0.0 to 1.0)
     */
    @Override
    public float getProgressRatio() {
        if (currentProgress > 0 && placedItem instanceof Preparable) {
            return Math.min(1.0f, currentProgress / GameConfig.CUTTING_REQUIRED_TIME_MS);
        }
        return 0.0f;
    }
    
    /**
     * Return true if cutting is in progress
     */
    @Override
    public boolean isActive() {
        return currentProgress > 0 && currentProgress < GameConfig.CUTTING_REQUIRED_TIME_MS;
    }


    /**
     * Menangani interaksi Chef (Plating, Drop, Cut, Pick Up).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // --- SKENARIO KRITIS: PLATING DARI UTENSIL $\rightarrow$ PIRING DI MEJA ---
        if (itemHand instanceof KitchenUtensil && placedItem instanceof Plate) {
            KitchenUtensil utensilHand = (KitchenUtensil) itemHand;
            Plate piringTable = (Plate) placedItem;
            
            // Blokir jika Chef sedang memotong
            if (currentCutter != null) { 
                log("INFO", "BLOCKED: Chef is busy chopping.");
                return; 
            }

            if (piringTable.getFood() == null && utensilHand.getContents() != null && !utensilHand.getContents().isEmpty()) {
                Preparable isiPreparable = utensilHand.getContents().iterator().next();
                
                // Cek state: Hanya bisa plating item yang sudah COOKED
                if (isiPreparable.getState() == nimons.entity.item.IngredientState.COOKED) {
                    Item isi = (Item) isiPreparable; 
                    
                    processPlating(piringTable, isi);
                    
                    if (piringTable.getFood() != null) { 
                        utensilHand.getContents().clear();
                        // LOG DIRAPIKAN
                        log("SUCCESS", "PLATED UTENSIL: Contents successfully plated.");
                    }
                } else {
                    // LOG DIRAPIKAN
                    log("INFO", "INGREDIENT CHECK: Content state (" + isiPreparable.getState().name() + ") is not COOKED.");
                }
            } else {
                log("FAIL", "Invalid state: Plate full, Utensil empty, or item mismatch.");
            }
            return;
        }
        // -----------------------------------------------------------------------------
        
        // SCENARIO 4: PAUSE CUTTING (Chef releases but progress kept)
        if (chef.isBusy() && chef == currentCutter) {
            currentCutter.setBusy(false); 
            // Clear activeStation so chef can resume at this station later
            if (activeStation == this) {
                activeStation = null;
            }
            // Don't reset currentCutter, keep progress
            log("INFO", "PAUSED: Chopping paused (Progress kept: " + (int)currentProgress + "ms). Can resume anytime.");
            return; 
        }

        // SCENARIO 3: PLATING/ASSEMBLY (Hand Plate $\rightarrow$ Table Item)
        if (itemHand instanceof Plate && placedItem != null) {
            processPlating((Plate) itemHand, placedItem);
            if (((Plate)itemHand).getFood() != null) placedItem = null; 
            return;
        }

        // SCENARIO 2: TARUH ITEM (Drop Hand $\rightarrow$ Table)
        if (itemHand != null && placedItem == null) {
            if (currentCutter != null) finishCutting(); 
            
            // LOG DIRAPIKAN
            log("ACTION", "DROPPED: " + itemHand.getName() + " placed on station.");
            placedItem = itemHand;
            chef.setInventory(null);
            currentProgress = 0;
            return;
        }
        
        // SCENARIO 1: AKSI MEMOTONG (Chop Start/Resume) - PRIORITAS TINGGI
        if (itemHand == null && placedItem instanceof Preparable) {
            Preparable p = (Preparable) placedItem;
            if (p.canBeChopped()) {
                // If switching to a different station, reset the previous station's progress
                if (activeStation != null && activeStation != this) {
                    activeStation.resetProgress();
                }
                activeStation = this;
                
                this.currentCutter = chef;
                chef.setBusy(true);
                if (currentProgress > 0) {
                    log("INFO", "RESUMED: Chopping resumed from " + (int)currentProgress + "ms.");
                } else {
                    log("ACTION", "STARTED: Chopping " + placedItem.getName() + "...");
                }
                return; 
            } else {
                // LOG DIRAPIKAN
                log("INFO", placedItem.getName() + " cannot be chopped or is already done.");
            }
        }
        
        // SCENARIO 0: AMBIL ITEM (Pick Up Table $\rightarrow$ Hand)
        if (itemHand == null && placedItem != null && currentCutter == null) {
            // LOG DIRAPIKAN
            log("ACTION", "TAKEN: " + placedItem.getName() + " picked up.");
            chef.setInventory(placedItem);
            placedItem = null;
            return;
        }

        log("INFO", "Invalid interaction scenario.");
    }
}