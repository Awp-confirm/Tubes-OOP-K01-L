package nimons.logic.order;

import nimons.entity.item.Dish;
// IMPORTS WAJIB (Penyebab Error 1 & 3)
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderManager {

    // Singleton Instance
    private static OrderManager instance;
    
    // List Pesanan
    private List<Dish> activeOrders;

    // Constructor Private
    private OrderManager() {
        this.activeOrders = new ArrayList<>();
        
        // --- DATA DUMMY (Opsional) ---
        // Kamu bisa un-comment ini kalau mau test validasi beneran
        // activeOrders.add(new Dish("D-KAPPAMAKI", "Kappa Maki", new ArrayList<>()));
    }

    // Method Singleton
    public static OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager();
        }
        return instance;
    }

    /**
     * Memvalidasi apakah masakan yang disajikan sesuai dengan pesanan aktif.
     */
    public boolean validateOrder(Dish dish) {
        if (dish == null) return false;

        // LOGIKA SEMENTARA: Selalu BENAR (True)
        // Agar kamu bisa test Serving Station tanpa pusing mikirin random order dulu.
        // Nanti kalau sistem order sudah jadi, ubah logic ini untuk cek list activeOrders.
        
        /* Logic Asli Nanti:
        for (Dish order : activeOrders) {
            if (order.getName().equals(dish.getName())) {
                activeOrders.remove(order); // Hapus order yang sudah selesai
                return true;
            }
        }
        return false;
        */
        
        return true; 
    }

    // Method untuk menambah pesanan baru (dipakai Game Loop nanti)
    public void addOrder(Dish dish) {
        activeOrders.add(dish);
    }
}