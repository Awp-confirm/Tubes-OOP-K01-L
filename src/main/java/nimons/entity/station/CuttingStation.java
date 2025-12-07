package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import nimons.entity.item.interfaces.Preparable;
import nimons.entity.item.IngredientState; // Import yang benar

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
     * Menangani progress timer pemotongan.
     */
    @Override
    public void update(long deltaTime) {
        // Logic Timer Berjalan: Hanya jika ada Chef yang berinteraksi dan barangnya Preparable.
        if (currentCutter != null && placedItem instanceof Preparable) {
            currentProgress += deltaTime;

            if (currentProgress >= REQUIRED_TIME) {
                // 1. Ubah state bahan (RAW -> CHOPPED)
                ((Preparable) placedItem).chop();
                
                // 2. Lepaskan Chef (Un-freeze)
                currentCutter.setBusy(false);
                currentCutter = null;
                currentProgress = 0; // Reset progress
                
                log("SUCCESS", "Item selesai dipotong: " + placedItem.getName());
            }
        }
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
            // Jika plating sukses (piring ada isinya), hapus item dari meja
            if (((Plate)itemHand).getFood() != null) placedItem = null;
            return;
        }

        // SCENARIO 2: TARUH ITEM (Drop)
        // Hand: Item, Table: Empty
        if (itemHand != null && placedItem == null) {
            log("ACTION", "Menaruh " + itemHand.getName());
            placedItem = itemHand;
            chef.setInventory(null);
            currentProgress = 0; // Reset progress untuk item baru
            return;
        }
        
        // SCENARIO 3: AKSI MEMOTONG (Chop) - PRIORITAS TINGGI
        // Syarat: Tangan kosong, Item ada, dan Item masih RAW
        if (itemHand == null && placedItem instanceof Preparable) {
            Preparable p = (Preparable) placedItem;
            
            // Validasi: Hanya boleh memotong bahan mentah (RAW)
            if (p.getState() == IngredientState.RAW) {
                // START CUTTING / RESUME
                this.currentCutter = chef;
                chef.setBusy(true); // BEKUKAN CHEF (Busy State)
                log("ACTION", "Mulai memotong...");
                return; // Keluar dari method agar tidak langsung Pick Up
            } else {
                // Jika sudah Cooked/Chopped, Chef tidak melakukan apa-apa (akan jatuh ke Pick Up)
                log("INFO", "Item ini tidak perlu dipotong.");
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
            chef.setBusy(false);
            currentCutter = null;
            log("INFO", "Berhenti memotong (Progress tersimpan).");
        }
    }
    
    /**
     * Getter untuk item yang diletakkan di atas Cutting Station (Keperluan GUI).
     */
    public Item getPlacedItem() { return placedItem; }
}