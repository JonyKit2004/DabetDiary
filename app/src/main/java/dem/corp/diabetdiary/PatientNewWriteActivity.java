package dem.corp.diabetdiary;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PatientNewWriteActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private Spinner mealTypeSpinner;
    private EditText foodGramsEditText;
    private EditText carbsPer100gEditText;
    private EditText carbsPerUnitEditText;
    private EditText bloodSugarLevelEditText;
    private Spinner injectionTypeSpinner;
    private EditText unitsTakenEditText;
    private EditText commentEditText;
    private Button saveButton;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_new_write);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("name", null);
        String patientId = sharedPreferences.getString("id", null);
        View headerView = navigationView.getHeaderView(0);
        TextView textView = headerView.findViewById(R.id.Name);
        textView.setText(username);
        TextView textView2 = headerView.findViewById(R.id.Id);
        textView2.setText(patientId);

        mealTypeSpinner = findViewById(R.id.meal_type_spinner);
        injectionTypeSpinner = findViewById(R.id.injection_type_spinner);
        foodGramsEditText = findViewById(R.id.food_grams);
        carbsPer100gEditText = findViewById(R.id.carbs_per_100g);
        carbsPerUnitEditText = findViewById(R.id.carbs_per_unit);
        bloodSugarLevelEditText = findViewById(R.id.blood_sugar_level);
        unitsTakenEditText = findViewById(R.id.units_taken);
        commentEditText = findViewById(R.id.comment);
        saveButton = findViewById(R.id.save_button);

        String[] mealTypes = {"Завтрак", "Полдник", "Обед", "Ужин", "Перекус"};
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mealTypes) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);
                return view;
            }
        };
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTypeSpinner.setAdapter(mealAdapter);

        String[] injectionTypes = {"Инсулин быстрого действия", "Инсулин длительного действия", "Таблетки"};
        ArrayAdapter<String> injectionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, injectionTypes) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK);
                return view;
            }
        };
        injectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        injectionTypeSpinner.setAdapter(injectionAdapter);

        saveButton.setOnClickListener(v -> saveRecord());

        loadButtons();
    }

    private void loadButtons() {
        View buttonsView = getLayoutInflater().inflate(R.layout.nav_buttons, null);
        navigationView.addHeaderView(buttonsView);

        Button button1 = buttonsView.findViewById(R.id.newwrite);
        Button button2 = buttonsView.findViewById(R.id.mywrites);
        Button button3 = buttonsView.findViewById(R.id.adddoctor);
        Button button4 = buttonsView.findViewById(R.id.mydoctors);
        Button button5 = buttonsView.findViewById(R.id.calculator);
        Button button7 = buttonsView.findViewById(R.id.aboutapp);
        Button button8 = buttonsView.findViewById(R.id.getout);

        button1.setOnClickListener(v -> {
            startActivity(new Intent(PatientNewWriteActivity.this, PatientNewWriteActivity.class));
            finish();
        });

        button2.setOnClickListener(v -> {
            startActivity(new Intent(PatientNewWriteActivity.this, PatientMyWritesActivity.class));
            finish();
        });

        button3.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Введите ID доктора");

            final EditText input = new EditText(this);
            builder.setView(input);

            builder.setPositiveButton("Добавить", null);

            AlertDialog dialog = builder.create();

            dialog.setOnShowListener(dialogInterface -> {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    String patient = sharedPreferences.getString("name", null) + "," + sharedPreferences.getString("id1", null);
                    String doctorId = input.getText().toString();

                    if (!doctorId.isEmpty()) {
                        findDoctorById(doctorId, patient);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Введите ID доктора", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            dialog.show();
        });

        button4.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String patientKey = sharedPreferences.getString("name", null) + "," + sharedPreferences.getString("id1", null);

            loadDoctors(patientKey);
        });

        button5.setOnClickListener(v -> {
            startActivity(new Intent(PatientNewWriteActivity.this, CalculatorActivity.class));
            finish();
        });


        button7.setOnClickListener(v -> {
            startActivity(new Intent(PatientNewWriteActivity.this, PatientAboutAppActivity.class));
            finish();
        });

        button8.setOnClickListener(v -> {
            finishAffinity();
        });
    }

    private void loadDoctors(String patientKey) {
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
                Toast.makeText(PatientNewWriteActivity.this, "Ошибка при получении данных: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDoctorDialog(List<String> doctorList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PatientNewWriteActivity.this);
        builder.setTitle("Список докторов");

        String[] doctorsArray = doctorList.toArray(new String[0]);

        builder.setItems(doctorsArray, (dialog, which) -> {
            String selectedDoctor = doctorsArray[which];

            new AlertDialog.Builder(PatientNewWriteActivity.this)
                    .setTitle("Удалить доктора?")
                    .setMessage("Вы уверены, что хотите удалить доктора " + selectedDoctor + "?")
                    .setPositiveButton("Да", (dialog1, which1) -> {
                        // Удаляем доктора из списка
                        removeDoctor(selectedDoctor);
                        Toast.makeText(PatientNewWriteActivity.this, "Доктор " + selectedDoctor + " удален.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Нет", (dialog12, which1) -> dialog12.dismiss())
                    .show();
        });

        builder.setNegativeButton("Закрыть", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeDoctor(String doctorName) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String patientKey = sharedPreferences.getString("name", null) + "," + sharedPreferences.getString("id1", null);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Patients").child(patientKey).child("doctors").child(doctorName);
        databaseReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                loadDoctors(patientKey);
            } else {
                Toast.makeText(PatientNewWriteActivity.this, "Ошибка при удалении доктора.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findDoctorById(String doctorId, String patient) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Doctors");
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference();

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
                    String doctorKey = doctorName + "," + doctorId;

                    databaseReference1.child("Patients").child(patient).child("doctors").child(doctorName+","+doctorId).setValue(true)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PatientNewWriteActivity.this, "Доктор добавлен", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PatientNewWriteActivity.this, "Ошибка записи в Patients: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(PatientNewWriteActivity.this, "Доктор не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PatientNewWriteActivity.this, "Ошибка при получении данных: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveRecord() {
        String mealType = mealTypeSpinner.getSelectedItem().toString();
        String foodGrams = foodGramsEditText.getText().toString();
        String carbsPer100g = carbsPer100gEditText.getText().toString();
        String carbsPerUnit = carbsPerUnitEditText.getText().toString();
        String bloodSugarLevel = bloodSugarLevelEditText.getText().toString();
        String injectionType = injectionTypeSpinner.getSelectedItem().toString();
        String unitsTaken = unitsTakenEditText.getText().toString();
        String comment = commentEditText.getText().toString();

        if (foodGrams.isEmpty() || carbsPer100g.isEmpty() || carbsPerUnit.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String patient = sharedPreferences.getString("name", null) + "," + sharedPreferences.getString("id1", null);

        String recordName = new SimpleDateFormat("dd.MM.yyyy,HH:mm", Locale.getDefault()).format(new java.util.Date());

        WriteRecord record = new WriteRecord(
                recordName,
                mealType,
                calculateBreadUnits(foodGrams, carbsPer100g, carbsPerUnit),
                bloodSugarLevel,
                injectionType,
                unitsTaken,
                comment
        );

        databaseReference.child("Patients").child(patient).child("writes").child(new SimpleDateFormat("dd-MM-yyyy,HH:mm", Locale.getDefault()).format(new java.util.Date())).setValue(record)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PatientNewWriteActivity.this, "Запись сохранена", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PatientNewWriteActivity.this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String calculateBreadUnits(String foodGrams, String carbsPer100g, String carbsPerUnit) {
        double foodGramsValue = Double.parseDouble(foodGrams);
        double carbsPer100gValue = Double.parseDouble(carbsPer100g);
        double carbsPerUnitValue = Double.parseDouble(carbsPerUnit);

        double result = (foodGramsValue / 100) * (carbsPer100gValue / carbsPerUnitValue);

        return String.valueOf(Math.round(result));
    }

    private void clearFields() {
        foodGramsEditText.setText("");
        carbsPer100gEditText.setText("");
        carbsPerUnitEditText.setText("");
        bloodSugarLevelEditText.setText("");
        unitsTakenEditText.setText("");
        commentEditText.setText("");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class WriteRecord {
        public String dateTime;
        public String mealType;
        public String breadUnits;
        public String bloodSugarLevel;
        public String injectionType;
        public String unitsTaken;
        public String comment;

        public WriteRecord() {
        }

        public WriteRecord(String dateTime, String mealType, String breadUnits, String bloodSugarLevel, String injectionType, String unitsTaken, String comment) {
            this.dateTime = dateTime;
            this.mealType = mealType;
            this.breadUnits = breadUnits;
            this.bloodSugarLevel = bloodSugarLevel;
            this.injectionType = injectionType;
            this.unitsTaken = unitsTaken;
            this.comment = comment;
        }
    }
}