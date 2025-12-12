package nimons.entity.station;

import java.util.ArrayList;
import java.util.List;

import nimons.core.SoundManager;
import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.BoilingPot;
import nimons.entity.item.Dish;
import nimons.entity.item.FryingPan;
import nimons.entity.item.Ingredient;
import nimons.entity.item.IngredientState;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.Plate;
import nimons.entity.item.interfaces.CookingDevice;
import nimons.entity.item.interfaces.Preparable;
import nimons.exceptions.InvalidIngredientStateException;
import nimons.exceptions.StationFullException;

/**
 * CookingStation: Menangani pemrosesan Ingredient (memasak/menggoreng).
 * Setiap stasiun memiliki Utensil Cooking statis (BoilingPot/FryingPan).
 */
public class CookingStation extends Station {

    private KitchenUtensil utensils;
    private long lastCookingSound = 0;  // Track last time cooking sound was played

    public CookingStation(String name, Position position) {
        super(name, position);
        
        final int x = position.getX();
        
        // Inisialisasi Utensil berdasarkan posisi X
        // 2 Boiling Pot (x=10, x=11) dan 1 Frying Pan (x=12)
        if (x == 10 || x == 11) {
            this.utensils = new BoilingPot();
            this.utensils.setPortable(true);
        } else if (x == 12) {
            this.utensils = new FryingPan();
            this.utensils.setPortable(true);
        } else {
            // Default to BoilingPot for other positions
            this.utensils = new BoilingPot(); 
            this.utensils.setPortable(true);
        }

        log("INIT", "Station initialized with " + this.utensils.getName() + ".");
    }
    
    public KitchenUtensil getUtensils() {
        return this.utensils;
    }
    
    public void placeUtensils(KitchenUtensil u) {
        this.utensils = u;
    }

    /**
     * Memajukan timer Utensil dan Ingredient di dalamnya setiap game tick.
     */
    @Override
    public void update(long deltaTime) {
        if (utensils instanceof CookingDevice) {
            ((CookingDevice) utensils).update(deltaTime); 
            
            // Update Cooking Timer untuk setiap Ingredient di dalam Utensil
            boolean isAnyIngredientCooking = false;
            for (Preparable prep : utensils.getContents()) {
                if (prep instanceof Ingredient) {
                    Ingredient ingredient = (Ingredient) prep;
                    String timerLog = ingredient.updateCooking(deltaTime); 
                    
                    if (timerLog != null) {
                        log("TIMER", timerLog); 
                    }
                    
                    // Check if any ingredient is cooking
                    if (ingredient.getState() == IngredientState.COOKING) {
                        isAnyIngredientCooking = true;
                    }
                }
            }
            
            // Play cooking sound (frying/boiling) every 500ms if cooking
            if (isAnyIngredientCooking) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastCookingSound >= 500) {
                    // Determine sound based on utensil type
                    if (utensils instanceof FryingPan) {
                        SoundManager.getInstance().playSoundEffect("frying");
                    } else if (utensils instanceof BoilingPot) {
                        SoundManager.getInstance().playSoundEffect("boiling");
                    }
                    lastCookingSound = currentTime;
                }
            }
        }
    }
    
    // --- PROGRESS BAR LOGIC (Dipertahankan sesuai logic yang diberikan) ---

    /**
     * Mengembalikan progress ratio (0.0 - 1.0) dari Ingredient pertama yang sedang dimasak.
     */
    @Override
    public float getProgressRatio() {
        if (utensils == null || utensils.getContents().isEmpty()) {
            return 0.0f;
        }

        Preparable prep = utensils.getContents().iterator().next();
        
        if (prep instanceof Ingredient) {
            Ingredient ingredient = (Ingredient) prep;
            
            if (ingredient.getState() == IngredientState.COOKING) {
                float required = ingredient.getRequiredCookingTime(); 
                float current = ingredient.getCurrentCookingTime();   

                if (required > 0) {
                    return Math.min(1.0f, current / required);
                }
            }
        }
        return 0.0f;
    }

    /**
     * Mengembalikan true jika Ingredient sedang dalam fase COOKING.
     */
    @Override
    public boolean isActive() {
        return getProgressRatio() > 0.0f && getProgressRatio() < 1.0f;
    }

    // ----------------------------------------------------------

    /**
     * Plating Awal: Mengemas Ingredient tunggal (Cooked) menjadi Dish Parsial.
     */
    private boolean processPlating(Plate p, Ingredient isi) {
        List<Preparable> components = new ArrayList<>(); 
        components.add(isi);
        
        // Buat Dish Parsial
        Dish newDish = new Dish(isi.getName().toLowerCase(), isi.getName(), components);
        
        p.placeDish(newDish); 
        
        return true; 
    }

    /**
     * Menangani interaksi Chef (Angkat Utensil, Plating, Masukkan Bahan, Taruh Utensil).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();

        if (this.utensils != null) {
            
            // 1. ANGKAT UTENSIL (Hand: Kosong)
            if (itemHand == null) {
                if (utensils.isPortable()) {
                    log("ACTION", "TAKEN: " + utensils.getName() + " lifted.");
                    chef.setInventory(utensils);
                    this.utensils = null;
                    return;
                } else {
                    log("FAIL", utensils.getName() + " is static and cannot be lifted.");
                    return;
                }
            }
            
            // 2. PLATING DARI KOMPOR (Hand: Piring)
            if (itemHand instanceof Plate) {
                Plate p = (Plate) itemHand;
                
                if (utensils.getContents() != null && !utensils.getContents().isEmpty() && p.isClean()) {
                    
                    Preparable isiPreparable = utensils.getContents().iterator().next(); 
                    Ingredient isi = (Ingredient) isiPreparable; 
                    
                    if (isi.getState() == IngredientState.COOKED) {
                        
                        // Cek: Plate harus kosong untuk Plating Awal dari Kompor
                        if (p.getFood() == null) {
                            
                            if (processPlating(p, isi)) {
                                utensils.getContents().clear();
                                log("SUCCESS", "PLATED: Cooked ingredient transferred to plate.");
                                
                                if (utensils instanceof CookingDevice) {
                                    ((CookingDevice) utensils).reset(); // Reset device setelah plating
                                }
                                return;
                            }
                            log("FAIL", "Initial plating failed.");
                            return;
                            
                        } else {
                            log("FAIL", "Plating rejected: Plate is already occupied.");
                            return;
                        }
                    } else {
                        log("INFO", "INGREDIENT CHECK: State (" + isi.getState().name() + ") is not COOKED.");
                        return;
                    }
                } else {
                    log("FAIL", "Invalid state: Plate dirty or Utensil empty.");
                    return;
                }
            }
            
            // 3. MASUKKAN BAHAN KE PANCI (Hand: Bahan) - Only if utensil is on station
            if (itemHand instanceof Preparable && utensils instanceof CookingDevice) {
                CookingDevice device = (CookingDevice) utensils;
                Preparable bahan = (Preparable) itemHand;
                
                try {
                    device.addIngredient(bahan);
                    chef.setInventory(null);
                    
                    device.startCooking();
                    
                    String ingredientName = ((Item)bahan).getName();
                    log("SUCCESS", "START COOKING: " + ingredientName + " placed into " + utensils.getName() + ".");
                    
                    // Play frying/cooking sound effect
                    SoundManager.getInstance().playSoundEffect("frying");
                    
                } catch (StationFullException e) {
                    log("FAIL", "✗ " + e.getMessage());
                } catch (InvalidIngredientStateException e) {
                    log("FAIL", "✗ " + e.getIngredientName() + " must be " + e.getRequiredState() + " (currently: " + e.getCurrentState() + ")");
                } catch (Exception e) {
                    log("FAIL", "Unexpected error: " + e.getMessage());
                }
                return;
            }

            log("FAIL", "Chef's hands are full or cannot interact with Utensil.");
            return;
        }

        // --- SKENARIO 4: TARUH UTENSIL (Jika Spot KOSONG) ---
        
        if (itemHand instanceof KitchenUtensil) {
            KitchenUtensil utensilHand = (KitchenUtensil) itemHand;
            
            log("ACTION", "DROPPED: " + utensilHand.getName() + " placed on station.");
            this.utensils = utensilHand;
            chef.setInventory(null);
            
            if (utensilHand.getContents() != null && !utensilHand.getContents().isEmpty()) {
                if (utensilHand instanceof CookingDevice) {
                    ((CookingDevice) utensilHand).startCooking();
                }
            }
            
        } else if (itemHand != null) {
            log("FAIL", "Cannot place " + itemHand.getName() + " on Cooking Station spot.");
        } else {
            log("INFO", "Station spot is empty. Chef can place Utensil.");
        }
    }
}