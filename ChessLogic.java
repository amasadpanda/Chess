import java.util.HashSet;

public class ChessLogic {
	
	//Base Piece Class
	//ChessLogic.Piece piece; import static whatever.something.ChessLogic.*;
	//This is a Christian logic class, so please, NO swearing!
	public static abstract class Piece {
		boolean white;
		public Piece(boolean isWhite) {
			white=isWhite;
		}
		abstract HashSet<Integer> getMoves(int loc, Piece[][] board, boolean forReal);
		boolean inBounds(int r,int c) {
			return r>=0&&r<8&&c>=0&&c<8;
		}
	}
	
	//Pawn
	public static class Pawn extends Piece {
		public Pawn(boolean isWhite) {
			super(isWhite);
		}
		HashSet<Integer> getMoves(int loc, Piece[][] board,boolean forReal){
			HashSet<Integer> moves=new HashSet<Integer>();
			int r=loc/8,c=loc%8;
			
			//If the Pawn is white, it starts on row 6 and goes up (down? I hate that convention.), otherwise, it starts on row 1 and goes down.
			int dr=-1,start=6;
			if(!white) {
				dr=1;
				start=1;
			}
			if(inBounds(r+dr,c)&&board[r+dr][c]==null) {
				if(!forReal||checkValidity(r,c,r+dr,c,board))
					moves.add((r+dr)*8+c);

				//Pawns can move two squares on their first turn.
				if(r==start) {
					if(inBounds(r+2*dr,c)&&board[r+2*dr][c]==null) {
						if(!forReal||checkValidity(r,c,r+2*dr,c,board))
							moves.add((r+2*dr)*8+c);
					}
				}
			}

			//Check if it can capture
			if(inBounds(r+dr,c-1)&&board[r+dr][c-1]!=null&&board[r+dr][c-1].white!=white) {
				if(!forReal||checkValidity(r,c,r+dr,c-1,board))
					moves.add((r+dr)*8+c-1);
			}
			if(inBounds(r+dr,c+1)&&board[r+dr][c+1]!=null&&board[r+dr][c+1].white!=white) {
				if(!forReal||checkValidity(r+dr,c,r,c+1,board))
					moves.add((r+dr)*8+c+1);
			}
			
			return moves;
		}
	}
	
	//King
	public static class King extends Piece {
		public King(boolean isWhite) {
			super(isWhite);
		}
		HashSet<Integer> getMoves(int loc,Piece[][] board,boolean forReal) {
			int r=loc/8,c=loc%8;
			int[] dr= {-1,-1,0,1,1,1,0,-1},dc= {0,1,1,1,0,-1,-1,-1};
			HashSet<Integer> moves=new HashSet<Integer>();
			
			//Check all eight directions
			for(int i=0;i<8;i++) {
				int r2=r+dr[i],c2=c+dc[i];
				if(inBounds(r2,c2)&&(board[r2][c2]==null||board[r2][c2].white!=white)) {
					if(!forReal||checkValidity(r,c,r2,c2,board))
						moves.add(r2*8+c2);
				}
			}
			return moves;
		}
	}
	
	//Knight
	public static class Knight extends Piece {
		public Knight(boolean isWhite) {
			super(isWhite);
		}
		HashSet<Integer> getMoves(int loc,Piece[][] board,boolean forReal){
			int r=loc/8,c=loc%8;
			int[] dr= {-2,-1,1,2,2,1,-1,-2}, dc= {1,2,2,1,-1,-2,-2,-1};
			HashSet<Integer> moves=new HashSet<Integer>();
			for(int i=0;i<8;i++) {
				int r2=r+dr[i],c2=c+dc[i];
				if(inBounds(r2,c2)&&(board[r2][c2]==null||board[r2][c2].white!=white)) {
					if(!forReal||checkValidity(r,c,r2,c2,board))
						moves.add(r2*8+c2);
				}
			}
			return moves;
		}
	}
	
	//Rook
	public static class Rook extends Piece {
		public Rook(boolean isWhite) {
			super(isWhite);
		}
		HashSet<Integer> getMoves(int loc,Piece[][] board,boolean forReal) {
			int r=loc/8,c=loc%8;
			int[] dr= {-1,0,1,0},dc= {0,1,0,-1};
			HashSet<Integer> moves=new HashSet<Integer>();
			for(int i=0;i<4;i++) {
				int r2=r,c2=c;
				//Trying going as far as possible in each direction
				for(int j=0;j<8;j++) {
					r2+=dr[i];
					c2+=dc[i];
					if(!inBounds(r2,c2))
						break;
					if(board[r2][c2]==null) {
						if(!forReal||checkValidity(r,c,r2,c2,board))
							moves.add(r2*8+c2);
						continue;
					}
					//Capture
					if(board[r2][c2].white!=white) {
						if(!forReal||checkValidity(r,c,r2,c2,board))
							moves.add(r2*8+c2);
					}
					
					//If there was a Piece here, the rook can't go any further in this direction.
					break;
				}
			}
			return moves;
		}
	}
	
	//Bishop
	public static class Bishop extends Piece {
		public Bishop(boolean isWhite) {
			super(isWhite);
		}
		HashSet<Integer> getMoves(int loc,Piece[][] board,boolean forReal){
			int r=loc/8,c=loc%8;
			int[] dr= {-1,1,1,-1},dc= {1,1,-1,-1};
			HashSet<Integer> moves=new HashSet<Integer>();
			for(int i=0;i<4;i++) {
				int r2=r,c2=c;
				//Trying going as far as possible in each direction
				for(int j=0;j<8;j++) {
					r2+=dr[i];
					c2+=dc[i];
					if(!inBounds(r2,c2))
						break;
					if(board[r2][c2]==null) {
						if(!forReal||checkValidity(r,c,r2,c2,board))
							moves.add(r2*8+c2);
						continue;
					}
					//Capture
					if(board[r2][c2].white!=white) {
						if(!forReal||checkValidity(r,c,r2,c2,board))
							moves.add(r2*8+c2);
					}
					
					break;
				}
			}
			return moves;
		}
	}
	
	//Queen
	public static class Queen extends Piece {
		public Queen(boolean isWhite) {
			super(isWhite);
		}
		
		//This is basically a rook with a king's dr/dc array.
		HashSet<Integer> getMoves(int loc,Piece[][] board,boolean forReal){
			int r=loc/8,c=loc%8;
			int[] dr= {-1,-1,0,1,1,1,0,-1},dc= {0,1,1,1,0,-1,-1,-1};
			HashSet<Integer> moves=new HashSet<Integer>();
			for(int i=0;i<8;i++) {
				int r2=r,c2=c;
				for(int j=0;j<8;j++) {
					r2+=dr[i];
					c2+=dc[i];
					if(!inBounds(r2,c2))
						break;
					if(board[r2][c2]==null) {
						if(!forReal||checkValidity(r,c,r2,c2,board))
							moves.add(r2*8+c2);
						continue;
					}
					//Capture
					if(board[r2][c2].white!=white) {
						if(!forReal||checkValidity(r,c,r2,c2,board))
							moves.add(r2*8+c2);
					}
					break;
				}
			}
			return moves;
		}
	}

	
	static public int findKing(boolean color,Piece[][] board) {
		for(int i=0;i<8;i++) {
			for(int j=0;j<8;j++) {
				if(board[i][j]!=null&&board[i][j] instanceof King&&board[i][j].white==color)
					return i*8+j;
			}
		}
		return -1;
	}

	static boolean checkValidity(int r,int c,int r2,int c2,Piece[][] board) {
		boolean good=false;
		Piece temp=board[r2][c2];
		board[r2][c2]=board[r][c];
		board[r][c]=null;
		if(!inCheck(board[r2][c2].white,board))
			good=true;
		board[r][c]=board[r2][c2];
		board[r2][c2]=temp;
		return good;
	}
	
	static boolean inCheck(boolean color,Piece[][] board) {
		int loc=findKing(color,board);
		for(int i=0;i<8;i++) {
			for(int j=0;j<8;j++) {
				if(board[i][j]!=null&&board[i][j].white!=color) {
					HashSet<Integer> moves=board[i][j].getMoves(i*8+j, board,false);
					if(moves.contains(loc))
						return true;
				}
			}
		}
		return false;
	}
	
	//One for checkmate, two for draw, zero for neither
	public static int gameOver(boolean color,Piece[][] board) {
		boolean check=inCheck(color,board);
		for(int i=0;i<8;i++) {
			for(int j=0;j<8;j++) {
				if(board[i][j]!=null&&board[i][j].white==color) {
					if(board[i][j].getMoves(i*8+j,board,true).size()!=0)
						return 0;
				}
			}
		}
		if(check)
			return 1;
		return 2;
	}
}
