package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate; 

import java.util.Stack; 

/**
 * WashingStation berfungsi untuk membersihkan piring kotor.
 * Menggunakan Stack untuk menumpuk piring kotor dan piring bersih.
 */
public class WashingStation extends Station {

    private Stack<Plate> dirtyPlates; // Tumpukan piring kotor (Input)
    private Stack<Plate> cleanPlates; // Tumpukan piring bersih (Output)
    private int washingProgress;      // Progress cuci (0-100)

    public WashingStation(String name, Position position) {
        super(name, position);
        this.dirtyPlates = new Stack<>();
        this.cleanPlates = new Stack<>();
        this.washingProgress = 0;
    }

    /**
     * Mengatur interaksi Chef dengan Washing Station.
     * Skenario: Menaruh piring kotor, Mengambil piring bersih, dan Mencuci.
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;

        Item itemHand = chef.getInventory();

        // 1. DROP DIRTY PLATE: Chef bawa piring kotor -> Taruh di tumpukan kotor
        if (itemHand instanceof Plate) {
            Plate p = (Plate) itemHand;
            if (!p.isClean()) {
                dirtyPlates.push(p);
                chef.setInventory(null);
                System.out.println("[DEBUG] Menumpuk piring kotor. Total antrean: " + dirtyPlates.size());
                return;
            }
        }

        // 2. PICK UP CLEAN PLATE: Chef tangan kosong & ada piring bersih -> Ambil
        if (itemHand == null && !cleanPlates.isEmpty()) {
            Plate p = cleanPlates.pop();
            chef.setInventory(p);
            System.out.println("[DEBUG] Mengambil piring bersih. Sisa bersih: " + cleanPlates.size());
            return;
        }

        // 3. WASHING PROCESS: Chef tangan kosong & ada piring kotor -> Mulai/Lanjut cuci
        // (Syarat: Tidak bisa mencuci jika tidak ada piring kotor)
        if (itemHand == null && !dirtyPlates.isEmpty()) {
            processWashing(chef);
        }
    }

    /**
     * Logika mencuci satu per satu dengan simulasi Busy State (Thread Sleep).
     * Progress dapat di-pause (tidak reset) jika interaksi terputus.
     */
    private void processWashing(Chef chef) {
        System.out.println("[ACTION] Mencuci piring... (Progress Awal: " + washingProgress + "%)");

        try {
            // Simulasi Busy State: Tahan program sejenak untuk simulasi kerja
            while (washingProgress < 100) {
                Thread.sleep(500); // Delay 0.5 detik
                washingProgress += 25; 
                System.out.println(">> Scrubbing... " + washingProgress + "%");
            }

            // Jika selesai mencuci
            if (washingProgress >= 100) {
                finishWashing();
            }

        } catch (InterruptedException e) {
            System.out.println("[ERROR] Proses mencuci terganggu!");
        }
    }

    /**
     * Menyelesaikan proses cuci: Pindahkan dari tumpukan kotor ke bersih.
     */
    private void finishWashing() {
        if (!dirtyPlates.isEmpty()) {
            Plate p = dirtyPlates.pop(); // Ambil dari tumpukan kotor
            
            p.setClean(true); // Ubah status jadi bersih
            
            cleanPlates.push(p); // Pindah ke tumpukan bersih
            
            System.out.println("[SUCCESS] Piring menjadi bersih! (Clean Stack: " + cleanPlates.size() + ")");
        }
        
        this.washingProgress = 0; // Reset progress untuk piring berikutnya
    }

    // Getter untuk jumlah tumpukan (bisa dipakai UI nanti)
    public int getDirtyCount() {
        return dirtyPlates.size();
    }

    public int getCleanCount() {
        return cleanPlates.size();
    }
}