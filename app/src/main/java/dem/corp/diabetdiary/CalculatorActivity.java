package dem.corp.diabetdiary;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CalculatorActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private EditText foodGramsEditText;
    private EditText carbsPer100gEditText;
    private EditText carbsPerUnitEditText;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        setupDrawerToggle(toolbar);

        foodGramsEditText = findViewById(R.id.food_grams);
        carbsPer100gEditText = findViewById(R.id.carbs_per_100g);
        carbsPerUnitEditText = findViewById(R.id.carbs_per_unit);
        resultTextView = findViewById(R.id.result);
        Button resultButton = findViewById(R.id.result_button);
        Button clearButton = findViewById(R.id.clear_button);

        resultButton.setOnClickListener(this::calculate);
        clearButton.setOnClickListener(this::clear);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("name", null);
        String patientId = sharedPreferences.getString("id", null);
        View headerView = navigationView.getHeaderView(0);
        TextView textView = headerView.findViewById(R.id.Name);
        textView.setText(username);
        TextView textView2 = headerView.findViewById(R.id.Id);
        textView2.setText(patientId);

        loadButtons();
    }

    private void setupDrawerToggle(Toolbar toolbar) {
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void loadButtons() {
        View buttonsView = getLayoutInflater().inflate(R.layout.nav_buttons, null);
        navigationView.addHeaderView(buttonsView);

        buttonsView.findViewById(R.id.newwrite).setOnClickListener(v -> startActivity(new Intent(this, PatientNewWriteActivity.class)));
        buttonsView.findViewById(R.id.mywrites).setOnClickListener(v -> startActivity(new Intent(this, PatientMyWritesActivity.class)));
        buttonsView.findViewById(R.id.adddoctor).setOnClickListener(v -> showAddDoctorDialog());
        buttonsView.findViewById(R.id.mydoctors).setOnClickListener(v -> loadPatientDoctors());
        buttonsView.findViewById(R.id.calculator).setOnClickListener(v -> startActivity(new Intent(this, CalculatorActivity.class)));
        buttonsView.findViewById(R.id.aboutapp).setOnClickListener(v -> startActivity(new Intent(this, PatientAboutAppActivity.class)));
        buttonsView.findViewById(R.id.getout).setOnClickListener(v -> finishAffinity());
    }

    private void showAddDoctorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите ID доктора");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Добавить", null);
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String doctorId = input.getText().toString();
                if (!doctorId.isEmpty()) {
                    String patient = getPatientKey();
                    findDoctorById(doctorId, patient);
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Введите ID доктора", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private String getPatientKey() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("name", null) + "," + sharedPreferences.getString("id1", null);
    }

    private void loadPatientDoctors() {
        String patientKey = getPatientKey();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Patients").child(patientKey).child("doctors");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> doctorList = new ArrayList<>();
                for (DataSnapshot doctorSnapshot : dataSnapshot.getChildren()) {
                    String doctorName = doctorSnapshot.getKey();
                    doctorList.add(doctorName);
                }
                showDoctorDialog(doctorList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CalculatorActivity.this, "Ошибка при получении данных: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDoctorDialog(List<String> doctorList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Список докторов");

        String[] doctorsArray = doctorList.toArray(new String[0]);
        builder.setItems(doctorsArray, (dialog, which) -> {
            String selectedDoctor = doctorsArray[which];
            confirmDoctorRemoval(selectedDoctor);
        });

        builder.setNegativeButton("Закрыть", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void confirmDoctorRemoval(String doctorName) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить доктора?")
                .setMessage("Вы уверены, что хотите удалить доктора " + doctorName + "?")
                .setPositiveButton("Да", (dialog, which) -> removeDoctor(doctorName))
                .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void removeDoctor(String doctorName) {
        String patientKey = getPatientKey();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Patients").child(patientKey).child("doctors").child(doctorName);
        databaseReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                loadPatientDoctors();
                Toast.makeText(CalculatorActivity.this, "Доктор " + doctorName + " удален.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CalculatorActivity.this, "Ошибка при удалении доктора.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findDoctorById(String doctorId, String patient) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Doctors");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String doctorName = null;
                boolean found = false;

                for (DataSnapshot doctorSnapshot : dataSnapshot.getChildren()) {
                    String key = doctorSnapshot.getKey();
                    if (key != null && key.contains(doctorId)) {
                        doctorName = doctorSnapshot.child("username").getValue(String.class);
                        found = true;
                        break;
                    }
                }

                if (found && doctorName != null) {
                    saveDoctorForPatient(doctorName, doctorId, patient);
                } else {
                    Toast.makeText(CalculatorActivity.this, "Доктор не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CalculatorActivity.this, "Ошибка при получении данных: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDoctorForPatient(String doctorName, String doctorId, String patient) {
        String doctorKey = doctorName + "," + doctorId;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("Patients").child(patient).child("doctors").child(doctorKey).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CalculatorActivity.this, "Доктор добавлен", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CalculatorActivity.this, "Ошибка записи в Patients: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        databaseReference.child("Doctors").child(doctorKey).child("patients").child(patient).setValue(true)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(CalculatorActivity.this, "Ошибка записи в Doctors: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void calculate(View view) {
        String foodGramsStr = foodGramsEditText.getText().toString();
        String carbsPer100gStr = carbsPer100gEditText.getText().toString();
        String carbsPerUnitStr = carbsPerUnitEditText.getText().toString();

        if (foodGramsStr.isEmpty() || carbsPer100gStr.isEmpty() || carbsPerUnitStr.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show();
            return;
        }

        double foodGrams = Double.parseDouble(foodGramsStr);
        double carbsPer100g = Double.parseDouble(carbsPer100gStr);
        double carbsPerUnit = Double.parseDouble(carbsPerUnitStr);

        double totalCarbs = (foodGrams * carbsPer100g) / 100;
        double breadUnits = totalCarbs / carbsPerUnit;

        resultTextView.setText(String.format("Результат: %.2f хлебных единиц", breadUnits));
    }

    private void clear(View view){
        foodGramsEditText.setText("");
        carbsPer100gEditText.setText("");
        carbsPerUnitEditText.setText("");
        resultTextView.setText("");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}