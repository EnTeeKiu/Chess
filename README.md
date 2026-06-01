# Chess Game - COMP1020 Final Project

## Project Structure
- `src/`: Contains all Java source code for the game, including the core logic and graphical user interface.
- `chess pieces/`: Contains the image resources required to render the game pieces on the board.

## How to Run the Application
The application can be run without any additional setup. 

To start the game:
1. Open the project in your preferred Java IDE (e.g., Eclipse, IntelliJ IDEA, VS Code).
2. Navigate to the `src/lab/BoardUI.java` file.
3. **Run the `BoardUI.java` class.** This will launch the graphical user interface for the game.

## Test Scenarios
To verify the core game logic and OOP design (such as piece movement validation, boundary checking, and collision) without the UI, you can run the `src/lab/TestRunner.java` class. 

The `TestRunner` contains predefined gameplay test scenarios that evaluate different piece movements (e.g., valid pawn advances, invalid blocked paths, valid knight jumps) and outputs the results to the console.
