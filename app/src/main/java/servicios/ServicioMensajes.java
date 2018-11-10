package servicios;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.whereismypet.whereismypet.R;

import actividades.MainActivity;

public class ServicioMensajes extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getNotification()!=null)
        {
            mostrarNotificacion(remoteMessage.getNotification().getBody());
        }
    }

    private void mostrarNotificacion(String body) {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i,PendingIntent.FLAG_ONE_SHOT);

        Uri sonido=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificacion = new NotificationCompat.Builder(this)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sonido)
                .setSmallIcon(R.drawable.icon_app)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notificacion.build());

    }
}
