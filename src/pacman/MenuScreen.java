package pacman;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Main menu screen for Pac-Man game
 */
public class MenuScreen extends StackPane {
    
    private Main mainApp;
    private VBox mainMenu;
    private VBox optionsPanel;
    private VBox mapsPanel;
    private VBox charactersPanel;
    
    // Game settings
    private int selectedMap = 0;
    private int selectedCharacter = 0;
    private int difficulty = 1; // 0=Easy, 1=Normal, 2=Hard
    private boolean soundEnabled = true;
    
    // Animation
    private Canvas animationCanvas;
    private AnimationTimer menuAnimation;
    private double animationPhase = 0;
    
    // Character options
    public static final String[] CHARACTER_NAMES = {"Classic Yellow", "Ms. Pac-Man", "Blue Pac", "Red Pac", "Green Pac"};
    public static final Color[] CHARACTER_COLORS = {Color.YELLOW, Color.HOTPINK, Color.CYAN, Color.RED, Color.LIMEGREEN};
    
    // Map options
    public static final String[] MAP_NAMES = {"Classic", "Open Arena", "Maze Runner", "Spiral"};
    
    public MenuScreen(Main mainApp) {
        this.mainApp = mainApp;
        setStyle("-fx-background-color: black;");
        setPrefSize(560, 680);
        
        createAnimationCanvas();
        createMainMenu();
        createOptionsPanel();
        createMapsPanel();
        createCharactersPanel();
        
        getChildren().addAll(animationCanvas, mainMenu);
        
        startAnimation();
    }
    
    private void createAnimationCanvas() {
        animationCanvas = new Canvas(560, 680);
        drawAnimatedBackground();
    }
    
    private void startAnimation() {
        menuAnimation = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 50_000_000) { // 20 FPS for background
                    animationPhase += 0.05;
                    drawAnimatedBackground();
                    lastUpdate = now;
                }
            }
        };
        menuAnimation.start();
    }
    
    private void drawAnimatedBackground() {
        GraphicsContext gc = animationCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 560, 680);
        
        // Draw animated dots pattern
        gc.setFill(Color.rgb(0, 0, 50));
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 34; j++) {
                double offset = Math.sin(animationPhase + i * 0.2 + j * 0.2) * 2;
                gc.fillOval(i * 20 + 8 + offset, j * 20 + 8 + offset, 4, 4);
            }
        }
        
        // Draw animated ghosts in background
        double ghostY = 600;
        for (int i = 0; i < 4; i++) {
            double ghostX = (animationPhase * 50 + i * 140) % 700 - 70;
            drawGhostShape(gc, ghostX, ghostY, getGhostColor(i), 0.3);
        }
        
        // Draw moving Pac-Man
        double pacX = (animationPhase * 60) % 700 - 50;
        drawPacManShape(gc, pacX, ghostY, CHARACTER_COLORS[selectedCharacter], 0.4);
    }
    
    private void drawGhostShape(GraphicsContext gc, double x, double y, Color color, double opacity) {
        gc.setGlobalAlpha(opacity);
        gc.setFill(color);
        gc.fillOval(x, y, 30, 30);
        gc.fillRect(x, y + 15, 30, 15);
        // Wavy bottom
        for (int i = 0; i < 3; i++) {
            gc.fillOval(x + i * 10, y + 25, 10, 10);
        }
        gc.setGlobalAlpha(1.0);
    }
    
    private void drawPacManShape(GraphicsContext gc, double x, double y, Color color, double opacity) {
        gc.setGlobalAlpha(opacity);
        gc.setFill(color);
        double mouthAngle = 30 + Math.sin(animationPhase * 5) * 15;
        gc.fillArc(x, y, 30, 30, mouthAngle, 360 - mouthAngle * 2, javafx.scene.shape.ArcType.ROUND);
        gc.setGlobalAlpha(1.0);
    }
    
    private Color getGhostColor(int index) {
        switch (index) {
            case 0: return Color.RED;
            case 1: return Color.PINK;
            case 2: return Color.CYAN;
            case 3: return Color.ORANGE;
            default: return Color.RED;
        }
    }
    
    private void createMainMenu() {
        mainMenu = new VBox(20);
        mainMenu.setAlignment(Pos.CENTER);
        mainMenu.setPadding(new Insets(50));
        
        // Title with glow effect
        Label title = new Label("PAC-MAN");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        title.setTextFill(Color.YELLOW);
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.ORANGE);
        shadow.setRadius(20);
        Glow glow = new Glow(0.8);
        glow.setInput(shadow);
        title.setEffect(glow);
        
        // Subtitle
        Label subtitle = new Label("JAVA EDITION");
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        subtitle.setTextFill(Color.WHITE);
        
        // Menu buttons
        Button startBtn = createMenuButton("▶  START GAME", Color.LIMEGREEN);
        startBtn.setOnAction(e -> {
            SoundManager.getInstance().play(SoundManager.MENU_SELECT);
            startGame();
        });
        
        Button mapsBtn = createMenuButton("🗺  SELECT MAP", Color.DODGERBLUE);
        mapsBtn.setOnAction(e -> {
            SoundManager.getInstance().play(SoundManager.MENU_NAVIGATE);
            showPanel(mapsPanel);
        });
        
        Button charactersBtn = createMenuButton("👤  CHARACTERS", Color.HOTPINK);
        charactersBtn.setOnAction(e -> {
            SoundManager.getInstance().play(SoundManager.MENU_NAVIGATE);
            showPanel(charactersPanel);
        });
        
        Button optionsBtn = createMenuButton("⚙  OPTIONS", Color.ORANGE);
        optionsBtn.setOnAction(e -> {
            SoundManager.getInstance().play(SoundManager.MENU_NAVIGATE);
            showPanel(optionsPanel);
        });
        
        Button exitBtn = createMenuButton("✕  EXIT", Color.RED);
        exitBtn.setOnAction(e -> System.exit(0));
        
        // Info text
        Label infoLabel = new Label("Use Arrow Keys or WASD to move\nSPACE to pause • R to restart • ESC to quit");
        infoLabel.setFont(Font.font("Arial", 12));
        infoLabel.setTextFill(Color.GRAY);
        infoLabel.setAlignment(Pos.CENTER);
        
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startBtn, mapsBtn, charactersBtn, optionsBtn, exitBtn);
        
        mainMenu.getChildren().addAll(title, subtitle, 
            createSpacer(30), buttonBox, createSpacer(20), infoLabel);
    }
    
    private Button createMenuButton(String text, Color hoverColor) {
        Button btn = new Button(text);
        btn.setPrefWidth(280);
        btn.setPrefHeight(50);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #333333, #1a1a1a);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #555555;" +
            "-fx-border-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        String hoverColorHex = String.format("#%02X%02X%02X",
            (int)(hoverColor.getRed() * 255),
            (int)(hoverColor.getGreen() * 255),
            (int)(hoverColor.getBlue() * 255));
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + hoverColorHex + ", #333333);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + hoverColorHex + ";" +
                "-fx-border-radius: 10;" +
                "-fx-cursor: hand;"
            );
            btn.setEffect(new Glow(0.3));
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #333333, #1a1a1a);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #555555;" +
                "-fx-border-radius: 10;" +
                "-fx-cursor: hand;"
            );
            btn.setEffect(null);
        });
        
        return btn;
    }
    
    private void createOptionsPanel() {
        optionsPanel = new VBox(20);
        optionsPanel.setAlignment(Pos.CENTER);
        optionsPanel.setPadding(new Insets(50));
        optionsPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.95);");
        
        Label title = createPanelTitle("OPTIONS");
        
        // Difficulty
        Label diffLabel = new Label("DIFFICULTY");
        diffLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        diffLabel.setTextFill(Color.WHITE);
        
        HBox diffBox = new HBox(10);
        diffBox.setAlignment(Pos.CENTER);
        
        Button easyBtn = createSmallButton("EASY", difficulty == 0);
        Button normalBtn = createSmallButton("NORMAL", difficulty == 1);
        Button hardBtn = createSmallButton("HARD", difficulty == 2);
        
        easyBtn.setOnAction(e -> {
            difficulty = 0;
            updateDifficultyButtons(easyBtn, normalBtn, hardBtn);
            SoundManager.getInstance().play(SoundManager.MENU_SELECT);
        });
        normalBtn.setOnAction(e -> {
            difficulty = 1;
            updateDifficultyButtons(easyBtn, normalBtn, hardBtn);
            SoundManager.getInstance().play(SoundManager.MENU_SELECT);
        });
        hardBtn.setOnAction(e -> {
            difficulty = 2;
            updateDifficultyButtons(easyBtn, normalBtn, hardBtn);
            SoundManager.getInstance().play(SoundManager.MENU_SELECT);
        });
        
        diffBox.getChildren().addAll(easyBtn, normalBtn, hardBtn);
        
        // Speed slider
        Label speedLabel = new Label("GAME SPEED");
        speedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        speedLabel.setTextFill(Color.WHITE);
        
        Slider speedSlider = new Slider(0.5, 2.0, 1.0);
        speedSlider.setMaxWidth(200);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);
        
        // Sound toggle
        Button soundBtn = createSmallButton(soundEnabled ? "🔊 SOUND: ON" : "🔇 SOUND: OFF", soundEnabled);
        soundBtn.setPrefWidth(180);
        soundBtn.setOnAction(e -> {
            soundEnabled = !soundEnabled;
            soundBtn.setText(soundEnabled ? "🔊 SOUND: ON" : "🔇 SOUND: OFF");
            updateButtonSelected(soundBtn, soundEnabled);
            SoundManager.getInstance().setSoundEnabled(soundEnabled);
            if (soundEnabled) {
                SoundManager.getInstance().play(SoundManager.MENU_SELECT);
            }
        });
        
        // Volume slider
        Label volumeLabel = new Label("VOLUME");
        volumeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        volumeLabel.setTextFill(Color.WHITE);
        
        Slider volumeSlider = new Slider(0, 1.0, 0.7);
        volumeSlider.setMaxWidth(200);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(0.25);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            SoundManager.getInstance().setVolume(newVal.floatValue());
        });
        
        // Back button
        Button backBtn = createMenuButton("← BACK", Color.GRAY);
        backBtn.setOnAction(e -> {
            SoundManager.getInstance().play(SoundManager.MENU_NAVIGATE);
            showPanel(mainMenu);
        });
        
        optionsPanel.getChildren().addAll(title, createSpacer(20),
            diffLabel, diffBox, createSpacer(10),
            speedLabel, speedSlider, createSpacer(10),
            soundBtn, createSpacer(10),
            volumeLabel, volumeSlider, createSpacer(20), backBtn);
    }
    
    private void createMapsPanel() {
        mapsPanel = new VBox(20);
        mapsPanel.setAlignment(Pos.CENTER);
        mapsPanel.setPadding(new Insets(50));
        mapsPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.95);");
        
        Label title = createPanelTitle("SELECT MAP");
        
        VBox mapButtons = new VBox(15);
        mapButtons.setAlignment(Pos.CENTER);
        
        for (int i = 0; i < MAP_NAMES.length; i++) {
            final int mapIndex = i;
            Button mapBtn = createMapButton(MAP_NAMES[i], getMapDescription(i), i == selectedMap);
            mapBtn.setOnAction(e -> {
                selectedMap = mapIndex;
                updateMapButtons(mapButtons);
                SoundManager.getInstance().play(SoundManager.MENU_SELECT);
            });
            mapButtons.getChildren().add(mapBtn);
        }
        
        // Back button
        Button backBtn = createMenuButton("← BACK", Color.GRAY);
        backBtn.setOnAction(e -> {
            SoundManager.getInstance().play(SoundManager.MENU_NAVIGATE);
            showPanel(mainMenu);
        });
        
        mapsPanel.getChildren().addAll(title, createSpacer(10), mapButtons, createSpacer(20), backBtn);
    }
    
    private Button createMapButton(String name, String description, boolean selected) {
        Button btn = new Button(name + "\n" + description);
        btn.setPrefWidth(300);
        btn.setPrefHeight(60);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        updateButtonSelected(btn, selected);
        return btn;
    }
    
    private String getMapDescription(int index) {
        switch (index) {
            case 0: return "The original Pac-Man maze";
            case 1: return "Wide open spaces";
            case 2: return "Complex corridors";
            case 3: return "Spiral pattern layout";
            default: return "";
        }
    }
    
    private void createCharactersPanel() {
        charactersPanel = new VBox(20);
        charactersPanel.setAlignment(Pos.CENTER);
        charactersPanel.setPadding(new Insets(50));
        charactersPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.95);");
        
        Label title = createPanelTitle("SELECT CHARACTER");
        
        // Character preview
        Canvas preview = new Canvas(80, 80);
        drawCharacterPreview(preview, selectedCharacter);
        
        Label charName = new Label(CHARACTER_NAMES[selectedCharacter]);
        charName.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        charName.setTextFill(CHARACTER_COLORS[selectedCharacter]);
        
        // Character selection buttons
        HBox charButtons = new HBox(10);
        charButtons.setAlignment(Pos.CENTER);
        
        for (int i = 0; i < CHARACTER_NAMES.length; i++) {
            final int charIndex = i;
            Button charBtn = createCharacterButton(i, i == selectedCharacter);
            charBtn.setOnAction(e -> {
                selectedCharacter = charIndex;
                drawCharacterPreview(preview, selectedCharacter);
                charName.setText(CHARACTER_NAMES[selectedCharacter]);
                charName.setTextFill(CHARACTER_COLORS[selectedCharacter]);
                updateCharacterButtons(charButtons);
                SoundManager.getInstance().play(SoundManager.MENU_SELECT);
            });
            charButtons.getChildren().add(charBtn);
        }
        
        // Back button
        Button backBtn = createMenuButton("← BACK", Color.GRAY);
        backBtn.setOnAction(e -> {
            SoundManager.getInstance().play(SoundManager.MENU_NAVIGATE);
            showPanel(mainMenu);
        });
        
        charactersPanel.getChildren().addAll(title, createSpacer(10), 
            preview, charName, createSpacer(10), charButtons, createSpacer(30), backBtn);
    }
    
    private Button createCharacterButton(int charIndex, boolean selected) {
        Button btn = new Button();
        btn.setPrefSize(50, 50);
        
        Canvas canvas = new Canvas(40, 40);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(CHARACTER_COLORS[charIndex]);
        gc.fillArc(5, 5, 30, 30, 30, 300, javafx.scene.shape.ArcType.ROUND);
        
        btn.setGraphic(canvas);
        
        if (selected) {
            btn.setStyle(
                "-fx-background-color: #444444;" +
                "-fx-border-color: " + toHexColor(CHARACTER_COLORS[charIndex]) + ";" +
                "-fx-border-width: 3;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: #222222;" +
                "-fx-border-color: #555555;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
            );
        }
        
        return btn;
    }
    
    private void drawCharacterPreview(Canvas canvas, int charIndex) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 80, 80);
        gc.setFill(CHARACTER_COLORS[charIndex]);
        double mouthAngle = 35;
        gc.fillArc(10, 10, 60, 60, mouthAngle, 360 - mouthAngle * 2, javafx.scene.shape.ArcType.ROUND);
    }
    
    private Label createPanelTitle(String text) {
        Label title = new Label(text);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.YELLOW);
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.ORANGE);
        shadow.setRadius(10);
        title.setEffect(shadow);
        
        return title;
    }
    
    private Button createSmallButton(String text, boolean selected) {
        Button btn = new Button(text);
        btn.setPrefWidth(80);
        btn.setPrefHeight(35);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        updateButtonSelected(btn, selected);
        return btn;
    }
    
    private void updateButtonSelected(Button btn, boolean selected) {
        if (selected) {
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #81C784;" +
                "-fx-border-radius: 8;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #333333, #1a1a1a);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #555555;" +
                "-fx-border-radius: 8;"
            );
        }
    }
    
    private void updateDifficultyButtons(Button easy, Button normal, Button hard) {
        updateButtonSelected(easy, difficulty == 0);
        updateButtonSelected(normal, difficulty == 1);
        updateButtonSelected(hard, difficulty == 2);
    }
    
    private void updateMapButtons(VBox container) {
        for (int i = 0; i < container.getChildren().size(); i++) {
            if (container.getChildren().get(i) instanceof Button) {
                Button btn = (Button) container.getChildren().get(i);
                updateButtonSelected(btn, i == selectedMap);
            }
        }
    }
    
    private void updateCharacterButtons(HBox container) {
        for (int i = 0; i < container.getChildren().size(); i++) {
            if (container.getChildren().get(i) instanceof Button) {
                Button btn = (Button) container.getChildren().get(i);
                if (i == selectedCharacter) {
                    btn.setStyle(
                        "-fx-background-color: #444444;" +
                        "-fx-border-color: " + toHexColor(CHARACTER_COLORS[i]) + ";" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
                    );
                } else {
                    btn.setStyle(
                        "-fx-background-color: #222222;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
                    );
                }
            }
        }
    }
    
    private String toHexColor(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
    
    private javafx.scene.layout.Region createSpacer(double height) {
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        spacer.setPrefHeight(height);
        return spacer;
    }
    
    private void showPanel(VBox panel) {
        getChildren().removeIf(node -> node instanceof VBox);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), panel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        getChildren().add(panel);
        fadeIn.play();
    }
    
    private void startGame() {
        menuAnimation.stop();
        mainApp.startGameWithSettings(selectedMap, selectedCharacter, difficulty);
    }
    
    public void stopAnimation() {
        if (menuAnimation != null) {
            menuAnimation.stop();
        }
    }
    
    // Getters for settings
    public int getSelectedMap() { return selectedMap; }
    public int getSelectedCharacter() { return selectedCharacter; }
    public int getDifficulty() { return difficulty; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public Color getCharacterColor() { return CHARACTER_COLORS[selectedCharacter]; }
}
