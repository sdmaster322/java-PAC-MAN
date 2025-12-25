package pacman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Represents a ghost enemy in the game
 * Each ghost has different AI behavior patterns
 */
public class Ghost {
    
    public enum GhostType {
        BLINKY(Color.RED, 13, 11),      // Red ghost - chases Pac-Man directly
        PINKY(Color.PINK, 14, 14),       // Pink ghost - ambushes ahead of Pac-Man
        INKY(Color.CYAN, 12, 14),        // Cyan ghost - unpredictable
        CLYDE(Color.ORANGE, 15, 14);     // Orange ghost - random/shy
        
        private final Color color;
        private final int startX;
        private final int startY;
        
        GhostType(Color color, int startX, int startY) {
            this.color = color;
            this.startX = startX;
            this.startY = startY;
        }
        
        public Color getColor() {
            return color;
        }
        
        public int getStartX() {
            return startX;
        }
        
        public int getStartY() {
            return startY;
        }
    }
    
    private double x, y;
    private int tileX, tileY;
    private Direction direction;
    private GhostType type;
    private GameBoard gameBoard;
    private Random random;
    
    // Ghost states
    private boolean frightened = false;
    private boolean eaten = false;
    private int frightenedTimer = 0;
    private boolean inGhostHouse = true;
    private int ghostHouseTimer = 0;
    
    // Movement
    private double speed = 1.5;
    private double moveProgress = 0;
    
    // Animation
    private int animationFrame = 0;
    private boolean blinking = false;
    
    public Ghost(GhostType type, GameBoard gameBoard) {
        this.type = type;
        this.gameBoard = gameBoard;
        this.random = new Random();
        reset();
    }
    
    public void reset() {
        this.tileX = type.getStartX();
        this.tileY = type.getStartY();
        this.x = tileX * GameBoard.TILE_SIZE;
        this.y = tileY * GameBoard.TILE_SIZE;
        this.direction = Direction.UP;
        this.frightened = false;
        this.eaten = false;
        this.frightenedTimer = 0;
        this.moveProgress = 0;
        this.inGhostHouse = true;
        this.ghostHouseTimer = getGhostHouseDelay();
    }
    
    private int getGhostHouseDelay() {
        switch (type) {
            case BLINKY: return 0;
            case PINKY: return 100;
            case INKY: return 200;
            case CLYDE: return 300;
            default: return 0;
        }
    }
    
    public void update(PacMan pacMan) {
        animationFrame++;
        
        // Handle ghost house exit
        if (inGhostHouse) {
            ghostHouseTimer--;
            if (ghostHouseTimer <= 0) {
                inGhostHouse = false;
                tileX = 13;
                tileY = 11;
                x = tileX * GameBoard.TILE_SIZE;
                y = tileY * GameBoard.TILE_SIZE;
                direction = Direction.LEFT;
            }
            return;
        }
        
        // Handle frightened mode
        if (frightened && !eaten) {
            frightenedTimer--;
            blinking = frightenedTimer < 120 && (frightenedTimer / 15) % 2 == 0;
            if (frightenedTimer <= 0) {
                frightened = false;
                blinking = false;
            }
        }
        
        // Handle eaten ghost returning to ghost house
        if (eaten) {
            speed = 4.0;
            // Check if ghost has reached the ghost house area (inside the house)
            // Ghost house is roughly at tiles (12-15, 13-15)
            if (tileY >= 13 && tileY <= 15 && tileX >= 12 && tileX <= 15) {
                // Ghost has reached the ghost house, respawn
                eaten = false;
                frightened = false;
                speed = 1.5;
                // Reset to spawn position
                tileX = type.getStartX();
                tileY = type.getStartY();
                x = tileX * GameBoard.TILE_SIZE;
                y = tileY * GameBoard.TILE_SIZE;
                direction = Direction.UP;
                inGhostHouse = true;
                ghostHouseTimer = 60; // Short delay before exiting again
            }
        }
        
        moveProgress += (eaten ? 4.0 : (frightened ? 1.0 : speed));
        
        if (moveProgress >= GameBoard.TILE_SIZE) {
            moveProgress = 0;
            
            // Move to next tile
            int nextTileX = tileX + direction.getDx();
            int nextTileY = tileY + direction.getDy();
            
            // Handle tunnel wrap-around
            if (nextTileX < 0) {
                nextTileX = GameBoard.BOARD_WIDTH - 1;
            } else if (nextTileX >= GameBoard.BOARD_WIDTH) {
                nextTileX = 0;
            }
            
            tileX = nextTileX;
            tileY = nextTileY;
            x = tileX * GameBoard.TILE_SIZE;
            y = tileY * GameBoard.TILE_SIZE;
            
            // Choose next direction
            chooseDirection(pacMan);
        } else {
            // Update pixel position
            x = tileX * GameBoard.TILE_SIZE + direction.getDx() * moveProgress;
            y = tileY * GameBoard.TILE_SIZE + direction.getDy() * moveProgress;
            
            // Handle tunnel for pixel position
            if (tileX == 0 && direction == Direction.LEFT) {
                x = tileX * GameBoard.TILE_SIZE - moveProgress;
            } else if (tileX == GameBoard.BOARD_WIDTH - 1 && direction == Direction.RIGHT) {
                x = tileX * GameBoard.TILE_SIZE + moveProgress;
            }
        }
    }
    
    private void chooseDirection(PacMan pacMan) {
        Direction[] possibleDirections = new Direction[4];
        int count = 0;
        
        // Check all four directions
        for (Direction dir : new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT}) {
            // Don't go back
            if (dir == direction.getOpposite()) {
                continue;
            }
            
            int nextX = tileX + dir.getDx();
            int nextY = tileY + dir.getDy();
            
            // Handle tunnel
            if (nextX < 0) nextX = GameBoard.BOARD_WIDTH - 1;
            else if (nextX >= GameBoard.BOARD_WIDTH) nextX = 0;
            
            // Eaten ghosts can pass through ghost house door (tile 4), others cannot
            if (gameBoard.canMove(nextX, nextY) && (eaten || gameBoard.getTile(nextX, nextY) != 4)) {
                possibleDirections[count++] = dir;
            }
        }
        
        if (count == 0) {
            // Dead end, turn around
            direction = direction.getOpposite();
            return;
        }
        
        if (count == 1) {
            direction = possibleDirections[0];
            return;
        }
        
        // Choose direction based on AI
        if (frightened && !eaten) {
            // Random movement when frightened
            direction = possibleDirections[random.nextInt(count)];
        } else if (eaten) {
            // Return to ghost house
            direction = getBestDirection(possibleDirections, count, 13, 14);
        } else {
            // Use ghost-specific AI
            int targetX, targetY;
            
            switch (type) {
                case BLINKY:
                    // Chase Pac-Man directly
                    targetX = pacMan.getTileX();
                    targetY = pacMan.getTileY();
                    break;
                case PINKY:
                    // Target 4 tiles ahead of Pac-Man
                    targetX = pacMan.getTileX() + pacMan.getDirection().getDx() * 4;
                    targetY = pacMan.getTileY() + pacMan.getDirection().getDy() * 4;
                    break;
                case INKY:
                    // Complex targeting based on Blinky and Pac-Man
                    targetX = pacMan.getTileX() + pacMan.getDirection().getDx() * 2;
                    targetY = pacMan.getTileY() + pacMan.getDirection().getDy() * 2;
                    targetX = targetX * 2 - 13; // Relative to Blinky's position
                    targetY = targetY * 2 - 11;
                    break;
                case CLYDE:
                    // Chase when far, scatter when close
                    double distance = Math.sqrt(
                        Math.pow(pacMan.getTileX() - tileX, 2) + 
                        Math.pow(pacMan.getTileY() - tileY, 2)
                    );
                    if (distance > 8) {
                        targetX = pacMan.getTileX();
                        targetY = pacMan.getTileY();
                    } else {
                        targetX = 0;
                        targetY = GameBoard.BOARD_HEIGHT - 1;
                    }
                    break;
                default:
                    targetX = pacMan.getTileX();
                    targetY = pacMan.getTileY();
            }
            
            direction = getBestDirection(possibleDirections, count, targetX, targetY);
        }
    }
    
    private Direction getBestDirection(Direction[] directions, int count, int targetX, int targetY) {
        Direction best = directions[0];
        double bestDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < count; i++) {
            int nextX = tileX + directions[i].getDx();
            int nextY = tileY + directions[i].getDy();
            
            double distance = Math.sqrt(
                Math.pow(targetX - nextX, 2) + 
                Math.pow(targetY - nextY, 2)
            );
            
            if (distance < bestDistance) {
                bestDistance = distance;
                best = directions[i];
            }
        }
        
        return best;
    }
    
    public void render(GraphicsContext gc) {
        double centerX = x + GameBoard.TILE_SIZE / 2.0;
        double centerY = y + GameBoard.TILE_SIZE / 2.0;
        double radius = GameBoard.TILE_SIZE / 2.0 - 1;
        
        if (eaten) {
            // Draw just eyes when eaten
            drawEyes(gc, centerX, centerY, radius);
            return;
        }
        
        // Ghost body color
        Color bodyColor;
        if (frightened) {
            bodyColor = blinking ? Color.WHITE : Color.BLUE;
        } else {
            bodyColor = type.getColor();
        }
        
        gc.setFill(bodyColor);
        
        // Draw ghost body (rounded top, wavy bottom)
        gc.fillArc(centerX - radius, centerY - radius, 
                   radius * 2, radius * 2,
                   0, 180, javafx.scene.shape.ArcType.ROUND);
        gc.fillRect(centerX - radius, centerY, radius * 2, radius);
        
        // Draw wavy bottom
        int waveOffset = (animationFrame / 5) % 2;
        for (int i = 0; i < 3; i++) {
            double wx = centerX - radius + i * (radius * 2 / 3);
            double wy = centerY + radius - 3;
            double wh = (i + waveOffset) % 2 == 0 ? 6 : 3;
            gc.fillOval(wx, wy, radius * 2 / 3, wh);
        }
        
        // Draw eyes
        if (!frightened) {
            drawEyes(gc, centerX, centerY, radius);
        } else {
            // Frightened face
            gc.setFill(Color.WHITE);
            gc.fillOval(centerX - radius * 0.5, centerY - radius * 0.3, 4, 4);
            gc.fillOval(centerX + radius * 0.2, centerY - radius * 0.3, 4, 4);
            
            // Wavy mouth
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.beginPath();
            gc.moveTo(centerX - radius * 0.5, centerY + radius * 0.3);
            for (int i = 0; i < 4; i++) {
                double mx = centerX - radius * 0.5 + i * radius * 0.3;
                double my = centerY + radius * 0.3 + (i % 2 == 0 ? 2 : -2);
                gc.lineTo(mx, my);
            }
            gc.stroke();
        }
    }
    
    private void drawEyes(GraphicsContext gc, double centerX, double centerY, double radius) {
        // Eye whites
        gc.setFill(Color.WHITE);
        gc.fillOval(centerX - radius * 0.6, centerY - radius * 0.4, radius * 0.5, radius * 0.6);
        gc.fillOval(centerX + radius * 0.1, centerY - radius * 0.4, radius * 0.5, radius * 0.6);
        
        // Pupils - look in direction of movement
        gc.setFill(Color.BLUE);
        double pupilOffsetX = direction.getDx() * 2;
        double pupilOffsetY = direction.getDy() * 2;
        gc.fillOval(centerX - radius * 0.45 + pupilOffsetX, centerY - radius * 0.25 + pupilOffsetY, radius * 0.25, radius * 0.35);
        gc.fillOval(centerX + radius * 0.2 + pupilOffsetX, centerY - radius * 0.25 + pupilOffsetY, radius * 0.25, radius * 0.35);
    }
    
    public void setFrightened(boolean frightened) {
        if (!eaten) {
            this.frightened = frightened;
            this.frightenedTimer = frightened ? 600 : 0; // About 10 seconds at 60 FPS
            if (frightened) {
                // Reverse direction when frightened
                this.direction = this.direction.getOpposite();
            }
        }
    }
    
    public void setEaten() {
        this.eaten = true;
        this.frightened = false;
    }
    
    public int getTileX() {
        return tileX;
    }
    
    public int getTileY() {
        return tileY;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public boolean isFrightened() {
        return frightened;
    }
    
    public boolean isEaten() {
        return eaten;
    }
    
    public GhostType getType() {
        return type;
    }
    
    public boolean isInGhostHouse() {
        return inGhostHouse;
    }
}
