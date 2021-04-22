package com.example.notemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button btn_register;

    private Toolbar mToolbar;

    // ProgessDialog
    private ProgressDialog mRegProgress;

    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Toolbar set
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Créer un compte");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Barre de progression après avoir cliqué sur "Créer un compte"
        mRegProgress = new ProgressDialog(this);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // Android Fields
        mName = (TextInputLayout) findViewById(R.id.reg_name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        btn_register = (Button) findViewById(R.id.btn_create_account);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                // Vérifier si des champs vide
                if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    // Barre de progression après avoir cliqué sur "Créer un compte"
                    mRegProgress.setTitle("Création en cours");
                    mRegProgress.setMessage("Veuillez patienter");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(name, email, password);
                }

            }
        });

    }

    private void register_user(final String name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener((task) -> {
            if (task.isSuccessful()) {

                FirebaseDatabase database =  FirebaseDatabase.getInstance();
                FirebaseUser current_user = mAuth.getCurrentUser();
                String uid = current_user.getUid();

                DatabaseReference mRef =  database.getReference().child("Users").child(uid);

                HashMap<String, String> userMap = new HashMap<>();
                userMap.put("name", name);
                userMap.put("status", "Bonjour ! J'utilise NoteMessenger !");
                userMap.put("image", "default");
                userMap.put("thumb_image", "default");

                mRef.setValue(userMap).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        mRegProgress.dismiss();

                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                        startActivity(mainIntent);
                        finish();
                    }
                });
            } else {
                mRegProgress.hide();
                Toast.makeText(RegisterActivity.this, "Nous ne pouvons pas créer votre compte. Veuillez vérifier les données insérées et réessayez.", Toast.LENGTH_LONG).show();
            }
        });
    }
}