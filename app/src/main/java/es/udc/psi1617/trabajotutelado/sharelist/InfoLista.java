package es.udc.psi1617.trabajotutelado.sharelist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.common.data.DataBufferObserverSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class InfoLista extends Activity {

    private String sig_creador;
    private String email;
    private String listaId;
    private String creador;
    private String listaNom;
    private DatabaseReference database;
    private DatabaseReference mislistas;
    private EditText et_editar_lista;
    static final String TAG = "TAG_InfoLista";
    private TextView nom_lista_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_lista);
        setTitle("  Info");
        database = FirebaseDatabase.getInstance().getReference();
        nom_lista_info = (TextView) findViewById(R.id.nom_lista_info);
        Button but_editar_lista = (Button) findViewById(R.id.but_editar_lista);
        et_editar_lista = (EditText) findViewById(R.id.et_editar_lista);

        but_editar_lista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editarLista();
            }
        });

        Button but_borrar_lista = (Button) findViewById(R.id.but_borrar_lista);
        but_borrar_lista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoBorrar();
            }
        });

        Button but_abandonar_lista = (Button) findViewById(R.id.but_abandonar_lista);
        but_abandonar_lista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAbandonar();
            }
        });

        Intent intent = getIntent();
        listaId = intent.getStringExtra("listaId");
        listaNom = intent.getStringExtra("listaNom");
        nom_lista_info.setText(listaNom);
        et_editar_lista.setText(listaNom);

        ListView lv = (ListView) findViewById(R.id.lv_participantes);
        final TextView tv_creador = (TextView) findViewById(R.id.tv_creador);

        DatabaseReference dbParticipantes = database.child("listas").child(listaId).child("participantes");
        DatabaseReference creador = database.child("listas").child(listaId).child("creador");
        creador.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    tv_creador.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseListAdapter<String> adaptador = new FirebaseListAdapter<String>(this, String.class, android.R.layout.simple_list_item_1, dbParticipantes) {
            @Override
            protected void populateView(View v, String model, int position) {
                TextView text = (TextView)v.findViewById(android.R.id.text1);
                text.setText(model);
            }
        };
        lv.setAdapter(adaptador);
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        mislistas = database.child("listas_usuarios").child(mAuth.getCurrentUser().getUid());
        otroBorra();
    }

    private void abandonarLista() {
        //key_abandonada = listaId;
        final FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        database.child("listas").child(listaId).child("creador").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                email = dataSnapshot.getValue().toString();
                Log.d(TAG, "email: " + email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference participantes = database.child("listas").child(listaId).child("participantes");

        participantes.child(mAuth.getCurrentUser().getUid()).removeValue();
        participantes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DataSnapshot hijo;
                if (dataSnapshot.getChildren().iterator().hasNext()) {
                    Log.d(TAG, "Hay más participantes");
                    hijo = dataSnapshot.getChildren().iterator().next();
                    sig_creador = hijo.getValue().toString();
                    Log.d(TAG, "sig creador: " + sig_creador);
                    String creador = mAuth.getCurrentUser().getEmail();
                    if (creador.equals(email)) {
                        database.child("listas").child(listaId).child("creador").setValue(sig_creador);
                        Log.d(TAG, creador + "->" + sig_creador);
                    }

                } else {
                    Log.d(TAG, "No hay más participantes");
                    borrarLista();
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        DatabaseReference lista = database.child("listas_usuarios").child(mAuth.getCurrentUser().getUid()).child(listaId);
        lista.removeValue();


        //Toast.makeText(this, "Has abandonado la lista '" + listaNom +"'", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void borrarLista() {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        final String email = mAuth.getCurrentUser().getEmail();
        database.child("listas").child(listaId).child("creador").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Entró");
                creador = dataSnapshot.getValue().toString();
                if (!email.equals(creador)) {
                    Log.d(TAG, "" + email + " " + creador);
                    Toast.makeText(getApplicationContext(), getString(R.string.error_no_tienes_permisos_str), Toast.LENGTH_SHORT).show();
                } else {
                    quitarLista();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void quitarLista() {
        DatabaseReference participantes = database.child("listas").child(listaId).child("participantes");
        participantes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    String participante = childSnapshot.getKey().toString();
                    database.child("listas_usuarios").child(participante).child(listaId).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        database.child("listas").child(listaId).removeValue();

    }

    private void dialogoAbandonar() {
        new AlertDialog.Builder(this)
                .setTitle("¿" +
                        getString(R.string.abandonar_lista_str) + listaNom + "'?")
                .setNegativeButton(getString(R.string.cancelar_str), null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        abandonarLista();
                    }
                })
                .show();
    }

    private void dialogoBorrar() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.borrar_lista_str) + listaNom + "'?")
                .setNegativeButton(getString(R.string.cancelar_str), null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        borrarLista();
                    }
                })
                .show();
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

    private void editarLista() {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        final String email = mAuth.getCurrentUser().getEmail();
        database.child("listas").child(listaId).child("creador").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Entró");
                creador = dataSnapshot.getValue().toString();
                if (!email.equals(creador)) {
                    Log.d(TAG, "" + email + " " + creador);
                    Toast.makeText(getApplicationContext(), getString(R.string.error_no_tienes_permisos_str), Toast.LENGTH_SHORT).show();
                } else {
                    cambiarNombre(et_editar_lista.getText().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void cambiarNombre(final String nuevo) {

        if (nuevo.isEmpty()) {
            et_editar_lista.setText(listaNom);
            Toast.makeText(this, getString(R.string.error_nombre_no_valido_str), Toast.LENGTH_SHORT).show();
            return;
        }
        listaNom = nuevo;

        nom_lista_info.setText(listaNom);

        database.child("listas").child(listaId).child("nombre").setValue(nuevo);

        DatabaseReference participantes = database.child("listas").child(listaId).child("participantes");
        participantes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    String participante = childSnapshot.getKey().toString();
                    database.child("listas_usuarios").child(participante).child(listaId).setValue(nuevo);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toast.makeText(this, getString(R.string.nombre_cambiado_str), Toast.LENGTH_SHORT).show();
    }
}
