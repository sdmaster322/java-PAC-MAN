package pacman;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
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
    
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");
        
        // Create header with score and lives
        VBox header = createHeader();
        root.setTop(header);
        
        // Create game board
        GameBoard gameBoard = new GameBoard();
        root.setCenter(gameBoard);
        
        // Create game controller
        gameController = new GameController(gameBoard, this);
        
        Scene scene = new Scene(root);
        scene.setFill(Color.BLACK);
        
        // Handle keyboard input
        scene.setOnKeyPressed(event -> {
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
                    primaryStage.close();
                    break;
                default:
                    break;
            }
        });
        
        primaryStage.setTitle("PAC-MAN - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        // Start the game
        gameController.startGame();
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
