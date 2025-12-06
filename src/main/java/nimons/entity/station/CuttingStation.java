package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Dish;
import nimons.entity.item.Item;
import nimons.entity.item.interfaces.Preparable;

/**
 * CuttingStation berfungsi untuk memotong bahan mentah (Raw -> Chopped).
 * Menggunakan prinsip Polimorfisme: Station hanya memerintahkan item untuk .chop().
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
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;

        Item itemHand = chef.getInventory();
        Item itemTable = this.placedItem;

        // 1. CUTTING: Prioritas utama jika item di meja bisa dipotong
        if (itemHand == null && itemTable != null && isChoppable(itemTable)) {
            processCutting(chef);
            return;
        }

        // 2. PICK UP: Ambil item (Reset progress)
        if (itemHand == null && itemTable != null) {
            chef.setInventory(itemTable);
            placeItem(null);
            this.cuttingProgress = 0;
            System.out.println("[DEBUG] " + name + ": Chef mengambil " + chef.getInventory().getName());
            return;
        }

        // 3. DROP: Taruh item ke meja
        if (itemHand != null && itemTable == null) {
            placeItem(itemHand);
            chef.setInventory(null);
            this.cuttingProgress = 0;
            System.out.println("[DEBUG] " + name + ": Chef menaruh " + this.placedItem.getName());
            return;
        }

        // 4. ASSEMBLY: Gabungkan bahan (jika resep cocok)
        if (itemHand != null && itemTable != null) {
            processAssembly(chef, itemHand, itemTable);
        }
    }

    /**
     * Logika memotong dengan simulasi Busy State menggunakan Thread Sleep.
     */
    private void processCutting(Chef chef) {
        System.out.println("[ACTION] Chef mulai memotong " + placedItem.getName() + "...");
        
        try {
            while (cuttingProgress < 100) {
                Thread.sleep(500); // Simulasi waktu kerja
                cuttingProgress += 20;
                System.out.println(">> Cutting Progress: " + cuttingProgress + "%");
            }

            if (cuttingProgress >= 100) {
                finishCutting();
            }

        } catch (InterruptedException e) {
            System.out.println("[ERROR] Proses memotong terganggu!");
        }
    }

    /**
     * Menyelesaikan proses potong.
     * Memanggil method .chop() pada item (Perubahan state ditangani oleh Item itu sendiri).
     */
    private void finishCutting() {
        if (placedItem instanceof Preparable) {
            // POLIMORFISME: Kita tidak perlu tahu ini Cucumber atau Meat.
            // Cukup suruh dia memotong dirinya sendiri.
            ((Preparable) placedItem).chop();
            
            System.out.println("[SUCCESS] Item berhasil dipotong menjadi: " + placedItem.getName());
        } else {
            System.out.println("[ERROR] Item tidak mengimplementasikan Preparable!");
        }
        
        this.cuttingProgress = 0;
    }

    /**
     * Cek apakah item valid untuk dipotong menggunakan interface Preparable.
     */
    private boolean isChoppable(Item item) {
        // Jika sedang dalam progres potong, anggap valid
        if (cuttingProgress > 0 && cuttingProgress < 100) return true;

        // Cek apakah item punya kemampuan untuk dipotong
        if (item instanceof Preparable) {
            return ((Preparable) item).canBeChopped();
        }
        return false;
    }

    /**
     * Logika Assembly untuk Cutting Station.
     */
    private void processAssembly(Chef chef, Item itemHand, Item itemTable) {
        // Logika resep internal sementara (sama seperti AssemblyStation)
        Item hasil = checkRecipe(itemHand, itemTable);

        if (hasil != null) {
            placeItem(hasil);
            chef.setInventory(null);
            this.cuttingProgress = 0;
            System.out.println("[SUCCESS] Assembly di Cutting Board Berhasil: " + hasil.getName());
        } else {
            System.out.println("[FAIL] Tidak bisa menggabungkan bahan di Cutting Station.");
        }
    }

    private Item checkRecipe(Item a, Item b) {
        String nA = a.getName();
        String nB = b.getName();
        if (check(nA, nB, "Roti", "Daging")) return new Dish("D-BURGER", "Burger", null);
        if (check(nA, nB, "Nasi", "Nori")) return new Dish("D-SUSHI", "NasiNori", null);
        return null;
    }

    private boolean check(String a, String b, String t1, String t2) {
        return (a.equalsIgnoreCase(t1) && b.equalsIgnoreCase(t2)) || (a.equalsIgnoreCase(t2) && b.equalsIgnoreCase(t1));
    }

    public Item getPlacedItem() {
        return placedItem;
    }

    public void placeItem(Item item) {
        this.placedItem = item;
    }
}