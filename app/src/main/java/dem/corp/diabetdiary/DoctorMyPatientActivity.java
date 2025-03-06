package dem.corp.diabetdiary;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
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

import java.util.HashMap;
import java.util.Map;

public class DoctorMyPatientActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private TableLayout patientsLayout;
    private DatabaseReference databaseReference;

    public String selectedPatientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_my_patient);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String name = sharedPreferences.getString("dname", null);
        String id = sharedPreferences.getString("did", null);
        String id1 = sharedPreferences.getString("did1", null);
        View headerView = navigationView.getHeaderView(0);
        TextView textView = headerView.findViewById(R.id.Name);
        textView.setText(name);
        TextView textView2 = headerView.findViewById(R.id.Id);
        textView2.setText(id);
        String doctor = name + "," + id1;

        patientsLayout = findViewById(R.id.mypatients);

        loadButtons();
        loadPatients(doctor);
    }

    private void loadButtons() {
        View buttonsView = getLayoutInflater().inflate(R.layout.nav_buttons_doctor, null);
        navigationView.addHeaderView(buttonsView);

        Button button9 = buttonsView.findViewById(R.id.mypatient);
        Button button7 = buttonsView.findViewById(R.id.aboutapp);
        Button button8 = buttonsView.findViewById(R.id.getout);

        button9.setOnClickListener(v -> {
            startActivity(new Intent(DoctorMyPatientActivity.this, DoctorMyPatientActivity.class));
            finish();
        });

        button7.setOnClickListener(v -> {
            startActivity(new Intent(DoctorMyPatientActivity.this, dem.corp.diabetdiary.DoctorMainActivity.class));
            finish();
        });

        button8.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadPatients(String doctorKey) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Doctors").child(doctorKey).child("patients");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (patientsLayout != null) {
                    patientsLayout.removeAllViews();
                }

                for (DataSnapshot patientSnapshot : dataSnapshot.getChildren()) {
                    String patientData = patientSnapshot.getKey();
                    String[] patientInfo = patientData.split(",");

                    if (patientInfo.length < 2) {
                        continue;
                    }

                    String patientName = patientInfo[0].trim();
                    String patientId = patientInfo[1].trim();
                    String doctorNotes = "";

                    if (patientSnapshot.child("doctorNotes").exists()) {
                        doctorNotes = patientSnapshot.child("doctorNotes").getValue(String.class);
                    }

                    addPatientView(patientName, patientId, doctorNotes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DoctorMyPatientActivity.this, "Ошибка при получении данных: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPatientView(String patientName, String patientId, String doctorNotes) {
        TableRow tableRow = new TableRow(this);

        TextView nameTextView = new TextView(this);
        nameTextView.setText(patientName);
        nameTextView.setTextSize(18);
        nameTextView.setTextColor(Color.BLACK);
        nameTextView.setPadding(14, 14, 14, 14);

        TextView notesTextView = new TextView(this);
        notesTextView.setText("," + doctorNotes);
        notesTextView.setTextSize(18);
        notesTextView.setTextColor(Color.BLACK);
        notesTextView.setPadding(14, 14, 14, 14);

        nameTextView.setOnClickListener(v -> {
            showPatientDialog(patientName);
            onPatientSelected(patientId);

            String wpname = patientName;
            String wpid = patientId;

            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("wpname", wpname);
            editor.putString("wpid", wpid);
            editor.apply();
        });

        tableRow.addView(nameTextView);
        tableRow.addView(notesTextView);

        patientsLayout.addView(tableRow);
    }

    private void onPatientSelected(String patientId) {
        selectedPatientId = patientId;
    }

    private void showPatientDialog(String patientId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_patient_info, null);
        builder.setView(dialogView);

        EditText editTextInfo = dialogView.findViewById(R.id.editText_info);
        Button buttonSave = dialogView.findViewById(R.id.button_save);
        Button buttonBack = dialogView.findViewById(R.id.button_back);
        Button buttonViewRecords = dialogView.findViewById(R.id.button_view_records);

        AlertDialog dialog = builder.create();

        buttonSave.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String name = sharedPreferences.getString("dname", null);
            String id1 = sharedPreferences.getString("did1", null);
            String doctorKey = name + "," + id1;
            String newText = editTextInfo.getText().toString();
            if (!newText.isEmpty() && selectedPatientId != null) {
                updatePatientInfo(doctorKey, selectedPatientId, newText);
                startActivity(new Intent(DoctorMyPatientActivity.this, DoctorMyPatientActivity.class));
                finish();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Введите информацию или выберите пациента", Toast.LENGTH_SHORT).show();
            }
        });

        buttonBack.setOnClickListener(v -> dialog.dismiss());

        buttonViewRecords.setOnClickListener(v -> {
            openPatientRecords(patientId);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updatePatientInfo(String doctorKey, String patientId, String newText) {
        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("Doctors")
                .child(doctorKey)
                .child("patients")
                .child(patientId);

        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currentDoctorNotes = dataSnapshot.child("doctorNotes").getValue(String.class);
                    String updatedNotes = currentDoctorNotes != null ? currentDoctorNotes + "; " + newText : newText;

                    patientRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Map<String, Object> newPatientData = new HashMap<>();
                            newPatientData.put("doctorNotes", updatedNotes);

                            patientRef.setValue(newPatientData).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(DoctorMyPatientActivity.this, "Информация обновлена", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(DoctorMyPatientActivity.this, "Ошибка при добавлении новой информации", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(DoctorMyPatientActivity.this, "Ошибка при удалении информации", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(DoctorMyPatientActivity.this, "Пациент не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DoctorMyPatientActivity.this, "Ошибка: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openPatientRecords(String patientId) {
        Intent intent = new Intent(this, PatientRecordsActivity.class);
        intent.putExtra("PATIENT_ID", patientId);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}