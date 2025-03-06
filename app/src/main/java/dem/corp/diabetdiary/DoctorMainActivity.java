package dem.corp.diabetdiary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class DoctorMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);

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
        View headerView = navigationView.getHeaderView(0);
        TextView textView = headerView.findViewById(R.id.Name);
        textView.setText(name);
        TextView textView2 = headerView.findViewById(R.id.Id);
        textView2.setText(id);

        loadButtons();
    }

    private void loadButtons() {
        View buttonsView = getLayoutInflater().inflate(R.layout.nav_buttons_doctor, null);
        navigationView.addHeaderView(buttonsView);

        Button button9 = buttonsView.findViewById(R.id.mypatient);
        Button button7 = buttonsView.findViewById(R.id.aboutapp);
        Button button8 = buttonsView.findViewById(R.id.getout);

        button9.setOnClickListener(v -> {
            startActivity(new Intent(DoctorMainActivity.this, DoctorMyPatientActivity.class));
            finish();
        });

        button7.setOnClickListener(v -> {
            startActivity(new Intent(DoctorMainActivity.this, DoctorMainActivity.class));
            finish();
        });

        button8.setOnClickListener(v -> {
            finish();
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