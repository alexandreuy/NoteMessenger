package com.example.notemessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName;
    private TextView mProfileStatus;
    private TextView mProfileTotalFriend;
    private Button mProfileSendRequestBtn, mProfileDeclineButton;

    private DatabaseReference mUsersDatabase;
    private ProgressDialog mProgressDialog;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendListDatabase;
    private DatabaseReference mNotificationDatabase;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private String mCurrent_state ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Récupère l'id de la personne sur le profile où on se situe
        String user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        // Permet récupérer la branche du profil sur lequel on se situe
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        // Création nouvelle branche dans la base Friend_req
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friends_req");

        // Création nouvelle branche dans la base Friend_list
        mFriendListDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance()
                .getReference().child("Users").child(mAuth.getCurrentUser().getUid());


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Fields
        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_display_name);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileTotalFriend = (TextView) findViewById(R.id.profile_total_friends);
        mProfileSendRequestBtn = (Button) findViewById(R.id.profile_send_req_friend_btn);
        mProfileDeclineButton = (Button) findViewById(R.id.profile_decline_friend_btn);

        mCurrent_state = "not_friends";

        mProfileDeclineButton.setVisibility(View.INVISIBLE);
        mProfileDeclineButton.setEnabled(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("En cours de chargement des données de l'utilisateur...");
        mProgressDialog.setTitle("Veuillez patienter");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();



        // Récupération des données du profil sur lequel on se situe
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String image = snapshot.child("image").getValue().toString();

                mProfileName.setText(name);
                mProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                // Liste d'amis et requête d'amis
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild(user_id)){
                            String request_type = snapshot.child(user_id).child("request_type").getValue().toString();

                            if(request_type.equals("received")){

                                mCurrent_state = "req_received";
                                mProfileSendRequestBtn.setText("Accepter la demande d'ami");
                                mProfileDeclineButton.setVisibility(View.VISIBLE);
                                mProfileDeclineButton.setEnabled(true);

                            } else if(request_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mProfileSendRequestBtn.setText("Annuler la demande d'ami");
                                mProfileDeclineButton.setVisibility(View.INVISIBLE);
                                mProfileDeclineButton.setEnabled(false);

                            }
                            mProgressDialog.dismiss();
                        } else {
                            mFriendListDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(user_id)){
                                        mCurrent_state = "friends";
                                        mProfileSendRequestBtn.setText("Supprimer cet ami");
                                        mProfileDeclineButton.setVisibility(View.INVISIBLE);
                                        mProfileDeclineButton.setEnabled(false);

                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mProfileSendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendRequestBtn.setEnabled(false);

                // Lorsque les deux utilisateurs ne sont pas amis
                if(mCurrent_state.equals("not_friends")){

                    DatabaseReference newNotifRef = mRootRef.child("notifications").child(user_id).push();
                    String newNotifId = newNotifRef.getKey();

                    HashMap<String, String> notifData = new HashMap<>();
                    notifData.put("from", mCurrentUser.getUid());
                    notifData.put("type", "request");

                    Map requestMap = new HashMap<>();
                    requestMap.put("Friends_req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friends_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotifId, notifData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if(error != null ){
                                Toast.makeText(ProfileActivity.this, "Oups, une erreur vient de se produire", Toast.LENGTH_LONG).show();
                            }
                            mProfileSendRequestBtn.setEnabled(true);
                            mCurrent_state = "req_sent";
                            mProfileSendRequestBtn.setText("Annuler la demande d'ami");

                        }
                    });
                }

                // Annuler la demande d'ami
                if(mCurrent_state.equals("req_sent")) {
                  mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener((OnSuccessListener) -> {
                    mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener((onSuccessListener) -> {
                        mProfileSendRequestBtn.setEnabled(true);
                        mCurrent_state= "not_friends";
                        mProfileSendRequestBtn.setText("Demander en ami");

                        mProfileDeclineButton.setVisibility(View.INVISIBLE);
                        mProfileDeclineButton.setEnabled(false);

                    });

                  });


                }

                // Demande d'ami reçue
                if(mCurrent_state.equals("req_received")){
                    String current_date = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date" , current_date);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date" , current_date);

                    friendsMap.put("Friends_req/" + mCurrentUser.getUid() + "/" + user_id , null);
                    friendsMap.put("Friends_req/" + user_id + "/" + mCurrentUser.getUid() , null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if(error == null) {
                                mProfileSendRequestBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendRequestBtn.setText("Retirer cet ami");

                                mProfileDeclineButton.setVisibility(View.INVISIBLE);
                                mProfileDeclineButton.setEnabled(false);

                            } else {
                                String errorM = error.getMessage();
                                Toast.makeText(ProfileActivity.this, errorM , Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }

                // Retirer ami
                if(mCurrent_state.equals("friends")){
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if(error == null) {

                                mCurrent_state = "no_friends";
                                mProfileSendRequestBtn.setText("Demander en ami");

                                mProfileDeclineButton.setVisibility(View.INVISIBLE);
                                mProfileDeclineButton.setEnabled(false);

                            } else {
                                String errorM = error.getMessage();
                                Toast.makeText(ProfileActivity.this, errorM , Toast.LENGTH_LONG).show();

                            }
                            mProfileSendRequestBtn.setEnabled(true);
                        }
                    });

                }
            }
        });

    }

    // Vérification si un utilisateur est connecté
    @Override
    public void onStart() {
        super.onStart();

        // Mettre à jour la valeur à vraie Firebase
        mUserRef.child("online").setValue("true");
    }


}