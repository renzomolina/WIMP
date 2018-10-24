package Modelo;

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



    private String nombre;
    private String descripcion;
    private String foto;
    private String latitud;
    private String longitud;
    private String tipo;
    private String creador;

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setLatitud(String posicion) { this.latitud = posicion; }
    public void setLongitud(String longitud) { this.longitud = longitud; }
    public void setFoto(String foto) { this.foto = foto; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setCreador(String creador) { this.creador = creador; }

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
    public String getFoto() {
        return foto;
    }
    public String getTipo() {
        return tipo;
    }
    public String getCreador() {
        return creador;
    }

    }
