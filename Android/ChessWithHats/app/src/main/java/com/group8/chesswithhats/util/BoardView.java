//@formatter:off
package com.group8.chesswithhats.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;

import static com.group8.chesswithhats.util.ChessLogic.*;

//TODO: make it highlight the opponent's last move?

/**
 * TODO: document your custom view class.
 */
public class BoardView extends View{
//    private String mExampleString; // TODO: use a default from R.string...
//    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
//    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
//    private Drawable mExampleDrawable;
//
//    private TextPaint mTextPaint;
//    private float mTextWidth;
//    private float mTextHeight;

    private static final HashSet<Integer> EMPTY = new HashSet<>();

    private int sideLen,sqLen,contentWidth,contentHeight,active = -1;
    private Piece[][] board = new Piece[8][8]; //start w/ empty board
    private HashSet<Integer> highlighted = EMPTY;
    private MakeMoveListener makeMoveListener;
    private boolean myTurn, white, ignore;

    //I genuinely don't know what these constructors take in or do.
    public BoardView(Context context) {
        super(context);
        System.out.println("Constructor 1");
        //init(null, 0);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        System.out.println("Constructor 2");
       //init(attrs, 0);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        System.out.println("Constructor 3");
        //init(attrs, defStyle);
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
    }

    private void sendAIMove(boolean white){
        //the loading figet spinner can't be shown here. It gets shown
        //at the beginning of the makeMove call...
        int move[] = ChessAI.getMove(board,white, 2);
        makeMoveListener.makeMove(move[0],move[1]);
    }

    public void setMakeMoveListener(MakeMoveListener listener){
        makeMoveListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        Resources res = getResources();

        contentWidth = getWidth() - paddingLeft - paddingRight;
        contentHeight = getHeight() - paddingTop - paddingBottom;

        sideLen = Math.min(contentWidth,contentHeight);
        sqLen = sideLen/8;
        Paint paint = new Paint();

        for(int i=0;i<8;i++){
            int r = white ? i : 7-i;
            for (int j=0;j<8;j++){
                int c = white ? j : 7-j;
                int L = c*sqLen, T = r*sqLen, R = c*sqLen + sqLen, B = r*sqLen + sqLen;
                int index = i*8 + j;
                if ((r + c) % 2 == 0)
                    paint.setARGB(255, 255, 255, 255);
                else
                    paint.setARGB(255, 0, 0, 0);
                canvas.drawRect(L, T, R, B, paint); //L T R B
                paint.setARGB(255,255,0,0);
                paint.setTextSize(36);
                canvas.drawText(""+index, L + sqLen/2, T + sqLen/2, paint);
                if(highlighted.contains(index)){
                    paint.setARGB(200,255,255,224);
                    canvas.drawRect(L,T,R,B,paint);
                }
                if(board[i][j]!=null){
                    Drawable img = res.getDrawable(board[i][j].getDrawableID());
                    img.setBounds(c*sqLen, r*sqLen, c*sqLen + sqLen, r*sqLen + sqLen); //L T R B
                    img.draw(canvas);
                }
            }
        }
    }

    //TODO: use performContextClick? That may be more appropriate here, since we don't need swipes or anything.
    @Override
    public boolean onTouchEvent(MotionEvent event){ //REEVALUTAE WHEN INVALIDATA LOL

        //This is PROBABLY sufficient! BoardView contains the board EXCLUSIVELY.
        //any extra stuff we add later would be in different views.
        if(!myTurn || ignore)
            return true;

        int x = (int)event.getX();
        int y = (int)event.getY();
        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            //System.out.printf("The user tapped (%d,%d)\n",x,y);
            x -= getPaddingLeft();
            x /= sqLen;
            y -= getPaddingTop();
            y /= sqLen;

            if(!white){
                y = 7-y;
                x = 7-x;
            }

            int index = y * 8 + x;

            System.out.printf("(%d,%d): Square %d\n", y, x, index);

            if (index < 0 || index > 63) {
                System.out.println("Out of bounds tap");
                active = -1;
                highlighted = EMPTY;
                return true;
            }

            if (active == -1) { //nothing selected
                if (board[y][x] != null && board[y][x].white==white) {
                    System.out.printf("Getting valid moves for %s...\n", board[y][x].getClass().getSimpleName());
                    highlighted = board[y][x].getMoves(index, board);
                    System.out.printf("%d valid moves.\n", highlighted.size());
                    //TODO: if highlighted.size()==0, don't actually select this piece. It's more intuitive.
                    //status.setText(String.format("%d valid moves",highlighted.size()));
                    active = index;
                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    invalidate(); //haha ironic
                }
            } else { //something selected!
                if (highlighted.contains(index)) {
                    int py = active / 8, px = active % 8;
                    //board[y][x] = board[py][px];
                    //board[py][px] = null;
                    if(makeMoveListener==null)
                        throw new NullPointerException("No make move listener in this board view!! Why.....");
                    ignore = true;
                    makeMoveListener.makeMove(active,index);
                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    highlighted = EMPTY;
                    active = -1;
                    //status.setText("Solid move, dude!");
                }else{
                    highlighted = EMPTY;
                    active = -1;
                    invalidate();
                }
                //invalidate();
            }
            return true;
        }
        return false;
    }

}
