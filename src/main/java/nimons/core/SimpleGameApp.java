package nimons.core;

import nimons.entity.chef.Chef;
import nimons.entity.chef.Direction;
import nimons.entity.common.Position;
import nimons.entity.item.BoilingPot;
import nimons.entity.item.FryingPan;
import nimons.entity.item.Item;
import nimons.entity.item.Oven; // Pastikan import Oven ada
import nimons.entity.station.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SimpleGameApp extends JPanel {

    // Konfigurasi Map
    private final int TILE_SIZE = 64;
    private final int MAP_COLS = 10;
    private final int MAP_ROWS = 8;
    
    // Game Entities
    private Chef player;
    private Map<String, Station> stations = new HashMap<>(); // Key: "x,y"
    
    // --- FITUR BARU: IN-GAME LOG ---
    private LinkedList<String> gameLogs = new LinkedList<>();
    private final int MAX_LOGS = 5; 
    
    public SimpleGameApp() {
        this.setPreferredSize(new Dimension(MAP_COLS * TILE_SIZE, MAP_ROWS * TILE_SIZE + 100)); 
        this.setBackground(Color.DARK_GRAY);
        this.setFocusable(true);
        
        // 1. SETUP LOGGING
        setupGuiLogger();

        // 2. SETUP GAME
        initGame();

        // 3. INPUT LISTENER
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e.getKeyCode());
            }
        });
        
        // 4. GAME LOOP TIMER (0.1 detik)
        new Timer(100, e -> {
            updateGameLogic(); 
            repaint();         
        }).start();
    }

    private void updateGameLogic() {
        for (Station s : stations.values()) {
            s.update(100); 
        }
    }

    private void setupGuiLogger() {
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {}
            @Override
            public void write(byte[] b, int off, int len) {
                String msg = new String(b, off, len).trim();
                if (!msg.isEmpty()) {
                    addLog(msg);
                    originalOut.println(msg); 
                }
            }
        }));
    }

    private void addLog(String msg) {
        gameLogs.add(msg);
        if (gameLogs.size() > MAX_LOGS) {
            gameLogs.removeFirst(); 
        }
    }

    private void initGame() {
        // Spawn Chef
        player = new Chef("P1", "Juna", new Position(5, 4), Direction.DOWN);

        // --- SETUP MAP SUSHI ---
        
        // Atas: Bahan Baku
        addStation(new IngredientStorageStation("Crate Rice", new Position(2, 0)));
        addStation(new IngredientStorageStation("Crate Nori", new Position(3, 0)));
        addStation(new IngredientStorageStation("Crate Cucumber", new Position(4, 0)));
        addStation(new IngredientStorageStation("Crate Fish", new Position(5, 0)));
        addStation(new IngredientStorageStation("Crate Shrimp", new Position(6, 0)));

        // Kanan: Alat Masak
        CookingStation stove1 = new CookingStation("Stove 1", new Position(9, 2));
        stove1.placeUtensils(new BoilingPot("Pot1", 5));
        addStation(stove1);

        CookingStation stove2 = new CookingStation("Stove 2", new Position(9, 3));
        stove2.placeUtensils(new FryingPan("Pan1", 1));
        addStation(stove2);

        // Tambahan Oven (Sesuai diskusi sebelumnya)
        CookingStation ovenSt = new CookingStation("Oven", new Position(9, 4));
        ovenSt.placeUtensils(new Oven("Oven1", 1));
        addStation(ovenSt);

        // Bawah: Kerja
        addStation(new CuttingStation("Cut Board 1", new Position(3, 7)));
        addStation(new CuttingStation("Cut Board 2", new Position(4, 7)));
        
        // MEJA RAKIT & COUNTER
        addStation(new AssemblyStation("Assembly", new Position(7, 7))); 
        addStation(new AssemblyStation("Counter", new Position(6, 7))); 

        // Kiri: Serving & Cuci
        
        // 1. Buat Storage & Serving terpisah biar bisa disambung
        PlateStorageStation plateStorage = new PlateStorageStation("Plates", new Position(0, 2));
        ServingStation servingStation = new ServingStation("Delivery", new Position(0, 5));
        
        // 2. Sambungkan Serving ke Storage (Buat balikin piring kotor)
        servingStation.setPlateStorage(plateStorage); 

        addStation(plateStorage);
        addStation(servingStation);
        addStation(new WashingStation("Sink", new Position(0, 3)));
        addStation(new TrashStation("Trash", new Position(0, 7)));
        
        System.out.println("Welcome to Nimonscooked GUI!");
        System.out.println(">> TIPS: Taruh Panci Panas di 'Counter', lalu ambil Piring untuk Plating!");
    }

    private void addStation(Station s) {
        String key = s.getPosition().getX() + "," + s.getPosition().getY();
        stations.put(key, s);
    }

    private void handleInput(int key) {
        if (key == KeyEvent.VK_W) player.move(Direction.UP, MAP_COLS, MAP_ROWS);
        if (key == KeyEvent.VK_S) player.move(Direction.DOWN, MAP_COLS, MAP_ROWS);
        if (key == KeyEvent.VK_A) player.move(Direction.LEFT, MAP_COLS, MAP_ROWS);
        if (key == KeyEvent.VK_D) player.move(Direction.RIGHT, MAP_COLS, MAP_ROWS);

        if (key == KeyEvent.VK_SPACE) {
            Position targetPos = player.getFacingPosition();
            String keyMap = targetPos.getX() + "," + targetPos.getY();
            
            Station targetStation = stations.get(keyMap);
            if (targetStation != null) {
                targetStation.onInteract(player); 
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 1. GAMBAR MAP
        for (int y = 0; y < MAP_ROWS; y++) {
            for (int x = 0; x < MAP_COLS; x++) {
                String key = x + "," + y;
                Station s = stations.get(key);
                int px = x * TILE_SIZE;
                int py = y * TILE_SIZE;

                if (s != null) {
                    // --- WARNA STATION ---
                    if (s instanceof IngredientStorageStation) g.setColor(new Color(139, 69, 19)); // Coklat Kayu
                    else if (s instanceof CookingStation) {
                         // Cek alat masak buat bedain warna dikit
                         if (((CookingStation)s).getUtensils() instanceof Oven) g.setColor(new Color(178, 34, 34)); // Merah Bata (Oven)
                         else g.setColor(Color.RED); // Merah Terang (Kompor)
                    }
                    else if (s instanceof CuttingStation) g.setColor(new Color(34, 139, 34)); // Hijau Hutan
                    else if (s instanceof AssemblyStation) {
                        if (s.getName().equals("Counter")) g.setColor(new Color(218, 165, 32)); // Goldenrod (Counter)
                        else g.setColor(Color.ORANGE); // Orange (Assembly)
                    }
                    else if (s instanceof TrashStation) g.setColor(Color.BLACK);
                    else if (s instanceof ServingStation) g.setColor(Color.MAGENTA);
                    else if (s instanceof WashingStation) g.setColor(Color.BLUE);
                    else g.setColor(Color.GRAY);
                    
                    g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                    
                    // --- LOGIKA TEXT (NAMA STATION JELAS) ---
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 11)); // Font agak kecil biar muat
                    
                    String displayName = s.getName();
                    
                    // Logic pembersihan nama
                    if (s instanceof IngredientStorageStation) {
                        // "Crate Rice" -> "Rice"
                        displayName = displayName.replace("Crate ", "");
                    } else if (s instanceof CookingStation) {
                        // "Stove 1" -> "Stove" (Ambil kata pertama)
                        displayName = displayName.split(" ")[0];
                    } else if (s instanceof CuttingStation) {
                        displayName = "Cut";
                    }
                    
                    // Gambar teks di tengah atas kotak
                    // Menggunakan FontMetrics biar rata tengah (opsional, tapi biar rapi)
                    FontMetrics fm = g.getFontMetrics();
                    int textX = px + (TILE_SIZE - fm.stringWidth(displayName)) / 2;
                    g.drawString(displayName, textX, py + 15);
                    
 // --- GAMBAR ITEM DI ATAS MEJA ---
                    Item itemOnTable = getStationItem(s);
                    if (itemOnTable != null) {
                        g.setColor(Color.CYAN);
                        g.fillOval(px + 20, py + 25, 24, 24); 
                        
                        g.setColor(Color.BLACK);
                        g.setFont(new Font("Arial", Font.PLAIN, 10));
                        
                        // --- FIX ANTI NULL POINTER ---
                        String itemName = itemOnTable.getName();
                        
                        // Jaga-jaga kalau getName() mengembalikan null
                        if (itemName == null) {
                            itemName = "?"; 
                        } else {
                            // Logika pemendekan nama (Sekarang aman karena sudah dicek tidak null)
                            if (itemName.contains("Boiling Pot")) itemName = "Pot";
                            else if (itemName.contains("Frying Pan")) itemName = "Pan";
                            else if (itemName.contains("Rice") && itemName.contains("COOKED")) itemName = "Rice(C)";
                            else if (itemName.contains("Rice") && itemName.contains("RAW")) itemName = "Rice(R)";
                            else if (itemName.length() > 7) itemName = itemName.substring(0, 6) + ".";
                        }
                        
                        // Gambar nama item
                        int itemTextX = px + (TILE_SIZE - g.getFontMetrics().stringWidth(itemName)) / 2;
                        g.drawString(itemName, itemTextX, py + 55);
                    
                    }
                } else {
                    // Lantai Kosong
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // 2. GAMBAR CHEF
        int cx = player.getPosition().getX() * TILE_SIZE;
        int cy = player.getPosition().getY() * TILE_SIZE;
        g.setColor(new Color(100, 149, 237)); // Cornflower Blue
        g.fillOval(cx + 10, cy + 10, TILE_SIZE - 20, TILE_SIZE - 20);
        
        // Penanda Arah Hadap (Kuning)
        g.setColor(Color.YELLOW);
        Position face = player.getFacingPosition();
        int fx = face.getX() * TILE_SIZE + TILE_SIZE/2;
        int fy = face.getY() * TILE_SIZE + TILE_SIZE/2;
        g.fillOval(fx-4, fy-4, 8, 8);

        // 3. UI PANEL (Bawah)
        int uiY = MAP_ROWS * TILE_SIZE;
        g.setColor(Color.BLACK);
        g.fillRect(0, uiY, getWidth(), 100);

        // Info Inventory
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Consolas", Font.BOLD, 14));
        String invText = "HAND: " + (player.getInventory() != null ? player.getInventory().getName() : "Empty");
        g.drawString(invText, 20, uiY + 25);
        g.drawLine(0, uiY + 35, getWidth(), uiY + 35);

        // --- RENDER LOGS ---
        g.setColor(Color.GREEN);
        g.setFont(new Font("Consolas", Font.PLAIN, 12));
        int logY = uiY + 50;
        for (String log : gameLogs) {
            g.drawString("> " + log, 20, logY);
            logY += 15;
        }
    }

    private Item getStationItem(Station s) {
        if (s instanceof IngredientStorageStation) return ((IngredientStorageStation)s).getPlacedItem();
        if (s instanceof CuttingStation) return ((CuttingStation)s).getPlacedItem();
        if (s instanceof AssemblyStation) return ((AssemblyStation)s).getPlacedItem();
        if (s instanceof CookingStation) return ((CookingStation)s).getUtensils();
        if (s instanceof WashingStation) return null; // Visual piring di sink belum dihandle via getter ini
        return null;
    }

    public static void main(String[] args) {
        JFrame window = new JFrame("Nimonscooked - Final GUI");
        SimpleGameApp game = new SimpleGameApp();
        
        window.add(game);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}