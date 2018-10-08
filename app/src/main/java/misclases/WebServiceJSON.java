package misclases;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public final class WebServiceJSON {
    private static ProgressDialog progressDialog;
    private static Usuario responseUser = null;
    private static RequestQueue requestQueue;
    private static Usuario user;
    private static CircleImageView perfil;
    //-----------------------------------URL WEB SERVICE------------------------------------------------
    private static String URL_LOGIN = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Logueo2.php";
    private static String URL_CONSULTAR_USUARIO ="http://www.secsanluis.com.ar/servicios/varios/wimp/W_ConsultarCliente.php";
    private static ContextWrapper wrapper;




    public static Usuario UserLogin(final Usuario user, final Context context) {
        progressDialog  = new ProgressDialog(context);
        progressDialog.setMessage("Por favor, esperar...");
        progressDialog.show();
        Map<String, String> params = new HashMap();
        params.put("User_Email",user.getEmail() );
        params.put("User_Password", user.getContraseña());
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL_LOGIN, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    JSONArray lista = response.getJSONArray("w_usuario");
                    JSONObject json_data = lista.getJSONObject(0);
                    String ServerResponse = json_data.getString("error");
                    if (ServerResponse.equalsIgnoreCase("FAIL")) {
                        Toast.makeText(context, "Correo o Contraseña incorrectos, por favor verifique los datos", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        user.setNombre(json_data.getString("nombre"));
                        user.setApellido(json_data.getString("apellido"));
                        user.setEmail(json_data.getString("correo"));
                        user.setFacebook(json_data.getString("facebook"));
                        user.setImagenPerfilBase(json_data.getString("imagen"));
                        responseUser = user;

                    }
                }catch(JSONException ignored){ }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                //TODO: handle failure
            }
        });
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(context).addToRequestQueue(jsonRequest);
        return responseUser;
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
        //editor.putString("contraseña", contraseña);
        editor.putString("base", DB);
        editor.putBoolean("rememberUser",remember);
        editor.apply();
    }

    public static Boolean getFromSharedPreferencesDB(String key,Context context){
        wrapper = new ContextWrapper(context);
        SharedPreferences sharedPreferences = wrapper.getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key,false);
    }

}
