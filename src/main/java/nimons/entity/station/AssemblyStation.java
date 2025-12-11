package nimons.entity.station;

import java.util.ArrayList;
import java.util.List;

import nimons.entity.chef.Chef;
import nimons.entity.common.Position;
import nimons.entity.item.Dish;
import nimons.entity.item.Ingredient;
import nimons.entity.item.Item;
import nimons.entity.item.KitchenUtensil;
import nimons.entity.item.Plate;
import nimons.entity.item.interfaces.Preparable;
import nimons.exceptions.InvalidIngredientStateException;
import nimons.exceptions.RecipeNotFoundException;
import nimons.logic.recipe.RecipeManager;

/**
 * AssemblyStation (A): Stasiun utama untuk merakit Dish kompleks.
 * Dapat menampung 1 Item (biasanya Plate) di atas meja.
 */
public class AssemblyStation extends Station {

    private Item placedItem; // Item yang ada di atas meja Assembly.

    public AssemblyStation(String name, Position position) {
        super(name, position);
    }
    
    /**
     * Assembly Lanjutan: Menambahkan Ingredient ke Dish yang sudah ada di Plate.
     * Mencocokkan komponen dengan resep final (RecipeManager).
     */
    private boolean attemptAssembly(Plate piring, Preparable itemToAdd) throws InvalidIngredientStateException, RecipeNotFoundException {
        if (!piring.isClean() || !(itemToAdd instanceof Ingredient)) {
            throw new InvalidIngredientStateException("Plate", "dirty or invalid", "clean plate with valid ingredient");
        }

        Dish currentDish = piring.getFood();
        Ingredient ingredientToAdd = (Ingredient) itemToAdd;
        
        if (currentDish == null) { 
            throw new InvalidIngredientStateException("Dish", "null", "existing dish on plate");
        }

        if (currentDish.canAddIngredient(ingredientToAdd)) {
            currentDish.addIngredient(ingredientToAdd);
            
            // Logika pencarian resep final
            Dish matchedDish = RecipeManager.findMatch(currentDish.getComponents()); 
            
            if (matchedDish != null) {
                // Ganti Dish Parsial dengan Dish Final jika resep cocok
                piring.setFood(matchedDish); 
            }
            
            return true;
        }
        return false;
    }

    /**
     * Plating Awal: Mengemas Ingredient tunggal (ex: Cooked Rice) menjadi Dish Parsial.
     * Digunakan untuk Plating pertama ke Plate kosong.
     */
    private boolean processPlating(Plate p, Ingredient isi) {
        List<Preparable> components = new ArrayList<>(); 
        components.add(isi); 
        
        // Buat Dish Parsial (Nama Dish akan sama dengan Ingredient pertama: Rice)
        Dish newDish = new Dish(isi.getName().toLowerCase(), isi.getName(), components);
        
        p.placeDish(newDish); 
        
        return true; 
    }


    /**
     * Menangani interaksi Chef (Pick Up, Drop, Plating, atau Assembly).
     */
    @Override
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();
        Item itemTable = this.placedItem;
        
        // --- SCENARIO 1: PLATING UTENSIL $\leftrightarrow$ PLATE DI MEJA ---
        if (itemHand instanceof KitchenUtensil && itemTable instanceof Plate) {
            Plate piringTable = (Plate) itemTable;
            KitchenUtensil utensilHand = (KitchenUtensil) itemHand;
            
            // Validasi: Plate bersih, Utensil berisi item
            if (piringTable.isClean() && utensilHand.getContents() != null && !utensilHand.getContents().isEmpty()) {
                
                Preparable isiPreparable = utensilHand.getContents().iterator().next();
                
                if (isiPreparable instanceof Ingredient && isiPreparable.getState() == nimons.entity.item.IngredientState.COOKED) {
                    
                    // Plating Awal Utensil ke Plate Kosong
                    if (piringTable.getFood() == null) {
                        Ingredient isi = (Ingredient) isiPreparable;
                        if (processPlating(piringTable, isi)) {
                            utensilHand.getContents().clear();
                            chef.setInventory(utensilHand); 
                            this.placedItem = piringTable; 
                            
                            log("SUCCESS", "PLATING UTENSIL: Cooked ingredient successfully plated to empty plate.");
                            return;
                        } else {
                            log("FAIL", "Plating Utensil failed.");
                            return;
                        }
                    } 
                    
                    // Assembly Lanjutan
                    try {
                        if (attemptAssembly(piringTable, isiPreparable)) {
                            utensilHand.getContents().clear();
                            chef.setInventory(utensilHand); 
                            this.placedItem = piringTable; 
                            
                            log("SUCCESS", "ASSEMBLY: Component added. Current Dish: " + piringTable.getFood().getName()); 
                            return;
                        } else {
                            log("FAIL", "COMBINATION REJECTED: Recipe mismatch or plate is full.");
                            return;
                        }
                    } catch (InvalidIngredientStateException e) {
                        log("FAIL", "✗ Assembly failed: " + e.getMessage());
                        return;
                    } catch (RecipeNotFoundException e) {
                        log("FAIL", "✗ Recipe not found: " + e.getMessage());
                        return;
                    }
                } else {
                    log("INFO", "INGREDIENT CHECK: Utensil contents are not COOKED or not an Ingredient.");
                }
            } else {
                log("FAIL", "Invalid state: Plate dirty or Utensil empty.");
            }
            return;
        }
        // -----------------------------------------------------------------------------

        // SCENARIO 2: PICK UP ITEM (Hand: Empty, Table: Item)
        if (itemHand == null && itemTable != null) {
            log("ACTION", "TAKEN: " + itemTable.getName() + " picked up.");
            chef.setInventory(itemTable);
            this.placedItem = null;
            return;
        }

        // SCENARIO 3: DROP ITEM (Hand: Item, Table: Empty)
        if (itemHand != null && itemTable == null) {
            log("ACTION", "DROPPED: " + itemHand.getName() + " placed on station.");
            this.placedItem = itemHand;
            chef.setInventory(null);
            return;
        }

        // SCENARIO 4: ASSEMBLY / KOMBINASI RESEP (Hand: Ingredient $\rightarrow$ Plate di Meja)
        if (itemHand != null && itemTable instanceof Plate) {
            Plate piring = (Plate) itemTable;
            
            if (!piring.isClean()) {
                log("FAIL", "Plate is dirty and cannot be used for assembly.");
                return;
            }

            // Plating/Assembly Langsung (Hanya melibatkan Ingredient)
            if (itemHand instanceof Ingredient) {
                Ingredient ingredientHand = (Ingredient) itemHand; 

                if (piring.getFood() == null) {
                    // 4a. Plating Manual Pertama (Plate di meja kosong)
                    if (processPlating(piring, ingredientHand)) {
                        chef.setInventory(null); 
                        log("SUCCESS", "PLATED: Initial ingredient added to plate.");
                        return;
                    }
                } else {
                    // 4b. Kombinasi Lanjutan (Plate sudah ada isinya)
                    try {
                        if (attemptAssembly(piring, ingredientHand)) {
                            chef.setInventory(null);
                            log("SUCCESS", "ASSEMBLY: Component added. Current Dish: " + piring.getFood().getName());
                            return;
                        } else {
                            log("FAIL", "COMBINATION REJECTED: " + ingredientHand.getName() + " is invalid for current dish.");
                            return;
                        }
                    } catch (InvalidIngredientStateException e) {
                        log("FAIL", "Assembly error: " + e.getIngredientName() + " (state: " + e.getCurrentState() + ", need: " + e.getRequiredState() + ")");
                        return;
                    } catch (RecipeNotFoundException e) {
                        log("FAIL", "No matching recipe for this combination");
                        return;
                    }
                }
            } else {
                log("FAIL", "Only Ingredient can be assembled onto a Plate.");
                return;
            }
        }
        
        // Fallback
        log("INFO", "Invalid interaction scenario.");
    }
    
    public Item getPlacedItem() { return placedItem; }
}