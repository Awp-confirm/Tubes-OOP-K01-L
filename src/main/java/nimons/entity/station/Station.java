package nimons.entity.station;

import java.util.ArrayList;
import java.util.List;

import nimons.entity.chef.Chef; 
import nimons.entity.common.Position;
import nimons.entity.item.Dish;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import nimons.entity.item.interfaces.Preparable;
import nimons.gui.GameScreen; // Tambahkan import GameScreen

/**
 * Kelas abstrak dasar (Base Class) untuk semua Station di Nimonscooked.
 * Menyediakan kerangka dasar interaksi, update timer, dan sistem logging.
 */
public abstract class Station {
    protected String name;
    protected Position position;

    public Station(String name, Position position) {
        this.name = name;
        this.position = position;
    }

    /**
     * Dipanggil saat Chef berinteraksi dengan stasiun (menekan tombol aksi).
     */
    public abstract void onInteract(Chef chef);

    /**
     * Dipanggil setiap frame (tick) game untuk mengupdate timer/progress.
     */
    public void update(long deltaTime) {
        // Default: Diharapkan dioverride oleh Stasiun dengan mekanisme timer.
    }

    public String getName() { return name; }
    public Position getPosition() { return position; }

    /**
     * Helper method untuk logging yang konsisten di konsol dan HUD.
     * Menggunakan Singleton GameScreen untuk mengirim pesan.
     */
    protected void log(String level, String message) {
        String stationType = this.getClass().getSimpleName(); 
        String formattedMessage = "[" + stationType + "] [" + level + "] " + message;
        
        System.out.println(formattedMessage);
        
        // 2. KIRIM KE GAME SCREEN 
        try {
            // Panggil addLog melalui Singleton Instance
            GameScreen.getInstance().addLog(formattedMessage); 
        } catch (Exception e) {
            // Log ke console sebagai fallback jika GameScreen belum siap
            System.err.println("CRITICAL ERROR: Failed to add log to GUI: " + e.getMessage());
        }
    } 
    
    /**
     * Mengembalikan rasio progres saat ini (0.0 hingga 1.0) untuk rendering Progress Bar.
     * Diharapkan di-override oleh Stasiun yang memiliki timer.
     */
    public float getProgressRatio() {
        return 0.0f; // Default: Tidak ada progress
    }
    
    /**
     * Mengembalikan true jika Stasiun sedang aktif memproses item.
     * Digunakan oleh GameScreen untuk menentukan rendering Progress Bar.
     */
    public boolean isActive() {
        return getProgressRatio() > 0.0f && getProgressRatio() < 1.0f;
    }
    
    /**
     * Helper method: Melakukan Plating Awal (Membungkus Item menjadi Dish).
     * MENGGUNAKAN ALIAS PLATE.GETFOOD()/SETFOOD().
     * 
     * @return true if plating successful, false otherwise
     */
    protected boolean processPlating(Plate piring, Item itemToPlate) {
        if (itemToPlate == null) return false;

        // Cek Piring Penuh
        if (piring.getFood() != null) { 
            log("FAIL", "PLATING REJECTED: Plate is full.");
            return false;
        }

        Dish dishSiapSaji;

        if (itemToPlate instanceof Dish) {
            // Item sudah Dish, langsung gunakan
            dishSiapSaji = (Dish) itemToPlate;
        } else {
            // Item adalah Ingredient: Bungkus menjadi Dish Parsial baru
            List<Preparable> components = new ArrayList<>();
            if (itemToPlate instanceof Preparable) {
                components.add((Preparable) itemToPlate);
            }
            // Buat Dish baru: ID dan Nama Dish mengambil dari Ingredient pertama
            dishSiapSaji = new Dish("D-" + itemToPlate.getName(), itemToPlate.getName(), components); 
        }

        // Masukkan Dish ke Piring
        piring.setFood(dishSiapSaji); 
        log("SUCCESS", "PLATED: " + dishSiapSaji.getName() + " successfully placed on plate.");
        return true;
    }
}