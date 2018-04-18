//@formatter:off
package com.group8.chesswithhats;

import android.util.Log;import java.util.HashMap;
import static com.group8.chesswithhats.util.ChessLogic.*;

//Pawn Rook Knight Bishop Queen King
public class Hats{

    public static final String T = "Hats";
    private static HashMap<String,int[][]> map;

    static{
        map = new HashMap<>();
        map.put("None",new int[][]{
            new int[]{R.drawable.none_black_pawn, R.drawable.none_black_rook, R.drawable.none_black_knight, R.drawable.none_black_bishop, R.drawable.none_black_queen, R.drawable.none_black_queen},
            new int[]{R.drawable.none_white_pawn, R.drawable.none_white_rook, R.drawable.none_white_knight, R.drawable.none_white_bishop, R.drawable.none_white_queen, R.drawable.none_white_queen}
		});
        map.put("Fedora",new int[][]{
            new int[]{R.drawable.fedora_black_pawn, R.drawable.fedora_black_rook, R.drawable.fedora_black_knight, R.drawable.fedora_black_bishop, R.drawable.fedora_black_queen, R.drawable.fedora_black_queen},
            new int[]{R.drawable.fedora_white_pawn, R.drawable.fedora_white_rook, R.drawable.fedora_white_knight, R.drawable.fedora_white_bishop, R.drawable.fedora_white_queen, R.drawable.fedora_white_queen}
		});
//        map.put("Top Hat",new int[][]{
//            new int[]{R.drawable.tophat_black_pawn, R.drawable.tophat_black_rook, R.drawable.tophat_black_knight, R.drawable.tophat_black_bishop, R.drawable.tophat_black_queen, R.drawable.tophat_black_queen},
//            new int[]{R.drawable.tophat_white_pawn, R.drawable.tophat_white_rook, R.drawable.tophat_white_knight, R.drawable.tophat_white_bishop, R.drawable.tophat_white_queen, R.drawable.tophat_white_queen}
//		});
    }

    public static int getDrawableID(Piece p, String hat){
        int[][] drawables = map.get(hat);
        if(drawables==null){
            Log.w(T,"Invalid hat type \""+hat+"\". Defaulting to no hat.");
            drawables = map.get("None");
        }
        return drawables[p.white?1:0][p.getType()]; //"static" value in each piece subclass.
    }

}
