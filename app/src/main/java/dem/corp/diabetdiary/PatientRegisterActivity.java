package dem.corp.diabetdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.regex.Pattern;
import dem.corp.diabetdiary.databinding.ActivityPatientregisterBinding;

public class PatientRegisterActivity extends AppCompatActivity {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();
    private ActivityPatientregisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientregisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userid = generateRandomString();
                String email = binding.emailEt.getText().toString();
                String password = binding.passwordEt.getText().toString();
                String username = binding.usernameEt.getText().toString();

                binding.emailErrorTv.setVisibility(View.GONE);
                binding.usernameErrorTv.setVisibility(View.GONE);
                binding.passwordErrorTv.setVisibility(View.GONE);

                boolean valid = true;

                if (email.isEmpty()) {
                    binding.emailErrorTv.setTextColor(Color.RED);
                    binding.emailErrorTv.setText("Поля не могут быть пустыми");
                    binding.emailErrorTv.setVisibility(View.VISIBLE);
                    binding.emailErrorTv.setBackgroundColor(Color.parseColor("#E0FFE0"));
                    valid = false;
                } else if (!isValidEmail(email)) {
                    binding.emailEt.setText("");
                    binding.emailErrorTv.setTextColor(Color.RED);
                    binding.emailErrorTv.setText("Неверный формат email");
                    binding.emailErrorTv.setVisibility(View.VISIBLE);
                    binding.emailErrorTv.setBackgroundColor(Color.parseColor("#E0FFE0"));
                    valid = false;
                }

                if (username.isEmpty()) {
                    binding.usernameErrorTv.setTextColor(Color.RED);
                    binding.usernameErrorTv.setText("Поля не могут быть пустыми");
                    binding.usernameErrorTv.setVisibility(View.VISIBLE);
                    binding.usernameErrorTv.setBackgroundColor(Color.parseColor("#E0FFE0"));
                    valid = false;
                } else if (!isValidUsername(username)) {
                    binding.usernameEt.setText("");
                    binding.usernameErrorTv.setTextColor(Color.RED);
                    binding.usernameErrorTv.setTextSize(10);
                    binding.usernameErrorTv.setText("Напишите в формате 'Иванов Иван Иванович'");
                    binding.usernameErrorTv.setVisibility(View.VISIBLE);
                    binding.usernameErrorTv.setBackgroundColor(Color.parseColor("#E0FFE0"));
                    valid = false;
                }

                if (password.isEmpty()) {
                    binding.passwordErrorTv.setTextColor(Color.RED);
                    binding.passwordErrorTv.setText("Поля не могут быть пустыми");
                    binding.passwordErrorTv.setVisibility(View.VISIBLE);
                    binding.passwordErrorTv.setBackgroundColor(Color.parseColor("#E0FFE0"));
                    valid = false;
                } else if (!isValidPassword(password)) {
                    binding.passwordEt.setText("");
                    binding.passwordErrorTv.setTextColor(Color.RED);
                    binding.passwordErrorTv.setTextSize(10);
                    binding.passwordErrorTv.setText("Пароль должен содержать минимум 8 символов и хотя бы 1 цифру и 1 букву");
                    binding.passwordErrorTv.setVisibility(View.VISIBLE);
                    binding.passwordErrorTv.setBackgroundColor(Color.parseColor("#E0FFE0"));
                    valid = false;
                }

                if (valid) {
                    showUserAgreementDialog(email, password, username, userid);
                }
            }

            private String generateRandomString() {
                StringBuilder sb = new StringBuilder(6);
                for (int i = 0; i < 6; i++) {
                    int index = random.nextInt(CHARACTERS.length());
                    sb.append(CHARACTERS.charAt(index));
                }
                return sb.toString();
            }

            private boolean isValidEmail(String email) {
                email = email.trim();
                String emailRegex = "^[\\w-\\.]+@[\\w-]+\\.[a-z]{2,3}$";
                return Pattern.compile(emailRegex).matcher(email).matches();
            }

            private boolean isValidUsername(String username) {
                username = username.trim();

                String usernameRegex = "^(?:[А-ЯЁ][а-яё]+|[A-Z][a-z]+)\\s(?:[А-ЯЁ][а-яё]+|[A-Z][a-z]+)\\s(?:[А-ЯЁ][а-яё]+|[A-Z][a-z]+)$";

                return Pattern.compile(usernameRegex).matcher(username).matches();
            }

            private boolean isValidPassword(String password) {
                String passwordRegex = "^(?=.*[a-zA-Zа-яА-ЯЁё])(?=.*\\d)[a-zA-Zа-яА-ЯЁё\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,}$";
                return Pattern.compile(passwordRegex).matcher(password).matches();
            }

            private void showUserAgreementDialog(String email, String password, String username, String userid) {
                String userAgreement = "Лицензионное соглашение пользователя\n" +
                        "Последнее обновление: 23.12.2024\n\n" +
                        "Настоящее Лицензионное соглашение пользователя (далее — «Соглашение») является юридически обязательным соглашением между вами (далее — «Пользователь») Китурко Артёмом Александровичем (далее — «Разработчик») относительно использования мобильного приложения DiabetDiary (далее — «Приложение»).\n\n" +
                        "1. Принятие условий\n" +
                        "Установив и/или используя Приложение, Вы соглашаетесь с условиями данного Соглашения. Если Вы не согласны с условиями, не устанавливайте и не используйте Приложение.\n\n" +
                        "2. Лицензия на использование\n" +
                        "Разработчик предоставляет Пользователю ограниченную, неисключительную, непередаваемую лицензию на использование Приложения в личных, некоммерческих целях на устройствах, которые принадлежат Пользователю.\n\n" +
                        "3. Ограничения\n" +
                        "Пользователь не имеет права:\n" +
                        "Изменять, адаптировать или создавать производные работы на основе Приложения.\n" +
                        "Распространять, передавать или сдавать в аренду Приложение третьим лицам.\n" +
                        "Использовать Приложение для незаконных целей.\n\n" +
                        "4. Интеллектуальная собственность\n" +
                        "Все права на Приложение, включая авторские права и товарные знаки, принадлежат Разработчику. Данное Соглашение не передает Пользователю никаких прав на интеллектуальную собственность, кроме тех, что прямо указаны в этом Соглашении.\n\n" +
                        "5. Отказ от ответственности\n" +
                        "Приложение предоставляется «как есть» без каких-либо гарантий. Разработчик не несет ответственности за любые убытки, возникшие в результате использования или невозможности использования Приложения.\n\n" +
                        "6. Изменения в Соглашении\n" +
                        "Разработчик оставляет за собой право в любое время изменять условия данного Соглашения. Изменения вступают в силу с момента их публикации в Приложении. Пользователь должен периодически проверять условия Соглашения на предмет изменений.\n\n" +
                        "7. Применимое право\n" +
                        "Настоящее Соглашение регулируется и толкуется в соответствии с законодательством Республики Беларусь.\n\n" +
                        "8. Контактная информация\n" +
                        "Если у вас есть вопросы относительно данного Соглашения, пожалуйста, свяжитесь с нами по адресу: akiturko7@gmail.com.";

                new AlertDialog.Builder(PatientRegisterActivity.this)
                        .setTitle("Пользовательское соглашение")
                        .setMessage(userAgreement)
                        .setPositiveButton("Принять", (dialog, which) -> {
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                String userId = username + "," + userid;
                                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Patients").child(userId);
                                                HashMap<String, String> userInfo = new HashMap<>();
                                                userInfo.put("id", userid);
                                                userInfo.put("whouse", "pacient");
                                                userInfo.put("email", email);
                                                userInfo.put("username", username);
                                                userInfo.put("password", password);

                                                databaseReference.setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            startActivity(new Intent(PatientRegisterActivity.this, LoginActivity.class));
                                                        } else {
                                                            Toast.makeText(getApplicationContext(), "Не удалось сохранить информацию о пациенте", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Неудача регистрации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        })
                        .setNegativeButton("Отменить", (dialog, which) -> {
                            finish();
                        })
                        .setCancelable(false)
                        .show();
            }
        });
    }
}