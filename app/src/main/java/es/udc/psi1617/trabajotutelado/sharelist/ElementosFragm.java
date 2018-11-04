package es.udc.psi1617.trabajotutelado.sharelist;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import static android.app.Activity.RESULT_OK;


public class ElementosFragm extends Fragment {

    private DatabaseReference database;
    private FirebaseUser user;
    private String listaId;
    private String listaNombre;
    private DatabaseReference dbElementos;
    private ListView lv;
    private TextView tv_nombre_lista;
    private View elemento_pulsado;
    private String nombre_elemento_pulsado;
    private  String clave_elemento_pulsado;
    private FirebaseListAdapter<Elemento> adaptador;
    private final Fragment context = this;
    private TextView tv_no_elements;
    private static final String TAG = "TAG_ElementosFragm";
    private String item_id;
    private String tachado;

    public ElementosFragm() {
        // Required empty public constructor
    }


    private void otroBorra() {

        DatabaseReference listas = database.child("listas");
        listas.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (getFragmentManager() == null) return;
                Log.d("TAG", "A");
                if (dataSnapshot.getKey().equals(listaId)) {
                    Toast.makeText(getActivity(), "'"+ listaNombre+getString(R.string.ya_no_esta_en_mis_listas_str), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "B");
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_elementos, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity.elementos = true;
        ((MainActivity) getActivity()).setTitle(getString(R.string.items_str));
        tv_no_elements = (TextView) getView().findViewById(R.id.tv_sin_elementos);
        database = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        Bundle args = getArguments();
        database.child("listas").child(args.getString("lista")).child("elementos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren() && getView() != null) {
                    tv_no_elements.setVisibility(View.VISIBLE);
                } else if (getView() != null){
                    tv_no_elements.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        lv = (ListView) getView().findViewById(R.id.lista2);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), InfoItem.class);
                String item_id = adaptador.getRef(position).getKey();
                intent.putExtra("lista_id", listaId);
                intent.putExtra("item_id", item_id);
                startActivityForResult(intent, 2);
            }
        });

        /*
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
        });*/
        tv_nombre_lista = (TextView) getView().findViewById(R.id.tv_nombre_lista);
        Button but_añadir_elemento = (Button) getView().findViewById(R.id.but_añadir_elemento);
        Button but_invitar = (Button) getView().findViewById(R.id.but_invitar);
        Button but_info = (Button) getView().findViewById(R.id.but_info);
        Button but_editar = (Button) getView().findViewById(R.id.but_editar);
        Button but_borrar = (Button) getView().findViewById(R.id.but_borrar);


        if (args != null) {
            //listaId = args.getString("lista");
            actualizarUI(args.getString("lista"));
        }


        but_añadir_elemento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAñadir();
            }
        });


        but_invitar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UsuariosParaInvitar.class);
                intent.putExtra("listaId", listaId);
                intent.putExtra("listaNom", listaNombre);
                startActivityForResult(intent, 1);
            }
        });


        but_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InfoLista.class);
                intent.putExtra("listaId", listaId);
                intent.putExtra("listaNom", listaNombre);
                startActivity(intent);
            }
        });


        // listener de pulsación larga del elemento en la lista para editar y borrar
        /*
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("editar_borrar", "pulsacion larga");
                elemento_pulsado = view;
                view.setBackgroundColor(Color.GRAY);
                getView().findViewById(R.id.grupo_añadir_invitar).setVisibility(View.GONE);
                getView().findViewById(R.id.grupo_editar_borrar).setVisibility(View.VISIBLE);

                // Obtener la clave y el valor del elemento pulsado
                clave_elemento_pulsado = adaptador.getRef(position).getKey();
                dbElementos.child(clave_elemento_pulsado).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        nombre_elemento_pulsado = dataSnapshot.getValue().toString();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                return false;
            }
        });
        */
        registerForContextMenu(lv);

        but_editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoEditar();
                elemento_pulsado.setBackgroundColor(Color.alpha(android.R.color.background_light));
                getView().findViewById(R.id.grupo_añadir_invitar).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.grupo_editar_borrar).setVisibility(View.GONE);
            }
        });
        otroBorra();

    }

    public void borrar() {
        dbElementos.child(clave_elemento_pulsado).removeValue();
        //Toast.makeText(getActivity(), "Elemento **" + nombre_elemento_pulsado + "** borrado", Toast.LENGTH_SHORT).show();
        //elemento_pulsado.setBackgroundColor(Color.alpha(android.R.color.background_light));
        getView().findViewById(R.id.grupo_añadir_invitar).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.grupo_editar_borrar).setVisibility(View.GONE);

        Toast.makeText(getActivity(), getString(R.string.item_borrado_str), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.lista2) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            clave_elemento_pulsado = adaptador.getRef(acmi.position).getKey();
            final Elemento obj = (Elemento) lv.getItemAtPosition(acmi.position);
            menu.add(getString(R.string.editar_str));
            menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Log.d(TAG, "Editar elemento");
                    dialogoEditar();
                    return false;
                }
            });
            menu.add(getString(R.string.borrar_str));
            menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Log.d(TAG, "Borrar elemento");
                    dialogoBorrar(obj.getNombre());
                    return false;
                }
            });
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private void dialogoBorrar(String item) {
        new AlertDialog.Builder(getActivity())
                .setTitle("¿" + getString(R.string.borrar_str) +" '" + item + "'?")
                .setNegativeButton(getString(R.string.cancelar_str), null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        borrar();
                    }
                })
                .show();
    }

    public void actualizarUI(String id) {
        listaId = id;

        // Conseguir el nombre de la lista
        database.child("listas").child(listaId).child("nombre").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;
                listaNombre = dataSnapshot.getValue().toString();
                tv_nombre_lista.setText(listaNombre);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        dbElementos = database.child("listas").child(listaId).child("elementos");

        adaptador = new FirebaseListAdapter<Elemento>(getActivity(), Elemento.class, android.R.layout.simple_list_item_1, dbElementos) {
            @Override
            protected void populateView(View v, Elemento model, int position) {
                TextView text = (TextView) v.findViewById(android.R.id.text1);
                text.setText(model.getNombre());
                text.setBackgroundColor(getResources().getColor(R.color.rojito));
                if (model.getEstado().equals("TACHADO")) {
                    text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    //text.setBackground(getResources().getDrawable(R.drawable.tachar));
                } else {
                    text.setPaintFlags(0);
                    //text.setBackground(getResources().getDrawable(R.drawable.sin_tachar));
                }
            }
        };
        lv.setAdapter(adaptador);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String id_usuario_invitado = data.getStringExtra("id");
                String email_invitado = data.getStringExtra("email");
                Log.d("invitado", "usuario invitado: " + id_usuario_invitado);
                Log.d("invitado", "email: " + email_invitado);
                compartirLista(id_usuario_invitado, email_invitado);
            }
        }
    }


    private void dialogoAñadir() {
        final EditText entrada = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.nuevo_elemento_str))
                .setView(entrada)
                .setNegativeButton(getString(R.string.cancelar_str), null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String elemento = entrada.getText().toString();
                        String yo = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        Elemento mi_elemento = new Elemento(yo, elemento);
                        if (!elemento.isEmpty()) {
                            String key = dbElementos.push().getKey();
                            dbElementos.child(key).setValue(mi_elemento);
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.entrada_no_valida_str), Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .show();
    }

    private void dialogoEditar() {
        final EditText entrada = new EditText(getActivity());
        entrada.setText(nombre_elemento_pulsado);
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.ponle_otro_nombre_al_item_str))
                .setView(entrada)
                .setNegativeButton(getString(R.string.cancelar_str), null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String elemento = entrada.getText().toString();
                        if (!elemento.isEmpty()) {
                            String yo = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                            Elemento mi_elemento = new Elemento(yo, elemento);
                            dbElementos.child(clave_elemento_pulsado).setValue(mi_elemento);
                            Toast.makeText(getActivity(), getString(R.string.elemento_modificado_str), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.entrada_no_valida_str), Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .show();
    }

    private void compartirLista(String idU, String email) {
        DatabaseReference dbLista = database.child("listas").child(listaId);
        dbLista.child("participantes").child(idU).setValue(email);
        database.child("listas_usuarios").child(idU).child(listaId).setValue(listaNombre);

        // mandar notificación
        database.child("invitaciones_usuarios").child(idU).child(listaId).setValue(listaNombre);
    }



}



























