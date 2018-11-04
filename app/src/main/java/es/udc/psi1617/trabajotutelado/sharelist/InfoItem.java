package es.udc.psi1617.trabajotutelado.sharelist;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class InfoItem extends android.app.Activity {

    private Button tachar;
    private Button destachar;
    private TextView creador;
    private TextView fecha_creador;
    private TextView estado;
    private TextView fecha_estado;
    private TextView usr_estado;
    private TextView estado_item;
    private TextView nombre_elemento;
    private TextView comentario;
    private LinearLayout ll_estado;
    private DatabaseReference database;
    private DatabaseReference elemento;
    private String lista_id, item_id;
    private String yo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info_item);
        tachar = (Button) findViewById(R.id.tachar);
        tachar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tachar();
            }
        });
        destachar = (Button) findViewById(R.id.destachar);
        destachar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destachar();
            }
        });
        yo = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        item_id = getIntent().getStringExtra("item_id");
        lista_id = getIntent().getStringExtra("lista_id");
        ll_estado = (LinearLayout) findViewById(R.id.ll_estado);
        database = FirebaseDatabase.getInstance().getReference();
        elemento = database.child("listas").child(lista_id).child("elementos").child(item_id);
        creador = (TextView) findViewById(R.id.nombre_creador_item);
        fecha_creador = (TextView) findViewById(R.id.fecha_creador_item);
        estado = (TextView) findViewById(R.id.estado);
        estado_item = (TextView) findViewById(R.id.estado_item);
        nombre_elemento = (TextView) findViewById(R.id.nombre_item);
        fecha_estado = (TextView) findViewById(R.id.fecha_estado);
        usr_estado = (TextView) findViewById(R.id.quien_tacho_item);
        comentario = (TextView) findViewById(R.id.comentario_item);
        elemento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    if (childSnapshot.getKey().equals("creador")) {
                        creador.setText(childSnapshot.getValue().toString());
                    } else if (childSnapshot.getKey().equals("estado")) {
                        estado.setText(childSnapshot.getValue().toString());
                    } else if (childSnapshot.getKey().equals("fecha_creacion")) {
                        fecha_creador.setText(childSnapshot.getValue().toString());
                    } else if (childSnapshot.getKey().equals("fecha_estado")) {
                        fecha_estado.setText(childSnapshot.getValue().toString());
                    } else if (childSnapshot.getKey().equals("nombre")) {
                        nombre_elemento.setText(childSnapshot.getValue().toString());
                    } else if (childSnapshot.getKey().equals("comentario")) {
                        comentario.setText(childSnapshot.getValue().toString());
                    } else if (childSnapshot.getKey().equals("estado")) {
                        estado.setText(childSnapshot.getValue().toString());
                    } else if (childSnapshot.getKey().equals("usr_estado")) {
                        usr_estado.setText(childSnapshot.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Botón "Atrás" en Action Bar
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(getString(R.string.volver_a_ver_todos_los_items_str));
        actionBar.setIcon(getResources().getDrawable(R.drawable.atras));
    }

    private void destachar() {
        dialogoDestachar();
    }

    private void dialogoDestachar() {
        final EditText motivo = new EditText(this);
        motivo.setHint(getString(R.string.motivo_para_destachar_str));
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.destachar_elemento_str))
                .setView(motivo)
                .setNegativeButton(getString(R.string.cancelar_str), null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String motivo_str = motivo.getText().toString();



                        if (!motivo_str.isEmpty()) {
//Cambios en el modelo
                            String nueva_fecha = dia_de_hoy();
                            elemento.child("comentario").setValue(motivo_str);
                            elemento.child("usr_estado").setValue(yo);
                            elemento.child("fecha_estado").setValue(nueva_fecha);
                            elemento.child("estado").setValue("DESTACHADO");

                            //Cambios en la vista
                            elemento.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                        if (childSnapshot.getKey().equals("creador")) {
                                            creador.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("estado")) {
                                            estado.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("fecha_creacion")) {
                                            fecha_creador.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("fecha_estado")) {
                                            fecha_estado.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("nombre")) {
                                            nombre_elemento.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("comentario")) {
                                            comentario.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("estado")) {
                                            estado.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("usr_estado")) {
                                            usr_estado.setText(childSnapshot.getValue().toString());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            comentario.setText(motivo_str);
                            estado.setText(getString(R.string.destachado_str));
                            fecha_estado.setText(nueva_fecha);
                            usr_estado.setText(yo);

                            Toast.makeText(getApplicationContext(), getString(R.string.destachado_con_exito_str), Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.debe_introducir_un_motivo_str), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void tachar() {
        dialogoTachar();
    }

    public String dia_de_hoy() {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        TimeZone timezone = TimeZone.getTimeZone("Europe/Madrid");
        df.setTimeZone(timezone);
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }



    public void dialogoTachar() {
        final EditText motivo = new EditText(this);
        motivo.setHint(getString(R.string.motivo_para_tachar_str));
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.tachar_elemento_str))
                .setView(motivo)
                .setNegativeButton(getString(R.string.cancelar_str), null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String motivo_str = motivo.getText().toString();



                        if (!motivo_str.isEmpty()) {


                            //Cambios en el modelo
                            String nueva_fecha = dia_de_hoy();
                            elemento.child("comentario").setValue(motivo_str);
                            elemento.child("usr_estado").setValue(yo);
                            elemento.child("fecha_estado").setValue(nueva_fecha);
                            elemento.child("estado").setValue("TACHADO");

                            //Cambios en la vista
                            elemento.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                        if (childSnapshot.getKey().equals("creador")) {
                                            creador.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("estado")) {
                                            estado.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("fecha_creacion")) {
                                            fecha_creador.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("fecha_estado")) {
                                            fecha_estado.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("nombre")) {
                                            nombre_elemento.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("comentario")) {
                                            comentario.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("estado")) {
                                            estado.setText(childSnapshot.getValue().toString());
                                        } else if (childSnapshot.getKey().equals("usr_estado")) {
                                            usr_estado.setText(childSnapshot.getValue().toString());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            comentario.setText(motivo_str);
                            estado.setText(getString(R.string.tachado_str));
                            fecha_estado.setText(nueva_fecha);
                            usr_estado.setText(yo);

                            Toast.makeText(getApplicationContext(), getString(R.string.tachado_con_exito_str), Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.debe_introducir_un_motivo_str), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
