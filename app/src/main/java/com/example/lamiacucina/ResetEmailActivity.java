package com.example.lamiacucina;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetEmailActivity extends AppCompatActivity {
    EditText emailTdtTxt;
    Button sendEmailButton;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_email);

        emailTdtTxt=findViewById(R.id.user_email);
        sendEmailButton=findViewById(R.id.btnLogin);

        auth=FirebaseAuth.getInstance();

        sendEmailButton.setOnClickListener(v -> {
            String email = emailTdtTxt.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplication(), "Enter your mail address", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetEmailActivity.this, "We sent you an e-mail with Reset Email Password Link", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(ResetEmailActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,Login.class));
        this.finish();
        super.onBackPressed();
    }
}