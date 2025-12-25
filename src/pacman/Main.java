package pacman;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * Main entry point for the Pac-Man game
 * A classic arcade game built with JavaFX
 */
public class Main extends Application {
    
    private GameController gameController;
    private Label scoreLabel;
    private Label livesLabel;
    private Label levelLabel;
    private Stage primaryStage;
    private MenuScreen menuScreen;
    private BorderPane gameRoot;
    private Scene menuScene;
    private Scene gameScene;
    
    // Game settings from menu
    private int selectedMap = 0;
    private int selectedCharacter = 0;
    private int difficulty = 1;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Create menu screen
        menuScreen = new MenuScreen(this);
        menuScene = new Scene(menuScreen);
        menuScene.setFill(Color.BLACK);
        
        // Setup menu key handling
        menuScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    startGameWithSettings(selectedMap, selectedCharacter, difficulty);
                    break;
                case ESCAPE:
                    primaryStage.close();
                    break;
                default:
                    break;
            }
        });
        
        primaryStage.setTitle("PAC-MAN - JavaFX");
        primaryStage.setScene(menuScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    /**
     * Start the game with selected settings from menu
     */
    public void startGameWithSettings(int mapIndex, int characterIndex, int difficultyLevel) {
        this.selectedMap = mapIndex;
        this.selectedCharacter = characterIndex;
        this.difficulty = difficultyLevel;
        
        // Stop menu animation
        if (menuScreen != null) {
            menuScreen.stopAnimation();
        }
        
        // Create game screen
        gameRoot = new BorderPane();
        gameRoot.setStyle("-fx-background-color: black;");
        
        // Create header with score and lives
        VBox header = createHeader();
        gameRoot.setTop(header);
        
        // Create game board with selected map
        GameBoard gameBoard = new GameBoard(mapIndex);
        gameRoot.setCenter(gameBoard);
        
        // Create game controller with settings
        gameController = new GameController(gameBoard, this, characterIndex, difficultyLevel);
        
        gameScene = new Scene(gameRoot);
        gameScene.setFill(Color.BLACK);
        
        // Handle keyboard input
        gameScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                case W:
                    gameController.setPacManDirection(Direction.UP);
                    break;
                case DOWN:
                case S:
                    gameController.setPacManDirection(Direction.DOWN);
                    break;
                case LEFT:
                case A:
                    gameController.setPacManDirection(Direction.LEFT);
                    break;
                case RIGHT:
                case D:
                    gameController.setPacManDirection(Direction.RIGHT);
                    break;
                case SPACE:
                    gameController.togglePause();
                    break;
                case R:
                    gameController.restartGame();
                    break;
                case ESCAPE:
                    returnToMenu();
                    break;
                case M:
                    returnToMenu();
                    break;
                default:
                    break;
            }
        });
        
        primaryStage.setScene(gameScene);
        
        // Start the game
        gameController.startGame();
    }
    
    /**
     * Return to main menu
     */
    public void returnToMenu() {
        if (gameController != null) {
            gameController.stopGame();
        }
        
        // Create new menu screen
        menuScreen = new MenuScreen(this);
        menuScene = new Scene(menuScreen);
        menuScene.setFill(Color.BLACK);
        
        menuScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    startGameWithSettings(selectedMap, selectedCharacter, difficulty);
                    break;
                case ESCAPE:
                    primaryStage.close();
                    break;
                default:
                    break;
            }
        });
        
        primaryStage.setScene(menuScene);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(10));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: black;");
        
        Label titleLabel = new Label("PAC-MAN");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.YELLOW);
        
        javafx.scene.layout.HBox statsBox = new javafx.scene.layout.HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        
        scoreLabel = new Label("SCORE: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        scoreLabel.setTextFill(Color.WHITE);
        
        livesLabel = new Label("LIVES: 3");
        livesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        livesLabel.setTextFill(Color.WHITE);
        
        levelLabel = new Label("LEVEL: 1");
        levelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        levelLabel.setTextFill(Color.WHITE);
        
        statsBox.getChildren().addAll(scoreLabel, livesLabel, levelLabel);
        header.getChildren().addAll(titleLabel, statsBox);
        
        return header;
    }
    
    public void updateScore(int score) {
        scoreLabel.setText("SCORE: " + score);
    }
    
    public void updateLives(int lives) {
        livesLabel.setText("LIVES: " + lives);
    }
    
    public void updateLevel(int level) {
        levelLabel.setText("LEVEL: " + level);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
