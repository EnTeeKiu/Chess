# COMP1020 OOP & Data Structures — Report Outline (Poster-Ready)
**Course:** COMP1020 Object-Oriented Programming & Data Structures, Spring 2026  
**Project:** Chess Plus — Chess Game with AI Opponents (Java Swing)  
**Team:** Group 17  
**Members:** Nguyen Quy Tu (Leader), Nguyen Dinh Nam Khanh, Nguyen Thien Quang, Bui Anh Minh, Nguyen Minh Nghia

> This outline is derived from the **final submitted report** (`Group_17_Oriented_Object_Programming copy.pdf`).  
> It is organised around the **6 required poster sections**: Introduction, Objective & Features, System Architecture, Implementation & Results, Challenges, and References.

---

## 1. Introduction

### 1.1 Problem Statement and Motivation
- Chess is a highly complex two-player strategy game with an estimated 10¹²⁰ possible game sequences.
- A digital chess engine must correctly enforce intricate rules while supporting efficient AI decision-making within practical response times.
- This makes chess an effective platform for demonstrating Object-Oriented Programming, data structures, and algorithmic problem solving.

### 1.2 Modifications Since the Interim Stage
At the interim stage, the AI subsystem was incomplete and the UI rendered pieces as text labels. Since then:
- **BeginnerBot** and **AmateurBot** were implemented using Minimax with Alpha-Beta pruning.
- **IntermediateBot** and **HardBot** added Piece-Square Table evaluation and Quiescence Search for tactical stability.
- **En Passant** was implemented using `Board.enPassantTarget`.
- **Undo system** was added through the Memento pattern using stacked `GameState` snapshots.
- **UI upgraded** to PNG-based rendering with a tabular algebraic move log.
- **Bot threading:** computation runs on a background thread; a `pieceSnapshot[][]` is captured before search so `drawPiecesFromSnapshot()` renders a stable board during calculation.
- **End-game dialogs** wrapped in `SwingUtilities.invokeLater()` to prevent EDT blocking.
- **King positions** cached as `whiteKingSquare` / `blackKingSquare` for O(1) access.
- **ChessBot interface** allows runtime difficulty switching without modifying the UI layer.

---

## 2. Objectives & Features

### 2.1 Project Objectives
- **Primary:** Develop a fully functional, rule-complete chess game in Java using a Swing GUI.
- **Secondary:** Implement AI opponents with progressively advanced search and evaluation techniques.
- **Scope:** All standard chess rules — legal movement, check/checkmate/stalemate detection, castling, en passant, and pawn promotion.

### 2.2 Game Modes and Bot Difficulty

| Bot Level | Depth | Evaluation | Special |
|---|---|---|---|
| BeginnerBot | 3 | Material only | — |
| AmateurBot | 5 | Material only | — |
| IntermediateBot | 6 | Material + Piece-Square Tables (12 tables) | PST delta in move ordering |
| HardBot | 6 | Material + Piece-Square Tables | + Quiescence Search |

- Two game modes: **Human vs. Human** (local) and **Human vs. Bot** (single-player).

### 2.3 Key Features and Functionalities
- Interactive 8×8 graphical chess board with **drag-and-drop** movement.
- **Legal move highlighting** on selected pieces.
- **Algebraic move logging** in a tabular UI panel.
- **Undo** functionality — Memento-based; reverts both human and bot moves in single-player.
- **Surrender** and **New Game** controls.
- **Bot computation on background thread** — UI stays responsive; board renders from snapshot during AI search.
- **Castling** (kingside and queenside) fully validated per FIDE rules.
- **Pawn promotion** — human chooses via dialog; bot auto-promotes to Queen.
- **En Passant** — tracked via `Board.enPassantTarget`, validated in `Pawn.isValidMove()` with strict colour checks.
- **Game-over dialogs** wrapped in `SwingUtilities.invokeLater()` to prevent EDT freezing.

### 2.4 Core Functionalities Summary

| Feature | Description |
|---|---|
| `isValidMove()` | Each piece class implements its own movement rules |
| `Board.isChecked()` | Detects attacks on the King using the active piece list |
| `willMoveResultInCheck()` | Simulates a move to reject illegal positions |
| `isCheckmate()` / `isStalemate()` | Combines check detection with legal move generation |
| `King.isValidCastle()` | Validates all FIDE castling conditions |
| En Passant | Tracked via `Board.enPassantTarget`, validated in `Pawn.isValidMove()` |
| Pawn promotion | Human players choose via dialog; bots auto-promote to Queen |
| BeginnerBot / AmateurBot | Minimax + Alpha-Beta at depths 3/5 with material evaluation |
| IntermediateBot / HardBot | Depth-6 Minimax with PST evaluation; HardBot adds Quiescence Search |
| `Stack<GameState>` | Stores deep-copied board snapshots for Undo |
| `getChessNotation()` | Generates algebraic notation for the move log |

---

## 3. System Architecture

### 3.1 High-Level Architecture and Module Organisation
The system is divided into **backend** and **frontend** layers:
- **Backend:** board state, move validation, game-state detection, turn management, AI computation.
- **Frontend:** rendering and mouse interaction.
- Communication between layers is coordinated through `GameController`; the UI never directly modifies board state.

```
ChessBoardUI (View + Controller)
    ├── Board (Model)
    │     ├── Square[8][8]
    │     └── Active Piece Index (int[], int[])
    ├── GameController (Game State)
    ├── MouseInputListener (Input Handling)
    ├── ChessBot (Interface)
    │     ├── BeginnerBot (depth 3, material only)
    │     ├── AmateurBot (depth 5, material only)
    │     ├── IntermediateBot (depth 6, material + PSTs)
    │     └── HardBot (depth 6, material + PSTs + QS)
    └── Piece Hierarchy (Abstract + Concrete)
          ├── Piece (abstract)
          │     ├── King
          │     ├── Queen
          │     ├── Rook
          │     ├── Bishop
          │     ├── Knight
          │     └── Pawn
    MoveHelper (static utility)
    Move (data class)
    Square (data class)
```

### 3.2 OOP Concepts Applied

#### Abstraction
- `Piece` (abstract class) hides movement implementation details — the engine only knows pieces can validate moves and clone themselves.
- `ChessBot` (interface) hides the entire AI algorithm — the game only interacts with `getBestMove()`.

#### Inheritance
- All six piece types (`King`, `Queen`, `Rook`, `Bishop`, `Knight`, `Pawn`) extend the abstract class `Piece`.
- Shared behaviour (`isWhite`, `isMoved`, `setMoved`) is inherited; movement rules are overridden.

#### Polymorphism
- `Piece.isValidMove()` is declared abstract — each subclass provides its own implementation.
- `ChessBoardUI` calls `piece.isValidMove(board, start, end)` uniformly for any piece type.
- `ChessBot` interface: `activeBot.getBestMove(board, isWhite)` works for any bot implementation.

#### Encapsulation
- `Square` stores coordinates, board colour, and a nullable `Piece` reference through controlled access methods.
- `Board` wraps the `Square[][]` structure and exposes controlled operations for move execution and simulation.

#### Memento Pattern (Design Pattern)
- Implemented via `GameState` inner class to snapshot board state, active pieces, and turn information.
- Used for the Undo system by restoring deep-copied `Piece` arrays via `clonePiece()`.

### 3.3 Class Summary

| Class / Interface | Type | Responsibility |
|---|---|---|
| `Piece` | Abstract Class | Shared piece state and movement contract |
| `King`, `Queen`, `Rook`, `Bishop`, `Knight`, `Pawn` | Concrete Classes | Piece-specific movement and deep-copy logic |
| `Board` | Class | Board state, simulation, game-state detection, en passant tracking |
| `Square` | Class | Single board cell with coordinates and piece reference |
| `GameController` | Class | Turn tracking and game configuration |
| `ChessBot` | Interface | Defines `getBestMove()` |
| `BeginnerBot` / `AmateurBot` | Classes | Material-based Minimax + Alpha-Beta bots |
| `IntermediateBot` / `HardBot` | Classes | Positional Minimax bots using PSTs; HardBot adds Quiescence Search |
| `MoveHelper` | Utility Class | Check detection and move simulation helpers |
| `Move` | Data Class | Stores move-related data |
| `GameState` | Inner Class | Undo snapshot stored in a `Stack` |
| `ChessBoardUI` | Class | Main rendering and game-loop controller |
| `MouseInputListener` | Class | Mouse event translation and drag handling |
| `TestRunner` | Class | Standalone move-validation tests |

### 3.4 Data Structures

| Data Structure | Where Used | Benefit |
|---|---|---|
| `Square[8][8]` (2D Array) | `Board` | O(1) access by (row, col), naturally maps to chess coordinates |
| `Square` references (`whiteKingSquare`, etc.) | `Board` | O(1) King location lookup instead of scanning |
| `int[] activePieceCoords` (size 33) | `Board` | O(n) iteration (n ≤ 32), avoids scanning all 64 cells |
| `int[] boardToIndex` (size 64) | `Board` | O(1) piece removal (swap-and-decrement) |
| `Stack<GameState>` | `ChessBoardUI` | Natural LIFO for move history (Memento Pattern) |
| `List<Move>` (ArrayList) | All bot classes | Dynamic list of legal moves per search node |
| `Map<String, BufferedImage>` (HashMap) | `ChessBoardUI` | O(1) piece image lookup during paint |
| `int[][] PIECE_OPENING / PIECE_ENDGAME` | `IntermediateBot`, `HardBot` | Piece-Square Tables: O(1) positional lookup per piece |

### 3.5 Algorithms

#### Minimax with Alpha-Beta Pruning
- Used by all four bot classes.
- White = maximising player; Black = minimising player.
- Alpha-Beta pruning: branches are cut when β ≤ α, reducing worst-case O(b^d) to best-case O(b^(d/2)).

#### Move Ordering (MVV-LVA)
- Promotions: +8000
- Captures: `10 × victim_value − attacker_value`
- Quiet moves: 0
- IntermediateBot / HardBot: adds **PST delta** (`toBonus − fromBonus`) to move score for positional ordering.

#### Board Evaluation
- **Material only** (BeginnerBot, AmateurBot): `Σ pieceValue` (Queen=900, Rook=500, Bishop=300, Knight=300, Pawn=100).
- **Material + PSTs** (IntermediateBot, HardBot): extends with Piece-Square Tables — 12 tables total (6 piece types × 2 phases: opening/middlegame and endgame).
- Game phase detection: `activePieceCount ≤ 18` triggers endgame tables (O(1) check).
- For Black pieces, positional bonuses are mirrored using `row = 7 − r`.

#### Piece-Square Table Principles
- **Pawns:** Centre advance rewarded; passive wing pawns penalised. Endgame: uniform forward progress encouraged.
- **Knights:** "Knight on the rim is dim" — edge penalised up to −50; centre rewarded up to +20.
- **Bishops:** Long diagonals and fianchetto squares rewarded; corners penalised.
- **Rooks:** 7th rank heavily rewarded; back rank d/e files slightly preferred.
- **Queen:** Penalised for early development; centre preferred in middlegame.
- **King (Opening):** Must hide — castled positions +30; centre heavily penalised −50.
- **King (Endgame):** Must march to centre — centre squares +40.

#### Quiescence Search (HardBot only)
- Eliminates the **horizon effect** where Minimax stops evaluation before a decisive capture sequence.
- At depth 0, `quiescenceSearch()` continues searching captures and promotions until the position is tactically stable.
- Uses a **stand-pat** score as baseline; Alpha-Beta pruning applied throughout.

### 3.6 Time and Space Complexity

| Operation | Complexity | Notes |
|---|---|---|
| Check detection (`isChecked`) | O(n) | Iterates active piece list (n ≤ 32) |
| Legal move generation | O(n × 64) | Each active piece evaluates all destination squares |
| Move simulation (`willMoveResultInCheck`) | O(n) | Temporary mutation + `isChecked` |
| Minimax (depth d, branching b) | O(b^d) worst | Alpha-Beta: O(b^(d/2)) best case |
| Active piece removal | O(1) | Swap-and-decrement with reverse map |
| Undo restoration | O(64) | Restores full board snapshot |
| PST lookup | O(1) per piece | Direct 2D array index |
| PST evaluation | O(n) | Added during material traversal |

### 3.7 Trade-offs and Optimisation Decisions
- BeginnerBot / AmateurBot use material-only evaluation for speed at lower depths.
- IntermediateBot / HardBot extend with PSTs for stronger positional play, trading speed for strategic quality.
- HardBot adds Quiescence Search, which may increase search time considerably in tactical positions.
- Active piece list avoids O(64) full-board scans.
- Board snapshot prevents flickering during AI search — clean separation between simulation and display state.
- Minimax uses a **make/undo simulation pattern** rather than cloning the entire board at every node, reducing temporary object allocation and garbage-collection pauses.

---

## 4. Implementation & Results

### 4.1 Programming Language, Libraries, and Frameworks
- **Language:** Java (JDK 11+)
- **GUI:** Java Swing (`JFrame`, `JPanel`, `JOptionPane`, `JScrollPane`, `Graphics2D`)
- **Image I/O:** `javax.imageio.ImageIO` for loading PNG piece images
- **Threading:** `java.lang.Thread` for background bot computation + `SwingUtilities.invokeLater()` for EDT-safe UI updates
- **No external libraries** — standard Java SE only.
- Compilable directly with `javac` and `java`.

### 4.2 File Structure and Package Organisation

```
OOP - Final Project/
├── src/
│   └── lab/
│       ├── ChessBoardUI.java       ← Main game window + game loop
│       ├── Board.java              ← Board model + game state logic
│       ├── Square.java             ← Cell data class
│       ├── GameController.java     ← Turn and mode management
│       ├── Piece.java              ← Abstract piece base class
│       ├── King.java               ← King movement + castling
│       ├── Queen.java              ← Queen movement
│       ├── Rook.java               ← Rook movement
│       ├── Bishop.java             ← Bishop movement
│       ├── Knight.java             ← Knight movement
│       ├── Pawn.java               ← Pawn movement + promotion detection
│       ├── Move.java               ← Move data class
│       ├── MoveHelper.java         ← Static game-state utility methods
│       ├── ChessBot.java           ← AI interface
│       ├── BeginnerBot.java        ← Minimax depth-3 AI (material only)
│       ├── AmateurBot.java         ← Minimax depth-5 AI (material only)
│       ├── IntermediateBot.java     ← Minimax depth-6 AI (material + PSTs)
│       ├── HardBot.java            ← Minimax depth-6 AI (material + PSTs + QS)
│       ├── MouseInputListener.java  ← Mouse event handler
│       └── TestRunner.java          ← Standalone move-validation tests
├── chess pieces/                    ← PNG assets (12 pieces × 2 colours)
└── documents/                       ← Guidelines + report files
```

Source code: [GitHub — EnTeeKiu/Chess](https://github.com/EnTeeKiu/Chess)

### 4.3 Key Implementation Decisions
- **`isValidMove()`** is a pure predicate — validates movement rules without modifying board state. All mutations are centralised through `Board.applyMove()` and `Board.undoMove()`.
- **Drag-and-drop:** `MouseInputListener` computes and highlights legal destinations; on release, submits to `tryMove()` for validation.
- **Bot threading:** when the bot's turn begins, `botThinking = true`, a snapshot is stored in `pieceSnapshot[][]`, and a background `Thread` runs Minimax. While `botThinking` is true, `paintComponent()` renders through `drawPiecesFromSnapshot()`. On completion, `SwingUtilities.invokeLater()` safely applies the move on the EDT.
- **Castling:** `King.isValidCastle()` verifies movement history, empty traversal squares, and attacked-square conditions.
- **Pawn promotion:** detected during move execution; bots auto-promote to Queen; humans choose via dialog.
- **Surrender:** reuses the same end-game flow as checkmate/stalemate through `promptNewGame()`, avoiding duplicated logic.
- **AI invocation** is centralised through `triggerBot()`, ensuring consistent behaviour after normal moves and Undo.
- **En Passant:** target tracked globally via `Board.enPassantTarget`; during captures, `willMoveResultInCheck()` and `minimax()` orchestrate removal/restoration of the captured pawn from a different square than the destination.

### 4.4 Testing and Evaluation

#### Test Methodology
- **Unit tests:** `TestRunner.java` validates `isValidMove()` for each piece type in isolation.
- **System tests:** Full match scenarios (Fool's Mate, Scholar's Mate, stalemate, castling, pawn promotion, Undo, all AI levels).

#### Backend Validation Results

| Test Case | Expected | Status |
|---|---|---|
| Pawn forward movement | Valid | ✅ Pass |
| Pawn backward movement | Invalid | ✅ Pass |
| Blocked rook movement | Invalid | ✅ Pass |
| Knight L-shape movement | Valid | ✅ Pass |
| Bishop diagonal movement | Valid | ✅ Pass |
| King moving into check | Rejected | ✅ Pass |
| Valid kingside castling | Allowed | ✅ Pass |
| Castling through check | Rejected | ✅ Pass |
| Checkmate detection | Detected | ✅ Pass |
| Stalemate detection | Detected | ✅ Pass |
| Pawn promotion | Success | ✅ Pass |
| Undo restoration (2-player) | Restored | ✅ Pass |
| Undo restoration (single-player) | Restored | ✅ Pass |

#### Discussion of Correctness, Robustness, and Usability
- All standard chess rules within the project scope were validated.
- The Undo system restores board state safely because each `GameState` stores deep-copied snapshots through `clonePiece()`.
- The UI remains responsive during AI computation due to the background threading model.
- Legal move highlighting improves usability; algebraic move log provides complete gameplay history.

#### Performance Evaluation

| Difficulty | Think Time |
|---|---|
| BeginnerBot (depth 3, material) | < 1 second |
| AmateurBot (depth 5, material) | 1–5 seconds |
| IntermediateBot (depth 6, material + PSTs) | 3–10 seconds |
| HardBot (depth 6, material + PSTs + QS) | 5–15+ seconds (tactical positions) |

No crashes, deadlocks, or invalid game-state transitions were observed during repeated gameplay sessions across all difficulty levels.

#### Undo via the Memento Pattern
- After each committed move, the system stores a complete `GameState` snapshot containing deep copies of the board and pieces.
- **Single-player:** Undo restores both the human move and the bot response by popping two states from the stack.
- **Multiplayer:** Only one state is restored per action.
- Storing complete snapshots simplifies restoration and guarantees correctness even for complex moves such as castling.

---

## 5. Challenges

### 5.1 Technical Difficulties

| Challenge | Description | Solution |
|---|---|---|
| **Check detection during simulation** | Early implementations evaluated moves against the live board state rather than the temporary post-simulation state, producing incorrect results during deeper Minimax recursion. | Strict apply-check-restore pattern inside `willMoveResultInCheck()`. |
| **UI flickering during AI computation** | Minimax mutates the same board object used by the renderer, causing temporary simulation states to appear on screen. | Render from a temporary board snapshot while `botThinking` is active; restore normal rendering once the AI move is delivered through `SwingUtilities.invokeLater()`. |
| **Castling edge cases** | FIDE rules require simultaneous validation of movement history, path clearance, and attacked squares. | Conditions implemented sequentially inside `King.isValidCastle()`. |
| **Active piece index management** | Captured pieces must be removed without shifting the entire array. | Swap-and-decrement strategy combined with `boardToIndex[]` reverse map for O(1) removal. |
| **Minimax State Corruption ("The Ghost Pawn Bug")** | During recursive simulation, improperly scoped En Passant targets allowed pawns to capture their own colour's ghost pieces. Combined with an uninitialised default in `boardToIndex[]`, this corrupted `activePieceCoords[]`, crashed the simulation thread, and triggered false stalemates. | Enforced strict colour validation in `Pawn.isValidMove()` via `epPawn.isWhite() != this.isWhite()`, and initialised `boardToIndex[]` to −1 for empty squares to preserve array bounds. |
| **UI thread blocking on dialogs** | End-game `JOptionPane` popups blocked the Swing Event Dispatch Thread, causing the final board repaint to freeze. | Wrapped all end-game dialogs in `SwingUtilities.invokeLater()`. |

### 5.2 Design and Architectural Issues
- **Bot code duplication:** `BeginnerBot` and `AmateurBot` share similar Minimax logic and differ mainly in search depth. **Future fix:** a single configurable implementation parameterised by depth.
- **God class risk:** `ChessBoardUI` still handles both rendering and portions of gameplay flow. **Partial mitigation:** reusable game-state logic delegated to `MoveHelper`; turn management to `GameController`.

### 5.3 Lessons Learned
- Frequent play-testing proved essential for detecting rule-validation bugs before they became deeply integrated.
- The `ChessBot` interface simplified runtime AI replacement and improved extensibility.
- All interface updates must be dispatched through `SwingUtilities.invokeLater()` to avoid race conditions on the EDT.

---

## 6. References

1. Knuth, D.E., & Moore, R.W. (1975). An analysis of alpha-beta pruning. *Artificial Intelligence*, 6(4), 293–326.
2. Oracle Corporation. (2024). *Java SE 11 API Documentation*. https://docs.oracle.com/en/java/javase/11/
3. FIDE. (2024). *FIDE Laws of Chess*. https://www.fide.com/FIDE/handbook/LawsOfChess.pdf
4. Chessprogramming Wiki. (2024). *Move Ordering*. https://www.chessprogramming.org/Move_Ordering
5. Chessprogramming Wiki. (2024). *Minimax*. https://www.chessprogramming.org/Minimax
6. Chessprogramming Wiki. (2024). *Alpha-Beta*. https://www.chessprogramming.org/Alpha-Beta
7. Group 17. (2026). *Chess Plus — Source Code Repository*. https://github.com/EnTeeKiu/Chess

---

## Appendix: Team Contributions

| Member | Contributions |
|---|---|
| **Nguyen Quy Tu** (Leader) | Project coordination; `GameController` and `Board`; check, checkmate, and stalemate detection; active piece index optimisation; Piece-Square Tables. |
| **Nguyen Dinh Nam Khanh** | Piece movement validation; sliding-piece path checking; castling validation; pawn promotion logic. |
| **Nguyen Thien Quang** | UI development; drag-and-drop interaction; move highlighting; move log panel; bot threading and snapshot rendering; Undo and game controls. |
| **Bui Anh Minh** | System integration; `TestRunner`; debugging and integration-stage issue resolution. |
| **Nguyen Minh Nghia** | AI subsystem implementation; all four bot classes; Minimax with Alpha-Beta pruning; move ordering; Quiescence Search; Queen logic optimisation. |

---

## Appendix: Conclusion & Future Work

### Achievements
- Fully rule-complete chess game in Java Swing.
- Four AI difficulty tiers with progressively advanced heuristics (material → PSTs → Quiescence Search).
- Clean OOP design: abstract `Piece` hierarchy, `ChessBot` interface for plug-and-play AI, Memento pattern for Undo.
- Background-threaded AI computation maintained UI responsiveness during Minimax search.
- Active piece index structure improved move generation and check detection efficiency.

### Current Limitations
- **Draw by repetition** and **50-move rule** not implemented.
- **BeginnerBot / AmateurBot** contain partially duplicated Minimax logic.

### Future Improvements
- Implement remaining chess rules (50-move, threefold repetition).
- Refactor AI into configurable `MinimaxBot(depth)` implementation.
- Add opening-book support.
- Introduce networked multiplayer and game timer functionality.
