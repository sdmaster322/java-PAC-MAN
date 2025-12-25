package pacman;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameBoard extends Canvas {
    
    public static final int TILE_SIZE = 20;
    public static final int BOARD_WIDTH = 28;
    public static final int BOARD_HEIGHT = 31;
    
    // Maze layout:
    // 0 = empty (path)
    // 1 = wall
    // 2 = dot
    // 3 = power pellet
    // 4 = ghost house door
    // 5 = empty (no dot)
    private int[][] maze;
    private int[][] originalMaze;
    
    //  maze layout
    private static final int[][] MAZE_TEMPLATE = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,2,2,2,2,2,2,2,2,2,2,2,2,1,1,2,2,2,2,2,2,2,2,2,2,2,2,1},
        {1,2,1,1,1,1,2,1,1,1,1,1,2,1,1,2,1,1,1,1,1,2,1,1,1,1,2,1},
        {1,3,1,1,1,1,2,1,1,1,1,1,2,1,1,2,1,1,1,1,1,2,1,1,1,1,3,1},
        {1,2,1,1,1,1,2,1,1,1,1,1,2,1,1,2,1,1,1,1,1,2,1,1,1,1,2,1},
        {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1},
        {1,2,1,1,1,1,2,1,1,2,1,1,1,1,1,1,1,1,2,1,1,2,1,1,1,1,2,1},
        {1,2,1,1,1,1,2,1,1,2,1,1,1,1,1,1,1,1,2,1,1,2,1,1,1,1,2,1},
        {1,2,2,2,2,2,2,1,1,2,2,2,2,1,1,2,2,2,2,1,1,2,2,2,2,2,2,1},
        {1,1,1,1,1,1,2,1,1,1,1,1,5,1,1,5,1,1,1,1,1,2,1,1,1,1,1,1},
        {5,5,5,5,5,1,2,1,1,1,1,1,5,1,1,5,1,1,1,1,1,2,1,5,5,5,5,5},
        {5,5,5,5,5,1,2,1,1,5,5,5,5,5,5,5,5,5,5,1,1,2,1,5,5,5,5,5},
        {5,5,5,5,5,1,2,1,1,5,1,1,1,4,4,1,1,1,5,1,1,2,1,5,5,5,5,5},
        {1,1,1,1,1,1,2,1,1,5,1,5,5,5,5,5,5,1,5,1,1,2,1,1,1,1,1,1},
        {5,5,5,5,5,5,2,5,5,5,1,5,5,5,5,5,5,1,5,5,5,2,5,5,5,5,5,5},
        {1,1,1,1,1,1,2,1,1,5,1,5,5,5,5,5,5,1,5,1,1,2,1,1,1,1,1,1},
        {5,5,5,5,5,1,2,1,1,5,1,1,1,1,1,1,1,1,5,1,1,2,1,5,5,5,5,5},
        {5,5,5,5,5,1,2,1,1,5,5,5,5,5,5,5,5,5,5,1,1,2,1,5,5,5,5,5},
        {5,5,5,5,5,1,2,1,1,5,1,1,1,1,1,1,1,1,5,1,1,2,1,5,5,5,5,5},
        {1,1,1,1,1,1,2,1,1,5,1,1,1,1,1,1,1,1,5,1,1,2,1,1,1,1,1,1},
        {1,2,2,2,2,2,2,2,2,2,2,2,2,1,1,2,2,2,2,2,2,2,2,2,2,2,2,1},
        {1,2,1,1,1,1,2,1,1,1,1,1,2,1,1,2,1,1,1,1,1,2,1,1,1,1,2,1},
        {1,2,1,1,1,1,2,1,1,1,1,1,2,1,1,2,1,1,1,1,1,2,1,1,1,1,2,1},
        {1,3,2,2,1,1,2,2,2,2,2,2,2,5,5,2,2,2,2,2,2,2,1,1,2,2,3,1},
        {1,1,1,2,1,1,2,1,1,2,1,1,1,1,1,1,1,1,2,1,1,2,1,1,2,1,1,1},
        {1,1,1,2,1,1,2,1,1,2,1,1,1,1,1,1,1,1,2,1,1,2,1,1,2,1,1,1},
        {1,2,2,2,2,2,2,1,1,2,2,2,2,1,1,2,2,2,2,1,1,2,2,2,2,2,2,1},
        {1,2,1,1,1,1,1,1,1,1,1,1,2,1,1,2,1,1,1,1,1,1,1,1,1,1,2,1},
        {1,2,1,1,1,1,1,1,1,1,1,1,2,1,1,2,1,1,1,1,1,1,1,1,1,1,2,1},
        {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };
    
    private int dotsRemaining;
    private int totalDots;
    
    public GameBoard() {
        super(BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE);
        initMaze();
    }
    
    public void initMaze() {
        maze = new int[BOARD_HEIGHT][BOARD_WIDTH];
        originalMaze = new int[BOARD_HEIGHT][BOARD_WIDTH];
        dotsRemaining = 0;
        
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                maze[y][x] = MAZE_TEMPLATE[y][x];
                originalMaze[y][x] = MAZE_TEMPLATE[y][x];
                if (maze[y][x] == 2 || maze[y][x] == 3) {
                    dotsRemaining++;
                }
            }
        }
        totalDots = dotsRemaining;
    }
    
    public void resetMaze() {
        dotsRemaining = 0;
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                maze[y][x] = originalMaze[y][x];
                if (maze[y][x] == 2 || maze[y][x] == 3) {
                    dotsRemaining++;
                }
            }
        }
    }
    
    public void render(PacMan pacMan, Ghost[] ghosts) {
        GraphicsContext gc = getGraphicsContext2D();
        
        // Clear the canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw maze
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                int tile = maze[y][x];
                double px = x * TILE_SIZE;
                double py = y * TILE_SIZE;
                
                switch (tile) {
                    case 1: // Wall
                        gc.setFill(Color.BLUE);
                        gc.fillRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                        gc.setStroke(Color.DARKBLUE);
                        gc.strokeRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                        break;
                    case 2: // Dot
                        gc.setFill(Color.WHITE);
                        gc.fillOval(px + TILE_SIZE/2 - 2, py + TILE_SIZE/2 - 2, 4, 4);
                        break;
                    case 3: // Power pellet
                        gc.setFill(Color.WHITE);
                        gc.fillOval(px + TILE_SIZE/2 - 5, py + TILE_SIZE/2 - 5, 10, 10);
                        break;
                    case 4: // Ghost house door
                        gc.setFill(Color.PINK);
                        gc.fillRect(px, py + TILE_SIZE/2 - 2, TILE_SIZE, 4);
                        break;
                }
            }
        }
        
        // Draw ghosts
        if (ghosts != null) {
            for (Ghost ghost : ghosts) {
                ghost.render(gc);
            }
        }
        
        // Draw Pac-Man
        if (pacMan != null) {
            pacMan.render(gc);
        }
    }
    
    public boolean isWall(int x, int y) {
        if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
            // Allow tunnel wrap-around
            if (y == 14 && (x < 0 || x >= BOARD_WIDTH)) {
                return false;
            }
            return true;
        }
        return maze[y][x] == 1;
    }
    
    public boolean isDot(int x, int y) {
        if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
            return false;
        }
        return maze[y][x] == 2;
    }
    
    public boolean isPowerPellet(int x, int y) {
        if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
            return false;
        }
        return maze[y][x] == 3;
    }
    
    public void eatDot(int x, int y) {
        if (x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT) {
            if (maze[y][x] == 2 || maze[y][x] == 3) {
                maze[y][x] = 5;
                dotsRemaining--;
            }
        }
    }
    
    public int getDotsRemaining() {
        return dotsRemaining;
    }
    
    public int getTotalDots() {
        return totalDots;
    }
    
    public int getTile(int x, int y) {
        if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
            return 0;
        }
        return maze[y][x];
    }
    
    public boolean canMove(int x, int y) {
        // Handle tunnel
        if (y == 14 && (x < 0 || x >= BOARD_WIDTH)) {
            return true;
        }
        if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
            return false;
        }
        return maze[y][x] != 1;
    }
    
    public void renderGameOver(int score, int level) {
        GraphicsContext gc = getGraphicsContext2D();
        
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;
        
        // Semi-transparent overlay
        gc.setFill(Color.rgb(0, 0, 0, 0.85));
        gc.fillRect(0, 0, getWidth(), getHeight());
        
        // Set text alignment to center
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        
        // Game Over text
        gc.setFill(Color.RED);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 48));
        gc.fillText("GAME OVER", centerX, centerY - 80);
        
        // Score text
        gc.setFill(Color.YELLOW);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 28));
        gc.fillText("FINAL SCORE: " + score, centerX, centerY - 20);
        
        // Level text
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 20));
        gc.fillText("Level Reached: " + level, centerX, centerY + 30);
        
        // Restart instruction with blinking effect
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));
        if ((System.currentTimeMillis() / 500) % 2 == 0) {
            gc.setFill(Color.CYAN);
        } else {
            gc.setFill(Color.WHITE);
        }
        gc.fillText("Press R to Restart", centerX, centerY + 90);
        
        // Reset text alignment for other rendering
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        gc.setTextBaseline(javafx.geometry.VPos.BASELINE);
    }
}
