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

public class AssemblyStation extends Station {

    private Item placedItem; 

    public AssemblyStation(String name, Position position) {
        super(name, position);
    }
    
    
        
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
            
            
            Dish matchedDish = RecipeManager.findMatch(currentDish.getComponents()); 
            
            if (matchedDish != null) {
                
                piring.setFood(matchedDish); 
            }
            
            return true;
        }
        return false;
    }

    
        
    private boolean processPlating(Plate p, Ingredient isi) {
        
        if (!isi.canBePlacedOnPlate()) {
            return false;
        }
        
        List<Preparable> components = new ArrayList<>(); 
        components.add(isi); 
        
        
        Dish newDish = new Dish(isi.getName().toLowerCase(), isi.getName(), components);
        
        p.placeDish(newDish); 
        
        return true; 
    }

    
    @Override
        
    public void onInteract(Chef chef) {
        if (chef == null) return;
        Item itemHand = chef.getInventory();
        Item itemTable = this.placedItem;
        
        
        if (itemHand instanceof KitchenUtensil && itemTable instanceof Plate) {
            Plate piringTable = (Plate) itemTable;
            KitchenUtensil utensilHand = (KitchenUtensil) itemHand;
            
            
            if (piringTable.isClean() && utensilHand.getContents() != null && !utensilHand.getContents().isEmpty()) {
                
                Preparable isiPreparable = utensilHand.getContents().iterator().next();
                
                if (isiPreparable instanceof Ingredient && isiPreparable.getState() == nimons.entity.item.IngredientState.COOKED) {
                    
                    
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
        

        
        if (itemHand == null && itemTable != null) {
            log("ACTION", "TAKEN: " + itemTable.getName() + " picked up.");
            chef.setInventory(itemTable);
            this.placedItem = null;
            return;
        }

        
        if (itemHand != null && itemTable == null) {
            log("ACTION", "DROPPED: " + itemHand.getName() + " placed on station.");
            this.placedItem = itemHand;
            chef.setInventory(null);
            return;
        }

        
        if (itemHand != null && itemTable instanceof Plate) {
            Plate piring = (Plate) itemTable;
            
            if (!piring.isClean()) {
                log("FAIL", "Plate is dirty and cannot be used for assembly.");
                return;
            }

            
            if (itemHand instanceof Ingredient) {
                Ingredient ingredientHand = (Ingredient) itemHand; 

                if (piring.getFood() == null) {
                    
                    if (processPlating(piring, ingredientHand)) {
                        chef.setInventory(null); 
                        log("SUCCESS", "PLATED: Initial ingredient added to plate.");
                        return;
                    }
                } else {
                    
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
        
        
        log("INFO", "Invalid interaction scenario.");
    }
    
    public Item getPlacedItem() { return placedItem; }
}