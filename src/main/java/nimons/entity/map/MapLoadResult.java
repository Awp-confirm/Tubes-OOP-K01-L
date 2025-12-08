package nimons.entity.map;

import java.util.List;

import nimons.entity.common.Position;

public class MapLoadResult {

    private TileManager tileManager;
    private List<Position> spawnPositions;

    public MapLoadResult(TileManager tileManager, List<Position> spawnPositions) {
        this.tileManager = tileManager;
        this.spawnPositions = spawnPositions;
    }

    public TileManager getTileManager() {
        return tileManager;
    }

    public void setTileManager(TileManager tileManager) {
        this.tileManager = tileManager;
    }

    public List<Position> getSpawnPositions() {
        return spawnPositions;
    }

    public void setSpawnPositions(List<Position> spawnPositions) {
        this.spawnPositions = spawnPositions;
    }
}
