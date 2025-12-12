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

/**
 * ServingStation (S): Menangani penyajian Dish kepada pelanggan.
 * Logika utama: Validasi Order, mencatat skor, dan menunda pengembalian piring kotor (delay 10 detik).
 */
public class ServingStation extends Station {

    private OrderManager orderManager;
    
    /** Inner class untuk menahan piring yang sedang dalam antrean pengembalian (delay) */
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
    
    private List<PendingPlate> pendingReturns; // List antrean piring kotor yang tertunda pengembaliannya.

    public ServingStation(String name, Position position) {
        super(name, position);
        // OrderManager adalah Singleton (diasumsikan)
        this.orderManager = OrderManager.getInstance();
        this.pendingReturns = new ArrayList<>();
    }
    
    /**
     * Get GameState lazily (always get fresh reference)
     */
    private GameState getGameState() {
        GameScreen gameScreen = GameScreen.getInstance();
        return gameScreen != null ? gameScreen.getGameState() : null;
    }

    /**
     * Update loop: Memajukan timer dan menangani pengembalian piring kotor setelah delay selesai.
     */
    @Override
    public void update(long deltaTime) {
        // --- Ambil PlateStorage dari Singleton ---
        PlateStorageStation plateStorage = PlateStorageStation.getInstance();
        
        if (plateStorage == null || pendingReturns.isEmpty()) return;

        Iterator<PendingPlate> it = pendingReturns.iterator();
        while (it.hasNext()) {
            PendingPlate pp = it.next();
            pp.timer -= deltaTime;
            
            // Cek jika 10 detik telah berlalu
            if (pp.timer <= 0) {
                // Kembalikan ke storage menggunakan method internal di PSS
                plateStorage.addPlateToStack(pp.plate); 
                it.remove();
            }
        }
    }

    /**
     * Menangani interaksi Chef (Menyajikan Dish).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // Scenario: Chef menyerahkan Plate
        if (itemHand instanceof Plate) {
            Plate piring = (Plate) itemHand;
            Dish masakan = piring.getFood(); 

            // Validasi 1: Piring Kosong?
            if (masakan == null) {
                log("FAIL", "REJECTED: Plate is empty. Only plated dishes can be served.");
                // Don't reduce lives or process empty plate - just reject
                return;
            }
            
            log("ACTION", "SERVING: Presenting " + masakan.getName() + " to customer...");

            // Validasi 2: Cocok dengan Order?
            nimons.entity.order.Order completedOrder = orderManager.completeOrder(masakan);

            if (completedOrder != null) {
                // Order matched! Add score
                int reward = completedOrder.getReward();
                GameState gameState = getGameState();
                if (gameState != null && gameState.getScore() != null) {
                    gameState.getScore().addScore(reward);
                }
                log("SUCCESS", "ORDER CORRECT! +" + reward + " points. Order removed from queue.");
            } else {
                log("FAIL", "ORDER MISMATCH! Dish '" + masakan.getName() + "' is not in the order list.");
                // Play wrong sound effect when serving wrong dish
                SoundManager.getInstance().playSoundEffect("wrong");
                // Reduce lives for wrong serve
                GameState gameState = getGameState();
                if (gameState != null) {
                    System.out.println("[ServingStation] Calling loseLife(). Current lives: " + gameState.getLives());
                    gameState.loseLife();
                    System.out.println("[ServingStation] After loseLife(). Lives now: " + gameState.getLives());
                } else {
                    System.out.println("[ServingStation] ERROR: gameState is null, cannot reduce lives!");
                }
            }
            
            // --- LOGIKA PENGEMBALIAN PIRING KOTOR (LANGSUNG KE STACK) ---
            
            // 1. Bersihkan Dish dari piring (Plate.removeDish() membersihkan dish dan set status kotor)
            log("DEBUG", "Before removeDish: isClean=" + piring.isClean() + ", hasFood=" + (piring.getFood() != null));
            piring.removeDish(); 
            log("DEBUG", "After removeDish: isClean=" + piring.isClean() + ", hasFood=" + (piring.getFood() != null));

            // 2. Ambil PlateStorage dari Singleton SEBELUM hapus dari chef
            PlateStorageStation plateStorage = PlateStorageStation.getInstance();
            
            if (plateStorage == null) {
                log("ERROR", "Plate Storage not initialized! Dirty plate lost."); 
                chef.setInventory(null);
                return;
            }

            // 3. Hapus dari tangan Chef
            chef.setInventory(null); 
            log("DEBUG", "Chef inventory cleared");

            // 4. Langsung masukkan ke top of stack
            plateStorage.addPlateToStack(piring);
            log("SUCCESS", "DIRTY PLATE RETURNED: Plate (clean=" + piring.isClean() + ") added to PlateStorage.");
        } else {
            log("INFO", "Only plated items can be served here.");
        }
    }
}