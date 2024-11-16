package lucas.carozzi.verduritassa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DividerItemDecoration;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class ResultadosActivity extends AppCompatActivity implements CosechaAdapter.OnCosechaClickListener {
    private static final String TAG = "ResultadosActivity";
    private RecyclerView cosechaList;
    private CosechaAdapter adapter;
    private TextView welcomeText;
    private MaterialButton deleteAllButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayList<Cosecha> cosechas;
    private ListenerRegistration cosechaListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados);

        initializeViews();
        setupFirebase();
        setupRecyclerView();
        loadCosechas();

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultadosActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        deleteAllButton.setOnClickListener(v -> confirmDeleteAll());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cosechaListener != null) {
            cosechaListener.remove();
        }
    }

    private void initializeViews() {
        cosechaList = findViewById(R.id.harvestList);
        welcomeText = findViewById(R.id.welcomeText);
        deleteAllButton = findViewById(R.id.deleteAllButton);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("usuarios").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String userName = documentSnapshot.getString("alias");
                        if (userName == null || userName.isEmpty()) {
                            userName = currentUser.getEmail();
                        }
                        welcomeText.setText(getString(R.string.welcome_message, userName));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ResultadosActivity.this, "Error al obtener el alias", Toast.LENGTH_SHORT).show();
                        welcomeText.setText(getString(R.string.welcome_message, currentUser.getEmail()));
                    });
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }




    private void setupRecyclerView() {
        cosechas = new ArrayList<>();
        adapter = new CosechaAdapter(cosechas, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        cosechaList.setLayoutManager(layoutManager);
        cosechaList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        cosechaList.setAdapter(adapter);
        cosechaList.setHasFixedSize(true);
    }

    private void loadCosechas() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "No user logged in");
            return;
        }

        if (cosechaListener != null) {
            cosechaListener.remove();
        }

        cosechaListener = db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading cosechas", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        cosechas.clear();
                        for (QueryDocumentSnapshot document : value) {
                            String id = document.getId();
                            String cropName = document.getString("cropName");
                            String harvestDate = document.getString("harvestDate");

                            if (cropName != null && harvestDate != null) {
                                cosechas.add(new Cosecha(id, cropName, harvestDate));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        cosechas.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.resultados_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            if (cosechaListener != null) {
                cosechaListener.remove();
            }
            mAuth.signOut();
            Intent intent = new Intent(ResultadosActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMenuClick(View view, Cosecha cosecha) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.cosechas_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                editCosecha(cosecha);
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                confirmDelete(cosecha);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void editCosecha(Cosecha cosecha) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("crop_name", cosecha.getCropName());
        intent.putExtra("harvest_date", cosecha.getHarvestDate());
        intent.putExtra("harvest_id", cosecha.getId());
        startActivity(intent);
    }

    private void confirmDelete(Cosecha cosecha) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteCosecha(cosecha))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_all)
                .setMessage(R.string.confirm_delete_all_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteAllCosechas())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteCosecha(Cosecha cosecha) {
        db.collection("harvests")
                .document(cosecha.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    adapter.removeCosecha(cosecha);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting cosecha", e);
                });
    }

    private void deleteAllCosechas() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    cosechas.clear();
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting all cosechas", e);
                });
    }
}
