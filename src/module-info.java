module pacman {
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires java.desktop;
    
    opens pacman to javafx.graphics, javafx.base, javafx.controls;
    exports pacman;
}
