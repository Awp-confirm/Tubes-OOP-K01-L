package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import nimons.entity.item.interfaces.Preparable;
// Import IngredientState tidak lagi diperlukan di sini karena logic dipindah ke Ingredient

/**
 * CuttingStation (C) menangani pemotongan bahan (RAW -> CHOPPED).
 * Berfungsi ganda sebagai Assembly/Holding Station.
 */
public class CuttingStation extends Station {

    private Item placedItem; // Bahan yang ada di atas meja
    private float currentProgress = 0;
    private final float REQUIRED_TIME = 3000; // Durasi Potong: 3 Detik
    private Chef currentCutter; // Referensi Chef yang sedang memotong (untuk Busy State)

    public CuttingStation(String name, Position position) {
        super(name, position);
    }

    /**
     * Getter untuk item yang diletakkan di atas Cutting Station (Keperluan GUI).
     */
    public Item getPlacedItem() { return placedItem; }

    /**
     * Menangani progress timer pemotongan.
     */
    @Override
    public void update(long deltaTime) {
        // Logic Timer Berjalan: Hanya jika ada Chef yang berinteraksi dan barangnya Preparable.
        if (currentCutter != null && placedItem instanceof Preparable) {
            Preparable p = (Preparable) placedItem;
            
            // Validasi: Pastikan item masih bisa dipotong saat update berjalan
            // (Mencegah error jika item sudah selesai di-chop di loop update sebelumnya)
            // Menggunakan canBeChopped() dari Ingredient adalah KUNCI.
            if (p.canBeChopped()) {
                currentProgress += deltaTime;
            } else {
                // Item sudah selesai atau tidak bisa dipotong lagi, hentikan timer
                finishCutting();
                return;
            }

            if (currentProgress >= REQUIRED_TIME) {
                // 1. Ubah state bahan (RAW -> CHOPPED) - Logic ada di Ingredient
                p.chop();
                
                // 2. Selesaikan proses (Lepaskan Chef)
                finishCutting();
            }
        }
    }
    
    /** Helper untuk mereset state setelah pemotongan selesai/batal */
    private void finishCutting() {
        if (currentCutter != null) {
            log("SUCCESS", "Item selesai dipotong: " + placedItem.getName());
            currentCutter.setBusy(false);
        } else {
            // Jika dipanggil dari update karena canBeChopped() false
            log("INFO", "Pemotongan berhenti: Item sudah mencapai state akhir.");
        }
        currentCutter = null;
        currentProgress = 0; // Reset progress untuk interaksi berikutnya
    }


    /**
     * Menangani interaksi Chef (Plating, Drop, Cut, Pick Up).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // SCENARIO 1: PLATING (Meja Potong sebagai Assembly Station)
        // Hand: Plate, Table: Item
        if (itemHand instanceof Plate && placedItem != null) {
            processPlating((Plate) itemHand, placedItem);
            // Jika plating sukses, hapus item dari meja
            // Note: Menggunakan getFood() karena Plate.java menggunakan alias getFood().
            if (((Plate)itemHand).getFood() != null) placedItem = null; 
            return;
        }

        // SCENARIO 2: TARUH ITEM (Drop)
        // Hand: Item, Table: Empty
        if (itemHand != null && placedItem == null) {
            // Jika sedang ada Chef yang memotong (dari sesi sebelumnya), hentikan dulu
            if (currentCutter != null) finishCutting(); 
            
            log("ACTION", "Menaruh " + itemHand.getName());
            placedItem = itemHand;
            chef.setInventory(null);
            currentProgress = 0; // Reset progress untuk item baru
            return;
        }
        
        // SCENARIO 3: AKSI MEMOTONG (Chop) - PRIORITAS TINGGI
        // Syarat: Tangan kosong, Item ada, Item dapat dipotong (Validasi Delegated)
        if (itemHand == null && placedItem instanceof Preparable) {
            Preparable p = (Preparable) placedItem;
            
            // Validasi: Delegasi ke Ingredient (Nori=false, Cucumber=true, Rice=false)
            if (p.canBeChopped()) {
                // START CUTTING / RESUME
                this.currentCutter = chef;
                chef.setBusy(true); // BEKUKAN CHEF (Busy State)
                log("ACTION", "Mulai memotong " + placedItem.getName() + "...");
                return; 
            } else {
                // Item tidak bisa dipotong (misal: Nori, atau Cucumber yang sudah CHOPPED)
                log("INFO", placedItem.getName() + " tidak dapat dipotong atau sudah dipotong.");
            }
        }
        
        // SCENARIO 4: AMBIL ITEM (Pick Up) - PRIORITAS RENDAH
        // Syarat: Tangan kosong, Meja ada barang, TIDAK SEDANG MEMOTONG
        if (itemHand == null && placedItem != null && currentCutter == null) {
            log("ACTION", "Mengambil " + placedItem.getName());
            chef.setInventory(placedItem);
            placedItem = null;
            return;
        }
        
        // SCENARIO 5: BATALKAN/PAUSE (Safety)
        // Jika Chef sedang busy karena memotong dan berinteraksi lagi (Spasi)
        if (chef.isBusy() && chef == currentCutter) {
            // Hentikan proses, progress tetap tersimpan
            log("INFO", "Berhenti memotong (Progress tersimpan: " + (int)currentProgress + "ms).");
            currentCutter.setBusy(false);
            currentCutter = null;
        }
    }
}