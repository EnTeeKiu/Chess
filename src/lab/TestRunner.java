package lab;

public class TestRunner {
    public static void printBoardState(Board board) {
        System.out.println("Current Board State:");
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getSquare(row, col).getPiece();
                if (piece == null) {
                    System.out.print("[  ] ");
                } else {
                    String color = piece.isWhite() ? "w" : "b";
                    String type = piece.getClass().getSimpleName().substring(0, 1);
                    System.out.print("[" + color + type + "] ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void testScenario(String scenarioName, Board board, int startRow, int startCol, int endRow, int endCol, boolean expected) {
        Square start = board.getSquare(startRow, startCol);
        Square end = board.getSquare(endRow, endCol);
        Piece piece = start.getPiece();

        System.out.println("Scenario: " + scenarioName);
        if (piece == null) {
            System.out.println("Result: FAILED - No piece at (" + startRow + "," + startCol + ")\n");
            return;
        }

        boolean result = piece.isValidMove(board, start, end);
        System.out.println("Attempting to move " + piece.getClass().getSimpleName() + " from (" + startRow + "," + startCol + ") to (" + endRow + "," + endCol + ")");
        System.out.println("Expected: " + expected + " | Actual: " + result);
        if (result == expected) {
            System.out.println("Status: PASSED\n");
        } else {
            System.out.println("Status: FAILED\n");
        }
    }

    public static void main(String[] args) {
        Board board = new Board();

        // Initialize standard chess board setup
        // Rooks
        board.getSquare(0,0).setPiece(new Rook(true));
        board.getSquare(0,7).setPiece(new Rook(true));
        board.getSquare(7,0).setPiece(new Rook(false));
        board.getSquare(7,7).setPiece(new Rook(false));
        
        // Knights
        board.getSquare(0,1).setPiece(new Knight(true));
        board.getSquare(0,6).setPiece(new Knight(true));
        board.getSquare(7,1).setPiece(new Knight(false));
        board.getSquare(7,6).setPiece(new Knight(false));
        
        // Bishops
        board.getSquare(0,2).setPiece(new Bishop(true));
        board.getSquare(0,5).setPiece(new Bishop(true));
        board.getSquare(7,2).setPiece(new Bishop(false));
        board.getSquare(7,5).setPiece(new Bishop(false));
        
        // Queens and Kings
        board.getSquare(0,3).setPiece(new Queen(true));
        board.getSquare(7,3).setPiece(new Queen(false));
        board.getSquare(0,4).setPiece(new King(true));
        board.getSquare(7,4).setPiece(new King(false));
        
        // Pawns
        for (int col = 0; col < 8; col++) {
            board.getSquare(1, col).setPiece(new Pawn(true));  // White Pawns on row 1
            board.getSquare(6, col).setPiece(new Pawn(false)); // Black Pawns on row 6
        }

        printBoardState(board);

        System.out.println("--- RUNNING GAMEPLAY SCENARIOS ---\n");
        
        testScenario("Valid White Pawn forward move (1 step)", board, 1, 0, 2, 0, true);
        testScenario("Invalid White Pawn backward move", board, 1, 0, 0, 0, false);
        testScenario("Invalid Rook move (blocked by own pawn)", board, 0, 0, 3, 0, false);
        testScenario("Valid White Knight L-shape jump over pawn", board, 0, 1, 2, 2, true);
        testScenario("Invalid Bishop move (blocked by own pawn)", board, 0, 2, 3, 5, false);
        testScenario("Invalid move to square occupied by own piece (Black Rook to Black King)", board, 7, 7, 7, 4, false);
    }
}