package nimons.core;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * SINGLETON PATTERN (Thread-Safe Implementation)
 * 
 * Singleton Pattern memastikan bahwa sebuah kelas hanya memiliki satu instance
 * dan menyediakan global access point ke instance tersebut.
 * 
 * Implementation: Double-Checked Locking dengan Volatile
 * - volatile: Memastikan visibility perubahan variable di semua thread
 * - synchronized block: Mencegah multiple threads membuat instance bersamaan
 * - Double-check: Performa optimization (hanya lock saat benar-benar perlu create instance)
 * 
 * Manfaat:
 * 1. Single Instance: Hanya satu SoundManager di seluruh aplikasi
 * 2. Global Access: Mudah diakses dari mana saja
 * 3. Lazy Initialization: Instance dibuat hanya saat pertama kali dibutuhkan
 * 4. Thread Safety: Aman digunakan dalam multi-threaded environment
 * 5. Resource Management: Mengelola audio resources secara terpusat
 * 
 * SoundManager untuk mengelola semua audio dalam game.
 * Menggunakan AudioClip untuk sound effects pendek dan MediaPlayer untuk musik latar.
 */
public class SoundManager {
    
    // volatile memastikan perubahan instance visible ke semua thread
    private static volatile SoundManager instance;
    
    // Sound effect clips (untuk SFX pendek yang bisa di-trigger cepat)
    private Map<String, AudioClip> soundEffects;
    
    // Music player (untuk musik latar yang berjalan loop)
    private MediaPlayer currentMusicPlayer;
    private String currentMusicName;
    
    // Volume control
    private double masterVolume = 1.0;
    private double effectsVolume = 1.0;
    private double musicVolume = 0.5;
    
    // Sound file paths (dalam JAR, relatif ke resources)
    private static final String SOUND_DIR = "/assets/sound/";
    
    /**
     * Private constructor untuk mencegah instantiasi langsung
     */
    private SoundManager() {
        this.soundEffects = new HashMap<>();
        loadAllSounds();
    }
    
    /**
     * Thread-safe getInstance() menggunakan Double-Checked Locking
     * 
     * @return Instance tunggal dari SoundManager
     */
    public static SoundManager getInstance() {
        // First check (no locking) - untuk performa
        if (instance == null) {
            // Synchronized block - hanya dieksekusi jika instance masih null
            synchronized (SoundManager.class) {
                // Second check (with locking) - untuk thread safety
                if (instance == null) {
                    instance = new SoundManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Load semua sound effects dan musik
     */
    private void loadAllSounds() {
        // Load music files
        // Note: Music files are loaded on-demand in playMusic() method
        
        // Load sound effects (WAV format)
        loadSoundEffect("chopping", "chopping.wav");
        loadSoundEffect("correct", "correct.wav");
        loadSoundEffect("fail", "fail.wav");
        loadSoundEffect("frying", "frying.wav");
        loadSoundEffect("pass", "pass.wav");
        loadSoundEffect("wrong", "wrong.wav");
        
        // Main menu music akan di-load saat diperlukan
    }
    
    /**
     * Load individual sound effect
     */
    private void loadSoundEffect(String name, String filename) {
        try {
            String resourcePath = SOUND_DIR + filename;
            String url = getClass().getResource(resourcePath).toString();
            AudioClip clip = new AudioClip(url);
            clip.setVolume(effectsVolume * masterVolume);
            soundEffects.put(name, clip);
            System.out.println("[SoundManager] Loaded SFX: " + name);
        } catch (NullPointerException e) {
            System.err.println("[SoundManager] Failed to load SFX: " + name + " (" + filename + ")");
        }
    }
    
    /**
     * Play sound effect
     */
    public void playSoundEffect(String name) {
        if (soundEffects.containsKey(name)) {
            AudioClip clip = soundEffects.get(name);
            clip.setVolume(effectsVolume * masterVolume);
            clip.play();
        } else {
            System.out.println("[SoundManager] Sound effect not found: " + name);
        }
    }
    
    /**
     * Play music dengan loop infinite (untuk music latar)
     */
    public void playMusic(String musicName, String filename) {
        // Stop musik sebelumnya jika ada
        if (currentMusicPlayer != null) {
            currentMusicPlayer.stop();
        }
        
        try {
            String resourcePath = SOUND_DIR + filename;
            String url = getClass().getResource(resourcePath).toString();
            Media media = new Media(url);
            currentMusicPlayer = new MediaPlayer(media);
            currentMusicPlayer.setVolume(musicVolume * masterVolume);
            currentMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            currentMusicPlayer.play();
            currentMusicName = musicName;
            System.out.println("[SoundManager] Playing music: " + musicName);
        } catch (Exception e) {
            System.err.println("[SoundManager] Failed to play music: " + musicName + " (" + filename + ")");
            // Try fallback music
            if (!filename.equals("music1.mp3")) {
                System.out.println("[SoundManager] Trying fallback music...");
                playMusic("fallback", "music1.mp3");
            }
        }
    }
    
    /**
     * Stop musik yang sedang dimainkan
     */
    public void stopMusic() {
        if (currentMusicPlayer != null) {
            currentMusicPlayer.stop();
            currentMusicPlayer = null;
            currentMusicName = null;
        }
    }
    
    /**
     * Set master volume (0.0 - 1.0)
     */
    public void setMasterVolume(double volume) {
        this.masterVolume = Math.max(0.0, Math.min(1.0, volume));
        updateAllVolumes();
    }
    
    /**
     * Set effects volume (0.0 - 1.0)
     */
    public void setEffectsVolume(double volume) {
        this.effectsVolume = Math.max(0.0, Math.min(1.0, volume));
        updateAllVolumes();
    }
    
    /**
     * Set music volume (0.0 - 1.0)
     */
    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume));
        updateAllVolumes();
    }
    
    /**
     * Update volume semua audio
     */
    private void updateAllVolumes() {
        // Update semua effects
        for (AudioClip clip : soundEffects.values()) {
            clip.setVolume(effectsVolume * masterVolume);
        }
        
        // Update music
        if (currentMusicPlayer != null) {
            currentMusicPlayer.setVolume(musicVolume * masterVolume);
        }
    }
    
    /**
     * Mute/unmute semua audio
     */
    public void setMuted(boolean muted) {
        if (muted) {
            setMasterVolume(0.0);
        } else {
            setMasterVolume(1.0);
        }
    }
    
    /**
     * Get current music name
     */
    public String getCurrentMusicName() {
        return currentMusicName;
    }
    
    /**
     * Cleanup resources
     */
    public void dispose() {
        stopMusic();
        soundEffects.clear();
    }
}
