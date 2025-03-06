package dem.corp.diabetdiary;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientRecordsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    private TableLayout tableLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_records);

        tableLayout = findViewById(R.id.patientwrites);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        loadPatientWrites();
    }

    private void loadPatientWrites() {
        String patientId = getPatientId();
        String patientName = getPatientName();
        if (patientId == null || patientName == null) {
            return;
        }

        fetchPatientWrites();
    }

    private String getPatientId() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String id = sharedPreferences.getString("wpid", null);

        return id;
    }

    private String getPatientName() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String name = sharedPreferences.getString("wpname", null);

        return name;
    }

    private void fetchPatientWrites() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String id = sharedPreferences.getString("wpid", null);
        String name = sharedPreferences.getString("wpname", null);
        databaseReference.child("Patients").child(name+","+id).child("writes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                handleDataSnapshot(dataSnapshot, name,id);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    private void handleDataSnapshot(DataSnapshot dataSnapshot,String patientName, String patientId) {
        Log.d("PatientRecordsActivity", "DataSnapshot exists: " + dataSnapshot.exists());
        Log.d("PatientRecordsActivity", "Children count: " + dataSnapshot.getChildrenCount());

        if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) {
            String message = "Нет записей для пациента: " + patientName+ "," + patientId;

            new AlertDialog.Builder(PatientRecordsActivity.this)
                    .setTitle("Информация")
                    .setMessage(message)
                    .setPositiveButton("ОК", (dialog, which) -> dialog.dismiss())
                    .setCancelable(false)
                    .show();

            return;
        }

        displayPatientRecords(dataSnapshot);
    }

    private void displayPatientRecords(DataSnapshot dataSnapshot) {
        TableRow headerRow = new TableRow(PatientRecordsActivity.this);
        String[] headers = {"Дата и время", "Уровень сахара", "Тип еды", "Единицы хлеба", "Тип инъекции", "Единицы", "Комментарий"};
        for (String header : headers) {
            TextView headerTextView = new TextView(PatientRecordsActivity.this);
            headerTextView.setText(header);
            headerTextView.setPadding(8, 8, 8, 8);
            headerTextView.setTextColor(Color.BLACK);
            headerRow.addView(headerTextView);
        }
        tableLayout.addView(headerRow);

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            WriteRecord record = snapshot.getValue(WriteRecord.class);
            if (record != null) {
                Log.d("PatientRecordsActivity", "Record found: " + record.dateTime);
                addRecordToTable(record);
            } else {
                Log.d("PatientRecordsActivity", "Record is null for snapshot key: " + snapshot.getKey());
            }
        }
    }

    private void addRecordToTable(WriteRecord record) {
        TableRow recordRow = new TableRow(PatientRecordsActivity.this);
        recordRow.addView(createTextView(record.dateTime));
        recordRow.addView(createTextView(record.bloodSugarLevel));
        recordRow.addView(createTextView(record.mealType));
        recordRow.addView(createTextView(record.breadUnits));
        recordRow.addView(createTextView(record.injectionType));
        recordRow.addView(createTextView(record.unitsTaken));
        recordRow.addView(createTextView(record.comment));

        tableLayout.addView(recordRow);
    }

    private void handleDatabaseError(DatabaseError databaseError) {
        Toast.makeText(PatientRecordsActivity.this, "Ошибка загрузки данных: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        return textView;
    }
}