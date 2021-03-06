//@formatter:off
package com.group8.chesswithhats;

import android.content.DialogInterface;import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group8.chesswithhats.server.CWHRequest;
import com.group8.chesswithhats.server.CWHResponse;
import com.group8.chesswithhats.server.OnCWHResponseListener;
import com.group8.chesswithhats.util.BoardView;
import com.group8.chesswithhats.util.ChessLogic;import com.group8.chesswithhats.util.Game;
import com.group8.chesswithhats.util.LoadingDialog;
import com.group8.chesswithhats.util.MakeMoveListener;

import java.util.HashMap;

//TODO: Option to forfeit!
public class GameActivity extends AppCompatActivity {

    public static final String T = "GameActivity";

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private HashMap<DatabaseReference, Object> listeners;

    private String gameID;
    private LoadingDialog loading;
    private Game game;

    private BoardView board;
    private TextView txtGameType;
    private TextView txtYou;
    private TextView txtOpponent;
    private TextView txtWinner;
    private ImageView btnLeaveGame;
    private ImageView flagTop;
    private ImageView flagBottom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        loading = new LoadingDialog(this, "The emperor has no clothes!!", "Please wait while he puts something on...");
        loading.show();

        gameID = getIntent().getStringExtra("gameid");

        database = FirebaseDatabase.getInstance();
        listeners = new HashMap<>();
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                checkLoginStatus();
            }
        };
        auth.addAuthStateListener(authListener);

        setContentView(R.layout.activity_game);
        board = (BoardView) findViewById(R.id.boardView);
        board.setMakeMoveListener(new MakeMoveListener() {
            @Override
            public boolean makeMove(final int start, final int end, boolean promotion, final boolean white) {
                if(promotion){ //Present promotion selection dialogue
                    Log.d(T,"Creating pawn promotion dialogue...");
                    AlertDialog.Builder selectorBuilder = new AlertDialog.Builder(GameActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View selector = inflater.inflate(R.layout.promotion_selector, null);
                    selectorBuilder.setView(selector);
                    selectorBuilder.setTitle("Promote your pawn");
                    final AlertDialog selectorDialog = selectorBuilder.create();

                    //one of three things needs to happen:
                    //1. This view cannot be backed out of
                    //2. Backing out of THIS view will also back out of GameActivity
                    //3. Backing out of THIS view will clear active in the board view
                    //Each button's callback sends an appropriately formatted request
                    selector.findViewById(R.id.btnQueen).setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            selectorDialog.dismiss();
                            sendMoveToServer(start, end, new ChessLogic.Queen(white).toString());
                        }
                    });
                    selector.findViewById(R.id.btnBishop).setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            selectorDialog.dismiss();
                            sendMoveToServer(start, end, new ChessLogic.Bishop(white).toString());
                        }
                    });
                    selector.findViewById(R.id.btnKnight).setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            selectorDialog.dismiss();
                            sendMoveToServer(start, end, new ChessLogic.Knight(white).toString());
                        }
                    });
                    selector.findViewById(R.id.btnRook).setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            selectorDialog.dismiss();
                            sendMoveToServer(start, end, new ChessLogic.Rook(white).toString());
                        }
                    });

                    selectorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {@Override
                        public void onDismiss(DialogInterface dialog) {
                            board.clearActive();
                        }});

                    selectorDialog.show();

                }else{ //There is no promotion for this move, so set promote to null.
                    sendMoveToServer(start, end, null);
                }
                return true; //This nested chaos makes things so unwieldy that I pretty much treat this as void.
            }
        });

        txtGameType = findViewById(R.id.game_txtGameType);
        txtYou = findViewById(R.id.game_txtYou);
        txtOpponent = findViewById(R.id.game_txtOpponent);
        flagBottom = findViewById(R.id.game_flagBottom);
        flagTop = findViewById(R.id.game_flagTop);
        txtWinner = findViewById(R.id.game_txtWinner);


        txtYou.setText(auth.getCurrentUser().getDisplayName());
        txtOpponent.setText(getIntent().getStringExtra("opponent"));

        DatabaseReference gameReference = database.getReference().child("games").child(gameID);
        ValueEventListener gameListener = new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                loading.show();
                try{
                    game = dataSnapshot.getValue(Game.class); //This is so cool
                    String userID = auth.getCurrentUser().getUid();
                    String opponent = getIntent().getStringExtra("opponent");
                    board.setStateFromGame(game, userID);

                    //Update the text view for whose turn it is
                    if(game.turn.startsWith("winner=")){
                        txtWinner.setVisibility(View.VISIBLE);
                        //Somebody has winned!
                        String winner = game.turn.substring(7);
                        if(winner.equals("Nobody!!!"))
                            txtWinner.setText("Stalemate...");
                        else if(winner.equals(userID))
                            txtWinner.setText("You won!");
                        else
                            txtWinner.setText("Better luck next time!");
                    }else if (game.black.equals(userID) && game.turn.equals("black") ||
                            game.white.equals(userID) && game.turn.equals("white")){
                        flagBottom.setVisibility(View.VISIBLE);
                        flagTop.setVisibility(View.INVISIBLE);
                    }else{
                        flagTop.setVisibility(View.VISIBLE);
                        flagBottom.setVisibility(View.INVISIBLE);
                    }
                    txtGameType.setText(game.gametype);



                    // Now, load hat information for black and white players!
                    database.getReference().child("users").child(game.black).child("hat").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.i(T,"Fetched black hat: \"" + dataSnapshot.getValue(String.class)+"\"");
                            board.setBlackHat(dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    database.getReference().child("users").child(game.white).child("hat").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.i(T,"Fetched white hat: \"" + dataSnapshot.getValue(String.class)+"\"");
                            board.setWhiteHat(dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }catch(Exception e){
                    Log.e(T, "Unable to load game", e);
                    GameActivity.this.finish();
                    Toast.makeText(GameActivity.this, "Unable to load game", Toast.LENGTH_SHORT).show();
                }
                loading.dismiss();
            }

            public void onCancelled(DatabaseError databaseError) {

            }
        };
        gameReference.addValueEventListener(gameListener);
        listeners.put(gameReference, gameListener);

        btnLeaveGame = findViewById(R.id.game_btnLeaveGame);
        btnLeaveGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClick = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which)
                        {
                            case DialogInterface.BUTTON_POSITIVE:
                                CWHRequest cwhRequest = new CWHRequest(auth.getCurrentUser(), CWHRequest.RequestType.LEAVE_GAME, new OnCWHResponseListener() {
                                    @Override
                                    public void onCWHResponse(CWHResponse response) {
                                        Toast.makeText(GameActivity.this, response.getMessage(), Toast.LENGTH_LONG).show();

                                        if (response.isSuccess())
                                        {
                                            btnLeaveGame.setVisibility(View.INVISIBLE);
                                            finish();
                                        }
                                        else
                                        {
                                            btnLeaveGame.setVisibility(View.VISIBLE);
                                            finish();
                                        }
                                    }
                                });
                                cwhRequest.getExtras().put("gameid", gameID);
                                cwhRequest.sendRequest(GameActivity.this);

                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                btnLeaveGame.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                builder.setMessage("Are you sure you want to forfeit this game?")
                        .setPositiveButton("Yes", dialogClick)
                        .setNegativeButton("No", dialogClick)
                        .show();
            }
        });
    }

    private void sendMoveToServer(int start, int end, String promote){
            loading.show();
            CWHRequest request = new CWHRequest(auth.getCurrentUser(), CWHRequest.RequestType.MAKE_MOVE, new OnCWHResponseListener() {
                @Override
                public void onCWHResponse(CWHResponse response) {
                    loading.dismiss();
                    if (response.isSuccess()) {
                        Log.i(T,"Move successfully sent.");
                        board.invalidate();
                    } else {
                        Log.e(T,"Couldn't send move: "+response.getMessage());
                        GameActivity.this.finish();
                        Toast.makeText(GameActivity.this, "Couldn't send move. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            request.put("start", ""+start);
            request.put("end", ""+end);
            request.put("gameid", gameID);
            if(promote!=null)
                request.put("promote",promote);
            request.sendRequest(GameActivity.this);
    }

    public void onBackPressed(){
        if(!board.onBackPressed())
            super.onBackPressed();
    }

    // Check if the user is signed in. If not, close the activity
    //FIXME: This gets called twice, for some reason?
    private void checkLoginStatus() {
        Log.d(T,"Checking login status");
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) { // We are not signed in!
            Log.w(T,"User is not signed in anymore!");
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLoginStatus();
    }

    protected void onDestroy(){
        super.onDestroy();
        //OHHHH boy...We need to manually keep track of and kill all the listeners.
        //Yeah. See below. Should remove all listeners that were properly inserted to the listeners hashmap.
        for (DatabaseReference reference : listeners.keySet()){
            Object listener = listeners.get(reference);
            if (listener instanceof ValueEventListener || listener instanceof ChildEventListener)
                reference.removeEventListener((ValueEventListener)listener);
        }
    }
}
