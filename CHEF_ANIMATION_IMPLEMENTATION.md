# Chef Animation Implementation Guide

## Overview
Implemented chef animation system for Nimons Cooked game with:
- **Idle State**: Static PNG image (`chef.png`)
- **Moving State**: Animated GIF (`move chef.gif`)
- **Directional Support**: All 4 directions (UP, DOWN, LEFT, RIGHT)
- **Initial State**: Chef spawns facing RIGHT

## Files Modified

### 1. GameScreen.java

#### Added Fields (Lines ~100-105)
```java
private Image chefMoveGif; // Chef move animation
private Map<Chef, Boolean> chefIsMoving = new HashMap<>(); // Track movement state per chef
```

#### Asset Loading (Lines ~215-220)
Added loading for the movement animation GIF:
```java
chefMoveGif = loadImage("/assets/picture/move chef.gif");
System.out.println("Chef move animation loaded: " + (chefMoveGif != null));
```

#### Chef Spawn Direction (Lines ~379, 402)
Changed initial direction from `Direction.DOWN` to `Direction.RIGHT`:
```java
playerChef = new Chef("player1", "Chef 1", spawnPos1, Direction.RIGHT);
chefIsMoving.put(playerChef, false); // Chef 1 awalnya idle

chef2 = new Chef("player2", "Chef 2", spawnPos2, Direction.RIGHT);
chefIsMoving.put(chef2, false); // Chef 2 awalnya idle
```

#### New Helper Methods

##### 1. `getChefImage(Chef chef)` (Lines ~355-365)
Selects the correct image based on movement state:
- Returns `chefMoveGif` if chef is moving
- Returns `chefImage` if chef is idle

```java
private Image getChefImage(Chef chef) {
    Boolean isMoving = chefIsMoving.getOrDefault(chef, false);
    if (isMoving && chefMoveGif != null) {
        return chefMoveGif;  // Show movement animation GIF
    }
    return chefImage;  // Show idle PNG image
}
```

##### 2. `drawChefWithDirection(...)` (Lines ~368-415)
Applies directional transformations to chef image:
- **RIGHT**: No transformation (default)
- **LEFT**: Flip horizontally
- **UP**: Rotate 180°
- **DOWN**: Normal drawing

Handles coordinate transformations with `gc.save()` and `gc.restore()` for proper rendering.

#### Movement State Tracking

##### In `update()` method (Lines ~548-553)
When chef finishes moving (position interpolation complete):
```java
if (Math.abs(dx) > 0.01 || Math.abs(dy) > 0.01) {
    chefRenderX += dx * GameConfig.MOVE_SPEED;
    chefRenderY += dy * GameConfig.MOVE_SPEED;
} else {
    chefRenderX = chefTargetX;
    chefRenderY = chefTargetY;
    chefIsMoving.put(activeChef, false);  // Set to idle
}
```

##### In `handleChefMovement()` method (Lines ~1505-1509)
When chef starts moving:
```java
if (tileManager.isWalkable(newPos)) {
    chefIsMoving.put(activeChef, true);  // Mark as moving
    // ... rest of movement logic
}
```

#### Chef Rendering (Lines ~1660-1665)
Updated `drawChef()` to use new image selection:
```java
Image chefDisplayImage = getChefImage(chef);

if (chefDisplayImage != null) {
    drawChefWithDirection(gc, chefDisplayImage, chefScreenX, chefScreenY, tileSize, chef.getDirection());
}
```

## Asset Files Required

Place these files in `src/main/resources/assets/picture/`:
- `chef.png` - Static idle image (facing RIGHT)
- `move chef.gif` - Animation for movement (facing RIGHT)

## How It Works

### Animation Flow
1. **Idle State**: Chef displays `chef.png` when not moving
2. **Movement Start**: 
   - `chefIsMoving.put(chef, true)` marks chef as moving
   - Next render shows `move chef.gif` with smooth interpolation
3. **Movement End**: 
   - Interpolation completes when chef reaches target position
   - `chefIsMoving.put(chef, false)` returns to idle state
   - Next render shows `chef.png` again

### Direction Handling
When rendering:
1. `getChefImage()` selects GIF or PNG based on movement state
2. `drawChefWithDirection()` applies rotation/flip transformation:
   - **RIGHT**: Show image as-is
   - **LEFT**: Flip horizontally (X-axis scale -1)
   - **UP**: Rotate 180° from center
   - **DOWN**: Show as-is (alternative: no change)

### State Management
- `chefIsMoving` HashMap tracks each chef's movement state
- Both chefs initialized as `false` (idle) on spawn
- Set to `true` when movement command executed
- Set back to `false` when smooth interpolation completes

## Directional Transformation Details

### LEFT (Flip Horizontal)
```java
gc.translate(x + size, y);      // Move to right edge
gc.scale(-1, 1);                 // Flip X-axis
gc.drawImage(image, 0, 0, size, size);
```

### UP (Rotate 180°)
```java
gc.translate(x + size/2, y + size/2);  // Move to center
gc.rotate(180);                         // Rotate 180°
gc.drawImage(image, -size/2, -size/2, size, size);
```

### RIGHT & DOWN
Normal drawing without transformation.

## Compilation Status
✅ **BUILD SUCCESSFUL** - All changes compile without errors

## Future Enhancements
1. Add separate GIF files for each direction if needed
2. Implement GIF frame timing control
3. Add chef action animations (cutting, cooking, etc.)
4. Add dash animation variant
5. Add carrying/holding item visual indicators

## Testing Checklist
- [ ] Chef appears with correct starting direction (RIGHT)
- [ ] Chef shows PNG image when idle
- [ ] Chef shows GIF animation when moving
- [ ] Chef faces correct direction (LEFT, RIGHT, UP, DOWN)
- [ ] Smooth interpolation between positions
- [ ] Directional transformations render correctly
- [ ] Both chefs animate independently
- [ ] No visual glitches or flickering

## Notes
- Transformations are applied per-frame during rendering, not modifying the original image
- Movement state is tracked separately for each chef in `chefIsMoving` Map
- GIF animation playback is handled by JavaFX's Image class automatically
- Fallback rendering (red circle + indicator) still works if images fail to load
