package dz.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

import dz.project.remoteData.RemoteDataManager;

public class AddUserInfoActivity extends AppCompatActivity {

    Intent intent;
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = new Intent();

        action = getIntent().getStringExtra("action"); // "ADD_EMAIL", "ADD_PHONE_NUMBER", "VALIDATE_EMAIL" or "VALIDATE_PHONE_NUMBER"

        switch(action) {
            case "ADD_EMAIL":
                addEmail();
                break;
            case "VALIDATE_EMAIL":
                validateEmail();
                break;

            case "ADD_PHONE_NUMBER":
                addPhoneNum();
                break;
            case "VALIDATE_PHONE_NUMBER":
                validatePhoneNum();
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------
    private void addEmail() {
        setContentView(R.layout.activity_adduserinfo_add_email);

        final TextView email_txt = findViewById(R.id.adduserinfo_add_email_txt);
        final TextView add_email_status_txt = findViewById(R.id.adduserinfo_add_email_status_txt);

        // handle buttons click listeners
        Button skip_btn = findViewById(R.id.adduserinfo_add_email_skip_btn);
        skip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                intent = new Intent();
                setResult(Activity.RESULT_CANCELED, intent);

                finish();
            }
        });
        Button submit_btn = findViewById(R.id.adduserinfo_add_email_submit_btn);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                add_email_status_txt.setText("enregistrement de l'email en cours ...");

                // send request
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST, RemoteDataManager.host + "/API/user/addInfo.php",
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                add_email_status_txt.setText(response);

                                try {
                                    JSONObject jObject = new JSONObject(response);
                                    String error = jObject.getString("error");

                                    switch (error) {
                                        case "SUCCESS":
                                            add_email_status_txt.setText("votre e-mail a bien ete enregistrer");

                                            intent.putExtra("user_email", email_txt.getText().toString());
                                            validateEmail();
//                                            finish();

                                            break;
                                        case "USER_UID_NOT_SET":
                                            add_email_status_txt.setText("vous devez etre connecter pour pouvoir ajouter un email.");
                                            break;
                                        case "USER_UID_NOT_VALID":
                                            add_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_UID_DOES_NOT_EXIST":
                                            add_email_status_txt.setText("votre compte est introuvable, vous devez vous reconecter.");
                                            break;
                                        case "USER_PASSWORD_NOT_SET":
                                            add_email_status_txt.setText("vous devez etre conecter pour ajouter un email.");
                                            break;
                                        case "USER_PASSWORD_NOT_VALID":
                                            add_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_PASSWORD_LENGTH_NOT_VALID":
                                            add_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_WRONG_PASSWORD":
                                            add_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_DATA_NOT_SET":
                                            add_email_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                            break;
                                        case "USER_DATA_INPUT_NOT_SET":
                                            add_email_status_txt.setText("vous devez dabord remplir le champ e-mail.");
                                            break;
                                        case "USER_EMAIL_ALREDY_EXISTS":
                                            add_email_status_txt.setText("cet e-mail est deja enregistrer.");
                                            break;
                                        case "USER_DATA_INPUT_NOT_VALID":
                                            add_email_status_txt.setText("l'E-mail que vous avez entrer n'est pas valide.");
                                            break;
                                        case "USER_DATA_ERROR":
                                            add_email_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                            break;
                                        case "DATABASE_INSERT_ERROR":
                                            add_email_status_txt.setText("nos serveurs sont surcharger actuellement, veuillez reesseyer plus tard.");
                                            break;
                                    }
                                } catch (JSONException e) {
                                    // should never get here
                                    add_email_status_txt.setText("can't read server response :(");
                                }
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                add_email_status_txt.setText("connexion impossible :(");
                            }
                        }
                )

                {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("user_uid", MainActivity.user.getUid());
                        params.put("user_password", MainActivity.user.getPassword());
                        params.put("user_data", "add_email");
                        params.put("user_data_input", email_txt.getText().toString());

                        return params;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(AddUserInfoActivity.this);
                queue.add(postRequest);

            }
        });
    }

    private void validateEmail() {
        setContentView(R.layout.activity_adduserinfo_validate_email);

        sendEmailValidationRequest();

        // handle buttons click listeners
        Button skip_btn = findViewById(R.id.adduserinfo_validate_email_skip_btn);
        skip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                intent.putExtra("user_email_verifyed", "false");
                if (action.equals("ADD_EMAIL")) {
                    setResult(3, intent);
                } else if (action.equals("VALIDATE_EMAIL")) {
                    setResult(5, intent);
                }

                finish();
            }
        });

        // handle buttons click listeners
        Button resend_request_btn = findViewById(R.id.adduserinfo_validate_email_resend_btn);
        resend_request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendEmailValidationRequest();
            }
        });

        Button submit_btn = findViewById(R.id.adduser_validate_email_submit_btn);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                TextView email_pin_1_txt = findViewById(R.id.adduser_validate_email_pin_1_txt);
                TextView email_pin_2_txt = findViewById(R.id.adduser_validate_email_pin_2_txt);
                TextView email_pin_3_txt = findViewById(R.id.adduser_validate_email_pin_3_txt);
                TextView email_pin_4_txt = findViewById(R.id.adduser_validate_email_pin_4_txt);

                final TextView validate_email_status_txt = findViewById(R.id.adduser_validate_email_status_txt);
                validate_email_status_txt.setText("E-mail en cours de validation ...");

                final String pin =
                        email_pin_1_txt.getText().toString() +
                        email_pin_2_txt.getText().toString() +
                        email_pin_3_txt.getText().toString() +
                        email_pin_4_txt.getText().toString();

                // send request
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST, RemoteDataManager.host + "/API/user/addInfo.php",
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                validate_email_status_txt.setText(response);

                                try {
                                    JSONObject jObject = new JSONObject(response);
                                    String error = jObject.getString("error");

                                    switch (error) {
                                        case "SUCCESS":
                                            validate_email_status_txt.setText("votre email a bien ete verifier.");

                                            intent.putExtra("user_email_verifyed", "true");
                                            if (action.equals("ADD_EMAIL")) {
                                                setResult(3, intent);
                                            } else if (action.equals("VALIDATE_EMAIL")) {
                                                setResult(5, intent);
                                            }

                                            finish();

                                            break;
                                        case "USER_UID_NOT_SET":
                                            validate_email_status_txt.setText("vous devez etre connecter pour pouvoir ajouter un email.");
                                            break;
                                        case "USER_UID_NOT_VALID":
                                            validate_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_UID_DOES_NOT_EXIST":
                                            validate_email_status_txt.setText("votre compte est introuvable, vous devez vous reconecter.");
                                            break;
                                        case "USER_PASSWORD_NOT_SET":
                                            validate_email_status_txt.setText("vous devez etre conecter pour ajouter un email.");
                                            break;
                                        case "USER_PASSWORD_NOT_VALID":
                                            validate_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_PASSWORD_LENGTH_NOT_VALID":
                                            validate_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_WRONG_PASSWORD":
                                            validate_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_DATA_NOT_SET":
                                            validate_email_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                            break;
                                        case "USER_DATA_INPUT_NOT_SET":
                                            validate_email_status_txt.setText("vous devez dabord remplir les champs du pin.");
                                            break;
                                        case "USER_EMAIL_NOT_REGISTRED":
                                            validate_email_status_txt.setText("vous devez d'abord eregistrer un e-mail.");
                                            break;
                                        case "USER_PIN_NOT_VALID":
                                            validate_email_status_txt.setText("ce n'est pas le bon pin.");
                                            break;
                                        case "USER_DATA_INPUT_NOT_VALID":
                                            validate_email_status_txt.setText("le pin que vous avez entrer n'est pas valide.");
                                            break;
                                        case "USER_DATA_ERROR":
                                            validate_email_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                            break;
                                        case "DATABASE_INSERT_ERROR":
                                            validate_email_status_txt.setText("nos serveurs sont surcharger actuellement, veuillez reesseyer plus tard.");
                                            break;
                                    }
                                } catch (JSONException e) {
                                    // should never get here
                                    validate_email_status_txt.setText("can't read server response :(");
                                }
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                validate_email_status_txt.setText("connexion impossible :(");
                            }
                        }
                )

                {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("user_uid", MainActivity.user.getUid());
                        params.put("user_password", MainActivity.user.getPassword());
                        params.put("user_data", "validate_email");
                        params.put("user_data_input", pin);

                        return params;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(AddUserInfoActivity.this);
                queue.add(postRequest);
            }
        });
    }

    private void sendEmailValidationRequest() {
        final TextView validate_email_status_txt = findViewById(R.id.adduser_validate_email_status_txt);
        validate_email_status_txt.setText("envoi de l'email en cours...");

        // send request
        StringRequest postRequest = new StringRequest(
                Request.Method.POST, RemoteDataManager.host + "/API/user/addInfo.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        validate_email_status_txt.setText(response);

                        try {
                            JSONObject jObject = new JSONObject(response);
                            String error = jObject.getString("error");

                            switch (error) {
                                case "SUCCESS":
                                    validate_email_status_txt.setText("l'email avec votre pin a ete envoyer avec succes.");

                                    break;
                                case "USER_UID_NOT_SET":
                                    validate_email_status_txt.setText("vous devez etre connecter pour pouvoir verifier un email.");
                                    break;
                                case "USER_UID_NOT_VALID":
                                    validate_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                    break;
                                case "USER_UID_DOES_NOT_EXIST":
                                    validate_email_status_txt.setText("votre compte est introuvable, vous devez vous reconecter.");
                                    break;
                                case "USER_PASSWORD_NOT_SET":
                                    validate_email_status_txt.setText("vous devez etre conecter pour ajouter un email.");
                                    break;
                                case "USER_PASSWORD_NOT_VALID":
                                    validate_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                    break;
                                case "USER_PASSWORD_LENGTH_NOT_VALID":
                                    validate_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                    break;
                                case "USER_WRONG_PASSWORD":
                                    validate_email_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                    break;
                                case "USER_DATA_NOT_SET":
                                    validate_email_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                    break;
                                case "USER_DATA_INPUT_NOT_SET":
                                    validate_email_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                    break;
                                case "USER_EMAIL_NOT_REGISTRED":
                                    validate_email_status_txt.setText("vous devez d'abord eregistrer un e-mail.");
                                    break;
                                case "USER_SEND_PIN_ERROR":
                                    validate_email_status_txt.setText("impossible de vous envoyer le pin, veuillez verifier votre email et reeseyer.");
                                    break;
                                case "USER_DATA_ERROR":
                                    validate_email_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                    break;
                                case "DATABASE_INSERT_ERROR":
                                    validate_email_status_txt.setText("nos serveurs sont surcharger actuellement, veuillez reesseyer plus tard.");
                                    break;
                            }
                        } catch (JSONException e) {
                            // should never get here
                            validate_email_status_txt.setText("can't read server response :(");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        validate_email_status_txt.setText("connexion impossible :(");
                    }
                }
        )

        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_uid", MainActivity.user.getUid());
                params.put("user_password", MainActivity.user.getPassword());
                params.put("user_data", "email_validation_request");
                params.put("user_data_input", "null");

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(AddUserInfoActivity.this);
        queue.add(postRequest);
    }

    // ---------------------------------------------------------------------------------------------
    private void addPhoneNum() {
        setContentView(R.layout.activity_adduserinfo_add_phone_number);

        final TextView phone_txt = findViewById(R.id.adduserinfo_add_phone_number_txt);
        final TextView add_phone_status_txt = findViewById(R.id.adduserinfo_add_phone_number_status_txt);

        // handle buttons click listeners
        Button skip_btn = findViewById(R.id.adduserinfo_add_phone_number_skip_btn);
        skip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                intent = new Intent();
                setResult(Activity.RESULT_CANCELED, intent);

                finish();
            }
        });
        Button submit_btn = findViewById(R.id.adduserinfo_add_phone_number_submit_btn);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                add_phone_status_txt.setText("eregistrement du numero de telephone en cours...");

                // send request
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST, RemoteDataManager.host + "/API/user/addInfo.php",
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                add_phone_status_txt.setText(response);

                                try {
                                    JSONObject jObject = new JSONObject(response);
                                    String error = jObject.getString("error");

                                    switch (error) {
                                        case "SUCCESS":
                                            add_phone_status_txt.setText("votre numero de telephone a bien ete enregistrer.");

                                            intent.putExtra("user_phone_number", phone_txt.getText().toString());
                                            validatePhoneNum();
//                                            finish();

                                            break;
                                        case "USER_UID_NOT_SET":
                                            add_phone_status_txt.setText("vous devez etre connecter pour pouvoir ajouter un numero de telephone.");
                                            break;
                                        case "USER_UID_NOT_VALID":
                                            add_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_UID_DOES_NOT_EXIST":
                                            add_phone_status_txt.setText("votre compte est introuvable, vous devez vous reconecter.");
                                            break;
                                        case "USER_PASSWORD_NOT_SET":
                                            add_phone_status_txt.setText("vous devez etre conecter pour ajouter un noumero de telephone.");
                                            break;
                                        case "USER_PASSWORD_NOT_VALID":
                                            add_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_PASSWORD_LENGTH_NOT_VALID":
                                            add_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_WRONG_PASSWORD":
                                            add_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_DATA_NOT_SET":
                                            add_phone_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                            break;
                                        case "USER_DATA_INPUT_NOT_SET":
                                            add_phone_status_txt.setText("vous devez dabord remplir le champ numero de telephone.");
                                            break;
                                        case "USER_DATA_INPUT_NOT_VALID":
                                            add_phone_status_txt.setText("le numero de telephone que vous avez entrer n'est pas valide.");
                                            break;
                                        case "USER_DATA_ERROR":
                                            add_phone_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                            break;
                                        case "USER_PHONE_ALREDY_EXISTS":
                                            add_phone_status_txt.setText("ce numero de telephone est deja enregistrer.");
                                            break;
                                        case "DATABASE_INSERT_ERROR":
                                            add_phone_status_txt.setText("nos serveurs sont surcharger actuellement, veuillez reesseyer plus tard.");
                                            break;
                                    }
                                } catch (JSONException e) {
                                    // should never get here
                                    add_phone_status_txt.setText("can't read server response :(");
                                }
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                add_phone_status_txt.setText("connexion impossible :(");
                            }
                        }
                )

                {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("user_uid", MainActivity.user.getUid());
                        params.put("user_password", MainActivity.user.getPassword());
                        params.put("user_data", "add_phone_number");
                        params.put("user_data_input", phone_txt.getText().toString());

                        return params;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(AddUserInfoActivity.this);
                queue.add(postRequest);

            }
        });
    }

    private void validatePhoneNum() {
        setContentView(R.layout.activity_adduserinfo_validate_phone_number);

        sendPhoneNumberValidationRequest();

        // handle buttons click listeners
        Button skip_btn = findViewById(R.id.adduserinfo_validate_phone_skip_btn);
        skip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                intent.putExtra("user_email_verifyed", "false");
                if (action.equals("ADD_PHONE_NUMBER")) {
                    setResult(4, intent);
                } else if (action.equals("VALIDATE_PHONE_NUMBER")) {
                    setResult(6, intent);
                }

                finish();
            }
        });

        // handle buttons click listeners
        Button resend_request_btn = findViewById(R.id.adduserinfo_validate_phone_resend_btn);
        resend_request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendPhoneNumberValidationRequest();
            }
        });

        Button submit_btn = findViewById(R.id.adduser_validate_phone_submit_btn);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                TextView phone_pin_1_txt = findViewById(R.id.adduser_validate_phone_pin_1_txt);
                TextView phone_pin_2_txt = findViewById(R.id.adduser_validate_phone_pin_2_txt);
                TextView phone_pin_3_txt = findViewById(R.id.adduser_validate_phone_pin_3_txt);
                TextView phone_pin_4_txt = findViewById(R.id.adduser_validate_phone_pin_4_txt);

                final TextView validate_phone_status_txt = findViewById(R.id.adduser_validate_phone_status_txt);
                validate_phone_status_txt.setText("numero de telephone en cours de validation ...");

                final String pin =
                        phone_pin_1_txt.getText().toString() +
                        phone_pin_2_txt.getText().toString() +
                        phone_pin_3_txt.getText().toString() +
                        phone_pin_4_txt.getText().toString();

                // send request
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST, RemoteDataManager.host + "/API/user/addInfo.php",
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                validate_phone_status_txt.setText(response);

                                try {
                                    JSONObject jObject = new JSONObject(response);
                                    String error = jObject.getString("error");

                                    switch (error) {
                                        case "SUCCESS":
                                            validate_phone_status_txt.setText("votre numero de telephone a bien ete verifier.");

                                            intent.putExtra("user_phone_verifyed", "true");
                                            if (action.equals("ADD_PHONE_NUMBER")) {
                                                setResult(4, intent);
                                            } else if (action.equals("VALIDATE_PHONE_NUMBER")) {
                                                setResult(6, intent);
                                            }

                                            finish();

                                            break;
                                        case "USER_UID_NOT_SET":
                                            validate_phone_status_txt.setText("vous devez etre connecter pour pouvoir verifier un numero de telephone.");
                                            break;
                                        case "USER_UID_NOT_VALID":
                                            validate_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_UID_DOES_NOT_EXIST":
                                            validate_phone_status_txt.setText("votre compte est introuvable, vous devez vous reconecter.");
                                            break;
                                        case "USER_PASSWORD_NOT_SET":
                                            validate_phone_status_txt.setText("vous devez etre conecter pour verifier un numero de telephone.");
                                            break;
                                        case "USER_PASSWORD_NOT_VALID":
                                            validate_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_PASSWORD_LENGTH_NOT_VALID":
                                            validate_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_WRONG_PASSWORD":
                                            validate_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                            break;
                                        case "USER_DATA_NOT_SET":
                                            validate_phone_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                            break;
                                        case "USER_DATA_INPUT_NOT_SET":
                                            validate_phone_status_txt.setText("vous devez dabord remplir les champs du pin.");
                                            break;
                                        case "USER_PHONE_NOT_REGISTRED":
                                            validate_phone_status_txt.setText("vous devez d'abord eregistrer un numero de telephone.");
                                            break;
                                        case "USER_PIN_NOT_VALID":
                                            validate_phone_status_txt.setText("ce n'est pas le bon pin.");
                                            break;
                                        case "USER_DATA_INPUT_NOT_VALID":
                                            validate_phone_status_txt.setText("le pin que vous avez entrer n'est pas valide.");
                                            break;
                                        case "USER_DATA_ERROR":
                                            validate_phone_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                            break;
                                        case "DATABASE_INSERT_ERROR":
                                            validate_phone_status_txt.setText("nos serveurs sont surcharger actuellement, veuillez reesseyer plus tard.");
                                            break;
                                    }
                                } catch (JSONException e) {
                                    // should never get here
                                    validate_phone_status_txt.setText("can't read server response :(");
                                }
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                validate_phone_status_txt.setText("connexion impossible :(");
                            }
                        }
                )

                {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("user_uid", MainActivity.user.getUid());
                        params.put("user_password", MainActivity.user.getPassword());
                        params.put("user_data", "validate_phone_number");
                        params.put("user_data_input", pin);

                        return params;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(AddUserInfoActivity.this);
                queue.add(postRequest);
            }
        });
    }

    private void sendPhoneNumberValidationRequest() {
        final TextView validate_phone_status_txt = findViewById(R.id.adduser_validate_phone_status_txt);
        validate_phone_status_txt.setText("envoi du message en cours...");

        // send request
        StringRequest postRequest = new StringRequest(
                Request.Method.POST, RemoteDataManager.host + "/API/user/addInfo.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        validate_phone_status_txt.setText(response);

                        try {
                            JSONObject jObject = new JSONObject(response);
                            String error = jObject.getString("error");

                            switch (error) {
                                case "SUCCESS":
                                    validate_phone_status_txt.setText("le message avec votre pin a bien ete envoyer.");

                                    break;
                                case "USER_UID_NOT_SET":
                                    validate_phone_status_txt.setText("vous devez etre connecter pour pouvoir verifier votre numero de telephone.");
                                    break;
                                case "USER_UID_NOT_VALID":
                                    validate_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                    break;
                                case "USER_UID_DOES_NOT_EXIST":
                                    validate_phone_status_txt.setText("votre compte est introuvable, vous devez vous reconecter.");
                                    break;
                                case "USER_PASSWORD_NOT_SET":
                                    validate_phone_status_txt.setText("vous devez etre conecter pour verifier un numero de telephone.");
                                    break;
                                case "USER_PASSWORD_NOT_VALID":
                                    validate_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                    break;
                                case "USER_PASSWORD_LENGTH_NOT_VALID":
                                    validate_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                    break;
                                case "USER_WRONG_PASSWORD":
                                    validate_phone_status_txt.setText("une erreur c'est produite, vous devez vous reconecter.");
                                    break;
                                case "USER_DATA_NOT_SET":
                                    validate_phone_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                    break;
                                case "USER_DATA_INPUT_NOT_SET":
                                    validate_phone_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                    break;
                                case "USER_PHONE_NOT_REGISTRED":
                                    validate_phone_status_txt.setText("vous devez d'abord eregistrer un numero de telephone.");
                                    break;
                                case "PHONE_PIN_MESSAGE_COOLDOWN":
                                    String time_to_wait = jObject.getString("time_to_wait");
                                    validate_phone_status_txt.setText(String.format("vous devez attendre %s secondes, avant de redemmander un message de confirmation.", time_to_wait));
                                    break;
                                case "MAX_PHONE_MESSAGES_TODAY_REACHED":
                                    validate_phone_status_txt.setText(String.format("vous avez attaint votre limite de message pour aujourduit veuillez reeseyer demain."));
                                    break;
                                case "USER_SEND_PIN_ERROR":
                                    validate_phone_status_txt.setText("impossible de vous envoyer le pin, veuillez verifier votre numero de telephone et reeseyer.");
                                    break;
                                case "USER_DATA_ERROR":
                                    validate_phone_status_txt.setText("une erreur c'est produite, veuillez reesseyer, metre a jours votre application ou si l'erruer persiste veuillez nous contacter.");
                                    break;
                                case "DATABASE_INSERT_ERROR":
                                    validate_phone_status_txt.setText("nos serveurs sont surcharger actuellement, veuillez reesseyer plus tard.");
                                    break;
                            }
                        } catch (JSONException e) {
                            // should never get here
                            validate_phone_status_txt.setText("can't read server response :(");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        validate_phone_status_txt.setText("connexion impossible :(");
                    }
                }
        )

        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_uid", MainActivity.user.getUid());
                params.put("user_password", MainActivity.user.getPassword());
                params.put("user_data", "phone_number_validation_request");
                params.put("user_data_input", "null");

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(AddUserInfoActivity.this);
        queue.add(postRequest);
    }

}
