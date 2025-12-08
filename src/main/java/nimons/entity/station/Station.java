package nimons.entity.station;

import java.util.ArrayList;
import java.util.List;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Dish;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import nimons.entity.item.interfaces.Preparable;

/**
 * Kelas abstrak dasar untuk semua Station di Nimonscooked.
 * Menyediakan logika umum seperti logger dan proses plating (Dish Wrapper).
 */
public abstract class Station {
    protected String name;
    protected Position position;

    public Station(String name, Position position) {
        this.name = name;
        this.position = position;
    }

    /**
     * Metode utama yang dipanggil ketika Chef berinteraksi (menekan tombol).
     */
    public abstract void onInteract(Chef chef);

    /**
     * Metode yang dipanggil setiap tick game untuk mengupdate timer/progress.
     */
    public void update(long deltaTime) {
        // Default: Subclasses yang memiliki timer (Cooking, Cutting, Washing) harus meng-override metode ini.
    }

    public String getName() { return name; }
    public Position getPosition() { return position; }

    /**
     * Helper method untuk logging yang konsisten di konsol/GUI.
     * @param type Tipe pesan (ACTION, SUCCESS, FAIL, INFO).
     * @param msg Isi pesan.
     */
    protected void log(String type, String msg) {
        // Contoh Output: [Cutting Station] [SUCCESS] Bahan terpotong!
        System.out.println("[" + this.name + "] [" + type + "] " + msg);
    }

    /**
     * Helper method untuk menangani logika Plating Universal (The Dish Wrapper).
     * Mengubah Ingredient menjadi Dish sebelum diletakkan di Piring.
     * MENGGUNAKAN ALIAS PLATE.GETFOOD()/SETFOOD().
     */
    protected void processPlating(Plate piring, Item itemToPlate) {
        if (itemToPlate == null) return;

        // 1. Cek Piring Penuh? MENGGUNAKAN GETFOOD() ALIAS
        if (piring.getFood() != null) { 
            log("FAIL", "Piring sudah penuh!");
            return;
        }

        // 2. Logic Wrapper / Konversi
        Dish dishSiapSaji;

        if (itemToPlate instanceof Dish) {
            // Item sudah berupa Dish, langsung casting.
            dishSiapSaji = (Dish) itemToPlate;
        } else {
            // Item adalah Ingredient (harus dibungkus jadi Dish baru).
            List<Preparable> components = new ArrayList<>();
            if (itemToPlate instanceof Preparable) {
                components.add((Preparable) itemToPlate);
            }
            // Buat Dish baru yang berisi ingredient ini sebagai komponen pertama.
            dishSiapSaji = new Dish("D-" + itemToPlate.getName(), itemToPlate.getName(), components);
        }

        // 3. Masukkan Dish ke Piring MENGGUNAKAN SETFOOD() ALIAS
        piring.setFood(dishSiapSaji); 
        log("SUCCESS", "Plating " + dishSiapSaji.getName() + " berhasil.");
    }
}