package com.group8.chesswithhats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class GameActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private FirebaseAuth auth;

    private String gameID;
    private LoadingDialog loading;
    Game game;
    BoardView board;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        loading = new LoadingDialog(this,"The emporer has no clothes!!","Please wait while he puts something on...");
        loading.show();

        gameID = getIntent().getStringExtra("gameid");

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                checkLoginStatus();
            }
        });

        setContentView(R.layout.activity_game);
        board = (BoardView)findViewById(R.id.boardView);
        board.setMakeMoveListener(new MakeMoveListener() {
            @Override
            public boolean makeMove(int start, int end) {
                loading.show();
                CWHRequest request = new CWHRequest(auth.getCurrentUser(), CWHRequest.RequestType.MAKE_MOVE, new OnCWHResponseListener() {
                    @Override
                    public void onCWHResponse(CWHResponse response) {
                        if (response.isSuccess()) {
                            System.out.println("Move successfully sent.");
                            loading.dismiss();
                            board.invalidate();
                        } else {
                            System.out.println("Something's wrong...");
                            loading.dismiss();
                            GameActivity.this.finish();
                            Toast.makeText(GameActivity.this, "Something's wrong...",Toast.LENGTH_SHORT).show();
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

        DatabaseReference ref = database.getReference().child("games").child(gameID);
        ref.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                loading.show();
                try {
                    game = dataSnapshot.getValue(Game.class);
                    board.setStateFromGame(game, auth.getCurrentUser().getUid());
                }catch(Exception e){
                    Log.e("GameActivity", "Unable to load game", e);
                    GameActivity.this.finish();
                    Toast.makeText(GameActivity.this, "Unable to load game", Toast.LENGTH_SHORT).show();
                }
                loading.dismiss();
            }
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    // Check if the user is signed in. If not, close the activity
    private void checkLoginStatus() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // We are not signed in!
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLoginStatus();
    }
}
