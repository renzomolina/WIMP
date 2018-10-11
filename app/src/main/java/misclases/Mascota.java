package misclases;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.whereismypet.whereismypet.BuildConfig;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Mascota {
    private static final String URL_MARCADOR = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_insertarMarcador.php";
    private static final String URL_SUBIRFOTO = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Upload.php";
    private static final String URL_CARPETAFOTO = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Imagenes/";
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;
    private String nombreFotoServidor;
    private String pathImageWebService;
    private ProgressDialog progressDialog;

    private String nombre;
    private String descripcion;
    private String foto;
    private String latitud;
    private String longitud;
    private String tipo;
    private int id_Marcador;
    private String creador;



    public String getNombre() {
        return nombre;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public String getLatitud() {
        return latitud;
    }
    public String getLongitud() {
        return longitud;
    }
    public int getId_Marcador() {
        return id_Marcador;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public void setLatitud(String posicion) {
        this.latitud = posicion;
    }
    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }
    public void setId_Marcador(int id_Marcador) {
        this.id_Marcador = id_Marcador;
    }
    public String getFoto() {
        return foto;
    }
    public void setFoto(String foto) {
        this.foto = foto;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public String getCreador() {
        return creador;
    }
    public void setCreador(String creador) {
        this.creador = creador;
    }


    public void CheckInPets(final Context context, final String pathX, final int COD_OPCION, boolean withImage){
        if(!withImage){
            setFoto(URL_CARPETAFOTO + "defaultpet.png");
        }
        else
            AndroidUploadService(context,pathX,COD_OPCION);
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_MARCADOR,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {
                        progressDialog.dismiss();
                        if(ServerResponse.equalsIgnoreCase("OK")) {

                            Toast.makeText(context, "Marcador exitoso...", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "Error, por favor, vuelve a intentar.", Toast.LENGTH_SHORT).show();
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
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("nombre",getNombre());
                params.put("descripcion", getDescripcion());
                params.put("latitud",getLatitud());
                params.put("longitud",getLongitud());
                params.put("foto", getFoto());
                params.put("tipo", getTipo());
                params.put("creador", getCreador());

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(context).addToRequestQueue(stringRequest);
    }


    public void AndroidUploadService(final Context context,final String pathX, final int tipoOpcion){
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.NAMESPACE = "com.whereismypet.whereismypet";
        try {
            String extension = pathX.substring(pathX.indexOf('.'),pathX.length());
            String uploadId = UUID.randomUUID().toString();
            nombreFotoServidor = String.valueOf(Calendar.getInstance().getTime()).replace(":","-").replace(" ","") + String.valueOf(Calendar.getInstance().getTimeInMillis());
            new MultipartUploadRequest(context, uploadId, URL_SUBIRFOTO)
                    .addFileToUpload(pathX, "picture")
                    .addParameter("filename", nombreFotoServidor)
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) { }
                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) { }
                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            //ELiminar imagen
                            if (tipoOpcion == COD_FOTO) {
                                //ELiminar imagen
                                File eliminar = new File(pathX);
                                if (eliminar.exists()) {
                                    if (eliminar.delete()) {
                                        System.out.println("archivo eliminado:" + pathX);
                                    } else {
                                        System.out.println("archivo no eliminado" + pathX);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) { }
                    })
                    .startUpload();
            pathImageWebService = URL_CARPETAFOTO + nombreFotoServidor+extension;
            setFoto(pathImageWebService);

        } catch (Exception exc) {
            System.out.println(exc.getMessage()+" "+exc.getLocalizedMessage());
        }
    }






}
