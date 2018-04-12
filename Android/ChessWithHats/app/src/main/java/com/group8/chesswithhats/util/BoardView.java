package com.group8.chesswithhats.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;

import static com.group8.chesswithhats.util.ChessLogic.*;

/**
 * TODO: document your custom view class.
 */
public class BoardView extends View {
//    private String mExampleString; // TODO: use a default from R.string...
//    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
//    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
//    private Drawable mExampleDrawable;
//
//    private TextPaint mTextPaint;
//    private float mTextWidth;
//    private float mTextHeight;

    private int sideLen,sqLen,contentWidth,contentHeight;
    private Piece[][] board;
    private int active = -1;
    private HashSet<Integer> highlighted;
    private static final HashSet<Integer> EMPTY = new HashSet<>();
    //private TextView status = findViewById(R.id.statusMessage);

    public BoardView(Context context) {
        super(context);
        init(null, 0);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        //TODO: This should read board state from the server.
        //Move this initial state creation to, say, ChessLogic?

        board = new Piece[8][8];
        for(int j=0;j<8;j++){
            board[1][j] = new Pawn(false);
            board[6][j] = new Pawn(true);
        }

        board[0] = new Piece[]{new Rook(false), new Knight(false), new Bishop(false), new Queen(false), new King(false), new Bishop(false), new Knight(false), new Rook(false)};
        board[7] = new Piece[]{new Rook(true), new Knight(true), new Bishop(true), new Queen(true), new King(true), new Bishop(true), new Knight(true), new Rook(true)};

        highlighted = new HashSet<>();

    }

//    private void invalidateTextPaintAndMeasurements() {
//        mTextPaint.setTextSize(mExampleDimension);
//        mTextPaint.setColor(mExampleColor);
//        mTextWidth = mTextPaint.measureText(mExampleString);
//
//        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
//        mTextHeight = fontMetrics.bottom;
//    }

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

        for(int i=0;i<8;i++) {
            for (int j = 0; j < 8; j++) {
                int L = j*sqLen, T = i*sqLen, R = j*sqLen + sqLen, B = i*sqLen + sqLen;
                int index = i*8 + j;
                if ((i + j) % 2 == 0)
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
                if(board[i][j]!=null) {
                    Drawable img = res.getDrawable(board[i][j].getDrawableID());
                    img.setBounds(j * sqLen, i * sqLen, j * sqLen + sqLen, i * sqLen + sqLen); //L T R B
                    img.draw(canvas);
                }
            }
        }
    }

    //TODO: use performContextClick? That may be more appropriate here, since we don't need swipes or anything.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //System.out.printf("The user tapped (%d,%d)\n",x,y);
                x -= getPaddingLeft();
                x/=sqLen;
                y -= getPaddingTop();
                y/=sqLen;

                int index = y*8 + x;

                System.out.printf("(%d,%d): Square %d\n",y,x,index);

                if(index<0 || index>63) {
                    System.out.println("Out of bounds tap");
                    active = -1;
                    highlighted = EMPTY;
                    return false;
                }

                if(active==-1){ //nothing selected
                    if (board[y][x] != null) {
                        System.out.printf("Getting valid moves for %s...\n", board[y][x].getClass().getSimpleName());
                        highlighted = board[y][x].getMoves(index, board);
                        System.out.printf("%d valid moves.\n", highlighted.size());
                        //status.setText(String.format("%d valid moves",highlighted.size()));
                        active = index;
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        invalidate(); //haha ironic
                    }
                }else{ //something selected!
                    //TODO: should this shit go in ChessLogic? So it can account for castling, etc.?
                    if(highlighted.contains(index)){
                        int py = active/8, px = active%8;
                        board[y][x] = board[py][px];
                        board[py][px] = null;
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        //status.setText("Solid move, dude!");
                    }
                    highlighted = EMPTY;
                    active = -1;
                    invalidate();
                }

                return true;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
        }
        return false;
    }

}
