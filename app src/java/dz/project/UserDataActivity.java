package dz.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dz.project.localData.querys.User;

import static dz.project.remoteData.RemoteDataManager.host;

public class UserDataActivity extends AppCompatActivity {

    private static User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = new User();

        setContentView(R.layout.activity_user_data);

        // handle buttons click listeners
        Button livreur_btn = findViewById(R.id.livreur_btn);
        livreur_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // start user_data_livreur layout
                showUserDataLivreurLayout();
            }
        });

        Button client_btn = findViewById(R.id.client_btn);
        client_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // start user_data_client layout
                showUserDataClientLayout();
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    private void showUserDataClientLayout() {
        setContentView(R.layout.activity_user_data_client);

        Button userDataClientHasAccount_btn = findViewById(R.id.userDataClientHasAccount_btn);
        userDataClientHasAccount_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                showUserDataClientSignInLayout();
            }
        });

        Button userDataClientNoAccount_btn = findViewById(R.id.userDataClientNoAccount_btn);
        userDataClientNoAccount_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                showUserDataClientRegisterLayout();
            }
        });
    }

    private void showUserDataClientSignInLayout() {
        setContentView(R.layout.activity_user_data_client_signin);

        final TextView user_name_email_txt = findViewById(R.id.user_data_client_signin_user_name_email_txt);
        final TextView user_password_txt = findViewById(R.id.user_data_client_signin_user_password_txt);
        final Button user_submit_btn = findViewById(R.id.user_data_client_signin_submit_btn);

        final TextView status_txt = findViewById(R.id.user_data_client_signin_status_txt);

        user_submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                status_txt.setText("Connexion en cours...");

                // send request
                StringRequest postRequest = new StringRequest(
                    Request.Method.POST, host + "/API/user/getUID.php",
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // status_txt.setText(response);

                            try {
                                JSONObject jObject = new JSONObject(response);
                                String error = jObject.getString("error");

                                switch (error) {
                                    case "SUCCESS":
                                        user.setUid(jObject.getString("UID"));
                                        user.setPassword(user_password_txt.getText().toString());
                                        user.setStatus("client");

                                        // insert user to local db
                                        User.insertUser(user);

                                        // set main activity user
                                        MainActivity.user = user;

                                        Intent intent=new Intent();
                                        intent.putExtra("user_uid", user.getUid());
                                        intent.putExtra("user_password", user.getPassword());
                                        intent.putExtra("user_status", user.getStatus());
                                        setResult(1, intent);

                                        //status_txt.setText(String.format("User (%s) Connected.", user.getUid()));

                                        finish();
                                        break;
                                    case "USER_NAME_EMAIL_NOT_SET":
                                        status_txt.setText("vous devez dabord remplir le champ nom dutilisateur/email.");
                                        break;
                                    case "USER_NAME_NOT_VALID":
                                        status_txt.setText("ce nom d'utilisateur est invalide.");
                                        break;
                                    case "USER_NAME_LENGTH_NOT_VALID":
                                        status_txt.setText("ce nom d'utilisateur est invalide.");
                                        break;
                                    case "CONNECTION_METHODE_ERROR":
                                        status_txt.setText("votre compte est erroner veuiller nous contacter.");
                                        break;
                                    case "USER_EMAIL_DOES_NOT_EXIST":
                                        status_txt.setText("l'email que vous avez entrer n'existe pas.");
                                        break;
                                    case "USER_NAME_DOES_NOT_EXIST":
                                        status_txt.setText("le nom d'utilisateur que vous avez entrer n'existe pas.");
                                        break;
                                    case "USER_PASSWORD_NOT_SET":
                                        status_txt.setText("vous devez dabord remplir le champ mot de passe.");
                                        break;
                                    case "USER_PASSWORD_NOT_VALID":
                                        status_txt.setText("le mot de passe que vous avez entrer n'est pas valid.");
                                        break;
                                    case "USER_PASSWORD_LENGTH_NOT_VALID":
                                        status_txt.setText("le mot de passe que vous avez entrer n'est pas valid.");
                                        break;
                                    case "USER_WRONG_PASSWORD":
                                        status_txt.setText("mot de passe incorrecte.");
                                        break;
                                }
                            } catch (JSONException e) {
                                // should never get here
                                status_txt.setText("can't read server response :(");
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            status_txt.setText("connexion impossible :(");
                        }
                    }
                )

                {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("user_name_email", user_name_email_txt.getText().toString());
                        params.put("user_password", user_password_txt.getText().toString());

                        return params;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(UserDataActivity.this);
                queue.add(postRequest);
            }
        });
    }

    private void showUserDataClientRegisterLayout() {
        setContentView(R.layout.activity_user_data_client_register);

        final TextView register_user_name_txt = findViewById(R.id.user_data_client_register_user_name_txt);
        final TextView register_password_txt = findViewById(R.id.user_data_register_user_password_txt);
        final TextView register_password_verif_txt = findViewById(R.id.user_data_register_user_password_verif_txt);
        final Button submit_btn = findViewById(R.id.user_data_client_register_submit_btn);

        final TextView status_txt = findViewById(R.id.user_data_client_register_status_txt);

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (!register_password_txt.getText().toString().equals(register_password_verif_txt.getText().toString())) {
                    status_txt.setText("les mot de passe ne sont pas pareil.");
                } else {
                    status_txt.setText("Verification de la requete...");

                    // send request
                    StringRequest postRequest = new StringRequest(
                        Request.Method.POST, host + "/API/user/register.php",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // status_txt.setText(response);

                                try {
                                    JSONObject jObject = new JSONObject(response);
                                    String error = jObject.getString("error");

                                    switch (error) {
                                        case "SUCCESS":
                                            user.setUid(jObject.getString("UID"));
                                            user.setPassword(register_password_txt.getText().toString());
                                            user.setStatus("client");

                                            // insert user to local db
                                            User.insertUser(user);

                                            // set main activity user
                                            MainActivity.user = user;

                                            Intent intent = new Intent();
                                            intent.putExtra("user_uid", user.getUid());
                                            intent.putExtra("user_password", user.getPassword());
                                            intent.putExtra("user_status", user.getStatus());
                                            setResult(1, intent);

                                            //status_txt.setText(String.format("User (%s) Connected.", user.getUid()));

                                            finish();
                                            break;
                                        case "USER_NAME_NOT_SET":
                                            status_txt.setText("vous devez dabord remplir le champ nom dutilisateur.");
                                            break;
                                        case "USER_NAME_NOT_VALID":
                                            status_txt.setText("ce nom d'utilisateur est invalide.");
                                            break;
                                        case "USER_NAME_LENGTH_NOT_VALID":
                                            status_txt.setText("ce nom d'utilisateur est invalide.");
                                            break;
                                        case "USER_NAME_ALREDY_EXISTS":
                                            status_txt.setText("ce nom d'utilisateur existe deja.");
                                            break;
                                        case "USER_PASSWORD_NOT_SET":
                                            status_txt.setText("vous devez dabord remplir le champ mot de passe.");
                                            break;
                                        case "USER_PASSWORD_NOT_VALID":
                                            status_txt.setText("le mot de passe que vous avez entrer n'est pas valid.");
                                            break;
                                        case "USER_PASSWORD_LENGTH_NOT_VALID":
                                            status_txt.setText("le mot de passe que vous avez entrer n'est pas valid.");
                                            break;
                                    }
                                } catch (JSONException e) {
                                    // should never get here
                                    status_txt.setText("can't read server response :(");
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                status_txt.setText("connexion impossible :(");
                            }
                        }
                    ) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("user_name", register_user_name_txt.getText().toString());
                            params.put("user_password", register_password_txt.getText().toString());

                            return params;
                        }
                    };

                    RequestQueue queue = Volley.newRequestQueue(UserDataActivity.this);
                    queue.add(postRequest);
                }
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    private void showUserDataLivreurLayout() {
        setContentView(R.layout.activity_user_data_livreur);

        Button userDataLivreurVerify_btn = findViewById(R.id.userDataLivreurVerify_btn);
        userDataLivreurVerify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                EditText userDataLivreurUID_txtInput = findViewById(R.id.userDataLivreurUID_txtInput);
                verifyLivreurId(userDataLivreurUID_txtInput.getText().toString());
            }
        });

        Button userDataLivreurCancel_btn = findViewById(R.id.userDataLivreurCancel_btn);
        userDataLivreurCancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                Intent intent=new Intent();
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        });
    }

    private void verifyLivreurId(String uid) {
        // TODO
        Toast.makeText(this, "UID: " + uid + " | en cours de developpement...", Toast.LENGTH_LONG).show();
    }

}
