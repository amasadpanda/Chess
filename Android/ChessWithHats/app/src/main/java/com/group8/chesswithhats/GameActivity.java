package com.group8.chesswithhats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.group8.chesswithhats.util.Game;
import com.group8.chesswithhats.util.LoadingDialog;
import com.group8.chesswithhats.util.MakeMoveListener;

import java.util.HashMap;

public class GameActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private HashMap<DatabaseReference, Object> listeners;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    private String gameID;
    private LoadingDialog loading;
    Game game;
    BoardView board;

    private TextView txtGameType;
    private TextView txtVersus;
    private TextView txtTurn;

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
            public boolean makeMove(int start, int end) {
                loading.show();
                CWHRequest request = new CWHRequest(auth.getCurrentUser(), CWHRequest.RequestType.MAKE_MOVE, new OnCWHResponseListener() {
                    @Override
                    public void onCWHResponse(CWHResponse response) {
                        loading.dismiss();
                        if (response.isSuccess()) {
                            Log.i("GameActivity","Move successfully sent.");
                            board.invalidate();
                        } else {
                            Log.e("GameActivity","Something's wrong...");
                            GameActivity.this.finish();
                            Toast.makeText(GameActivity.this, "Something's wrong...", Toast.LENGTH_SHORT).show();
                            System.out.println(response.getMessage());
                        }
                    }
                });
                request.put("start", ""+start);
                request.put("end", ""+end);
                request.put("gameid", gameID);
                request.sendRequest(GameActivity.this);
                return true; //just always return true for now? This nested chaos makes things very unwieldy.
            }
        });

        txtGameType = findViewById(R.id.game_txtGameType);
        txtVersus = findViewById(R.id.game_txtVersus);
        txtTurn = findViewById(R.id.game_txtTurn);

        //TODO: make it so white always goes first here. For clarity!
        txtVersus.setText(auth.getCurrentUser().getDisplayName() + " vs. " + getIntent().getStringExtra("opponent"));

        DatabaseReference gameReference = database.getReference().child("games").child(gameID);
        ValueEventListener gameListener = new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                loading.show();
                try{
                    game = dataSnapshot.getValue(Game.class); //This is so cool
                    board.setStateFromGame(game, auth.getCurrentUser().getUid());

                    // Update the text view for turn...
                    if (game.black.equals(auth.getCurrentUser().getUid()) && game.turn.equals("black") ||
                            game.white.equals(auth.getCurrentUser().getUid()) && game.turn.equals("white"))
                    {
                        txtTurn.setText("Your move");
                    }
                    else
                    {
                        txtTurn.setText(getIntent().getStringExtra("opponent") + "'s move");
                    }
                }catch(Exception e){
                    Log.e("GameActivity", "Unable to load game", e);
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
    }

    // Check if the user is signed in. If not, close the activity
    private void checkLoginStatus() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) { // We are not signed in!
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
        // Yeah. See below. Should remove all listeners that were properly inserted to the listeners hashmap.
        for (DatabaseReference reference : listeners.keySet())
        {
            Object listener = listeners.get(reference);
            if (listener instanceof ValueEventListener)
            {
                reference.removeEventListener((ValueEventListener)listener);
            }
            else if (listener instanceof ChildEventListener)
            {
                reference.removeEventListener((ValueEventListener)listener);
            }
        }
    }
}
