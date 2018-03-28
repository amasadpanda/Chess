import java.util.HashSet;

public class ChessLogic {
	
	//Base Piece Class
	public abstract class piece {
		boolean white;
		public piece(boolean isWhite) {
			white=isWhite;
		}
		abstract HashSet<Integer> getMoves(int loc, piece[][] board);
		boolean inBounds(int r,int c) {
			return r>=0&&r<8&&c>=0&&c<8;
		}
	}
	
	//Pawn
	public class pawn extends piece {
		public pawn(boolean isWhite) {
			super(isWhite);
		}
		HashSet<Integer> getMoves(int loc, piece[][] board){
			HashSet<Integer> moves=new HashSet<Integer>();
			int r=loc/8,c=loc%8;
			
			//If the pawn is white, it starts on row 6 and goes up (down? I hate that convention.), otherwise, it starts on row 1 and goes down.
			int dr=-1,start=6;
			if(!white) {
				dr=1;
				start=1;
			}
			if(inBounds(r+dr,c)&&board[r+dr][c]==null) {
				moves.add((r+dr)*8+c);

				//Pawns can move two squares on their first turn.
				if(r==start) {
					if(inBounds(r+2*dr,c)&&board[r+2*dr][c]==null) {
						moves.add((r+2*dr)*8+c);
					}
				}
			}

			//Check if it can capture
			if(inBounds(r+dr,c-1)&&board[r+dr][c-1]!=null&&board[r+dr][c-1].white!=white) {
				moves.add((r+dr)*8+c-1);
			}
			if(inBounds(r+dr,c+1)&&board[r+dr][c+1]!=null&&board[r+dr][c+1].white!=white) {
				moves.add((r+dr)*8+c+1);
			}
			
			return moves;
		}
	}
	
	//King
	public class king extends piece {
		public king(boolean isWhite) {
			super(isWhite);
		}
		HashSet<Integer> getMoves(int loc,piece[][] board) {
			int r=loc/8,c=loc%8;
			int[] dr= {-1,-1,0,1,1,1,0,-1},dc= {0,1,1,1,0,-1,-1,-1};
			HashSet<Integer> moves=new HashSet<Integer>();
			
			//Check all eight directions
			for(int i=0;i<8;i++) {
				int r2=r+dr[i],c2=c+dc[i];
				if(inBounds(r2,c2)&&(board[r2][c2]==null||board[r2][c2].white!=white)) {
					moves.add(r2*8+c2);
				}
			}
			return moves;
		}
	}
	
	//Knight
	public class knight extends piece {
		public knight(boolean isWhite) {
			super(isWhite);
		}
		HashSet<Integer> getMoves(int loc,piece[][] board){
			int r=loc/8,c=loc%8;
			int[] dr= {-2,-1,1,2,2,1,-1,-2}, dc= {1,2,2,1,-1,-2,-2,-1};
			HashSet<Integer> moves=new HashSet<Integer>();
			for(int i=0;i<8;i++) {
				int r2=r+dr[i],c2=c+dc[i];
				if(inBounds(r2,c2)&&(board[r2][c2]==null||board[r2][c2].white!=white)) {
					moves.add(r2*8+c2);
				}
			}
			return moves;
		}
	}
	
	//Rook
	public class rook extends piece {
		public rook(boolean isWhite) {
			super(isWhite);
		}
		HashSet<Integer> getMoves(int loc,piece[][] board) {
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
						moves.add(r2*8+c2);
						continue;
					}
					//Capture
					if(board[r2][c2].white!=white)
						moves.add(r2*8+c2);
					
					//If there was a piece here, the rook can't go any further in this direction.
					break;
				}
			}
			return moves;
		}
	}
	
	//Bishop
	public class bishop extends piece {
		public bishop(boolean isWhite) {
			super(isWhite);
		}
		HashSet<Integer> getMoves(int loc,piece[][] board){
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
						moves.add(r2*8+c2);
						continue;
					}
					//Capture
					if(board[r2][c2].white!=white)
						moves.add(r2*8+c2);
					
					//If there was a piece here, the bishop can't go any further in this direction.
					break;
				}
			}
			return moves;
		}
	}
	
	//Queen
	public class queen extends piece {
		public queen(boolean isWhite) {
			super(isWhite);
		}
		
		//This is basically a rook with a king's dr/dc array.
		HashSet<Integer> getMoves(int loc,piece[][] board){
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
						moves.add(r2*8+c2);
						continue;
					}
					//Capture
					if(board[r2][c2].white!=white)
						moves.add(r2*8+c2);
					break;
				}
			}
			return moves;
		}
	}
}
