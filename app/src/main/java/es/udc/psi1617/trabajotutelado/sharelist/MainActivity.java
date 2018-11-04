package es.udc.psi1617.trabajotutelado.sharelist;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends Activity implements listaSeleccionadaListener, AyudaFragm.OnFragmentInteractionListener{

    private static final String TAG = "TAG_MainActivity";
    public static boolean main = false;
    static public boolean elementos = false;

    @Override
    protected void onResume() {
        super.onResume();
        main = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        main = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_container) != null) {
            ListasFragm fragment = new ListasFragm();
            getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        }

        Intent notifIntent = getIntent();
        String id = notifIntent.getStringExtra("id");
        int n = notifIntent.getIntExtra("num", -1);

        // quitar la notificación
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(n);


        if (n != -1) {
            n = -1;
            listaSeleccionada(id, "");
        }

        startService(new Intent(this, FirebaseBackgroundService.class));
    }

    @Override
    public void listaSeleccionada(String id, String nombre_lista) {
        ElementosFragm fragm_elementos = (ElementosFragm) getFragmentManager().findFragmentById(R.id.fragm_elementos);
        if (fragm_elementos != null) {
            // Pantallas grandes
            fragm_elementos.actualizarUI(id);

        } else {
            ElementosFragm elementosFragm = new ElementosFragm();
            Bundle args = new Bundle();

            args.putString("lista", id);
            elementosFragm.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, elementosFragm).addToBackStack(null).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                dialogoCerrarSesion();
                return true;
            case R.id.ayuda:
                Log.d(TAG, "hola ayuda");
                ListasFragm listasFragm = (ListasFragm) getFragmentManager().findFragmentById(R.id.fragm_listas);
                if (!elementos) {
                    Log.d(TAG, "listas fragment");
                    AyudaFragm ayudaFragm = new AyudaFragm();
                    Bundle bundle = new Bundle();
                    bundle.putString("volver", "listas");
                    ayudaFragm.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, ayudaFragm).addToBackStack(null).commit();
                }
                ElementosFragm elementosFragm = (ElementosFragm) getFragmentManager().findFragmentById(R.id.fragm_elementos);
                if (elementos) {
                    Log.d(TAG, "elementos fragment");
                    AyudaFragm ayudaFragm = new AyudaFragm();
                    Bundle bundle = new Bundle();
                    bundle.putString("volver", "elementos");
                    ayudaFragm.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, ayudaFragm).addToBackStack(null).commit();
                }



            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void dialogoCerrarSesion () {

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.cerrar_sesion_str))
                .setNegativeButton(getString(R.string.cancelar_str), null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }
                })
                .show();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
