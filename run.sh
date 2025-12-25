#!/bin/bash

echo "========================================"
echo "       PAC-MAN - JavaFX Game"
echo "========================================"
echo

# Path to JavaFX - Update this path to your JavaFX installation
# Download JavaFX from: https://openjfx.io/
if [ -z "$PATH_TO_FX" ]; then
    # Try common locations
    if [ -d "/usr/share/openjfx/lib" ]; then
        PATH_TO_FX="/usr/share/openjfx/lib"
    elif [ -d "$HOME/javafx-sdk-21/lib" ]; then
        PATH_TO_FX="$HOME/javafx-sdk-21/lib"
    elif [ -d "$HOME/javafx-sdk-17/lib" ]; then
        PATH_TO_FX="$HOME/javafx-sdk-17/lib"
    elif [ -d "/opt/javafx-sdk-21/lib" ]; then
        PATH_TO_FX="/opt/javafx-sdk-21/lib"
    else
        echo
        echo "ERROR: JavaFX SDK not found!"
        echo
        echo "Please download JavaFX SDK from: https://openjfx.io/"
        echo "Then either:"
        echo "  1. Set PATH_TO_FX environment variable to the lib folder"
        echo "  2. Or edit this script and set the PATH_TO_FX variable"
        echo
        echo "Example: export PATH_TO_FX=/path/to/javafx-sdk-21/lib"
        echo
        exit 1
    fi
fi

echo "Using JavaFX from: $PATH_TO_FX"
echo

# Create output directory
mkdir -p out

echo "Compiling..."
javac --module-path "$PATH_TO_FX" --add-modules javafx.controls,javafx.graphics -d out src/module-info.java src/pacman/*.java

if [ $? -ne 0 ]; then
    echo
    echo "Compilation failed!"
    exit 1
fi

echo "Compilation successful!"
echo
echo "Starting PAC-MAN..."
echo

# Run the game
java --module-path "$PATH_TO_FX:out" --add-modules javafx.controls,javafx.graphics -m pacman/pacman.Main
