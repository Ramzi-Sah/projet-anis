package dz.project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dz.project.commandes.Article;
import dz.project.commandes.Commande;
import dz.project.common.PagerFragmentAdapter;
import dz.project.livreurs.Livreur;
import dz.project.remoteData.RemoteDataManager;

public class ClientActivity extends AppCompatActivity implements
        ClientActivity_ProfileFragment.ClientActivity_ProfileFragment_Listner,
        ClientActivity_LivreursFragment.ClientActivity_LivreursFragment_Listner,
        ClientActivity_CommandesFragment.ClientActivity_CommandesFragment_Listner {

    private static String client_user_name = "";
    private static String client_email = "NO_EMAIL";
    private static boolean client_email_verifyed = false;
    private static String client_phone = "NO_PHONE_NUMBER";
    private static boolean client_phone_verifyed = false;

    public static ArrayList<Livreur> client_livreurs;
    private static String client_livreurs_status = "";

    public static ArrayList<Commande> client_commandes;
    private static String client_commande_status = "its working !!";

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PagerFragmentAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // recover client guid and connect
        connect();
    }

    private void initMainClientLayout() {
        setContentView(R.layout.activity_client_main);

        // init tab layout
        tabLayout = findViewById(R.id.client_main_tab_layout);
        viewPager = findViewById(R.id.client_main_pager);
        tabLayout.addTab(tabLayout.newTab().setText("Profile"));
        tabLayout.addTab(tabLayout.newTab().setText("Livreurs"));
        tabLayout.addTab(tabLayout.newTab().setText("Commandes"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        adapter = new PagerFragmentAdapter(this, getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // handle client livreurs
        client_livreurs = new ArrayList<Livreur>();
        recoverLivreursList();

        // handle client commandes
        client_commandes = new ArrayList<Commande>();
        recoverCommandesList();
    }

    private void updateMainClientLayout() {
        handleProfileFragment();
        handleLivreursFragment();
        handleCommandesFragment();
    }

    //----------------------------------------------------------------------------------------------
    private void handleProfileFragment() {
        ClientActivity_ProfileFragment profileFragment = adapter.profileFragment;

        profileFragment.setUserName(client_user_name);
        profileFragment.setEmail(client_email, client_email_verifyed);
        profileFragment.setPhoneNumber(client_phone, client_phone_verifyed);
    }

    @Override
    public void ClientActivity_ProfileFragment_onInputSent(String input) {
        Intent intent = new Intent(ClientActivity.this, AddUserInfoActivity.class);
        switch (input) {
            case "SETUP":
                handleProfileFragment();
                break;
            case "DISCONNECT":
                disconnect();
                break;
            case "ADD_EMAIL":
                intent.putExtra("action", "ADD_EMAIL");
                startActivityForResult(intent, 3);
                handleProfileFragment();
                break;
            case "VERIFY_EMAIL":
                intent.putExtra("action", "VALIDATE_EMAIL");
                startActivityForResult(intent, 3);
                break;
            case "ADD_PHONE_NUMBER":
                intent.putExtra("action", "ADD_PHONE_NUMBER");
                startActivityForResult(intent, 4);
                break;
            case "VERIFY_PHONE_NUMBER":
                intent.putExtra("action", "VALIDATE_PHONE_NUMBER");
                startActivityForResult(intent, 4);
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    private void handleLivreursFragment() {
        ClientActivity_LivreursFragment livreursFragment = adapter.livreursFragment; // FIXME: could throw nullObject, should verify if its set up
        if (livreursFragment == null) {
            return;
        }

        livreursFragment.setText(client_livreurs_status);

        livreursFragment.initLivreursList(this);
        livreursFragment.setLivreurList(client_livreurs);
    }

    @Override
    public void ClientActivity_LivreursFragment_onInputSent(String input) {
        Intent intent = new Intent(ClientActivity.this, AddUserInfoActivity.class);
        switch (input) {
            case "SETUP":
                handleLivreursFragment();
                break;
            case "ADD_LIVREUR":
                addLivreur();
                break;
        }

        String[] input_data = input.split(":");
        if (input_data[0].equals("REMOVE_LIVREUR")) {
            removeLivreur(input_data[1]);
        }
    }

    private void addLivreur() {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_client_add_livreur, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog addLivreurDialog = builder.create();
        addLivreurDialog.show();

        final EditText livreur_uid_editTxt= dialogView.findViewById(R.id.addlivreur_dialog_livreur_uid_editTxt);
        final TextView status_txt= dialogView.findViewById(R.id.addlivreur_dialog_status_txt);
        Button submi_btn= dialogView.findViewById(R.id.addlivreur_dialog_submit_btn);
        submi_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String uid_livreur = livreur_uid_editTxt.getText().toString();
                status_txt.setText("Verification de l'uid...");

                // send request
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST, RemoteDataManager.host + "/API/client/addData.php",
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                status_txt.setText(response);

                                try {
                                    JSONObject jObject = new JSONObject(response);
                                    String error = jObject.getString("error");

                                    switch (error) {
                                        case "SUCCESS":
                                            status_txt.setText("le livreur a ete ajouter avec success.");
                                            addLivreurDialog.cancel();

                                            recoverLivreursList();

                                            break;
                                        case "USER_UID_NOT_SET":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_UID_NOT_VALID":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_UID_DOES_NOT_EXIST":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_PASSWORD_NOT_SET":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_PASSWORD_NOT_VALID":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_PASSWORD_LENGTH_NOT_VALID":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_WRONG_PASSWORD":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_DATA_ERROR":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_DATA_NOT_SET":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;

                                        case "USER_DATA_INPUT_NOT_SET":
                                            status_txt.setText("vous devez dabord remplir le champ uid livreur.");
                                            break;
                                        case "USER_DATA_INPUT_NOT_VALID":
                                            status_txt.setText("l'uid livreur que vous avez entrer n'est pas valide.");
                                            break;

                                        case "LIVREUR_UID_DOES_NOT_EXIST":
                                            status_txt.setText("cet uid livreur nexiste pas.");
                                            break;
                                        case "LIVREUR_UID_ALREDY_REGISTRED":
                                            status_txt.setText("vous avez deja enregistrer ce livreur.");
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
                        params.put("user_uid", MainActivity.user.getUid());
                        params.put("user_password", MainActivity.user.getPassword());
                        params.put("user_data", "add_client_livreurs");
                        params.put("user_data_input", livreur_uid_editTxt.getText().toString());

                        return params;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(ClientActivity.this);
                queue.add(postRequest);
            }
        });
    }

    private void removeLivreur(String livreur_uid) {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_client_remove_livreur, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog removeLivreurDialog = builder.create();
        removeLivreurDialog.show();

        final TextView status_txt= dialogView.findViewById(R.id.removelivreur_dialog_status_btn);
        final String livreur_uid_verifyed = livreur_uid;

        Button cancel_btn = dialogView.findViewById(R.id.removelivreur_dialog_no_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                removeLivreurDialog.cancel();
            }
        });

        Button validate_btn= dialogView.findViewById(R.id.removelivreur_dialog_yes_btn);
        validate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                status_txt.setText("Verification de l'uid...");

                // send request
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST, RemoteDataManager.host + "/API/client/addData.php",
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                status_txt.setText(response);

                                try {
                                    JSONObject jObject = new JSONObject(response);
                                    String error = jObject.getString("error");

                                    switch (error) {
                                        case "SUCCESS":
                                            status_txt.setText("le livreur a ete retirer avec success.");
                                            removeLivreurDialog.cancel();

                                            recoverLivreursList();

                                            break;
                                        case "USER_UID_NOT_SET":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_UID_NOT_VALID":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_UID_DOES_NOT_EXIST":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_PASSWORD_NOT_SET":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_PASSWORD_NOT_VALID":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_PASSWORD_LENGTH_NOT_VALID":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_WRONG_PASSWORD":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_DATA_ERROR":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;
                                        case "USER_DATA_NOT_SET":
                                            status_txt.setText("vous devez vous reconnecter.");
                                            break;

                                        case "USER_DATA_INPUT_NOT_SET":
                                            status_txt.setText("vous devez dabord remplir le champ uid livreur.");
                                            break;
                                        case "USER_DATA_INPUT_NOT_VALID":
                                            status_txt.setText("l'uid livreur que vous avez entrer n'est pas valide.");
                                            break;

                                        case "LIVREUR_UID_NOT_REGISTRED":
                                            status_txt.setText("cet uid livreur nexiste pas.");
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
                        params.put("user_uid", MainActivity.user.getUid());
                        params.put("user_password", MainActivity.user.getPassword());
                        params.put("user_data", "remove_client_livreurs");
                        params.put("user_data_input", livreur_uid_verifyed);

                        return params;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(ClientActivity.this);
                queue.add(postRequest);
            }
        });
    }

    private void recoverLivreursList() {
        // send request
        StringRequest postRequest = new StringRequest(
            Request.Method.POST, RemoteDataManager.host + "/API/client/getData.php",
            new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response) {
                    client_livreurs_status = response;

                    try {
                        JSONObject jObject = new JSONObject(response);
                        String error = jObject.getString("error");

                        switch (error) {
                            case "SUCCESS":
                                client_livreurs_status = "";
//                                    client_livreurs_status = jObject.getString("client_livreurs");

                                String array_data_unparsed = jObject.getString("client_livreurs");
                                array_data_unparsed = array_data_unparsed
                                        .replace("[", "")
                                        .replace("]", "")
                                        .replace("\"", "");
                                String[] split = array_data_unparsed.split(",");
                                List<String> livreurUIDsList = new ArrayList<String>();
                                if (!split[0].equals("")) {
                                    livreurUIDsList = Arrays.asList(split);
                                }

                                client_livreurs.clear();
                                for (int i = 0; i < livreurUIDsList.size(); i++) {
                                    client_livreurs.add(new Livreur(livreurUIDsList.get(i)));
                                }

                                if (client_livreurs.size() == 0) {
                                    client_livreurs_status = "aucun livreur enregistrer.";
                                    handleLivreursFragment();
                                }

                                recoverLivreursData();

                                return;
                            case "USER_UID_NOT_SET":
                                client_livreurs_status = "vous devez vous reconnecter.";
                                break;
                            case "USER_UID_NOT_VALID":
                                client_livreurs_status = "vous devez vous reconnecter.";
                                break;
                            case "USER_UID_DOES_NOT_EXIST":
                                client_livreurs_status = "vous devez vous reconnecter.";
                                break;
                            case "USER_PASSWORD_NOT_SET":
                                client_livreurs_status = "vous devez vous reconnecter.";
                                break;
                            case "USER_PASSWORD_NOT_VALID":
                                client_livreurs_status = "vous devez vous reconnecter.";
                                break;
                            case "USER_PASSWORD_LENGTH_NOT_VALID":
                                client_livreurs_status = "vous devez vous reconnecter.";
                                break;
                            case "USER_WRONG_PASSWORD":
                                client_livreurs_status = "vous devez vous reconnecter.";
                                break;
                            case "USER_DATA_ERROR":
                                client_livreurs_status = "vous devez vous reconnecter.";
                                break;
                            case "USER_DATA_INPUT_NOT_SET":
                                client_livreurs_status = "une erreur c'est produite, vous devez metre a jours votre application.";
                                break;
                            case "USER_DATA_NOT_SET":
                                client_livreurs_status = "vous devez vous reconnecter.";
                                break;
                        }
                    } catch (JSONException e) {
                        // should never get here
                        client_livreurs_status = "can't read server response :(";
                    }

                    // update layout
                    handleLivreursFragment();
                }
            },
            new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error) {
                    client_livreurs_status = "connexion impossible :(";

                    // update layout
                    handleLivreursFragment();
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
                params.put("user_data", "client_livreurs");
                params.put("user_data_input", "null");

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(ClientActivity.this);
        queue.add(postRequest);
    }

    private void recoverLivreursData() {
        for (int i = 0; i < client_livreurs.size(); i++) {
            final int livreur_index = i;

            // send request
            StringRequest postRequest = new StringRequest(
                    Request.Method.POST, RemoteDataManager.host + "/API/client/getData.php",
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            client_livreurs_status = response;

                            try {
                                JSONObject jObject = new JSONObject(response);
                                String error = jObject.getString("error");

                                switch (error) {
                                    case "SUCCESS":
                                        client_livreurs_status = "";

                                        String livreur_uid = jObject.getString("uid");
                                        String livreur_user_name = jObject.getString("user_name");
                                        String livreur_user_email = jObject.getString("user_email");
                                        String livreur_user_phone = jObject.getString("user_phone");
                                        String livreur_status = jObject.getString("livreur_status");

                                        client_livreurs.get(livreur_index).setUid(livreur_uid);
                                        client_livreurs.get(livreur_index).setUser_name(livreur_user_name);
                                        client_livreurs.get(livreur_index).setUser_email(livreur_user_email);
                                        client_livreurs.get(livreur_index).setUser_phone(livreur_user_phone);
                                        client_livreurs.get(livreur_index).setStatus(livreur_status);

                                        handleLivreursFragment();

                                        break;
                                    case "USER_UID_NOT_SET":
                                        client_livreurs_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_UID_NOT_VALID":
                                        client_livreurs_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_UID_DOES_NOT_EXIST":
                                        client_livreurs_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_PASSWORD_NOT_SET":
                                        client_livreurs_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_PASSWORD_NOT_VALID":
                                        client_livreurs_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_PASSWORD_LENGTH_NOT_VALID":
                                        client_livreurs_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_WRONG_PASSWORD":
                                        client_livreurs_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_DATA_ERROR":
                                        client_livreurs_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_DATA_INPUT_NOT_SET":
                                        client_livreurs_status = "une erreur c'est produite, vous devez metre a jours votre application.";
                                        break;
                                    case "USER_DATA_NOT_SET":
                                        client_livreurs_status = "vous devez vous reconnecter.";
                                        break;
                                }
                            } catch (JSONException e) {
                                // should never get here
                                client_livreurs_status = "can't read server response :(";
                            }

                            // update layout
                            handleLivreursFragment();
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            client_livreurs_status = "connexion impossible :(";

                            // update layout
                            handleLivreursFragment();
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
                    params.put("user_data", "livreur_user_info");
                    params.put("user_data_input", client_livreurs.get(livreur_index).getLivreur_uid());

                    return params;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(ClientActivity.this);
            queue.add(postRequest);
        }
    }


    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    private void handleCommandesFragment() {
        ClientActivity_CommandesFragment commandesFragment = adapter.commandesFragment; // FIXME: could throw nullObject, should verify if its set up
        if (commandesFragment == null) {
            return;
        }

        commandesFragment.setStatusText(client_commande_status);

//        client_commandes.clear();
//        client_commandes.add(new Commande("commande 1"));
//        client_commandes.add(new Commande("commande 2"));
//        client_commandes.add(new Commande("commande 3"));

        commandesFragment.initCommandesList(this);
        commandesFragment.setCommandesList(client_commandes);
    }

    @Override
    public void ClientActivity_CommandesFragment_onInputSent(String input, String data) {
        Intent intent = new Intent(ClientActivity.this, AddUserInfoActivity.class);
        switch (input) {
            case "SETUP":
                handleCommandesFragment();
                break;
            case "COMMANDE_INFO":
                openCommandeInfoPanel(data);
                break;
            case "ADD_COMMANDE":
                createCommande();
                break;
        }
    }

    private void openCommandeInfoPanel(String commandeUid) {
        // start user Data activity
        Intent commandeInfoActivity = new Intent(ClientActivity.this, ClientActivity_Commande_Info_Panel.class);
        commandeInfoActivity.putExtra("commande_uid", commandeUid);
        startActivityForResult(commandeInfoActivity, 1);
    };

    private void removeCommande(String commande_uid) {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_client_remove_commande, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog removeCommandeDialog = builder.create();
        removeCommandeDialog.show();

        final TextView status_txt= dialogView.findViewById(R.id.removeCommande_dialog_status_btn);
        final String commande_uid_verifyed = commande_uid;

        Button cancel_btn = dialogView.findViewById(R.id.removeCommande_dialog_no_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                removeCommandeDialog.cancel();
            }
        });

        Button validate_btn= dialogView.findViewById(R.id.removeCommande_dialog_yes_btn);
        validate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status_txt.setText("Verification de la commande " + commande_uid_verifyed + " ...");
                // send request
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST, RemoteDataManager.host + "/API/client/commandes.php",
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                String status_response = response;

                                try {
                                    JSONObject jObject = new JSONObject(response);
                                    String error = jObject.getString("error");

                                    switch (error) {
                                        case "SUCCESS":
                                            status_response = "la commande " + commande_uid_verifyed + " a été annulée.";
                                            recoverCommandesList();
                                            break;
                                        case "USER_UID_NOT_SET":
                                            status_response = "vous devez vous reconnecter.";
                                            break;
                                        case "USER_UID_NOT_VALID":
                                            status_response = "vous devez vous reconnecter.";
                                            break;
                                        case "USER_UID_DOES_NOT_EXIST":
                                            status_response = "vous devez vous reconnecter.";
                                            break;
                                        case "USER_PASSWORD_NOT_SET":
                                            status_response = "vous devez vous reconnecter.";
                                            break;
                                        case "USER_PASSWORD_NOT_VALID":
                                            status_response = "vous devez vous reconnecter.";
                                            break;
                                        case "USER_PASSWORD_LENGTH_NOT_VALID":
                                            status_response = "vous devez vous reconnecter.";
                                            break;
                                        case "USER_WRONG_PASSWORD":
                                            status_response = "vous devez vous reconnecter.";
                                            break;
                                        case "COMMANDE_UID_DOES_NOT_EXIST":
                                            status_response = "cette commande n'existe pas.";
                                            break;
                                        case "NOT_YOUR_COMMANDE":
                                            status_response = "cette commande n'est pas la votre.";
                                            break;
                                        case "DATABASE_INSERT_ERROR":
                                            status_response = "impossible de se connecter au serveur.";
                                            break;
                                    }
                                } catch (JSONException e) {
                                    // should never get here
                                    status_response = "can't read server response :(";
                                }

                                // display status
                                Toast.makeText(getApplicationContext(), status_response, Toast.LENGTH_SHORT).show();

                                // close dialog;
                                removeCommandeDialog.cancel();
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // display status
                                Toast.makeText(getApplicationContext(), "connexion impossible :(", Toast.LENGTH_SHORT).show();

                                // update layout
                                handleCommandesFragment();
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
                        params.put("user_data", "cancel_commande");
                        params.put("commande_uid", commande_uid_verifyed);

                        return params;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(ClientActivity.this);
                queue.add(postRequest);
            }
        });
    }

    private void confirmCommande(String commande_uid) {
        // TODO: confirm commande

    }

    private void createCommande() {
        // send request
        StringRequest postRequest = new StringRequest(
                Request.Method.POST, RemoteDataManager.host + "/API/client/commandes.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        String status_response = response;

                        try {
                            JSONObject jObject = new JSONObject(response);
                            String error = jObject.getString("error");

                            switch (error) {
                                case "SUCCESS":
                                    String commande_uid = jObject.getString("commande_uid");
                                    status_response = "la commande " + commande_uid + " a été crée.";
                                    recoverCommandesList();
                                    break;
                                case "USER_UID_NOT_SET":
                                    status_response = "vous devez vous reconnecter.";
                                    break;
                                case "USER_UID_NOT_VALID":
                                    status_response = "vous devez vous reconnecter.";
                                    break;
                                case "USER_UID_DOES_NOT_EXIST":
                                    status_response = "vous devez vous reconnecter.";
                                    break;
                                case "USER_PASSWORD_NOT_SET":
                                    status_response = "vous devez vous reconnecter.";
                                    break;
                                case "USER_PASSWORD_NOT_VALID":
                                    status_response = "vous devez vous reconnecter.";
                                    break;
                                case "USER_PASSWORD_LENGTH_NOT_VALID":
                                    status_response = "vous devez vous reconnecter.";
                                    break;
                                case "USER_WRONG_PASSWORD":
                                    status_response = "vous devez vous reconnecter.";
                                    break;
                                case "DATABASE_INSERT_ERROR":
                                    status_response = "impossible de se connecter au serveur.";
                                    break;
                            }
                        } catch (JSONException e) {
                            // should never get here
                            status_response = "can't read server response :(";
                        }

                        // display status
                        Toast.makeText(getApplicationContext(), status_response, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // display status
                        Toast.makeText(getApplicationContext(), "connexion impossible :(", Toast.LENGTH_SHORT).show();

                        // update layout
                        handleCommandesFragment();
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
                params.put("user_data", "add_commande");

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(ClientActivity.this);
        queue.add(postRequest);
    }

    private void recoverCommandesList() {
        // send request
        StringRequest postRequest = new StringRequest(
                Request.Method.POST, RemoteDataManager.host + "/API/client/commandes.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        client_commande_status = response;

                        try {
                            JSONObject jObject = new JSONObject(response);
                            String error = jObject.getString("error");

                            switch (error) {
                                case "SUCCESS":
                                    client_commande_status = "";
//                                    client_commande_status = jObject.getString("client_commandes_uids");

                                    String array_data_unparsed = jObject.getString("client_commandes_uids");
                                    array_data_unparsed = array_data_unparsed
                                            .replace("[", "")
                                            .replace("]", "")
                                            .replace("\"", "");
                                    String[] split = array_data_unparsed.split(",");
                                    List<String> commandeUIDsList = new ArrayList<String>();
                                    if (!split[0].equals("")) {
                                        commandeUIDsList = Arrays.asList(split);
                                    }

                                    client_commandes.clear();
                                    for (int i = 0; i < commandeUIDsList.size(); i++) {
                                        client_commandes.add(new Commande(commandeUIDsList.get(i)));
                                    }

                                    if (client_commandes.size() == 0) {
                                        client_commande_status = "aucune commande enregistrer.";
                                        handleCommandesFragment();
                                    }

                                    recoverCommandeData();

                                    return;
                                case "USER_UID_NOT_SET":
                                    client_commande_status = "vous devez vous reconnecter.";
                                    break;
                                case "USER_UID_NOT_VALID":
                                    client_commande_status = "vous devez vous reconnecter.";
                                    break;
                                case "USER_UID_DOES_NOT_EXIST":
                                    client_commande_status = "vous devez vous reconnecter.";
                                    break;
                                case "USER_PASSWORD_NOT_SET":
                                    client_commande_status = "vous devez vous reconnecter.";
                                    break;
                                case "USER_PASSWORD_NOT_VALID":
                                    client_commande_status = "vous devez vous reconnecter.";
                                    break;
                                case "USER_PASSWORD_LENGTH_NOT_VALID":
                                    client_commande_status = "vous devez vous reconnecter.";
                                    break;
                                case "USER_WRONG_PASSWORD":
                                    client_commande_status = "vous devez vous reconnecter.";
                                    break;
                                case "USER_DATA_ERROR":
                                    client_commande_status = "vous devez vous reconnecter.";
                                    break;
                                case "USER_DATA_INPUT_NOT_SET":
                                    client_commande_status = "une erreur c'est produite, vous devez metre a jours votre application.";
                                    break;
                                case "USER_DATA_NOT_SET":
                                    client_commande_status = "vous devez vous reconnecter.";
                                    break;
                            }
                        } catch (JSONException e) {
                            // should never get here
                            client_commande_status = "can't read server response :(";
                        }

                        // update layout
                        handleCommandesFragment();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        client_commande_status = "connexion impossible :(";

                        // update layout
                        handleCommandesFragment();
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
                params.put("user_data", "get_client_commandes");

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(ClientActivity.this);
        queue.add(postRequest);
    }

    private void recoverCommandeData() {
        for (int i = 0; i < client_commandes.size(); i++) {
            final int commande_index = i;

            // send request
            StringRequest postRequest = new StringRequest(
                    Request.Method.POST, RemoteDataManager.host + "/API/client/commandes.php",
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            client_commande_status = response;

                            try {
                                JSONObject jObject = new JSONObject(response);
                                String error = jObject.getString("error");

                                switch (error) {
                                    case "SUCCESS":
                                        client_commande_status = "";

                                        String dateCreated = jObject.getString("date_created");
                                        String assignedLivreurUID = jObject.getString("livreur_uid");
                                        String commande_status = jObject.getString("commande_status");

                                        client_commandes.get(commande_index).setDateCreated(dateCreated);
                                        client_commandes.get(commande_index).setAssignedLivreurUID(assignedLivreurUID);
                                        client_commandes.get(commande_index).setStatus(commande_status);

                                        // recover articles
                                        String array_data_unparsed = jObject.getString("articles_uids");
                                        array_data_unparsed = array_data_unparsed
                                                .replace("[", "")
                                                .replace("]", "")
                                                .replace("\"", "");
                                        String[] split = array_data_unparsed.split(",");
                                        List<String> articlesUIDsList = new ArrayList<String>();
                                        if (!split[0].equals("") && !split[0].equals("null")) {
                                            articlesUIDsList = Arrays.asList(split);
                                        }

                                        client_commandes.get(commande_index).clearArticles();
                                        for (int i = 0; i < articlesUIDsList.size(); i++) {
                                            client_commandes.get(commande_index).addArticle(new Article(articlesUIDsList.get(i)));
                                        }

                                        // recover articles quantities
                                        array_data_unparsed = jObject.getString("articles_quantities");
                                        array_data_unparsed = array_data_unparsed
                                                .replace("[", "")
                                                .replace("]", "")
                                                .replace("\"", "");
                                        split = array_data_unparsed.split(",");
                                        List<String> articlesQuantitiesList = new ArrayList<String>();
                                        if (!split[0].equals("") && !split[0].equals("null")) {
                                            articlesQuantitiesList = Arrays.asList(split);
                                        }

                                        for (int i = 0; i < articlesQuantitiesList.size(); i++) {
                                            client_commandes.get(commande_index).getArticle(i).setQuantity(Float.valueOf(articlesQuantitiesList.get(i)));
                                        }

                                        break;
                                    case "USER_UID_NOT_SET":
                                        client_commande_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_UID_NOT_VALID":
                                        client_commande_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_UID_DOES_NOT_EXIST":
                                        client_commande_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_PASSWORD_NOT_SET":
                                        client_commande_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_PASSWORD_NOT_VALID":
                                        client_commande_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_PASSWORD_LENGTH_NOT_VALID":
                                        client_commande_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_WRONG_PASSWORD":
                                        client_commande_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_DATA_ERROR":
                                        client_commande_status = "vous devez vous reconnecter.";
                                        break;
                                    case "USER_DATA_INPUT_NOT_SET":
                                        client_commande_status = "une erreur c'est produite, vous devez metre a jours votre application.";
                                        break;
                                    case "USER_DATA_NOT_SET":
                                        client_commande_status = "vous devez vous reconnecter.";
                                        break;
                                }
                            } catch (JSONException e) {
                                // should never get here
                                client_commande_status = "can't read server response :(";
                            }

                            // update layout
                            handleCommandesFragment();
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            client_commande_status = "connexion impossible :(";

                            // update layout
                            handleCommandesFragment();
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
                    params.put("user_data", "get_commande_data");
                    params.put("commande_uid", client_commandes.get(commande_index).getUid());

                    return params;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(ClientActivity.this);
            queue.add(postRequest);
        }
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    private void connect() {
        setContentView(R.layout.activity_connection);

        final TextView connection_status_txt = findViewById(R.id.connection_status_txt);
        connection_status_txt.setText("connection en cours...");

        Button retry_btn = findViewById(R.id.retry_btn);
        retry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                connect();
            }
        });

        Button disconnect_btn = findViewById(R.id.disconnect_btn);
        disconnect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                disconnect();
            }
        });

        final LinearLayout retry_layout = findViewById(R.id.retry_layout);;
        retry_layout.setVisibility(View.INVISIBLE);

        // send request
        StringRequest postRequest = new StringRequest(
                Request.Method.POST, RemoteDataManager.host + "/API/user/getData.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        connection_status_txt.setText(response);

                        try {
                            JSONObject jObject = new JSONObject(response);
                            String error = jObject.getString("error");

                            switch (error) {
                                case "SUCCESS":
                                    client_user_name = jObject.getString("user_name");
                                    client_email = jObject.getString("user_email");
                                    client_phone = jObject.getString("user_phone");

                                    client_email_verifyed = !jObject.getString("user_email_verified").equals("0");
                                    client_phone_verifyed = !jObject.getString("user_phone_verified").equals("0");

                                    // set main activity user
                                    initMainClientLayout();

                                    break;
                                case "USER_UID_NOT_SET":
                                    connection_status_txt.setText("vous devez vous reconnecter.");
                                    disconnect();
                                    break;
                                case "USER_UID_NOT_VALID":
                                    connection_status_txt.setText("vous devez vous reconnecter.");
                                    disconnect();
                                    break;
                                case "USER_UID_DOES_NOT_EXIST":
                                    connection_status_txt.setText("vous devez vous reconnecter.");
                                    disconnect();
                                    break;
                                case "USER_PASSWORD_NOT_SET":
                                    connection_status_txt.setText("vous devez vous reconnecter.");
                                    disconnect();
                                    break;
                                case "USER_PASSWORD_NOT_VALID":
                                    connection_status_txt.setText("vous devez vous reconnecter.");
                                    disconnect();
                                    break;
                                case "USER_PASSWORD_LENGTH_NOT_VALID":
                                    connection_status_txt.setText("vous devez vous reconnecter.");
                                    disconnect();
                                    break;
                                case "USER_WRONG_PASSWORD":
                                    connection_status_txt.setText("vous devez vous reconnecter.");
                                    disconnect();
                                    break;
                                case "USER_DATA_ERROR":
                                    connection_status_txt.setText("vous devez vous reconnecter.");
                                    disconnect();
                                    break;
                                case "USER_DATA_NOT_SET":
                                    connection_status_txt.setText("vous devez vous reconnecter.");
                                    disconnect();
                                    break;
                            }
                        } catch (JSONException e) {
                            // should never get here
                            connection_status_txt.setText("can't read server response :(");
                            retry_layout.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        connection_status_txt.setText("connexion impossible :(");
                        retry_layout.setVisibility(View.VISIBLE);
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
                params.put("user_data", "all");

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(ClientActivity.this);
        queue.add(postRequest);
    }

    private void disconnect() {
        Intent intent=new Intent();
        intent.putExtra("action", "DISCONNECT_USER");
        setResult(1, intent);

        finish();
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    // Call Back method  to get the Message form other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_CANCELED) {
            if(resultCode == 3) {
                // set email
                client_email = data.getStringExtra("user_email");
                client_email_verifyed = Boolean.parseBoolean(data.getStringExtra("user_email_verifyed"));

            } else if (resultCode == 4) {
                // set phone number
                client_phone = data.getStringExtra("user_phone_number");
                client_phone_verifyed = Boolean.parseBoolean(data.getStringExtra("user_phone_verifyed"));

            } else if (resultCode == 5) {
                // set email
                client_email_verifyed = Boolean.parseBoolean(data.getStringExtra("user_email_verifyed"));

            } else if (resultCode == 6) {
                // set phone number
                client_phone_verifyed = Boolean.parseBoolean(data.getStringExtra("user_phone_verifyed"));
            } else if (resultCode == 7) {
                // remove command
                String commande_uid = data.getStringExtra("command_uid");
                removeCommande(commande_uid);
            } else if (resultCode == 8) {
                // confirm command
                String commande_uid = data.getStringExtra("command_uid");
                confirmCommande(commande_uid);
            }
        }

        // update layout
        updateMainClientLayout();
    }

    //----------------------------------------------------------------------------------------------


}
