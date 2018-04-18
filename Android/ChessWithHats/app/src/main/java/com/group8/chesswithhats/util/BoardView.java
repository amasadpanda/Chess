//@formatter:off
package com.group8.chesswithhats.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.group8.chesswithhats.Hats;import java.util.HashSet;

import static com.group8.chesswithhats.util.ChessLogic.*;

//TODO: make it highlight the opponent's last move?
public class BoardView extends View{

    private static final HashSet<Integer> EMPTY = new HashSet<>();
    public static final String T = "BoardView";

    private int sideLen,sqLen,contentWidth,contentHeight,active = -1;
    private Piece[][] board = new Piece[8][8]; //start w/ empty board
    private HashSet<Integer> highlighted = EMPTY;
    private MakeMoveListener makeMoveListener;
    private boolean myTurn, white, ignore;

    private Context context;

    // Hats default to none... only if they are explicitly set do they get hats.
    private String blackHat = "none";
    private String whiteHat = "none";

    //I genuinely don't know what these constructors take in or do.
    public BoardView(Context context) {
        super(context);
        this.context = context;
        Log.d(T,"Constructor 1");
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        Log.d(T,"Constructor 2");
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        Log.d(T,"Constructor 3");
    }

    public void setBlackHat(String blackhat){
        this.blackHat = (blackhat == null ? "none" : blackhat.toLowerCase());
        invalidate();
    }

    public void setWhiteHat(String whitehat){
        this.whiteHat = (whitehat == null ? "none" : whitehat.toLowerCase());
        invalidate();
    }

    public void setStateFromGame(Game game, String user){
        //if(game.turn.contains("win_"))
        board = Game.toPieceArray(game.board);
        ignore = false;
        if(game.black.equals(user)) {
            myTurn = game.turn.equals("black");
            white = false;
            if(!myTurn && game.white.equals("COMPUTER"))
                sendAIMove(true);
        }else{
            myTurn = game.turn.equals("white");
            white = true;
            if(!myTurn && game.black.equals("COMPUTER"))
                sendAIMove(false);
        }
        invalidate();
        Log.d(T,"Game state successfully loaded. It's "+(myTurn?"your":"the opponent's")+" turn.");
    }

    private void sendAIMove(boolean white){
        //the loading figet spinner can't be shown here. It gets shown
        //at the beginning of the makeMove call...
        //TODO: switch back to pablo's code when he finishes that up.
        //TODO: also enable him to use pawn promotion.
        int move[] = ChessAI.getRandomMove(board,white);
        if(move==null){
            Log.d(T,"No moves left, human player won :(");
            return;
        }
        Log.i(T,"Sending AI move to server...");
        makeMoveListener.makeMove(move[0],move[1],white); //server is OK with you making a move on behalf of the other team.
    }

    public void setMakeMoveListener(MakeMoveListener listener){
        makeMoveListener = listener;
    }

    private Paint paint = new Paint();
    private Resources res = getResources();

    @Override
    //FIXME: We need to invalidate in at least one other place. Things are disappearing and taking a while to update.
    protected void onDraw(Canvas canvas) {

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        contentWidth = getWidth() - paddingLeft - paddingRight;
        contentHeight = getHeight() - paddingTop - paddingBottom;

        sideLen = Math.min(contentWidth,contentHeight);
        sqLen = sideLen/8;

        //loop through each space and draw it
        for(int i=0;i<8;i++){
            int r = white ? i : 7-i; //The board must be rotated if the player is black.
            for (int j=0;j<8;j++){
                int c = white ? j : 7-j;
                int L = c*sqLen, T = r*sqLen, R = c*sqLen + sqLen, B = r*sqLen + sqLen;
                int index = i*8 + j;
                //manually set color and draw a square
                if ((r + c) % 2 == 0)
                    paint.setARGB(255, 255, 255, 255);
                else
                    paint.setARGB(255, 0, 0, 0);
                canvas.drawRect(L, T, R, B, paint); //L T R B
                //draw space index
//                paint.setARGB(255,255,0,0);
//                paint.setTextSize(36);
//                canvas.drawText(""+index, L + sqLen/2, T + sqLen/2, paint);
                if(highlighted.contains(index)){
                    paint.setARGB(200,255,255,224);
                    canvas.drawRect(L,T,R,B,paint); //add a yellow tint over this square
                }
                //Note i,j and not r,c here. We're basically taking the pieces from each coordinate
                //and drawing them at a different location.
                if(board[i][j]!=null){

                    Drawable img;

                    if (board[i][j].white)
                        img = res.getDrawable(Hats.getDrawableID(board[i][j], whiteHat));
                    else
                        img = res.getDrawable(Hats.getDrawableID(board[i][j], blackHat));

                    img.setBounds(c*sqLen, r*sqLen, c*sqLen + sqLen, r*sqLen + sqLen); //L T R B
                    img.draw(canvas);
                }
            }
        }
    }

    //TODO: use performContextClick? That may be more appropriate here, since we don't need swipes or anything.
    @Override
    public boolean onTouchEvent(MotionEvent event){

        //BoardView contains EXCLUSIVELY the chess board. Other menu options will be
        //accessible via other views, so this is OK to do.
        if(!myTurn || ignore)
            return true;

        int x = (int)event.getX();
        int y = (int)event.getY();
        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            //Log.d(T,String.format("The user tapped (%d,%d)\n",x,y));

            //parse screen coordinates into chess board coordinates
            x -= getPaddingLeft();
            x /= sqLen;
            y -= getPaddingTop();
            y /= sqLen;

            if(!white){ //rotate board for black
                y = 7-y;
                x = 7-x;
            }

            int index = y * 8 + x;

            Log.d(T,String.format("The user tapped (%d,%d): Square %d\n", y, x, index));

            if (index < 0 || index > 63) {
                Log.d(T,"Out of bounds tap");
                active = -1;
                highlighted = EMPTY;
                return true;
            }

            if (active == -1) { //nothing selected
                if (board[y][x] != null && board[y][x].white==white) { //we've found a valid selection!
                    Log.d(T,String.format("Getting valid moves for %s...", board[y][x].getClass().getSimpleName()));
                    highlighted = board[y][x].getMoves(index, board);
                    Log.d(T,String.format("%d valid moves.", highlighted.size()));
                    if(highlighted.size()>0){
                        active = index;
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        invalidate(); //haha ironic
                    }else{
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    }
                }
            } else { //something selected!
                if (highlighted.contains(index)){ //a valid move location was tapped.
                    if(makeMoveListener==null)
                        throw new NullPointerException("No make move listener in this board view!! Why.....");
                    ignore = true; //ignore all future taps until response from database. This prevents double moves from getting sent.
                    int py = active/8, px = active%8;
                    if(board[py][px] instanceof Pawn && ((white && y==0) || (!white && y==7))){
                        //we got a promotion on our hands, everybody...
                        Log.i(T,"Requesting and sending pawn promotion...");
                        makeMoveListener.makeMove(active,index,true,white);
                    }else{
                        Log.i(T,"Sending move to server...");
                        makeMoveListener.makeMove(active,index,white);
                    }

                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    highlighted = EMPTY;
                    active = -1;
                    //solid move, dude! Board will shortly be updated with state from server.
                }else{ //invalid location. This is how you cancel a piece selection.
                    clearActive();
                }
                //invalidate();
            }
            return true;
        }
        return false;
    }

    public void clearActive(){
        ignore = false;
        highlighted = EMPTY;
        active = -1;
        invalidate();
    }

    public boolean onBackPressed(){
        if(active!=-1){
            clearActive();
            return true;
        }
        return false;
    }

}
