package nimons.entity.station;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Item;
import nimons.entity.item.Plate;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.interfaces.Preparable; 
import nimons.entity.item.interfaces.CookingDevice;

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

        // Cek: Apakah Utensil sudah terpasang?
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
            if (!utensils.getContents().isEmpty()) {
                // Casting isi Utensil ke Item untuk Helper Plating
                Item isi = (Item) utensils.getContents().iterator().next();
                
                // Menggunakan Helper Plating (Wrapper Ingredient -> Dish)
                processPlating((Plate) itemHand, isi);
                
                // Jika sukses masuk piring, kosongkan Utensil
                if (((Plate)itemHand).getFood() != null) {
                    utensils.getContents().clear();
                    log("INFO", "Isi Utensil dikosongkan setelah plating.");
                    // Reset status memasak (misal agar timer tidak jalan lagi)
                    if (utensils instanceof CookingDevice) {
                        ((CookingDevice) utensils).reset(); 
                    }
                }
            } else {
                log("INFO", "Panci kosong, tidak ada yang bisa di-plating.");
            }
            return;
        }
        
        // SCENARIO 2: MASUKKAN BAHAN KE PANCI (Hand: Bahan)
        if (itemHand != null) {
            // Validasi: Utensil harus bisa menerima bahan, dan tangan harus Preparable
            if (utensils instanceof CookingDevice && itemHand instanceof Preparable) {
                CookingDevice device = (CookingDevice) utensils;
                Preparable bahan = (Preparable) itemHand;
                
                if (utensils.getContents().isEmpty()) {
                    // Validasi CRUCIAL: Cek apakah Utensil mau menerima bahan ini? (Boiling Pot vs Timun)
                    if (device.canAccept(bahan)) {
                        utensils.getContents().add(bahan);
                        chef.setInventory(null);
                        log("ACTION", "Bahan " + ((Item)bahan).getName() + " masuk ke " + utensils.getName());
                    } else {
                        log("FAIL", utensils.getName() + " menolak bahan " + ((Item)bahan).getName() + ".");
                    }
                } else {
                    log("FAIL", "Panci sedang terisi/memasak.");
                }
            } else {
                log("FAIL", "Item ini tidak bisa dimasak atau alat tidak bisa dipakai memasak.");
            }
            return;
        }
    }

    /**
     * Getter Utensil yang sedang terpasang (untuk keperluan update timer global).
     */
    public KitchenUtensil getUtensils() { return utensils; }
}