package com.example.notemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mStatus;

    private Button mSavevbtn;

    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;


    // Firebase
    private DatabaseReference mStatusDatabase;

    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);


        // Firebase`
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_user = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user);

        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance()
                .getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Statut");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Récupérer la valeur envoyé par SettingsActivity
        String status_value = getIntent().getStringExtra("status_value");

        mStatus = (TextInputLayout) findViewById(R.id.textInputLayout);
        mSavevbtn = (Button) findViewById(R.id.button_change_status);

        mStatus.getEditText().setText(status_value);

        mSavevbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ProgressBar
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Modification ...");
                mProgress.setMessage("Nous changeons votre statut...");
                mProgress.show();

                String status = mStatus.getEditText().getText().toString();

                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();
                            Toast.makeText(getApplicationContext(), "Statut changé avec succès", Toast.LENGTH_LONG).show();

                        } else {
                            mProgress.hide();
                            Toast.makeText(getApplicationContext(), "Il y a eu une erreur dans le changement de votre statut", Toast.LENGTH_LONG).show();
                        }
                    }
                });
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