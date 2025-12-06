package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;

/**
 * CuttingStation berfungsi untuk memotong bahan mentah (Raw -> Chopped).
 * Station ini juga memiliki kemampuan Assembly (merakit) dan menaruh barang.
 */
public class CuttingStation extends Station {

    private Item placedItem;
    private int cuttingProgress; // Menyimpan progress potong (0 - 100)

    public CuttingStation(String name, Position position) {
        super(name, position);
        this.placedItem = null;
        this.cuttingProgress = 0;
    }

    /**
     * Mengatur interaksi Chef dengan Cutting Station.
     * Mendukung aksi: Cutting (Prioritas), Pick Up, Drop, dan Assembly.
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;

        Item itemHand = chef.getInventory();
        Item itemTable = this.placedItem;

        // 1. CUTTING: Prioritas utama jika tangan kosong, ada bahan, dan bahan bisa dipotong
        if (itemHand == null && itemTable != null && isChoppable(itemTable)) {
            processCutting(chef);
            return;
        }

        // 2. PICK UP: Ambil item (Reset progress jika barang diambil setengah jalan)
        if (itemHand == null && itemTable != null) {
            chef.setInventory(itemTable);
            
            // Kosongkan meja dan reset progress
            placeItem(null);
            this.cuttingProgress = 0;
            
            System.out.println("[DEBUG] " + name + ": Chef mengambil " + chef.getInventory().getName());
            return;
        }

        // 3. DROP: Taruh item ke meja (Reset progress untuk barang baru)
        if (itemHand != null && itemTable == null) {
            placeItem(itemHand);
            chef.setInventory(null);
            this.cuttingProgress = 0;
            
            System.out.println("[DEBUG] " + name + ": Chef menaruh " + this.placedItem.getName());
            return;
        }

        // 4. ASSEMBLY: Gabungkan bahan jika tangan dan meja terisi
        if (itemHand != null && itemTable != null) {
            processAssembly(chef, itemHand, itemTable);
        }
    }

    /**
     * Logika memotong dengan simulasi Progress Bar & Busy State menggunakan Thread Sleep.
     */
    private void processCutting(Chef chef) {
        System.out.println("[ACTION] Chef mulai memotong " + placedItem.getName() + "...");
        
        try {
            // Loop simulasi progres: Bertambah 20% setiap iterasi (0.5 detik)
            while (cuttingProgress < 100) {
                Thread.sleep(500); // Simulasi waktu kerja (Busy State)
                cuttingProgress += 20;
                System.out.println(">> Cutting Progress: " + cuttingProgress + "%");
            }

            // Jika progress mencapai 100%, ubah item menjadi Chopped
            if (cuttingProgress >= 100) {
                finishCutting();
            }

        } catch (InterruptedException e) {
            System.out.println("[ERROR] Proses memotong terganggu!");
        }
    }

    /**
     * Mengubah item menjadi bentuk terpotong (Chopped) menggunakan RecipeManager.
     */
    private void finishCutting() {
        // TODO: Implement recipe matching using Recipe class
        Item hasilPotong = null; // RecipeManager.getChoppedResult(placedItem);

        if (hasilPotong != null) {
            System.out.println("[SUCCESS] " + placedItem.getName() + " berhasil dipotong menjadi " + hasilPotong.getName());
            placeItem(hasilPotong); // Update item di meja
        } else {
            System.out.println("[ERROR] Gagal memotong item (Resep tidak ditemukan).");
        }
        
        this.cuttingProgress = 0; // Reset progress setelah selesai
    }

    /**
     * Cek apakah item valid untuk dipotong (menggunakan RecipeManager).
     * Juga mengembalikan true jika proses potong sedang berlangsung (progress > 0).
     */
    private boolean isChoppable(Item item) {
        if (cuttingProgress > 0 && cuttingProgress < 100) return true;
        // TODO: Implement recipe matching using Recipe class
        return false; // RecipeManager.getChoppedResult(item) != null;
    }

    /**
     * Logika Assembly untuk Cutting Station (Delegasi ke RecipeManager).
     */
    private void processAssembly(Chef chef, Item itemHand, Item itemTable) {
        // TODO: Implement recipe matching using Recipe class
        Item hasil = null; // RecipeManager.getResult(itemHand, itemTable);

        if (hasil != null) {
            placeItem(hasil);
            chef.setInventory(null);
            this.cuttingProgress = 0; // Reset progress jika terjadi assembly
            System.out.println("[SUCCESS] Assembly di Cutting Board Berhasil: " + hasil.getName());
        } else {
            System.out.println("[FAIL] Tidak bisa menggabungkan bahan di Cutting Station.");
        }
    }

    public Item getPlacedItem() {
        return placedItem;
    }

    /**
     * Menempatkan item di atas Cutting Station.
     */
    public void placeItem(Item item) {
        this.placedItem = item;
    }
}