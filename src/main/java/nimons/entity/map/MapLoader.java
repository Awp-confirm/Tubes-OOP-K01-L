package nimons.entity.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // Tambahkan import HashMap

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
import nimons.entity.station.WashingStation.WashingMode; // Tambahkan import WashingMode

public class MapLoader {

    /**
     * Load map dari resources/assets/maps/{stageId}.txt
     */
    public MapLoadResult load(String stageId) {
        String path = "/assets/maps/" + stageId + ".txt";

        List<String> lines = readAllLines(path);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Map file empty: " + path);
        }

        // ... (Logika penentuan width, height, dan padding tetap sama) ...
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
        
        // --- REVISI 1: List sementara untuk menyimpan semua Station ---
        // Kita perlu menyimpan referensi semua Station untuk koneksi di akhir.
        List<WashingStation> washingStations = new ArrayList<>();
        
        // Kita juga perlu menyimpan ServingStation untuk koneksi Singleton (untuk amannya, meski sudah diatasi)
        // ServingStation servingStation = null; 
        // PlateStorageStation plateStorageStation = null;
        // Kita tidak perlu menyimpan S dan P karena sudah diatasi dengan Singleton/Statik.

        // PASS 1: Membaca dan membuat semua Tile dan Station
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
                    // --- REVISI 2: Instansiasi W - Default Mode SINK ---
                    case 'W':
                        // Semua W dibuat sebagai SINK, mode akan diubah di PASS 2
                        WashingStation ws = new WashingStation("Washing Station", pos, WashingMode.SINK);
                        washingStations.add(ws); // Simpan sementara
                        station = ws;
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

        // PASS 2: Menganalisis dan Menghubungkan Station (Washing Station)
        
        // --- REVISI 3: Menghubungkan W SINK dan W RACK ---
        if (washingStations.size() >= 2) {
            // Asumsi: Kita pasangkan W yang berdekatan.
            // Peta Anda menunjukkan dua W yang berdampingan (misal, W[0] dan W[1])
            
            WashingStation sink = washingStations.get(0);
            WashingStation rack = washingStations.get(1);
            
            // Atur mode RACK pada W kedua (yang berfungsi sebagai output)
            rack.setMode(WashingStation.WashingMode.RACK);
            
            // Lakukan koneksi dari SINK ke RACK
            sink.setOutputRack(rack); 
            
            // Catatan: Jika ada lebih dari 2 W (misal 4 W), logika ini harus diperluas
            // untuk mendeteksi semua pasangan W yang berdekatan.
            
            // Untuk amannya, pastikan semua W lain yang tidak terpakai dikembalikan ke mode default (jika ada)
            // Namun, untuk saat ini, kita hanya koneksikan 2 yang pertama.
        }
        // ----------------------------------------------------


        TileManager tileManager = new TileManager(width, height, tiles);

        // --- REVISI 4: Panggil koneksi Singleton/Statik (Jika ada) ---
        // Jika Anda menggunakan metode GameManager.connectStations(), Anda panggil di sini.
        // Karena kita menggunakan Singleton pada S dan P, ini sudah diatasi, tetapi ini adalah
        // tempat yang tepat untuk koneksi lainnya.

        return new MapLoadResult(tileManager, spawnPositions);
    }

    private List<String> readAllLines(String path) {
        List<String> lines = new ArrayList<>();
        // ... (Implementasi readAllLines tetap sama) ...
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