package dem.corp.diabetdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

import android.app.AlertDialog;
import android.widget.EditText;

import dem.corp.diabetdiary.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.emailEt.getText().toString();
                String password = binding.passwordEt.getText().toString();

                binding.emailorpasswordErrorTv.setVisibility(View.GONE);

                boolean valid = true;

                if (email.isEmpty() || password.isEmpty()) {
                    binding.emailorpasswordErrorTv.setTextColor(Color.RED);
                    binding.emailorpasswordErrorTv.setTextSize(17);
                    binding.emailorpasswordErrorTv.setText("Поля не могут быть пустыми");
                    binding.emailorpasswordErrorTv.setGravity(Gravity.CENTER);
                    binding.emailorpasswordErrorTv.setVisibility(View.VISIBLE);
                    binding.emailorpasswordErrorTv.setBackgroundColor(Color.parseColor("#E0FFE0"));
                    valid = false;
                }

                if (valid){
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        checkUserType(email);
                                    } else {
                                        binding.emailEt.setText("");
                                        binding.passwordEt.setText("");
                                        binding.emailorpasswordErrorTv.setBackgroundColor(Color.parseColor("#E0FFE0"));
                                        binding.emailorpasswordErrorTv.setTextSize(17);
                                        binding.emailorpasswordErrorTv.setTextColor(Color.parseColor("#FFA500"));
                                        binding.emailorpasswordErrorTv.setGravity(Gravity.CENTER);
                                        binding.emailorpasswordErrorTv.setText("Неверный email или пароль");
                                        binding.emailorpasswordErrorTv.setVisibility(View.VISIBLE);
                                        binding.forgotPasswordTv.setBackgroundColor(Color.parseColor("#E0FFE0"));
                                        binding.forgotPasswordTv.setText("Забыли пароль?");
                                        binding.forgotPasswordTv.setTextSize(15);
                                        binding.forgotPasswordTv.setGravity(Gravity.CENTER);
                                        binding.forgotPasswordTv.setTextColor(Color.parseColor("#0000ff"));

                                        binding.forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showResetPasswordDialog();
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });

        binding.goToRegisterActivityTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, WhoRegisterActivity.class));
            }
        });
    }
    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_reset_password_dialog, null);
        builder.setView(customLayout);

        final EditText emailInput = customLayout.findViewById(R.id.email_input);
        Button sendButton = customLayout.findViewById(R.id.button_send);
        Button cancelButton = customLayout.findViewById(R.id.button_cancel);

        AlertDialog dialog = builder.create();

        sendButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            final boolean[] userFound = {false};

            database.child("Doctors").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (DataSnapshot doctorSnapshot : task.getResult().getChildren()) {
                        String dbEmail = doctorSnapshot.child("email").getValue(String.class);
                        if (dbEmail != null && email.equals(dbEmail.trim())) {
                            userFound[0] = true;
                            sendPasswordResetEmail(email, dialog);
                            return;
                        }
                    }
                }

                database.child("Patients").get().addOnCompleteListener(patientTask -> {
                    if (patientTask.isSuccessful() && patientTask.getResult() != null) {
                        for (DataSnapshot patientSnapshot : patientTask.getResult().getChildren()) {
                            String dbEmail = patientSnapshot.child("email").getValue(String.class);
                            if (dbEmail != null && email.equals(dbEmail.trim())) {
                                userFound[0] = true;
                                sendPasswordResetEmail(email, dialog);
                                return;
                            }
                        }
                    }

                    if (!userFound[0]) {
                        Toast.makeText(LoginActivity.this, "Пользователь с таким email не существует", Toast.LENGTH_SHORT).show();
                        emailInput.setText("");
                    }
                });
            });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void sendPasswordResetEmail(String email, AlertDialog dialog) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(resetTask -> {
                    if (resetTask.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Ссылка для сброса пароля отправлена на ваш email", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка: " + resetTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserType(String email) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        database.child("Doctors").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                boolean userFound = false;
                for (DataSnapshot doctorSnapshot : task.getResult().getChildren()) {
                    String dbEmail = doctorSnapshot.child("email").getValue(String.class);
                    String dbName = doctorSnapshot.child("username").getValue(String.class);
                    String dbId = doctorSnapshot.child("id").getValue(String.class);
                    if (dbEmail != null && email.equals(dbEmail.trim())) {
                        userFound = true;
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("dname", dbName);
                        editor.putString("did", "Id: " + dbId);
                        editor.putString("did1", dbId);
                        editor.apply();
                        startActivity(new Intent(LoginActivity.this, DoctorMainActivity.class));
                        finish();
                        return;
                    }
                }

                if (!userFound) {
                    database.child("Patients").get().addOnCompleteListener(patientTask -> {
                        if (patientTask.isSuccessful() && patientTask.getResult() != null) {
                            for (DataSnapshot patientSnapshot : patientTask.getResult().getChildren()) {
                                String dbEmail = patientSnapshot.child("email").getValue(String.class);
                                String dbName = patientSnapshot.child("username").getValue(String.class);
                                String dbId = patientSnapshot.child("id").getValue(String.class);
                                if (dbEmail != null && email.equals(dbEmail.trim())) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("name", dbName);
                                    editor.putString("id", "Id: " + dbId);
                                    editor.putString("id1", dbId);
                                    editor.apply();
                                    startActivity(new Intent(LoginActivity.this, PatientAboutAppActivity.class));
                                    finish();
                                    return;
                                }
                            }
                        }

                        binding.emailorpasswordErrorTv.setText("Пользователь не найден");
                        binding.emailorpasswordErrorTv.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }
}