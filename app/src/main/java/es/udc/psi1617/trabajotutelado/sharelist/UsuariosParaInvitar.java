package es.udc.psi1617.trabajotutelado.sharelist;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



public class UsuariosParaInvitar extends Activity {
    //private FirebaseUser user;
    DatabaseReference dbUsuarios;
    DatabaseReference dbUsuariosInvitar;
    static final String TAG = "TAG_Invitar";
    String email;
    private String listaId;
    private EditText et_email_invitar;
    private ListView lv;
    private TextView tv_sin_res;
    private boolean resultados = false;
    private String sss = "";
    private String listaNom;


    private DatabaseReference database, mislistas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios_para_invitar);
        Intent intent = getIntent();
        listaId = intent.getStringExtra("listaId");
        listaNom = intent.getStringExtra("listaNom");
        database = FirebaseDatabase.getInstance().getReference();
        //user = FirebaseAuth.getInstance().getCurrentUser();
        dbUsuarios = database.child("usuarios");
        dbUsuariosInvitar = database.child("usuarios_invitar");
        setTitle("  Invitar");
        tv_sin_res = (TextView) findViewById(R.id.tv_sin_res);
        tv_sin_res.setVisibility(View.GONE);
        lv = (ListView) findViewById(R.id.lista_usuarios);
        poblarLista("");
        et_email_invitar = (EditText) findViewById(R.id.et_email_invitar);
        et_email_invitar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Log.d(TAG, "A");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sss = s.toString();
                poblarLista(sss);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Log.d(TAG, "C");
            }
        });


        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        mislistas = database.child("listas_usuarios").child(mAuth.getCurrentUser().getUid());
        otroBorra();
        nuevoUsuario();

    }

    void listaInvitados(final String s) {
        dbUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    if (childSnapshot.getValue().toString().toLowerCase().contains(s.toLowerCase()) && !s.equals("")) {
                        dbUsuariosInvitar.child(key).setValue(childSnapshot.getValue());
                    } else {
                        dbUsuariosInvitar.child(key).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void poblarLista(final String s1) {
        listaInvitados(s1);
        final FirebaseListAdapter<String> adaptador = new FirebaseListAdapter<String>(this, String.class, android.R.layout.simple_list_item_1, dbUsuariosInvitar) {
            @Override
            protected void populateView(View v, final String model, int position) {
                final TextView text = (TextView) v.findViewById(android.R.id.text1);
                text.setText(model);
                text.setBackgroundColor(Color.TRANSPARENT);
                DatabaseReference participantes = database.child("listas").child(listaId).child("participantes");
                participantes.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            if (childSnapshot.getValue().equals(model)) {
                                text.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                                text.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.error_ya_invitado_str), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        lv.setAdapter(adaptador);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String identificador = adaptador.getRef(position).getKey();

                dbUsuarios.child(identificador).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        email = dataSnapshot.getValue().toString();
                        Intent databack = new Intent();
                        databack.putExtra("id", identificador);
                        databack.putExtra("email", email);

                        setResult(RESULT_OK, databack);
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });
    }

    private void nuevoUsuario() {
        dbUsuarios.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                poblarLista(sss);
                Log.d(TAG, "added");

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void otroBorra() {
        mislistas.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (MainActivity.main == false) {
                    if (dataSnapshot.getKey().equals(listaId)) {
                        Toast.makeText(getApplicationContext(), "'"+ listaNom+getString(R.string.ya_no_esta_en_mis_listas_str), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
