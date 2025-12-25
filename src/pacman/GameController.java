package pacman;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Main game controller handling game logic, timing, and state management
 */
public class GameController {
    
    private GameBoard gameBoard;
    private Main mainApp;
    private PacMan pacMan;
    private Ghost[] ghosts;
    
    private AnimationTimer gameLoop;
    private boolean running = false;
    private boolean paused = false;
    private boolean gameOver = false;
    
    // Game state
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    private int ghostsEatenCombo = 0;
    
    // Points
    private static final int DOT_POINTS = 10;
    private static final int POWER_PELLET_POINTS = 50;
    private static final int[] GHOST_POINTS = {200, 400, 800, 1600};
    
    // Timing
    private long lastUpdate = 0;
    private static final long FRAME_TIME = 16_666_667; // ~60 FPS in nanoseconds
    
    public GameController(GameBoard gameBoard, Main mainApp) {
        this.gameBoard = gameBoard;
        this.mainApp = mainApp;
        initGame();
    }
    
    private void initGame() {
        pacMan = new PacMan(gameBoard);
        ghosts = new Ghost[4];
        ghosts[0] = new Ghost(Ghost.GhostType.BLINKY, gameBoard);
        ghosts[1] = new Ghost(Ghost.GhostType.PINKY, gameBoard);
        ghosts[2] = new Ghost(Ghost.GhostType.INKY, gameBoard);
        ghosts[3] = new Ghost(Ghost.GhostType.CLYDE, gameBoard);
        
        createGameLoop();
    }
    
    private void createGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                
                if (now - lastUpdate >= FRAME_TIME) {
                    if (gameOver) {
                        renderGameOver();
                    } else if (!paused) {
                        update();
                        render();
                    }
                    lastUpdate = now;
                }
            }
        };
    }
    
    public void startGame() {
        running = true;
        gameLoop.start();
        render();
    }
    
    public void stopGame() {
        running = false;
        gameLoop.stop();
    }
    
    public void togglePause() {
        paused = !paused;
        if (paused) {
            showMessage("PAUSED", "Press SPACE to continue");
        }
    }
    
    public void restartGame() {
        score = 0;
        lives = 3;
        level = 1;
        gameOver = false;
        ghostsEatenCombo = 0;
        
        gameBoard.initMaze();
        pacMan.reset();
        for (Ghost ghost : ghosts) {
            ghost.reset();
        }
        
        mainApp.updateScore(score);
        mainApp.updateLives(lives);
        mainApp.updateLevel(level);
        
        if (!running) {
            startGame();
        }
        paused = false;
    }
    
    private void update() {
        if (!pacMan.isAlive()) {
            // Still need to update PacMan for death animation
            pacMan.update();
            if (pacMan.isDeathAnimationComplete()) {
                handleDeath();
            }
            return;
        }
        
        // Update Pac-Man
        pacMan.update();
        
        // Check for dot eating
        int px = pacMan.getTileX();
        int py = pacMan.getTileY();
        
        if (gameBoard.isDot(px, py)) {
            gameBoard.eatDot(px, py);
            score += DOT_POINTS;
            mainApp.updateScore(score);
        } else if (gameBoard.isPowerPellet(px, py)) {
            gameBoard.eatDot(px, py);
            score += POWER_PELLET_POINTS;
            mainApp.updateScore(score);
            activatePowerMode();
        }
        
        // Check for level completion
        if (gameBoard.getDotsRemaining() == 0) {
            nextLevel();
            return;
        }
        
        // Update ghosts
        for (Ghost ghost : ghosts) {
            ghost.update(pacMan);
        }
        
        // Check collisions
        checkCollisions();
    }
    
    private void activatePowerMode() {
        ghostsEatenCombo = 0;
        for (Ghost ghost : ghosts) {
            ghost.setFrightened(true);
        }
    }
    
    private void checkCollisions() {
        int pacX = pacMan.getTileX();
        int pacY = pacMan.getTileY();
        
        for (Ghost ghost : ghosts) {
            if (ghost.isInGhostHouse()) continue;
            
            int ghostX = ghost.getTileX();
            int ghostY = ghost.getTileY();
            
            // Check if same tile
            if (pacX == ghostX && pacY == ghostY) {
                if (ghost.isFrightened() && !ghost.isEaten()) {
                    // Eat the ghost
                    ghost.setEaten();
                    int points = GHOST_POINTS[Math.min(ghostsEatenCombo, 3)];
                    score += points;
                    ghostsEatenCombo++;
                    mainApp.updateScore(score);
                } else if (!ghost.isEaten()) {
                    // Pac-Man dies
                    pacMan.die();
                }
            }
            
            // Also check proximity for smoother collision
            double dx = Math.abs(pacMan.getX() - ghost.getX());
            double dy = Math.abs(pacMan.getY() - ghost.getY());
            if (dx < GameBoard.TILE_SIZE * 0.7 && dy < GameBoard.TILE_SIZE * 0.7) {
                if (ghost.isFrightened() && !ghost.isEaten()) {
                    ghost.setEaten();
                    int points = GHOST_POINTS[Math.min(ghostsEatenCombo, 3)];
                    score += points;
                    ghostsEatenCombo++;
                    mainApp.updateScore(score);
                } else if (!ghost.isEaten()) {
                    pacMan.die();
                }
            }
        }
    }
    
    private void handleDeath() {
        lives--;
        mainApp.updateLives(lives);
        
        if (lives <= 0) {
            gameOver = true;
            showGameOver();
        } else {
            // Reset positions
            pacMan.reset();
            for (Ghost ghost : ghosts) {
                ghost.reset();
            }
        }
    }
    
    private void nextLevel() {
        level++;
        mainApp.updateLevel(level);
        
        // Reset board with all dots
        gameBoard.resetMaze();
        
        // Reset positions
        pacMan.reset();
        for (Ghost ghost : ghosts) {
            ghost.reset();
        }
        
        // Show level message
        paused = true;
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Level Complete!");
            alert.setHeaderText("Level " + (level - 1) + " Complete!");
            alert.setContentText("Get ready for Level " + level + "!");
            alert.showAndWait();
            paused = false;
        });
    }
    
    private void showGameOver() {
        // Game over is now handled by the game loop rendering
        // No dialog needed
    }
    
    private void renderGameOver() {
        gameBoard.render(pacMan, ghosts);
        gameBoard.renderGameOver(score, level);
    }
    
    private void showMessage(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.show();
        });
    }
    
    private void render() {
        gameBoard.render(pacMan, ghosts);
    }
    
    public void setPacManDirection(Direction direction) {
        if (pacMan != null && !paused && !gameOver) {
            pacMan.setDirection(direction);
        }
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public int getScore() {
        return score;
    }
    
    public int getLives() {
        return lives;
    }
    
    public int getLevel() {
        return level;
    }
}
