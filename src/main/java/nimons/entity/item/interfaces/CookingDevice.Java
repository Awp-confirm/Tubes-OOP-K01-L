package nimons.entity.item.interfaces;

public interface CookingDevice {
    // Timer update untuk proses memasak (12s Matang, 24s Gosong)
    void update(long deltaTime);
    
    // Reset status alat (dipakai Trash Station)
    void reset();
    
    // Cek apakah alat sedang bekerja
    boolean isCooking();
    
    // Validasi bahan: Apakah alat ini mau menerima bahan X? 
    // (Misal: BoilingPot cuma mau Rice, FryingPan cuma mau Chopped)
    boolean canAccept(Preparable ingredient);
}