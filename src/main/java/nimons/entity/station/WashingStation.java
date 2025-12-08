package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate; 

import java.util.Stack; 

/**
 * WashingStation (W) berfungsi untuk membersihkan piring kotor.
 * Logika utama: Busy State (Chef beku), Timer (3 detik), dan Progress yang tersimpan.
 */
public class WashingStation extends Station {

    private Stack<Plate> dirtyPlates; // Tumpukan piring kotor (Input)
    private Stack<Plate> cleanPlates; // Tumpukan piring bersih (Output)
    
    // Timer dan Progress
    private Plate plateInWash; // Piring yang sedang dicuci (dedicated slot)
    private float currentProgress = 0;
    private final float REQUIRED_TIME = 3000; // 3 Detik per piring
    private Chef currentWasher; // Menyimpan referensi Chef yang sedang mencuci

    public WashingStation(String name, Position position) {
        super(name, position);
        this.dirtyPlates = new Stack<>();
        this.cleanPlates = new Stack<>();
    }

    /**
     * Update loop: Menangani progress timer mencuci piring.
     */
    @Override
    public void update(long deltaTime) {
        // Hanya jalan jika ada Chef yang berinteraksi
        if (currentWasher != null && plateInWash != null) {
            currentProgress += deltaTime;
            
            // Optional: Log progress (misal setiap detik)
            // if (currentProgress % 1000 < 100) log("PROGRESS", "Washing... " + (int)(currentProgress/1000) + "s");

            if (currentProgress >= REQUIRED_TIME) {
                finishWashing();
            }
        }
    }

    /**
     * Mengatur interaksi Chef dengan Washing Station.
     * Skenario: Menaruh piring kotor, Mengambil piring bersih, dan Memulai/Menghentikan cuci.
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // SCENARIO 1: DROP DIRTY PLATE
        if (itemHand instanceof Plate) {
            Plate p = (Plate) itemHand;
            if (!p.isClean()) {
                dirtyPlates.push(p); // Piring kotor masuk antrean
                chef.setInventory(null);
                log("ACTION", "Menumpuk piring kotor. Antrean: " + dirtyPlates.size());
            } else {
                log("FAIL", "Piring sudah bersih. Tidak perlu dicuci!");
            }
            return;
        }

        // SCENARIO 2: PICK UP CLEAN PLATE
        if (itemHand == null && !cleanPlates.isEmpty() && currentWasher == null) {
            // Hanya bisa ambil jika tangan kosong dan proses cuci tidak sedang aktif/dimulai
            Plate p = cleanPlates.pop();
            chef.setInventory(p);
            log("ACTION", "Mengambil piring bersih. Sisa bersih: " + cleanPlates.size());
            return;
        }

        // SCENARIO 3: START/RESUME/CANCEL WASHING
        if (itemHand == null) {
            
            // a. Jika slot cuci kosong, muat piring dari tumpukan kotor
            if (plateInWash == null && !dirtyPlates.isEmpty()) {
                plateInWash = dirtyPlates.pop(); // Ambil dari tumpukan kotor
                // currentProgress tetap sama jika logic resume dipanggil (tapi karena pop, kita anggap baru)
            }
            
            // b. Mulai/Lanjut cuci jika ada piring di slot
            if (plateInWash != null) {
                // Set Busy State dan mulai timer di update()
                this.currentWasher = chef;
                chef.setBusy(true); 
                log("ACTION", "Chef mulai menggosok piring... (Progress tersimpan: " + (int)currentProgress + "ms)");
                return;
            }
        }
        
        // SCENARIO 4: BATALKAN/PAUSE CUCI (Safety - Interaksi ulang saat busy)
        if (chef.isBusy() && chef == currentWasher) {
            chef.setBusy(false);
            currentWasher = null;
            log("INFO", "Proses mencuci dihentikan (Progress tersimpan: " + (int)currentProgress + "ms).");
        }
    }

    /**
     * Menyelesaikan proses cuci: Pindahkan piring dari slot cuci ke tumpukan bersih.
     */
    private void finishWashing() {
        if (plateInWash != null) {
            plateInWash.setClean(true); // Ubah status jadi bersih
            cleanPlates.push(plateInWash); // Pindah ke tumpukan bersih
            
            log("SUCCESS", "Piring menjadi bersih!");
        }
        
        // Reset state
        this.currentProgress = 0;
        this.plateInWash = null;
        
        // Lepas Chef (Un-freeze)
        if (currentWasher != null) {
            currentWasher.setBusy(false);
            currentWasher = null;
        }
    }

    // Getter untuk status
    public int getDirtyCount() { return dirtyPlates.size(); }
    public int getCleanCount() { return cleanPlates.size(); }
}