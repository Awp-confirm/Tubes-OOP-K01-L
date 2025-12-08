package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.Plate; 
import nimons.entity.item.interfaces.CookingDevice;
import nimons.entity.item.interfaces.Preparable;
import nimons.entity.item.Dish; // Diperlukan untuk casting hasil plating

/**
 * CookingStation (R) menangani interaksi Chef dengan kompor/oven.
 * Logic utama adalah mendelegasikan timer ke Utensil dan memvalidasi input bahan.
 */
public class CookingStation extends Station {

    private KitchenUtensil utensils; // Utensil aktif (Panci/Wajan/Oven)

    public CookingStation(String name, Position position) {
        super(name, position);
    }
    
    /**
     * Setup awal: Menaruh Utensil di atas station.
     */
    public void placeUtensils(KitchenUtensil u) {
        this.utensils = u;
    }

    /**
     * Mendelegasikan timer masakan ke Utensil.
     * Masak dijalankan otomatis tanpa Chef di adjacent cell.
     */
    @Override
    public void update(long deltaTime) {
        if (utensils instanceof CookingDevice) {
            // CookingDevice.update() akan menangani perpindahan state (RAW -> COOKED -> BURNED)
            ((CookingDevice) utensils).update(deltaTime);
        }
    }

    /**
     * Menangani interaksi Chef (Menaruh/Mengambil Utensil, Menaruh Bahan, atau Plating).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        // --- INTERAKSI DENGAN SPOT KOSONG ---
        if (this.utensils == null) {
            // SCENARIO 4: TARUH UTENSIL (Hand: Panci/Wajan)
            if (itemHand instanceof KitchenUtensil) {
                log("ACTION", "Menaruh " + itemHand.getName());
                this.utensils = (KitchenUtensil) itemHand;
                chef.setInventory(null);
            } else if (itemHand == null) {
                log("INFO", "Station kosong, Chef bisa menaruh Utensil.");
            } else {
                log("FAIL", "Tidak bisa menaruh " + itemHand.getName() + ", butuh Utensil.");
            }
            return;
        }

        // SCENARIO 3: ANGKAT UTENSIL (Hand: Kosong)
        if (itemHand == null && utensils != null) {
            log("ACTION", "Mengangkat " + utensils.getName());
            chef.setInventory(utensils);
            this.utensils = null;
            return;
        }

        // --- INTERAKSI DENGAN UTENSIL YANG SUDAH TERPASANG ---

        // SCENARIO 1: PLATING LANGSUNG DARI KOMPOR (Hand: Piring)
        if (itemHand instanceof Plate) {
            if (utensils.getContents() != null && !utensils.getContents().isEmpty()) {
                // Utensil.getContents() mengembalikan Set<Preparable>. Kita ambil elemen pertama.
                Item isi = (Item) utensils.getContents().iterator().next(); 
                
                // Menggunakan Helper Plating (Wrapper Ingredient -> Dish)
                processPlating((Plate) itemHand, isi);
                
                // Jika sukses masuk piring (Plate.getFood() tidak null), kosongkan Utensil
                if (((Plate)itemHand).getFood() != null) { 
                    utensils.getContents().clear();
                    log("SUCCESS", "Isi Utensil di-plating ke piring.");
                    
                    // Reset status memasak (penting agar timer berhenti)
                    if (utensils instanceof CookingDevice) {
                        ((CookingDevice) utensils).reset(); 
                    }
                }
            } else {
                log("INFO", utensils.getName() + " kosong, tidak ada yang bisa di-plating.");
            }
            return;
        }
        
        // SCENARIO 2: MASUKKAN BAHAN KE PANCI (Hand: Bahan)
        if (itemHand != null) {
            // Validasi: Utensil harus CookingDevice, dan tangan harus Preparable
            if (utensils instanceof CookingDevice && itemHand instanceof Preparable) {
                CookingDevice device = (CookingDevice) utensils;
                Preparable bahan = (Preparable) itemHand;
                
                // Pastikan Utensil masih kosong
                if (utensils.getContents().isEmpty()) {
                    // Validasi CRUCIAL: Cek apakah Utensil mau menerima bahan ini? (Delegate ke CookingDevice.canAccept())
                    if (device.canAccept(bahan)) {
                        utensils.getContents().add(bahan);
                        chef.setInventory(null);
                        
                        // Setelah bahan masuk, Utensil (device) mulai proses internalnya
                        device.startCooking();
                        log("ACTION", "Bahan " + ((Item)bahan).getName() + " masuk ke " + utensils.getName() + ". Memasak dimulai.");
                    } else {
                        log("FAIL", utensils.getName() + " menolak bahan " + ((Item)bahan).getName() + " (State/Tipe tidak cocok).");
                    }
                } else {
                    log("FAIL", utensils.getName() + " sedang terisi/memasak. Hanya boleh satu bahan.");
                }
            } else {
                log("FAIL", "Item di tangan tidak bisa dimasak atau Utensil tidak mendukung memasak.");
            }
            return;
        }
    }

    /**
     * Getter Utensil yang sedang terpasang (untuk keperluan update timer global).
     */
    public KitchenUtensil getUtensils() { return utensils; }
}