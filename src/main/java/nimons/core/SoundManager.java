package nimons.core;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * SoundManager - Mengelola audio dalam game (Singleton Pattern)
 * Menggunakan AudioClip untuk sound effects dan MediaPlayer untuk musik latar.
 */
public class SoundManager {
    
    private static volatile SoundManager instance;
    private Map<String, AudioClip> soundEffects;
    private MediaPlayer currentMusicPlayer;
    private String currentMusicName;
    
    private double masterVolume = 1.0;
    private double effectsVolume = 1.0;
    private double musicVolume = 0.5;
    
    private static final String SOUND_DIR = "/assets/sound/";
    
    private SoundManager() {
        this.soundEffects = new HashMap<>();
        loadAllSounds();
    }
    
    /**
     * Mengembalikan instance singleton SoundManager
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            synchronized (SoundManager.class) {
                if (instance == null) {
                    instance = new SoundManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Load semua sound effects
     */
    private void loadAllSounds() {
        loadSoundEffect("chopping", "chopping.wav");
        loadSoundEffect("correct", "correct.wav");
        loadSoundEffect("fail", "fail.wav");
        loadSoundEffect("frying", "frying.wav");
        loadSoundEffect("pass", "pass.wav");
        loadSoundEffect("wrong", "wrong.wav");
    }
    
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
     * Memainkan musik dengan loop infinite
     */
    public void playMusic(String musicName, String filename) {
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
            if (!filename.equals("music1.mp3")) {
                System.out.println("[SoundManager] Trying fallback music...");
                playMusic("fallback", "music1.mp3");
            }
        }
    }
    
    public void stopMusic() {
        if (currentMusicPlayer != null) {
            currentMusicPlayer.stop();
            currentMusicPlayer = null;
            currentMusicName = null;
        }
    }
    
    public void setMasterVolume(double volume) {
        this.masterVolume = Math.max(0.0, Math.min(1.0, volume));
        updateAllVolumes();
    }
    
    public void setEffectsVolume(double volume) {
        this.effectsVolume = Math.max(0.0, Math.min(1.0, volume));
        updateAllVolumes();
    }
    
    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume));
        updateAllVolumes();
    }
    
    private void updateAllVolumes() {
        for (AudioClip clip : soundEffects.values()) {
            clip.setVolume(effectsVolume * masterVolume);
        }
        
        if (currentMusicPlayer != null) {
            currentMusicPlayer.setVolume(musicVolume * masterVolume);
        }
    }
    
    public void setMuted(boolean muted) {
        if (muted) {
            setMasterVolume(0.0);
        } else {
            setMasterVolume(1.0);
        }
    }
    
    public String getCurrentMusicName() {
        return currentMusicName;
    }
    
    public void dispose() {
        stopMusic();
        soundEffects.clear();
    }
}
