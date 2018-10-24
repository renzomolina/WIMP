package finalClass;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.EditText;

import com.whereismypet.whereismypet.R;

import java.util.regex.Pattern;

public final class GeneralMethod {

    //-----------------------------------VALIDACIONES REGEX-------------------------------------------------------------
    private static final String REGEX_LETRAS = "^[a-zA-ZáÁéÉíÍóÓúÚñÑüÜ\\s]+$";
    private static final String REGEX_EMAIL= "^[a-zA-Z0-9\\._-]+@[a-zA-Z0-9]{2,}[.] [a-zA-Z] {2,4}$";


    //-----------------------------------Imagen Circular----------------------------------------------------------------
    public static Bitmap getBitmapClip(Bitmap bitmap) {
        int maxLenth = bitmap.getWidth() <= bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(maxLenth,
                maxLenth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, maxLenth, maxLenth);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(maxLenth / 2, maxLenth / 2,
                maxLenth / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
    //---------------------------------------VALIDACIONES DE COMPONENTES------------------------------------------------
    public static boolean RegexRegistro(String edit, Activity activity) {
        boolean respuestaValidacion = false;
        Drawable msgerror = activity.getResources().getDrawable(R.drawable.icon_error);
        msgerror.setBounds(0, 0, msgerror.getIntrinsicWidth(), msgerror.getIntrinsicHeight());

        final EditText etNombre = activity.findViewById(R.id.nombreRegistro),
                        etApellido = activity.findViewById(R.id.apellidoRegistro),
                        etCorreo = activity.findViewById(R.id.emailRegistro),
                        etContrasena = activity.findViewById(R.id.passRegistro),
                        etConfimarContrasena = activity.findViewById(R.id.confirmpassRegistro);

        switch (edit) {
            case "nombre": {
                Pattern p = Pattern.compile(REGEX_LETRAS);
                if (!p.matcher(etNombre.getText().toString()).matches()) {
                    etNombre.setError("Este campo permite solo letras", msgerror);
                } else {
                    etNombre.setError(null);
                    respuestaValidacion = true;

                }
            }break;
            case "apellido": {
                Pattern p = Pattern.compile(REGEX_LETRAS);
                if (!p.matcher(etApellido.getText().toString()).matches()) {
                    etApellido.setError("Este campo permite solo letras", msgerror);
                    return false;
                } else {
                    etApellido.setError(null);
                    respuestaValidacion = true;
                }
            }break;
            case "email": {

                Pattern p = Pattern.compile(REGEX_EMAIL);
                if (!p.matcher(etCorreo.getText().toString()).matches()) {
                    etCorreo.setError("Correo Invalido", msgerror);
                } else {
                    etCorreo.setError(null);
                    respuestaValidacion = true;                }
            }break;

            case "nombrevacio": {
                if (CheckEditTextIsEmptyOrNot(etNombre)) {
                    etNombre.setError("Campo Vacio", msgerror);
                } else {
                    respuestaValidacion = true;
                }
            }break;

            case "apellidovacio": {

                if (CheckEditTextIsEmptyOrNot(etApellido)) {
                    etApellido.setError("Campo Vacio", msgerror);
                } else {
                    respuestaValidacion = true;
                }
            }break;

            case "correovacio": {

                if (CheckEditTextIsEmptyOrNot(etCorreo)) {
                    etCorreo.setError("Campo Vacio", msgerror);
                } else {
                    respuestaValidacion = true;
                }

            }break;

            case "contraseñavacio": {

                if (CheckEditTextIsEmptyOrNot(etContrasena)) {
                    etContrasena.setError("Campo Vacio", msgerror);
                } else {
                    respuestaValidacion = true;
                }
            }break;

            case "confirmacontraseñavacio": {
                if (CheckEditTextIsEmptyOrNot(etConfimarContrasena)) {
                    etConfimarContrasena.setError("Campo Vacio", msgerror);
                } else {
                    respuestaValidacion = true;

                }
            }break;

            case "confirmacontraseña": {
                if (etContrasena.getText().toString().equals(etConfimarContrasena.getText().toString())) {
                    respuestaValidacion = true;
                } else {
                    etConfimarContrasena.setError("Debe coincidir con Contraseña", msgerror);
                }
            }break;
        }
        return respuestaValidacion;
    }

    public static boolean RegexLogin(String edit, Activity activity) {
        boolean respuestaValidacion = false;
        Drawable msgerror = activity.getResources().getDrawable(R.drawable.icon_error);
        msgerror.setBounds(0, 0, msgerror.getIntrinsicWidth(), msgerror.getIntrinsicHeight());
        final EditText etCorreoLogin = activity.findViewById(R.id.CorreoLogin),
                        etContrasenaLogin = activity.findViewById(R.id.PasswordLogin);
        switch (edit) {
            case "correo": {
                Pattern p = Pattern.compile(REGEX_EMAIL);
                if (!p.matcher(etCorreoLogin.getText().toString()).matches()) {
                    etCorreoLogin.setError("Correo Invalido", msgerror);
                } else {
                    etCorreoLogin.setError(null);
                    respuestaValidacion = true;
                }
            }break;

            case "correovacio": {

                if (CheckEditTextIsEmptyOrNot(etCorreoLogin)) {
                    etCorreoLogin.setError("Campo Vacio", msgerror);
                } else {
                    respuestaValidacion = true;
                }

            }break;

            case "contrasenavacio": {

                if (CheckEditTextIsEmptyOrNot(etContrasenaLogin)) {
                    etContrasenaLogin.setError("Campo Vacio", msgerror);
                } else {
                    respuestaValidacion = true;
                }
            }break;
        }
        return respuestaValidacion;
    }

    private static boolean CheckEditTextIsEmptyOrNot(EditText editText){
        return (TextUtils.isEmpty(editText.getText().toString().trim()));
    }


}
