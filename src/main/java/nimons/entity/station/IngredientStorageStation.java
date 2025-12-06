package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Dish;
import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;
import nimons.entity.item.Item;

/**
 * IngredientStorageStation berfungsi sebagai tempat mengambil bahan mentah (Spawn).
 * Memiliki stok tak terbatas.
 * Station ini juga multifungsi: bisa untuk menaruh item dan merakit hidangan (Assembly).
 */
public class IngredientStorageStation extends Station {

    private Item placedItem;       // Barang yang diletakkan sementara (jika ada)
    private String ingredientName; // Nama bahan yang dihasilkan station ini

    public IngredientStorageStation(String name, Position position) {
        super(name, position);
        this.placedItem = null;
        // Parsing nama station untuk menentukan jenis bahan. Contoh: "Crate Rice" -> "Rice"
        this.ingredientName = name.replace("Crate ", "").replace("Storage ", "");
    }

    /**
     * Mengatur interaksi Chef dengan Storage Station.
     * Skenario: Ambil item tumpangan, Spawn bahan baru, Taruh item, dan Assembly.
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;

        Item itemHand = chef.getInventory();

        // 1. PICK UP PLACED ITEM: Prioritas mengambil barang yang sedang diletakkan/numpang
        if (itemHand == null && placedItem != null) {
            chef.setInventory(placedItem);
            placedItem = null;
            System.out.println("[DEBUG] " + name + ": Chef mengambil item tumpangan.");
            return;
        }

        // 2. SPAWN INGREDIENT: Ambil bahan baru jika station kosong & tangan kosong
        if (itemHand == null && placedItem == null) {
            Item newIng = spawnIngredient();
            chef.setInventory(newIng);
            System.out.println("[DEBUG] " + name + ": Chef mengambil bahan baru (" + newIng.getName() + ")");
            return;
        }

        // 3. DROP ITEM: Menaruh barang apapun ke station ini (Fungsi meja)
        if (itemHand != null && placedItem == null) {
            placedItem = itemHand;
            chef.setInventory(null);
            System.out.println("[DEBUG] " + name + ": Chef menaruh " + placedItem.getName());
            return;
        }

        // 4. ASSEMBLY: Jika membawa bahan & ada bahan di station, coba gabungkan
        if (itemHand != null && placedItem != null) {
            processAssembly(chef, itemHand, placedItem);
        }
    }

    /**
     * Logika Assembly (Merakit Hidangan) di atas Storage Station.
     * Sama seperti AssemblyStation, menggunakan helper resep internal sementara.
     */
    private void processAssembly(Chef chef, Item itemHand, Item itemTable) {
        Item hasil = checkRecipe(itemHand, itemTable);

        if (hasil != null) {
            // Berhasil: Item di station berubah jadi hasil masakan
            placedItem = hasil;
            chef.setInventory(null);
            System.out.println("[SUCCESS] Assembly di Storage Berhasil: " + hasil.getName());
        } else {
            System.out.println("[FAIL] Tidak bisa menggabungkan item di sini.");
        }
    }

    /**
     * Helper sementara untuk cek resep (Hardcoded) agar Milestone 2 jalan.
     */
    private Item checkRecipe(Item a, Item b) {
        String nA = a.getName();
        String nB = b.getName();
        
        // Menggunakan constructor Dish(id, name, components)
        if (check(nA, nB, "Roti", "Daging")) return new Dish("D-BURGER", "Burger", null);
        if (check(nA, nB, "Nasi", "Nori")) return new Dish("D-SUSHI", "NasiNori", null);
        
        return null;
    }

    private boolean check(String a, String b, String t1, String t2) {
        return (a.equalsIgnoreCase(t1) && b.equalsIgnoreCase(t2)) || (a.equalsIgnoreCase(t2) && b.equalsIgnoreCase(t1));
    }

    /**
     * Membuat object Ingredient baru.
     * Menggunakan Anonymous Class karena Ingredient bersifat Abstract.
     */
    private Item spawnIngredient() {
        // ID Dummy, Nama sesuai station, State RAW
        return new Ingredient("I-" + ingredientName, ingredientName, IngredientState.RAW) {
            @Override public boolean canBeChopped() { return true; }
            @Override public boolean canBeCooked() { return true; }
            @Override public boolean canBePlacedOnPlate() { return true; }
            
            @Override 
            public void chop() {
                // Logika dummy change state
                this.setName(this.getName() + " (Chopped)");
            }
            
            @Override 
            public void cook() {
                // Logika dummy change state
                this.setName(this.getName() + " (Cooked)");
            }
        };
    }

    public Item getPlacedItem() {
        return placedItem;
    }

    public void setPlacedItem(Item placedItem) {
        this.placedItem = placedItem;
    }
}