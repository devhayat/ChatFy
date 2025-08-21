package com.example.wordwave;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;


public class Authentication extends AppCompatActivity {

    private TabLayout authenticatoin_tablayout;
    private androidx.constraintlayout.widget.ConstraintLayout view_signup, view_signin;
    private EditText signup_username, signup_password, signup_email, signin_email, signin_password;
    private TextView signin_textview, signup_textview;
    private ProgressBar progressBar_signup, progressBar_signin;
    private TextView signin_forgetpassword;
    private FirebaseAuth mAuth;
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authentication);

        initlization();
    }

    protected void initlization() {

        authenticatoin_tablayout = findViewById(R.id.authentication_tablayout);
        view_signin = findViewById(R.id.view_signin);
        view_signup = findViewById(R.id.view_signup);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // If already logged in → go to MainActivity
        if (currentUser != null) {
            startActivity(new Intent(Authentication.this, MainActivity.class));
            finish();
        }

        authenticatoin_tablayout.selectTab(authenticatoin_tablayout.getTabAt(1));
        view_signup.setVisibility(View.GONE);
        view_signin.setVisibility(View.VISIBLE);

        authenticatoin_tablayout.setOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (tab.getPosition() == 1) {
                            view_signup.setVisibility(View.GONE);
                            view_signin.setVisibility(View.VISIBLE);
                        } else {
                            view_signin.setVisibility(View.GONE);
                            view_signup.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                }
        );

        emailPasswordAuthentication();
    }

    protected void emailPasswordAuthentication() {
        signin_email = findViewById(R.id.signin_email);
        signin_password = findViewById(R.id.signin_password);
        signin_textview = findViewById(R.id.signin_textview);
        progressBar_signin = findViewById(R.id.progressBar_signin);
        signup_email = findViewById(R.id.signup_email);
        signup_password = findViewById(R.id.signup_password);
        signup_username = findViewById(R.id.signup_username);
        signup_textview = findViewById(R.id.signup_textview);
        progressBar_signup = findViewById(R.id.progressBar_signup);
        signin_forgetpassword = findViewById(R.id.signin_forgetpassword);

        FirebaseFirestore fstore = FirebaseFirestore.getInstance();

        signin_forgetpassword.setOnClickListener(v ->
                startActivity(new Intent(Authentication.this, ForgetPassword.class))
        );

        // 🔹 Sign In Button Click
        signin_textview.setOnClickListener(v -> {
            String email = signin_email.getText().toString().trim();
            String password = signin_password.getText().toString().trim();

            if (email.equals("")) {
                signin_email.setError("Enter Email");
                return;
            } else if ((!email.contains("@")) || (!email.contains("."))) {
                signin_email.setError("Invalid Email");
                return;
            } else if (password.equals("")) {
                signin_password.setError("Enter Password");
                return;
            } else if (password.length() < 8) {
                signin_password.setError("Invalid Password");
                return;
            }

            signinProgress(true);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Authentication.this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();


                            startActivity(new Intent(Authentication.this, MainActivity.class));
                            finish();
                        } else {
                            ToastMaker.show(getApplicationContext(), "Wrong Password Or Email ");
                        }
                        signinProgress(false);
                    });
        });

        // 🔹 Sign Up Button Click
        signup_textview.setOnClickListener(v -> {
            String email = signup_email.getText().toString().trim();
            String password = signup_password.getText().toString().trim();
            String username = signup_username.getText().toString().trim();

            if (username.equals("")) {
                signup_username.setError("Enter Username");
                return;
            } else if (username.length() < 6) {
                signup_username.setError("Invalid Username");
                return;
            } else if (email.equals("")) {
                signup_email.setError("Enter Email");
                return;
            } else if ((!email.contains("@")) || (!email.contains("."))) {
                signup_email.setError("Invalid Email");
                return;
            } else if (password.equals("")) {
                signup_password.setError("Enter Password");
                return;
            } else if (password.length() < 8) {
                signup_password.setError("Invalid Password");
                return;
            }

            signupProgress(true);

            fstore.collection("users").whereEqualTo("userName", username)
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                signup_username.setError("Username is unavailable");
                                signupProgress(false);
                            } else {
                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(Authentication.this, signupTask -> {
                                            if (signupTask.isSuccessful()) {
                                                FirebaseUser firebaseUser = mAuth.getCurrentUser();



                                                Intent intent = new Intent(Authentication.this, Profile_Initlization.class);
                                                intent.putExtra("comeFrom", "Email");
                                                intent.putExtra("email", email);
                                                intent.putExtra("username", username);
                                                intent.putExtra("isNewUser", "true");
                                                intent.putExtra("sign", "signUp");
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                ToastMaker.show(getApplicationContext(), "Failed to register, try another email !!!");
                                            }
                                            signupProgress(false);
                                        });
                            }
                        } else {
                            signupProgress(false);
                            Log.d("error", "Firebase failed to check username availability.");
                        }
                    });
        });
    }

    private void signupProgress(boolean isInprogress) {
        signup_textview.setEnabled(!isInprogress);
        signup_textview.setVisibility(isInprogress ? View.INVISIBLE : View.VISIBLE);
        progressBar_signup.setVisibility(isInprogress ? View.VISIBLE : View.INVISIBLE);
    }

    private void signinProgress(boolean isInprogress) {
        signin_textview.setEnabled(!isInprogress);
        signin_textview.setVisibility(isInprogress ? View.INVISIBLE : View.VISIBLE);
        progressBar_signin.setVisibility(isInprogress ? View.VISIBLE : View.INVISIBLE);
    }

    // 🔹 Google Sign-In
    public void google_OnClick(View view) {
        initlizationGoogle();
        signInGoogle();
    }

    protected void initlizationGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("403203569963-q2ejs6dt3r5379thv49l3046digtuqn4.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();



                        if (!isNewUser && authenticatoin_tablayout.getSelectedTabPosition() == 1) {
                            startActivity(new Intent(Authentication.this, MainActivity.class));
                            finish();
                            return;
                        }

                        Intent intent = new Intent(Authentication.this, Profile_Initlization.class);
                        intent.putExtra("isNewUser", isNewUser + "");
                        intent.putExtra("sign", authenticatoin_tablayout.getSelectedTabPosition() == 0 ? "signUp" : "signIn");
                        intent.putExtra("comeFrom", "Google");
                        startActivity(intent);
                        finish();
                    } else {
                        ToastMaker.show(Authentication.this, "Authentication failed.");
                    }
                });
    }

    public void phone_OnClick(View view) {
        Intent intent = new Intent(Authentication.this, PhoneNumber_Authentication.class);
        intent.putExtra("sign", authenticatoin_tablayout.getSelectedTabPosition() == 1 ? "signIn" : "signUp");
        startActivity(intent);
    }



}
