package dem.corp.diabetdiary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PatientAboutAppActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_about_app);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String name = sharedPreferences.getString("name", null);
        String id = sharedPreferences.getString("id", null);
        View headerView = navigationView.getHeaderView(0);
        TextView textView = headerView.findViewById(R.id.Name);
        textView.setText(name);
        TextView textView2 = headerView.findViewById(R.id.Id);
        textView2.setText(id);

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
            startActivity(new Intent(PatientAboutAppActivity.this, PatientNewWriteActivity.class));
            finish();
        });

        button2.setOnClickListener(v -> {
            startActivity(new Intent(PatientAboutAppActivity.this, PatientMyWritesActivity.class));
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
            startActivity(new Intent(PatientAboutAppActivity.this, CalculatorActivity.class));
            finish();
        });


        button7.setOnClickListener(v -> {
            startActivity(new Intent(PatientAboutAppActivity.this, PatientAboutAppActivity.class));
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
                Toast.makeText(PatientAboutAppActivity.this, "Ошибка при получении данных: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDoctorDialog(List<String> doctorList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PatientAboutAppActivity.this);
        builder.setTitle("Список докторов");

        String[] doctorsArray = doctorList.toArray(new String[0]);

        builder.setItems(doctorsArray, (dialog, which) -> {
            String selectedDoctor = doctorsArray[which];

            new AlertDialog.Builder(PatientAboutAppActivity.this)
                    .setTitle("Удалить доктора?")
                    .setMessage("Вы уверены, что хотите удалить доктора " + selectedDoctor + "?")
                    .setPositiveButton("Да", (dialog1, which1) -> {

                        removeDoctor(selectedDoctor);
                        Toast.makeText(PatientAboutAppActivity.this, "Доктор " + selectedDoctor + " удален.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PatientAboutAppActivity.this, "Ошибка при удалении доктора.", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(PatientAboutAppActivity.this, "Доктор добавлен", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PatientAboutAppActivity.this, "Ошибка записи в Patients: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(PatientAboutAppActivity.this, "Доктор не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PatientAboutAppActivity.this, "Ошибка при получении данных: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}