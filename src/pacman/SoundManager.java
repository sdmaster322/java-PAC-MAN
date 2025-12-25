package pacman;

import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all game sound effects using synthesized audio
 */
public class SoundManager {
    
    private static SoundManager instance;
    private boolean soundEnabled = true;
    private Map<String, byte[]> soundCache;
    private float volume = 0.7f;
    
    // Sound types
    public static final String CHOMP = "chomp";
    public static final String POWER_PELLET = "power_pellet";
    public static final String EAT_GHOST = "eat_ghost";
    public static final String DEATH = "death";
    public static final String GAME_START = "game_start";
    public static final String LEVEL_COMPLETE = "level_complete";
    public static final String EXTRA_LIFE = "extra_life";
    public static final String SIREN = "siren";
    public static final String FRIGHTENED = "frightened";
    public static final String MENU_SELECT = "menu_select";
    public static final String MENU_NAVIGATE = "menu_navigate";
    
    private SoundManager() {
        soundCache = new HashMap<>();
        initializeSounds();
    }
    
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    private void initializeSounds() {
        // Pre-generate all sound effects
        soundCache.put(CHOMP, generateChompSound());
        soundCache.put(POWER_PELLET, generatePowerPelletSound());
        soundCache.put(EAT_GHOST, generateEatGhostSound());
        soundCache.put(DEATH, generateDeathSound());
        soundCache.put(GAME_START, generateGameStartSound());
        soundCache.put(LEVEL_COMPLETE, generateLevelCompleteSound());
        soundCache.put(EXTRA_LIFE, generateExtraLifeSound());
        soundCache.put(FRIGHTENED, generateFrightenedSound());
        soundCache.put(MENU_SELECT, generateMenuSelectSound());
        soundCache.put(MENU_NAVIGATE, generateMenuNavigateSound());
    }
    
    public void play(String soundName) {
        if (!soundEnabled) return;
        
        byte[] soundData = soundCache.get(soundName);
        if (soundData == null) return;
        
        // Play sound in a separate thread to avoid blocking
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(44100, 8, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                
                if (!AudioSystem.isLineSupported(info)) {
                    return;
                }
                
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                
                // Apply volume
                if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                    gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(dB, gainControl.getMaximum())));
                }
                
                line.start();
                line.write(soundData, 0, soundData.length);
                line.drain();
                line.close();
            } catch (Exception e) {
                // Silently ignore audio errors
            }
        }).start();
    }
    
    // Sound generation methods using simple waveforms
    
    private byte[] generateChompSound() {
        // Short "waka" sound
        int sampleRate = 44100;
        int duration = 50; // milliseconds
        int samples = sampleRate * duration / 1000;
        byte[] data = new byte[samples];
        
        for (int i = 0; i < samples; i++) {
            double time = (double) i / sampleRate;
            double freq = 400 + 200 * Math.sin(time * 80); // Frequency sweep
            double sample = Math.sin(2 * Math.PI * freq * time);
            sample *= 1.0 - (double) i / samples; // Fade out
            data[i] = (byte) (sample * 80);
        }
        return data;
    }
    
    private byte[] generatePowerPelletSound() {
        // Longer ascending sound
        int sampleRate = 44100;
        int duration = 200;
        int samples = sampleRate * duration / 1000;
        byte[] data = new byte[samples];
        
        for (int i = 0; i < samples; i++) {
            double time = (double) i / sampleRate;
            double freq = 200 + 600 * ((double) i / samples); // Rising frequency
            double sample = Math.sin(2 * Math.PI * freq * time);
            sample *= 0.8 * (1.0 - 0.5 * (double) i / samples);
            data[i] = (byte) (sample * 100);
        }
        return data;
    }
    
    private byte[] generateEatGhostSound() {
        // Satisfying "gulp" sound
        int sampleRate = 44100;
        int duration = 300;
        int samples = sampleRate * duration / 1000;
        byte[] data = new byte[samples];
        
        for (int i = 0; i < samples; i++) {
            double time = (double) i / sampleRate;
            double progress = (double) i / samples;
            double freq = 800 - 600 * progress; // Descending frequency
            double sample = Math.sin(2 * Math.PI * freq * time);
            // Add some harmonics
            sample += 0.3 * Math.sin(4 * Math.PI * freq * time);
            sample *= 0.7 * (1.0 - progress);
            data[i] = (byte) (sample * 90);
        }
        return data;
    }
    
    private byte[] generateDeathSound() {
        // Descending spiral death sound
        int sampleRate = 44100;
        int duration = 1500;
        int samples = sampleRate * duration / 1000;
        byte[] data = new byte[samples];
        
        for (int i = 0; i < samples; i++) {
            double time = (double) i / sampleRate;
            double progress = (double) i / samples;
            double freq = 600 * Math.pow(0.3, progress); // Exponential decay
            double sample = Math.sin(2 * Math.PI * freq * time);
            // Add wobble
            sample *= 1.0 + 0.3 * Math.sin(time * 30);
            sample *= 1.0 - progress; // Fade out
            data[i] = (byte) (sample * 100);
        }
        return data;
    }
    
    private byte[] generateGameStartSound() {
        // Classic Pac-Man intro-style jingle
        int sampleRate = 44100;
        int duration = 800;
        int samples = sampleRate * duration / 1000;
        byte[] data = new byte[samples];
        
        // Musical notes (simplified jingle)
        double[] notes = {523.25, 659.25, 783.99, 659.25, 523.25, 783.99}; // C5, E5, G5, E5, C5, G5
        int noteDuration = samples / notes.length;
        
        for (int i = 0; i < samples; i++) {
            int noteIndex = Math.min(i / noteDuration, notes.length - 1);
            double time = (double) i / sampleRate;
            double freq = notes[noteIndex];
            double sample = Math.sin(2 * Math.PI * freq * time);
            // Add envelope
            int notePosition = i % noteDuration;
            double envelope = 1.0 - Math.pow((double) notePosition / noteDuration, 2);
            sample *= envelope * 0.8;
            data[i] = (byte) (sample * 90);
        }
        return data;
    }
    
    private byte[] generateLevelCompleteSound() {
        // Victory fanfare
        int sampleRate = 44100;
        int duration = 600;
        int samples = sampleRate * duration / 1000;
        byte[] data = new byte[samples];
        
        double[] notes = {523.25, 587.33, 659.25, 783.99, 1046.50}; // C5, D5, E5, G5, C6
        int noteDuration = samples / notes.length;
        
        for (int i = 0; i < samples; i++) {
            int noteIndex = Math.min(i / noteDuration, notes.length - 1);
            double time = (double) i / sampleRate;
            double freq = notes[noteIndex];
            double sample = Math.sin(2 * Math.PI * freq * time);
            sample += 0.3 * Math.sin(4 * Math.PI * freq * time); // Harmonic
            int notePosition = i % noteDuration;
            double envelope = 1.0 - 0.5 * Math.pow((double) notePosition / noteDuration, 2);
            sample *= envelope * 0.7;
            data[i] = (byte) (sample * 90);
        }
        return data;
    }
    
    private byte[] generateExtraLifeSound() {
        // Cheerful ascending arpeggio
        int sampleRate = 44100;
        int duration = 400;
        int samples = sampleRate * duration / 1000;
        byte[] data = new byte[samples];
        
        double[] notes = {261.63, 329.63, 392.00, 523.25}; // C4, E4, G4, C5
        int noteDuration = samples / notes.length;
        
        for (int i = 0; i < samples; i++) {
            int noteIndex = Math.min(i / noteDuration, notes.length - 1);
            double time = (double) i / sampleRate;
            double freq = notes[noteIndex];
            double sample = Math.sin(2 * Math.PI * freq * time);
            sample += 0.5 * Math.sin(4 * Math.PI * freq * time);
            int notePosition = i % noteDuration;
            double envelope = 1.0 - 0.3 * ((double) notePosition / noteDuration);
            sample *= envelope * 0.8;
            data[i] = (byte) (sample * 85);
        }
        return data;
    }
    
    private byte[] generateFrightenedSound() {
        // Wobbly, nervous sound
        int sampleRate = 44100;
        int duration = 150;
        int samples = sampleRate * duration / 1000;
        byte[] data = new byte[samples];
        
        for (int i = 0; i < samples; i++) {
            double time = (double) i / sampleRate;
            double freq = 150 + 50 * Math.sin(time * 40); // Wobbling frequency
            double sample = Math.sin(2 * Math.PI * freq * time);
            sample *= 0.6;
            data[i] = (byte) (sample * 70);
        }
        return data;
    }
    
    private byte[] generateMenuSelectSound() {
        // Confirmation beep
        int sampleRate = 44100;
        int duration = 150;
        int samples = sampleRate * duration / 1000;
        byte[] data = new byte[samples];
        
        for (int i = 0; i < samples; i++) {
            double time = (double) i / sampleRate;
            double sample = Math.sin(2 * Math.PI * 880 * time); // A5
            sample += 0.5 * Math.sin(2 * Math.PI * 1108.73 * time); // C#6
            sample *= 0.6 * (1.0 - (double) i / samples);
            data[i] = (byte) (sample * 80);
        }
        return data;
    }
    
    private byte[] generateMenuNavigateSound() {
        // Short blip
        int sampleRate = 44100;
        int duration = 50;
        int samples = sampleRate * duration / 1000;
        byte[] data = new byte[samples];
        
        for (int i = 0; i < samples; i++) {
            double time = (double) i / sampleRate;
            double sample = Math.sin(2 * Math.PI * 440 * time); // A4
            sample *= 0.5 * (1.0 - (double) i / samples);
            data[i] = (byte) (sample * 70);
        }
        return data;
    }
    
    // Settings
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setVolume(float volume) {
        this.volume = Math.max(0, Math.min(1, volume));
    }
    
    public float getVolume() {
        return volume;
    }
}
