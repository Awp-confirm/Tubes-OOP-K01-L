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
import nimons.entity.station.Rack;
import nimons.entity.station.ServingStation;
import nimons.entity.station.Station;
import nimons.entity.station.TrashStation;
import nimons.entity.station.WashingStation;

public class MapLoader {

    
        
    public MapLoadResult load(String stageId) {
        String path = "/assets/maps/" + stageId + ".txt";

        List<String> lines = readAllLines(path);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Map file empty: " + path);
        }

        
        int width = 0;
        for (String line : lines) {
            if (line.length() > width) {
                width = line.length();
            }
        }
        int height = lines.size();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.length() < width) {
                StringBuilder sb = new StringBuilder(line);
                while (sb.length() < width) {
                    sb.append('.');
                }
                lines.set(i, sb.toString());
            }
        }

        Tile[][] tiles = new Tile[height][width];
        List<Position> spawnPositions = new ArrayList<>();
        
        
        
        List<WashingStation> washingStations = new ArrayList<>();
        List<Rack> racks = new ArrayList<>();
        
        
        
        
        

        
        for (int y = 0; y < height; y++) {
            String line = lines.get(y);
            for (int x = 0; x < width; x++) {
                char ch = line.charAt(x);
                Position pos = new Position(x, y);

                Tile tile = new Tile();
                tile.setPosition(pos);
                tile.setWall(false);

                Station station = null;

                switch (ch) {
                    case '#':
                    case 'X':
                        tile.setWall(true);
                        break;
                    case '.':
                        break;
                    case 'V':
                        spawnPositions.add(pos);
                        break;
                    case 'C':
                        station = new CuttingStation("Cutting Station", pos);
                        break;
                    case 'R':
                        station = new CookingStation("Cooking Station", pos);
                        break;
                    case 'A':
                        station = new AssemblyStation("Assembly Station", pos);
                        break;
                    case 'S':
                        station = new ServingStation("Serving Counter", pos);
                        break;
                    
                    case 'W':
                        WashingStation ws = new WashingStation("Washing Station", pos);
                        washingStations.add(ws);
                        station = ws;
                        break;
                    case 'K':
                        Rack rack = new Rack("Rack", pos);
                        racks.add(rack);
                        station = rack;
                        break;
                    case 'I':
                        station = new IngredientStorageStation("Ingredient Storage", pos);
                        break;
                    case 'P':
                        station = new PlateStorageStation("Plate Storage", pos);
                        break;
                    case 'T':
                        station = new TrashStation("Trash Station", pos);
                        break;
                    default:
                        break;
                }

                if (station != null) {
                    tile.setStation(station);
                }

                tiles[y][x] = tile;
            }
        }

        
        
        
        
        if (!washingStations.isEmpty() && !racks.isEmpty()) {
            
            
            WashingStation sink = washingStations.get(0);
            Rack outputRack = racks.get(0);
            
            sink.setOutputRack(outputRack);
            
            System.out.println("MapLoader: WashingStation linked to Rack at " + outputRack.getPosition());
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
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read map resource: " + path, e);
        }

        return lines;
    }
}