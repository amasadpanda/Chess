package com.group8.chesswithhats.util;

import static com.group8.chesswithhats.util.ChessLogic.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class ChessAI {
    static boolean white;
    static int difficulty;
    public static int[] getMove(Piece[][] board, boolean w, int diff){
        // Create a list of all peices that may be moved
    	white = w;
    	difficulty = diff;
		ArrayList<Piece> movingPeices = new ArrayList<>();
		ArrayList<Integer> locations = new ArrayList<>();
		int location = 0;
		for(Piece[] row : board) {
			for(Piece piece : row) {
				if(piece != null && (w == (piece.white))) {
					movingPeices.add(piece);
					locations.add(location);
					System.out.println("Piece at loc: " + location +" has moves: ");
					for(int v : piece.getMoves(location, board, true))
						System.out.print(v + " ");
					System.out.println();
				}
				location++;
			}
		}
        int best = -Integer.MAX_VALUE/2;
		int ans[] = new int[2];

        for(int i = 0; i < movingPeices.size(); i++) {
        	int r = locations.get(i)/8;
        	int c = locations.get(i)%8;
        	
        	HashSet<Integer> pieceMoves = movingPeices.get(i).getMoves(locations.get(i), board, true);
        	for(int move : pieceMoves) {
        		int nr = move/8;
        		int nc = move%8;
        		System.out.println("TRYING " + " row " + r +" : col " + c + " loc " + locations.get(i));
        		System.out.println("Going to " + " row " + nr +" : col " + nc + " loc " + (nr * 8 + nc));
        		Piece newBoard[][] = new Piece[8][8];
				for(int j = 0; j < 8; j++) {
					for(int k = 0; k < 8; k++) {
						newBoard[j][k] = board[j][k] == null ? null : board[j][k].copy();
					}
				}
				
				ChessLogic.movePiece(r, c, nr, nc, newBoard);
				int score = alphaBetaPruning(Integer.MIN_VALUE/2, Integer.MAX_VALUE/2, board, !white, 0);
        		if(score > best) {
        			best = score;
        			ans[0] = r * 8 + c;
        			ans[1] = nr * 8 + nc;
        		}
        	}
        }
        System.out.println("RES: " + best);
        return ans;
    }
	
    
    
	public static Integer heuristic(Piece[][] board, boolean isWhite) {
		
		//Check if game is won
		if(ChessLogic.gameOver(isWhite, board) == 1) {
			return isWhite == white ? Integer.MIN_VALUE/2 : Integer.MAX_VALUE/2; // Fix values here?
		}
		int pieceValue = 0;
		for(Piece[] row : board) {
			for(Piece p : row) {
				if(p == null) continue;
				if(isWhite == p.white) {
					pieceValue += getScore(p);
				} else {
					pieceValue -= getScore(p);
				}
			}
		}
		
		boolean coveredSquares[][] = new boolean[8][8];
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(board[i][j] != null && board[i][j].white == isWhite) {
					HashSet<Integer> moves = board[i][j].getMoves(i * 8 + j, board, true);
					coveredSquares[i][j] = true;
					for(int m : moves) {
						int r = m/8;
						int c = r % 8;
						coveredSquares[r][c] = true;
					}
				}
			}
		}
		int totalCoverage = 0;
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(coveredSquares[i][j]) totalCoverage++;
			}
		}
		
		// This function might need to be tweaked
		return 10 * pieceValue + totalCoverage;
	}
	
	public static int getScore(Piece p) {
		if(p instanceof Knight || p instanceof Bishop) {
			return 3;
		}
		if(p instanceof Rook) {
			return 5;
		}
		if(p instanceof Queen) {
			return 9;
		}
		return 1;
	}
	
	public static Integer alphaBetaPruning(int a, int b, Piece[][] board, boolean whiteTurn, int depth) {
		//Check if game is over
		if(ChessLogic.gameOver(whiteTurn, board) == 1) {
			return whiteTurn == white ? Integer.MIN_VALUE/2 : Integer.MAX_VALUE/2;
		} 
		if(ChessLogic.gameOver(whiteTurn, board) == 2) {
			return 0;
		}
		
		//check if we have reached the maximum depth
		if(depth == difficulty) {
			//Score the board
			return heuristic(board, whiteTurn);
		}
		
		// Create a list of all peices that may be moved
		ArrayList<Piece> movingPeices = new ArrayList<>();
		ArrayList<Integer> locations = new ArrayList<>();
		int location = 0;
		for(Piece[] row : board) {
			for(Piece piece : row) {
				if(piece != null && (whiteTurn == (piece.white))) {
					movingPeices.add(piece);
					locations.add(location);
				}
				location++;
			}
		}
		int best = whiteTurn == white ? Integer.MIN_VALUE/2 : Integer.MAX_VALUE/2;
		for(int i = 0; i < movingPeices.size(); i++) {
			// For every piece get all of its moves and we shall try them
			Piece p = movingPeices.get(i);
			HashSet<Integer> peiceMoves = p.getMoves(locations.get(i), board, true);
			int originalRow = locations.get(i)/8;
			int originalCol = locations.get(i)%8;
			for(Integer move : peiceMoves) {
				int newRow = move/8;
				int newCol = move%8;

				// Create a copy of the board
				Piece newBoard[][] = new Piece[8][8];
				for(int j = 0; j < 8; j++) {
					for(int k = 0; k < 8; k++) {
						newBoard[j][k] = board[j][k] == null ? null : board[j][k].copy();
					}
				}
				
				ChessLogic.movePiece(originalRow, originalCol, newRow, newCol, newBoard);
				
				// If maximizing
				if(whiteTurn == white) {
					Integer val = alphaBetaPruning(a,b,newBoard,!whiteTurn,depth+1);
					best = Math.max(best, val);
					a = Math.max(a, best);
					if(b <= a)
						return best;
				} else { // If minimizing
					Integer val = alphaBetaPruning(a,b,newBoard,!whiteTurn,depth+1);
					best = Math.min(best, val);
					b = Math.min(b, best);
					if(b <= a)
						return best;
				}
			}
		}
		return best;
	}

}

