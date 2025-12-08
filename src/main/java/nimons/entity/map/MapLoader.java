package nimons.entity.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import nimons.entity.common.Position;
import nimons.entity.station.AssemblyStation;
import nimons.entity.station.CookingStation;
import nimons.entity.station.CuttingStation;
import nimons.entity.station.IngredientStorageStation;
import nimons.entity.station.PlateStorageStation;
import nimons.entity.station.ServingStation;
import nimons.entity.station.Station;
import nimons.entity.station.TrashStation;
import nimons.entity.station.WashingStation;

public class MapLoader {

    /**
     * Load map dari resources/maps/{stageId}.txt
     *
     * Contoh:
     *  stageId = "stage1"  ->  /maps/stage1.txt
     */
    public MapLoadResult load(String stageId) {
        String path = "/maps/" + stageId + ".txt";

        List<String> lines = readAllLines(path);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Map file empty: " + path);
        }

        int height = lines.size();
        int width = lines.get(0).length();

        // Validasi: semua baris sama panjang
        for (String line : lines) {
            if (line.length() != width) {
                throw new IllegalArgumentException("Inconsistent row length in map file: " + path);
            }
        }

        Tile[][] tiles = new Tile[height][width];
        List<Position> spawnPositions = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            String line = lines.get(y);
            for (int x = 0; x < width; x++) {
                char ch = line.charAt(x);
                Position pos = new Position(x, y);

                Tile tile = new Tile();
                tile.setPosition(pos);

                // default: floor, bukan wall
                tile.setWall(false);

                Station station = null;

                switch (ch) {
                    case '#':
                    case 'X':
                        // X = Wall/Obstacle
                        tile.setWall(true);
                        break;

                    case '.':
                        // . = Walkable Space
                        break;

                    case 'V':
                        // V = Spawn Chef Point
                        spawnPositions.add(pos);
                        // lantai biasa, bukan wall
                        break;

                    case 'C':
                        // C = Cutting Station
                        station = new CuttingStation("Cutting Station", pos);
                        tile.setStation(station);
                        break;

                    case 'R':
                        // R = Cooking Station (Stove/Oven)
                        station = new CookingStation("Cooking Station", pos);
                        tile.setStation(station);
                        break;

                    case 'A':
                        // A = Assembly Station
                        station = new AssemblyStation("Assembly Station", pos);
                        tile.setStation(station);
                        break;

                    case 'S':
                        // S = Serving Counter
                        station = new ServingStation("Serving Counter", pos);
                        tile.setStation(station);
                        break;

                    case 'W':
                        // W = Washing Station (Sink)
                        station = new WashingStation("Washing Station", pos);
                        tile.setStation(station);
                        break;

                    case 'I':
                        // I = Ingredient Storage (Crate)
                        station = new IngredientStorageStation("Ingredient Storage", pos);
                        tile.setStation(station);
                        break;

                    case 'P':
                        // P = Plate Storage
                        station = new PlateStorageStation("Plate Storage", pos);
                        tile.setStation(station);
                        break;

                    case 'T':
                        // T = Trash Station
                        station = new TrashStation("Trash Station", pos);
                        tile.setStation(station);
                        break;

                    default:
                        // karakter tidak dikenal, sementara anggap floor
                        break;
                }

                tiles[y][x] = tile;
            }
        }

        TileManager tileManager = new TileManager(width, height, tiles);

        return new MapLoadResult(tileManager, spawnPositions);
    }

    private List<String> readAllLines(String path) {
        List<String> lines = new ArrayList<>();

        InputStream is = MapLoader.class.getResourceAsStream(path);
        if (is == null) {
            throw new IllegalArgumentException("Map resource not found: " + path);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                // boleh trimRight kalau takut ada carriage return, tapi jangan ubah panjang map
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read map resource: " + path, e);
        }

        return lines;
    }
}
