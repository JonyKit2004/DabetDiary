package dem.corp.diabetdiary;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import dem.corp.diabetdiary.databinding.WhoregisterBinding;

public class WhoRegisterActivity extends AppCompatActivity {
    private WhoregisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = WhoregisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goToDoctorRegisterActivityTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WhoRegisterActivity.this, DoctorRegisterActivity.class));
            }
        });

        binding.goToPotientRegisterActivityTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WhoRegisterActivity.this, PatientRegisterActivity.class));
            }
        });
    }
}