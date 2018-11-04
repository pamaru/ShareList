package es.udc.psi1617.trabajotutelado.sharelist;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class FirebaseBackgroundService extends Service{

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference dbInvitaciones = FirebaseDatabase.getInstance().getReference().child("invitaciones_usuarios").child(user.getUid());
    private ChildEventListener listener;
    private static int n = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        new Thread(new Runnable() {
            public void run() {

                listener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d("invitacion", "nueva invitacion:  " + dataSnapshot.getValue());

                        postNotificacion(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
                        dbInvitaciones.child(dataSnapshot.getKey()).removeValue();
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
                };

                dbInvitaciones.addChildEventListener(listener);

            }
        }).start();

    }


    private void postNotificacion(String idLista, String lista) {
        NotificationCompat.Builder notific = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.te_han_invitado_a_la_lista_str) + lista + "'")
                .setSmallIcon(R.drawable.logo)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                //.setOnlyAlertOnce(true)
                .setContentText(getString(R.string.toca_para_verla_str))
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pebble));

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.putExtra("id", idLista);
        notIntent.putExtra("num", n);
        notIntent.setAction(Long.toString(System.currentTimeMillis()));
        //Log.d("hola", "**** mi n = " + n);


        PendingIntent contIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notific.setContentIntent(contIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(n++, notific.build());
    }
}
