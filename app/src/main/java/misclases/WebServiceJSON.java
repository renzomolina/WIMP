package misclases;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;
import com.whereismypet.whereismypet.R;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import actividades.MainActivity;
import de.hdodenhof.circleimageview.CircleImageView;


public final class WebServiceJSON {
    private static ProgressDialog progressDialog;
    private static CircleImageView perfil;
    //-----------------------------------URL WEB SERVICE------------------------------------------------
    private static final String URL_LOGIN = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Logueo2.php";
    private static String URL_CONSULTAR_USUARIO ="http://www.secsanluis.com.ar/servicios/varios/wimp/W_ConsultarCliente.php";
    private static ContextWrapper wrapper;
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
                                savedLoginSharedPreferencesDB(user.getEmail(),"DB",true,context);
                            }
                            else{
                                savedLoginSharedPreferencesDB(user.getEmail(),"DB",false,context);
                            }
                            InicioSesionCorrecto(context,activity);
                        } else {
                            Toast.makeText(context, "Lo siento, pero el correo ya se encuentra registrado", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();

                    }
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
        URL_CONSULTAR_USUARIO = URL_CONSULTAR_USUARIO + "?correo=" + getFromSharedPreferences("correo",context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_CONSULTAR_USUARIO, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray lista = response.getJSONArray("w_usuario");
                            JSONObject json_data = lista.getJSONObject(0);
                            String imgPerfil = json_data.getString("imagen");
                            perfil = activity.findViewById(R.id.imgPerfilMenu);

                            Picasso(context,imgPerfil,perfil);
                        } catch (JSONException ignored) {
                            String ex = ignored.getMessage();
                            Toast.makeText(context,ex,Toast.LENGTH_LONG);
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

    //--------------------------------------PREFERENCIAS----------------------------------------------------------------
    public static String getFromSharedPreferences(String key,Context context){
        wrapper = new ContextWrapper(context);
        SharedPreferences sharedPreferences = wrapper.getSharedPreferences("Mis preferencias", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }
    public static void savedLoginSharedPreferencesFB(String token, String userID, String FB,Context context){
        wrapper = new ContextWrapper(context);
        SharedPreferences sharedPreferences;
        sharedPreferences = wrapper.getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("userID", userID);
        editor.putString("facebook",FB);
        editor.apply();
    }

    public static void savedLoginSharedPreferencesDB(String correo, String DB, boolean remember,Context context){
        wrapper = new ContextWrapper(context);
        SharedPreferences sharedPreferences;
        sharedPreferences = wrapper.getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("correo", correo);
        editor.putString("base", DB);
        editor.putBoolean("rememberUser",remember);
        editor.apply();
    }

    public static Boolean getFromSharedPreferencesDB(String key,Context context){
        wrapper = new ContextWrapper(context);
        SharedPreferences sharedPreferences = wrapper.getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key,false);
    }
    //-------------------------------------------------------------------------------------------------
    public static void InicioSesionCorrecto(Context context,Activity activity) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(context,i,new Bundle());
        activity.startActivity(i);
        activity.finish();
    }

    public static  void Picasso(Context context,String imgPerfil,CircleImageView circleImageView){
        Picasso.with(context)
                .load(imgPerfil)
                .error(R.drawable.com_facebook_profile_picture_blank_square)
                .fit()
                .centerInside()
                .into(circleImageView);
    }
}
