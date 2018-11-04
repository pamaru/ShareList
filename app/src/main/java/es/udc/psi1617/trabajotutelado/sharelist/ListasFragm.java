package es.udc.psi1617.trabajotutelado.sharelist;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.google.firebase.database.ValueEventListener;


public class ListasFragm extends Fragment {

    static final String TAG = "TAG_ListasFragm";
    private DatabaseReference database;
    private FirebaseUser user;
    private listaSeleccionadaListener listener;
    private boolean longclick = false;
    private FirebaseAuth mAuth;


    public ListasFragm() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_listas, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            Log.d(TAG, "entra en onAttach");
            listener = (listaSeleccionadaListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity "+activity.toString()
                    +" must implement his method");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            Log.d(TAG, "entra en onAttach");
            listener = (listaSeleccionadaListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity "+context.toString()
                    +" must implement his method");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity.elementos = false;
        ((MainActivity) getActivity()).setTitle(getString(R.string.listas_str));

        database = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        database.child("listas_usuarios").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren() && getView() != null) {
                    TextView tv_no_listas = (TextView) getView().findViewById(R.id.tv_no_listas);
                    tv_no_listas.setText(getString(R.string.sin_listas_str));
                    RelativeLayout layout = (RelativeLayout) (getView().findViewById(R.id.no_listas));
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
                    params.setMargins(0, 400, 0, 0);
                    layout.setLayoutParams(params);
                } else if (getView() != null){
                    RelativeLayout layout = (RelativeLayout) (getView().findViewById(R.id.no_listas));
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
                    params.setMargins(0, 0, 0, 0);
                    layout.setLayoutParams(params);
                    TextView tv_no_listas = (TextView) getView().findViewById(R.id.tv_no_listas);
                    if (dataSnapshot.getChildrenCount() == 1)
                        tv_no_listas.setText(dataSnapshot.getChildrenCount() + " lista");
                    if (dataSnapshot.getChildrenCount() > 1)
                        tv_no_listas.setText(dataSnapshot.getChildrenCount() + " listas");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final Button but_añadir = (Button) getView().findViewById(R.id.but_añadir);
        but_añadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAñadir();
            }
        });

        ListView lv = (ListView) getView().findViewById(R.id.lista1);

        DatabaseReference dbListas = database.child("listas_usuarios").child(user.getUid());

        final FirebaseListAdapter<String> adaptador = new FirebaseListAdapter<String>(getActivity(), String.class, android.R.layout.simple_list_item_1, dbListas) {
            @Override
            protected void populateView(View v, String model, int position) {
                TextView text = (TextView)v.findViewById(android.R.id.text1);
                text.setText(model);
                text.setBackgroundColor(getResources().getColor(R.color.rojito));
            }
        };
        lv.setAdapter(adaptador);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (longclick) {
                    longclick = false;
                    return;
                }
                String identificador = adaptador.getRef(position).getKey();
                String nombre_lista = adaptador.getRef(position).toString();
                Log.d(TAG, "short click");
                listener.listaSeleccionada(identificador, nombre_lista);
            }
        });
    }

    private void dialogoAñadir() {
        final EditText entrada = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.ponle_nombre_a_tu_lista_str))
                .setView(entrada)
                .setNegativeButton(getString(R.string.cancelar_str), null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nombreLista = entrada.getText().toString();
                        if (!nombreLista.isEmpty()) {
                            String key = database.child("listas_usuarios").child(user.getUid()).push().getKey();
                            database.child("listas").child(key).child("nombre").setValue(nombreLista);
                            database.child("listas_usuarios").child(user.getUid()).child(key).setValue(nombreLista);

                            mAuth = FirebaseAuth.getInstance();
                            String uid = mAuth.getCurrentUser().getUid();
                            String email = mAuth.getCurrentUser().getEmail();
                            DatabaseReference dbLista = database.child("listas").child(key);
                            dbLista.child("creador").setValue(email);
                            dbLista.child("participantes").child(uid).setValue(email);
                            database.child("listas_usuarios").child(uid).child(key).setValue(nombreLista);

                        } else {
                            Toast.makeText(getActivity(), getString(R.string.entrada_no_valida_str), Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .show();
    }
}

interface listaSeleccionadaListener {
    void listaSeleccionada(String id, String nombre_lista);
}






























