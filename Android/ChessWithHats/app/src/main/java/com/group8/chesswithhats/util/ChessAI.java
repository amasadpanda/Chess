package com.group8.chesswithhats.util;

import static com.group8.chesswithhats.util.ChessLogic.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class ChessAI {
	static boolean white;
	static int difficulty;

	// Written by Philip Rodriguez for testing purposes while Pablo works on the actual AI.
	public static int[] getRandomMove(Piece[][] board, boolean w) {
		ArrayList<Piece> computerPieces = new ArrayList<>();
		ArrayList<Integer> positions = new ArrayList<>();
		int loc = 0;
		for (int r = 0; r < board.length; r++)
		{
			for (int c = 0; c < board[r].length; c++)
			{
				if (board[r][c] != null && board[r][c].white == w) {
					computerPieces.add(board[r][c]);
					positions.add(loc);
				}
				loc++;
			}
		}

		// Select a random black piece and attempt to return a move for it!
		Random random = new Random();
		while (computerPieces.size() > 0) {
			// Select piece at random
			int pieceToMove = random.nextInt(computerPieces.size());

			// Get valid next moves as list
			ArrayList<Integer> validNextMoves = new ArrayList<>(computerPieces.get(pieceToMove).getMoves(positions.get(pieceToMove), board));

			// There are no available moves for the selected piece!
			if (validNextMoves.size() == 0)
			{
				// Remove this piece from the running.
				computerPieces.remove(pieceToMove);
				positions.remove(pieceToMove);
			}
			else {
				// Move it!
				return new int[]{positions.get(pieceToMove), validNextMoves.get(random.nextInt(validNextMoves.size()))};
			}
		}

		// Absolutely no remaining moves available for the computer.
		return null;
	}
	static int ans[];
	public static int[] getMove(Piece[][] board, boolean w, int diff) {
		// Create a list of all peices that may be moved
		white = w;
		difficulty = diff;

		ans = new int[2];
		alphaBetaPruning(Integer.MIN_VALUE/2, Integer.MAX_VALUE/2, board, white, 0);
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
		return 100 * pieceValue + totalCoverage;
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
			return heuristic(board, !whiteTurn);
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
					if (best < val || (best == val && Math.random() < .5)) {
						best = val;
						if(depth == 0) {
							ans[0] = originalRow * 8 + originalCol;
							ans[1] = newRow * 8 + newCol;
						}
					}
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

