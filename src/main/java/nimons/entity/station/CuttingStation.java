package nimons.entity.station;

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

    private Item placedItem; 
    private float currentProgress = 0;
    private final float REQUIRED_TIME = 3000; 
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
     */
    @Override
    public void update(long deltaTime) {
        // Logic Timer Berjalan: Hanya jika ada Chef yang berinteraksi.
        if (currentCutter != null && placedItem instanceof Preparable) {
            Preparable p = (Preparable) placedItem;
            
            // Validasi: Pastikan item masih bisa dipotong.
            if (p.canBeChopped()) {
                currentProgress += deltaTime;
            } else {
                // Item sudah selesai atau tidak bisa dipotong lagi, hentikan timer
                finishCutting();
                return;
            }

            if (currentProgress >= REQUIRED_TIME) {
                // 1. Ubah state bahan (RAW -> CHOPPED)
                p.chop();
                
                // 2. Selesaikan proses (Lepaskan Chef)
                finishCutting();
            }
        }
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
        currentCutter = null;
        currentProgress = 0; 
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
        
        // SCENARIO 4: BATALKAN/PAUSE CUTTING (Chef-in-Progress)
        if (chef.isBusy() && chef == currentCutter) {
            currentCutter.setBusy(false); 
            currentCutter = null; 
            // LOG DIRAPIKAN
            log("INFO", "PAUSED: Chopping stopped (Progress kept: " + (int)currentProgress + "ms).");
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
        
        // SCENARIO 1: AKSI MEMOTONG (Chop Start) - PRIORITAS TINGGI
        if (itemHand == null && placedItem instanceof Preparable) {
            Preparable p = (Preparable) placedItem;
            if (p.canBeChopped()) {
                this.currentCutter = chef;
                chef.setBusy(true); 
                // LOG DIRAPIKAN
                log("ACTION", "START CUTTING: Chef begins chopping " + placedItem.getName() + ".");
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

    /**
     * Mengimplementasikan getProgressRatio() (0.0 - 1.0) untuk cutting progress.
     */
    @Override
    public float getProgressRatio() {
        if (REQUIRED_TIME == 0 || placedItem == null || currentCutter == null) {
            return 0.0f;
        }
        return Math.min(1.0f, currentProgress / REQUIRED_TIME);
    }
    
    /**
     * Mengimplementasikan isActive() yang lebih spesifik untuk CuttingStation.
     */
    @Override
    public boolean isActive() {
        // Aktif jika ada Chef, ada item, dan sedang dipotong
        return currentCutter != null && placedItem != null && currentProgress < REQUIRED_TIME;
    }
}