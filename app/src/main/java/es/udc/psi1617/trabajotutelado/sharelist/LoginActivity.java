package es.udc.psi1617.trabajotutelado.sharelist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
//import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.view.Window;
import android.widget.Toast;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText et_email;
    private EditText et_contraseña;


    private static final String TAG = "EmailPassword";
    private DatabaseReference database;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        // Vistas
        et_email = (EditText) findViewById(R.id.et_email);
        et_contraseña = (EditText) findViewById(R.id.et_contraseña);

        // Botones
        findViewById(R.id.but_iniciar_sesion).setOnClickListener(this);
        findViewById(R.id.but_crear_cuenta).setOnClickListener(this);

        database = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            autenticacionExitosa(mAuth.getCurrentUser());
        }
    }

    private void crearCuenta(String email, String contraseña) {
        Log.d(TAG, "crearCuenta: " + email);
        if (email.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(this, getString(R.string.campos_no_validos_str), Toast.LENGTH_SHORT).show();
        } else if (contraseña.length() < 6) {
            Toast.makeText(this, getString(R.string.minimo6_caracteres_str), Toast.LENGTH_SHORT).show();
        } else {

            showProgressDialog();

            mAuth.createUserWithEmailAndPassword(email, contraseña)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                            if (task.isSuccessful()) {
                                autenticacionExitosa(task.getResult().getUser());
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.ya_registrado_str),
                                        Toast.LENGTH_SHORT).show();
                                hideProgressDialog();
                            }
                        }
                    });
        }
    }

    private void iniciarSesion(String email, String contraseña) {
        Log.d(TAG, "iniciarSesion: " + email);
        if (email.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(this, getString(R.string.campos_no_validos_str), Toast.LENGTH_SHORT).show();
        } else {

            showProgressDialog();

            mAuth.signInWithEmailAndPassword(email, contraseña)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                            if (task.isSuccessful()) {
                                autenticacionExitosa(task.getResult().getUser());
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.cuenta_contraseña_incorrecta_str),
                                        Toast.LENGTH_SHORT).show();
                                hideProgressDialog();
                            }
                        }
                    });
        }
    }

    private void autenticacionExitosa(FirebaseUser user) {
        database.child("usuarios").child(user.getUid()).setValue(user.getEmail());

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.but_crear_cuenta) {
            crearCuenta(et_email.getText().toString(), et_contraseña.getText().toString());
        } else if (i == R.id.but_iniciar_sesion) {
            iniciarSesion(et_email.getText().toString(), et_contraseña.getText().toString());
        }
    }

}










































