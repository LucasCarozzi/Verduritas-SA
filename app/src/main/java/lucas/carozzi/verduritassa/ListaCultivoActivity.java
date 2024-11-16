package lucas.carozzi.verduritassa;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Calendar;
import android.view.View;


public class ListaCultivoActivity extends AppCompatActivity implements CosechaAdapter.OnCosechaClickListener {

    private TextView welcomeText;
    private TextView harvestTitle;
    private MaterialButton buttonNuevaCosecha;
    private MaterialButton buttonResultados;
    private MaterialButton buttonVolver;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView harvestList;
    private CosechaAdapter adapter;
    private ArrayList<Cosecha> cosechas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_cultivo);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        welcomeText = findViewById(R.id.welcomeText);
        harvestTitle = findViewById(R.id.harvestTitle);
        buttonNuevaCosecha = findViewById(R.id.buttonNuevaCosecha);
        buttonResultados = findViewById(R.id.buttonResultados);
        buttonVolver = findViewById(R.id.buttonVolver);
        harvestList = findViewById(R.id.harvestList);

        // Configurar título de cosechas
        harvestTitle.setText(R.string.todays_harvests);

        // Configurar RecyclerView
        cosechas = new ArrayList<>();
        harvestList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CosechaAdapter(cosechas, this);
        harvestList.setAdapter(adapter);

        // Configura el nombre de usuario
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userName = currentUser.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = currentUser.getEmail();
            }
            welcomeText.setText(getString(R.string.welcome_message, userName));
        }

        // Cargar las cosechas
        loadCosechas();

        buttonNuevaCosecha.setOnClickListener(v -> agregarNuevaCosecha());
        buttonResultados.setOnClickListener(v -> mostrarResultados());
        buttonVolver.setOnClickListener(v -> volverAHome());
    }

    private void loadCosechas() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDay = calendar.getTimeInMillis();

        db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .whereLessThanOrEqualTo("timestamp", endOfDay)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cosechas.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String cropName = doc.getString("cropName");
                        String harvestDate = doc.getString("harvestDate");
                        if (cropName != null && harvestDate != null) {
                            cosechas.add(new Cosecha(id, cropName, harvestDate));
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(ListaCultivoActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void agregarNuevaCosecha() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void mostrarResultados() {
        Intent intent = new Intent(this, ResultadosActivity.class);
        startActivity(intent);
    }

    private void volverAHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        volverAHome();
    }

    @Override
    public void onMenuClick(View view, Cosecha cosecha) {
    }
}
