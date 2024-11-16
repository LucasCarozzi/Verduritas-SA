package lucas.carozzi.verduritassa;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText emailInput, passwordInput, aliasInput, countryInput, genderInput;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Referencia a los campos del formulario
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        aliasInput = findViewById(R.id.aliasInput);
        countryInput = findViewById(R.id.countryInput);
        genderInput = findViewById(R.id.genderInput);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String alias = aliasInput.getText().toString().trim();
        String country = countryInput.getText().toString().trim();
        String gender = genderInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(alias)
                || TextUtils.isEmpty(country) || TextUtils.isEmpty(gender)) {
            Toast.makeText(RegisterActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // la contraseña cumpla que tenga al menos 6 caracteres
        if (password.length() < 6) {
            Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear usuario con email y contraseña
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(alias)
                                .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                            if (profileTask.isSuccessful()) {
                                // Guardar los datos adicionales en Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("alias", alias);
                                userData.put("email", email);
                                userData.put("country", country);
                                userData.put("gender", gender);

                                // Guardar en Firestore
                                db.collection("usuarios").document(user.getUid())
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RegisterActivity.this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            } else {
                                Toast.makeText(RegisterActivity.this, "Error al actualizar perfil: " + profileTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error en el registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
