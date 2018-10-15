package clases_estaticas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.whereismypet.whereismypet.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import misclases.GestionarFacebook;
import misclases.Usuario;

public final class Facebook {

    private static List<String> permisoNecesario = Arrays.asList("email", "user_birthday", "user_friends", "public_profile");

    private static Profile profile;
    private static AccessTokenTracker accessTokenTracker;
    private static ProfileTracker profileTracker;
    private static Usuario user;
    public static CallbackManager callbackManager;



    public static CallbackManager FacebookLogin(final Context context, final Activity activity, LoginButton loginButton) {
        CallbackManager returnCallbackManager = callbackManager;
        try {
            LoginManager.getInstance().logOut();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (GestionarFacebook.comprobarInternet(context)) {
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().logInWithReadPermissions(activity, permisoNecesario);

            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    RelativeLayout containerLogin = activity.findViewById(R.id.ContainerLogin);
                    containerLogin.setVisibility(View.GONE);
                    String send_token = loginResult.getAccessToken().getToken(),
                            send_user = loginResult.getAccessToken().getUserId();
                    GeneralMethods.savedLoginSharedPreferencesFB(send_token, send_user, "FB", context);
                    GeneralMethods.InicioSesionCorrecto(context,activity);

                    GraphRequest request = GraphRequest.newMeRequest(Token(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                   /* try {
                                       user.setEmail(object.getString("email"));
                                        user.setFacebook(object.getString("id"));


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }*/
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,gender,birthday");
                    request.setParameters(parameters);
                    request.executeAsync();
                    accessTokenTracker = new AccessTokenTracker() {
                        @Override
                        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                        }
                    };
                    profileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            DatosPerfil(currentProfile);
                        }
                    };
                    accessTokenTracker.startTracking();
                    profileTracker.startTracking();
                    profile = Profile.getCurrentProfile();

                }

                @Override
                public void onCancel() {
                    Log.e("Login Cancelado", "n" + "login de Facebook cancelado");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.e("Login Error", "n" + "error de inicio de sesión de Facebook" + error.toString());
                }
            });
           returnCallbackManager = callbackManager;
        }
        return returnCallbackManager;
    }

    private static AccessToken Token() {
        return AccessToken.getCurrentAccessToken();
    }

    public static boolean isLoggedIn(Context context) {
        boolean user, expirado, vacio;
        AccessToken accessToken = Token();
        if (accessToken != null) {
            user = (accessToken.getToken().equals(GeneralMethods.getFromSharedPreferences("token", context)) ||
                    accessToken.getUserId().equals(GeneralMethods.getFromSharedPreferences("userID", context)));
            expirado = !accessToken.isExpired();
            vacio = !accessToken.getToken().isEmpty();

            return (user && expirado && vacio);

        }
        return (false);
    }

    private static void DatosPerfil(Profile perfil) {
        user.setNombre(perfil.getFirstName());
        user.setApellido(perfil.getLastName());
        user.setImagenPerfilFacebook(perfil.getProfilePictureUri(400, 400));
    }


}
