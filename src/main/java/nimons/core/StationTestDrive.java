package nimons.core;

import nimons.entity.chef.Chef;
import nimons.entity.chef.Direction; // Import Direction
import nimons.entity.common.Position;
import nimons.entity.item.BoilingPot;
import nimons.entity.item.Item;
import nimons.entity.station.*;

public class StationTestDrive {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== MULAI SIMULASI SUSHI KITCHEN ===");

        // 1. SETUP: Siapkan Aktor dan Station
        // REVISI: Menggunakan constructor lengkap (ID, Nama, Posisi, Arah)
        // Asumsi Direction adalah Enum (UP/DOWN/LEFT/RIGHT). Jika error, ganti null.
        Chef juna = new Chef("C001", "Chef Juna", new Position(0, 0), Direction.DOWN);
        
        IngredientStorageStation crateRice = new IngredientStorageStation("Crate Rice", new Position(0, 0));
        IngredientStorageStation crateNori = new IngredientStorageStation("Crate Nori", new Position(1, 0));
        IngredientStorageStation crateTimun = new IngredientStorageStation("Crate Cucumber", new Position(2, 0));
        
        CookingStation stove = new CookingStation("Kompor", new Position(3, 0));
        CuttingStation cuttingBoard = new CuttingStation("Talenan", new Position(4, 0));
        AssemblyStation mejaRakit = new AssemblyStation("Meja Assembly", new Position(5, 0));

        // Taruh Panci di Kompor (Manual setup)
        BoilingPot panci = new BoilingPot("Panci-1", 5);
        stove.placeUtensils(panci);

        System.out.println("\n--- TEST 1: MASAK NASI (THREADING) ---");
        // Ambil Beras
        crateRice.onInteract(juna); 
        System.out.println("Chef bawa: " + getNameSafe(juna.getInventory())); 

        // Taruh Beras di Kompor
        stove.onInteract(juna); 
        
        // Cek status panci
        System.out.println("Status Panci: Sedang masak? (Tunggu 4 detik...)");
        
        // Simulasi nunggu
        Thread.sleep(4000); 

        // Cek isi panci
        if (!panci.getContents().isEmpty()) {
            Item isiPanci = (Item) panci.getContents().iterator().next();
            System.out.println("Isi Panci sekarang: " + isiPanci.getName()); // Harusnya Nasi (Cooked)
            
            // Cheat: Ambil paksa dari panci
            juna.setInventory(isiPanci); 
            panci.getContents().clear(); 
            System.out.println("Chef mengambil: " + juna.getInventory().getName());
        }

        
        System.out.println("\n--- TEST 2: PERSIAPAN ASSEMBLY (BASE) ---");
        // Taruh Nasi Matang di Meja Rakit
        mejaRakit.onInteract(juna);
        System.out.println("Meja Rakit ada: " + getNameSafe(mejaRakit.getPlacedItem()));

        // Ambil Nori
        crateNori.onInteract(juna);
        System.out.println("Chef bawa: " + getNameSafe(juna.getInventory()));

        // Gabung Nori + Nasi (Assembly)
        mejaRakit.onInteract(juna);
        System.out.println("Hasil Assembly 1: " + getNameSafe(mejaRakit.getPlacedItem())); // Harusnya Sushi Base


        System.out.println("\n--- TEST 3: POTONG TIMUN (POLYMORPHISM) ---");
        juna.setInventory(null);

        // Ambil Timun
        crateTimun.onInteract(juna);
        System.out.println("Chef bawa: " + getNameSafe(juna.getInventory()));

        // Taruh di Cutting Board
        cuttingBoard.onInteract(juna);
        
        // Potong (Interact lagi)
        System.out.println("Proses memotong... (Tunggu simulasi)");
        cuttingBoard.onInteract(juna); // Trigger cut
        
        // Ambil hasil potong
        cuttingBoard.onInteract(juna); 
        System.out.println("Chef bawa hasil potong: " + getNameSafe(juna.getInventory())); 


        System.out.println("\n--- TEST 4: FINAL ASSEMBLY (KAPPA MAKI) ---");
        // Bawa Timun Potong ke Meja Rakit
        mejaRakit.onInteract(juna);
        
        String hasilAkhir = getNameSafe(mejaRakit.getPlacedItem());
        System.out.println(">>> HASIL AKHIR DI MEJA: " + hasilAkhir);
        
        if (hasilAkhir.equals("Kappa Maki")) {
            System.out.println("✅ TEST SUKSES! LOGIKA JALAN SEMUA!");
        } else {
            System.out.println("❌ TEST GAGAL! Resep belum cocok.");
        }
    }

    // Helper biar ga error NullPointer kalau item null
    private static String getNameSafe(Item item) {
        return (item != null) ? item.getName() : "Kosong";
    }
}