package clases_estaticas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.appindexing.builders.StickerBuilder;
import com.squareup.picasso.Picasso;
import com.whereismypet.whereismypet.R;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import actividades.LoginActivity;
import actividades.MainActivity;
import actividades.RegistroActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import misclases.Usuario;
import misclases.VolleySingleton;

import static com.facebook.FacebookSdk.getApplicationContext;


public final class WebServiceJSON {
    private static ProgressDialog progressDialog;
    private static CircleImageView perfil;
    //-----------------------------------URL WEB SERVICE-------------------------------------------------------------
    private static final String URL_LOGIN = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Logueo2.php";
    private static String URL_CONSULTAR_USUARIO ="http://www.secsanluis.com.ar/servicios/varios/wimp/W_ConsultarCliente.php";
    private static String URL_CORREO = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_ValidarCorreo.php";
    private static final String URL_REGISTRO = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Registro.php";
    //----------------------------------CODIGO IMAGEN----------------------------------------------------------------
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;
    //-------------------------------------METODOS-------------------------------------------------------------------

    public static void UserLogin(final Usuario user, final Context context, final Boolean remember, final Activity activity) {
        progressDialog  = new ProgressDialog(context);
        progressDialog.setMessage("Logueando...");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {
                        progressDialog.dismiss();
                        if (ServerResponse.equalsIgnoreCase("OK")) {
                            if(remember){
                                GeneralMethods.savedLoginSharedPreferencesDB(user.getEmail(),"DB",true,context);
                            }
                            else{
                                GeneralMethods.savedLoginSharedPreferencesDB(user.getEmail(),"DB",false,context);
                            }
                            GeneralMethods.InicioSesionCorrecto(context,activity);
                        } else {
                            Toast.makeText(context, "Lo siento, ocurrio un inconveniente al intentar iniciar, asegurese de estar registrado y vuelva a intentarlo", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss(); }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("correo", user.getEmail());
                params.put("pass", user.getContraseña());

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(context).addToRequestQueue(stringRequest);
    }

    public static void ConsultarPerfil(final Context context, final Activity activity){
        URL_CONSULTAR_USUARIO = URL_CONSULTAR_USUARIO + "?correo=" + GeneralMethods.getFromSharedPreferences("correo",context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_CONSULTAR_USUARIO, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray lista = response.getJSONArray("w_usuario");
                            JSONObject json_data = lista.getJSONObject(0);
                            String imgPerfil = json_data.getString("imagen");
                            perfil = activity.findViewById(R.id.imgPerfilMenu);

                            GeneralMethods.Picasso(context,imgPerfil,perfil);
                        } catch (JSONException ignored) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(context, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(context).addToRequestQueue(jsonObjectRequest);
    }

    public static void ValidarCorreo(final Usuario user, final Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Validando correo...");
        progressDialog.show();

        URL_CORREO = URL_CORREO + "?correo=" + user.getEmail();
        URL_CORREO  = URL_CORREO.replace(" ","%20");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_CORREO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {
                        progressDialog.dismiss();
                        if (ServerResponse.equalsIgnoreCase("OK")) {

                        } else {
                            Toast.makeText(context, "Lo siento, pero el correo ya se encuentra registrado", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Toast.makeText(context, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("correo", user.getEmail());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(context).addToRequestQueue(stringRequest);
    }

    public void Registro(final Usuario user, final Context context,final Activity activity, final String pathImageWebService){

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGISTRO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {
                        progressDialog.dismiss();
                        if(ServerResponse.equalsIgnoreCase("OK")) {
                            Toast.makeText(context, "Usuario registrado exitosamente!!!", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(context, LoginActivity.class);
                            activity.startActivity(i);
                            activity.finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Error al registrar, por favor, vuelve a intentar.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("nombre",user.getNombre());
                params.put("apellido", user.getApellido());
                params.put("correo",user.getEmail());
                params.put("pass",user.getContraseña());
                params.put("imagen",pathImageWebService );
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(context).addToRequestQueue(stringRequest);

    }

    private void enviarImagenDB(int tipoOpcion,Context context, String pathImagen ){
        String nombreFotoServidor = String.valueOf(Calendar.getInstance().getTime()).replace(":","-").replace(" ","") + String.valueOf(Calendar.getInstance().getTimeInMillis());
        switch (tipoOpcion){
            case COD_SELECCIONA:{
                GeneralMethods.AndroidUploadService(pathImagen,nombreFotoServidor, context);
            }break;
            case COD_FOTO:{
                GeneralMethods.AndroidUploadService(pathImagen,nombreFotoServidor,context);
            }break;
            default:break;
        }
    }

    public String getPath(Uri uri,Context context) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = context.getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
}
