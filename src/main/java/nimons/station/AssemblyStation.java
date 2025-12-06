package nimons.station;

// DEPENDENCY ALERT:
// Class-class berikut masih menggunakan versi DUMMY dari package nimons.dummy. (internal)
import nimons.dummy.Chef;
import nimons.dummy.GameState;
import nimons.dummy.Item;
import nimons.dummy.Position;
import nimons.dummy.RecipeManager;

/**
 * AssemblyStation berfungsi sebagai tempat menaruh bahan (Drop)
 * dan menggabungkan dua bahan menjadi hidangan baru (Assembly).
 */
public class AssemblyStation extends Station {

    private Item placedItem; // Item yang sedang diletakkan di meja

    public AssemblyStation(String name, Position position) {
        super(name, position);
        this.placedItem = null;
    }

    /**
     * Mengatur interaksi Chef dengan Assembly Station.
     * Mendukung aksi: Pick Up, Drop, dan Merge/Assembly.
     */
    @Override
    public void onInteract(Chef chef, GameState state) {
        if (chef == null) return;

        Item itemHand = chef.getInventory();
        Item itemTable = this.placedItem;

        // 1. PICK UP: Ambil item dari meja jika tangan Chef kosong
        if (itemHand == null && itemTable != null) {
            chef.setInventory(itemTable);
            
            // Kosongkan meja
            placeItem(null); 
            
            System.out.println("[DEBUG] " + name + ": Chef mengambil " + chef.getInventory().getName());
            return;
        }

        // 2. DROP: Taruh item ke meja jika meja kosong
        if (itemHand != null && itemTable == null) {
            placeItem(itemHand);
            chef.setInventory(null);
            
            System.out.println("[DEBUG] " + name + ": Chef menaruh " + this.placedItem.getName());
            return;
        }

        // 3. ASSEMBLY: Lakukan penggabungan jika tangan dan meja sama-sama berisi item
        if (itemHand != null && itemTable != null) {
            processAssembly(chef, itemHand, itemTable);
        }
    }

    /**
     * Logika penggabungan item menggunakan RecipeManager.
     * Jika sukses, item di meja berubah dan item di tangan hilang.
     */
    private void processAssembly(Chef chef, Item itemHand, Item itemTable) {
        // Logika pencocokan resep ke RecipeManager
        Item hasil = RecipeManager.getResult(itemHand, itemTable);

        if (hasil != null) {
            // Berhasil: Update item meja menjadi hasil masakan
            placeItem(hasil);
            
            // Kosongkan tangan chef
            chef.setInventory(null);
            
            System.out.println("[SUCCESS] Assembly Berhasil: " + hasil.getName());
        } else {
            // Gagal: Resep tidak cocok
            System.out.println("[FAIL] Resep tidak valid: " + itemHand.getName() + " + " + itemTable.getName());
        }
    }

    public Item getPlacedItem() {
        return placedItem;
    }

    /**
     * Menempatkan item di atas station.
     */
    public void placeItem(Item item) {
        this.placedItem = item;
    }
}