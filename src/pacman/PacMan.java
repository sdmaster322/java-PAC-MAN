package pacman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents the Pac-Man player character
 */
public class PacMan {
    
    private double x, y;
    private int tileX, tileY;
    private Direction direction;
    private Direction nextDirection;
    private GameBoard gameBoard;
    
    // Animation
    private double mouthAngle = 45;
    private double mouthDirection = 5;
    private boolean alive = true;
    private int deathAnimationFrame = 0;
    
    // Movement
    private double speed = 2.0;
    private double moveProgress = 0;
    private boolean moving = false;
    
    // Starting position
    public static final int START_TILE_X = 14;
    public static final int START_TILE_Y = 23;
    
    public PacMan(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        reset();
    }
    
    public void reset() {
        this.tileX = START_TILE_X;
        this.tileY = START_TILE_Y;
        this.x = tileX * GameBoard.TILE_SIZE;
        this.y = tileY * GameBoard.TILE_SIZE;
        this.direction = Direction.NONE;
        this.nextDirection = Direction.NONE;
        this.moving = false;
        this.moveProgress = 0;
        this.alive = true;
        this.deathAnimationFrame = 0;
        this.mouthAngle = 45;
    }
    
    public void setDirection(Direction dir) {
        this.nextDirection = dir;
    }
    
    public void update() {
        if (!alive) {
            // Death animation
            deathAnimationFrame++;
            return;
        }
        
        // Try to change direction if requested
        if (nextDirection != Direction.NONE && nextDirection != direction) {
            int nextTileX = tileX + nextDirection.getDx();
            int nextTileY = tileY + nextDirection.getDy();
            
            if (gameBoard.canMove(nextTileX, nextTileY)) {
                direction = nextDirection;
                moving = true;
            }
        }
        
        if (direction == Direction.NONE) {
            return;
        }
        
        // Calculate next tile
        int nextTileX = tileX + direction.getDx();
        int nextTileY = tileY + direction.getDy();
        
        // Handle tunnel wrap-around
        if (nextTileX < 0) {
            nextTileX = GameBoard.BOARD_WIDTH - 1;
        } else if (nextTileX >= GameBoard.BOARD_WIDTH) {
            nextTileX = 0;
        }
        
        // Check if can move
        if (!gameBoard.canMove(nextTileX, nextTileY)) {
            moving = false;
            return;
        }
        
        moving = true;
        moveProgress += speed;
        
        // Update pixel position
        double targetX = tileX * GameBoard.TILE_SIZE + direction.getDx() * moveProgress;
        double targetY = tileY * GameBoard.TILE_SIZE + direction.getDy() * moveProgress;
        
        // Handle tunnel wrap-around for pixel position
        if (direction == Direction.LEFT && tileX == 0) {
            targetX = tileX * GameBoard.TILE_SIZE - moveProgress;
            if (targetX < -GameBoard.TILE_SIZE) {
                targetX = (GameBoard.BOARD_WIDTH - 1) * GameBoard.TILE_SIZE;
            }
        } else if (direction == Direction.RIGHT && tileX == GameBoard.BOARD_WIDTH - 1) {
            targetX = tileX * GameBoard.TILE_SIZE + moveProgress;
            if (targetX > GameBoard.BOARD_WIDTH * GameBoard.TILE_SIZE) {
                targetX = 0;
            }
        }
        
        x = targetX;
        y = targetY;
        
        // Check if reached next tile
        if (moveProgress >= GameBoard.TILE_SIZE) {
            tileX = nextTileX;
            tileY = nextTileY;
            x = tileX * GameBoard.TILE_SIZE;
            y = tileY * GameBoard.TILE_SIZE;
            moveProgress = 0;
        }
        
        // Animate mouth
        mouthAngle += mouthDirection;
        if (mouthAngle >= 45 || mouthAngle <= 5) {
            mouthDirection = -mouthDirection;
        }
    }
    
    public void render(GraphicsContext gc) {
        double centerX = x + GameBoard.TILE_SIZE / 2.0;
        double centerY = y + GameBoard.TILE_SIZE / 2.0;
        double radius = GameBoard.TILE_SIZE / 2.0 - 1;
        
        if (!alive) {
            // Death animation - Pac-Man shrinking/disappearing
            double shrinkFactor = Math.max(0, 1 - deathAnimationFrame / 30.0);
            gc.setFill(Color.YELLOW);
            double deathAngle = deathAnimationFrame * 6;
            gc.fillArc(centerX - radius * shrinkFactor, 
                      centerY - radius * shrinkFactor,
                      radius * 2 * shrinkFactor, 
                      radius * 2 * shrinkFactor,
                      90 + deathAngle, 
                      360 - deathAngle * 2, 
                      javafx.scene.shape.ArcType.ROUND);
            return;
        }
        
        gc.setFill(Color.YELLOW);
        
        // Calculate rotation based on direction
        double startAngle = mouthAngle;
        double arcExtent = 360 - 2 * mouthAngle;
        
        switch (direction) {
            case RIGHT:
                startAngle = mouthAngle;
                break;
            case LEFT:
                startAngle = 180 + mouthAngle;
                break;
            case UP:
                startAngle = 90 + mouthAngle;
                break;
            case DOWN:
                startAngle = 270 + mouthAngle;
                break;
            default:
                startAngle = mouthAngle;
        }
        
        gc.fillArc(centerX - radius, centerY - radius, 
                   radius * 2, radius * 2,
                   startAngle, arcExtent, 
                   javafx.scene.shape.ArcType.ROUND);
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
    
    public Direction getDirection() {
        return direction;
    }
    
    public boolean isMoving() {
        return moving;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void die() {
        alive = false;
        deathAnimationFrame = 0;
    }
    
    public int getDeathAnimationFrame() {
        return deathAnimationFrame;
    }
    
    public boolean isDeathAnimationComplete() {
        return deathAnimationFrame > 60;
    }
}
