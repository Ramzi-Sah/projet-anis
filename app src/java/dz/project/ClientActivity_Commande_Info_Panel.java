package dz.project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import dz.project.commandes.Article;
import dz.project.commandes.ArticlesAdapter;
import dz.project.commandes.Commande;

public class ClientActivity_Commande_Info_Panel extends AppCompatActivity {

    ArticlesAdapter adapter;

    String commande_uid;
    Commande commande;

    TextView commande_uid_txt;
    TextView commande_date_created_txt;
    TextView commande_status_txt;
    TextView commande_assigned_livreur_txt;
    TextView commande_articles_number_txt;

    Button commande_delete_btn;
    Button commande_confirm_btn;
    Button commande_asign_livreur_btn;

    TextView commande_articles_list_txt;
    RecyclerView commande_articles_list_rv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_command_info_panel);

        commande_articles_list_rv = findViewById(R.id.commande_articles_list_rv);
        initArticlesList(this);

        updateCommandeInfoUI();
        updateCommandeArticlesUI();
    }

    private void updateCommandeArticlesUI() {
        commande_articles_list_txt = findViewById(R.id.commande_articles_list_txt);

        if (commande.getSizeArticles() == 0) {
            commande_articles_list_txt.setText("no article.");
        }

        adapter.setArticles(commande.getArticles());

//        for (int i = 0; i < commande.getSizeArticles(); i++) {
//            commande_articles_list_txt.setText(String.format("%s %s %s", commande_articles_list_txt.getText(), commande.getArticle(i).getUid(), commande.getArticle(i).getQuantity()));
//        }


/*
        // send request
        StringRequest postRequest = new StringRequest(
                Request.Method.POST, RemoteDataManager.host + "/API/client/addData.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        commande_articles_list_txt.setText(response);

                        try {
                            JSONObject jObject = new JSONObject(response);
                            String error = jObject.getString("error");

                            switch (error) {
                                case "SUCCESS":
                                    String articles = jObject.getString("error");
//                                    String  = ;

                                    commande_articles_list_txt.setText(articles);

                                    break;
                                case "USER_UID_NOT_SET":
                                    commande_articles_list_txt.setText("vous devez vous reconnecter.");
                                    break;
                                case "USER_UID_NOT_VALID":
                                    commande_articles_list_txt.setText("vous devez vous reconnecter.");
                                    break;
                                case "USER_UID_DOES_NOT_EXIST":
                                    commande_articles_list_txt.setText("vous devez vous reconnecter.");
                                    break;
                                case "USER_PASSWORD_NOT_SET":
                                    commande_articles_list_txt.setText("vous devez vous reconnecter.");
                                    break;
                                case "USER_PASSWORD_NOT_VALID":
                                    commande_articles_list_txt.setText("vous devez vous reconnecter.");
                                    break;
                                case "USER_PASSWORD_LENGTH_NOT_VALID":
                                    commande_articles_list_txt.setText("vous devez vous reconnecter.");
                                    break;
                                case "USER_WRONG_PASSWORD":
                                    commande_articles_list_txt.setText("vous devez vous reconnecter.");
                                    break;
                                case "USER_DATA_ERROR":
                                    commande_articles_list_txt.setText("vous devez vous reconnecter.");
                                    break;
                                case "USER_DATA_NOT_SET":
                                    commande_articles_list_txt.setText("vous devez vous reconnecter.");
                                    break;

                                case "USER_DATA_INPUT_NOT_SET":
                                    commande_articles_list_txt.setText("vous devez dabord remplir le champ uid livreur.");
                                    break;
                                case "USER_DATA_INPUT_NOT_VALID":
                                    commande_articles_list_txt.setText("l'uid livreur que vous avez entrer n'est pas valide.");
                                    break;

                            }
                        } catch (JSONException e) {
                            // should never get here
                            commande_articles_list_txt.setText("can't read server response :(");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        commande_articles_list_txt.setText("connexion impossible :(");
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

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(ClientActivity_Commande_Info_Panel.this);
        queue.add(postRequest);
*/
    }


    private void updateCommandeInfoUI() {
        // assign ui
        commande_uid_txt = findViewById(R.id.commande_uid_txt);
        commande_date_created_txt = findViewById(R.id.commande_date_created_txt);
        commande_status_txt = findViewById(R.id.commande_status_txt);
        commande_assigned_livreur_txt = findViewById(R.id.commande_assigned_livreur_txt);
        commande_articles_number_txt = findViewById(R.id.commande_articles_number_txt);

        commande_delete_btn = findViewById(R.id.commande_cancel_btn);
        commande_confirm_btn = findViewById(R.id.commande_confirm_btn);
        commande_asign_livreur_btn = findViewById(R.id.commande_asign_livreur_btn);

        // get commande
        commande_uid = getIntent().getStringExtra("commande_uid");

        for (int i = 0; i < ClientActivity.client_commandes.size(); i++) {
            if (ClientActivity.client_commandes.get(i).uid.equals(commande_uid)) {
                commande = ClientActivity.client_commandes.get(i);
                break;
            }
        }
        if (commande == null) {
            finish();
        }


        // set ui data
        commande_uid_txt.setText(commande.getUid());
        commande_date_created_txt.setText(commande.getDateCreated());

        switch (commande.getStatus()) {
            case "null":
                commande_status_txt.setText("non confirmée");
                commande_status_txt.setTextColor(0xffff0000);
                break;
            case "NOT_CONFIRMED":
                commande_status_txt.setText("non confirmée");
                commande_status_txt.setTextColor(0xffff0000);
                break;
            case "WAITING_LIVREUR_CONFIRMATION":
                commande_status_txt.setText("en attente du livreur");
                commande_status_txt.setTextColor(0xff0000ff);
                break;
            case "CONFIRMED":
                commande_status_txt.setText("confirmée");
                commande_status_txt.setTextColor(0x0000ff00);
                break;
        }


        if (commande.getAssignedLivreurUID().equals("NO_LIVREUR_ASSIGNED")) {
            commande_assigned_livreur_txt.setTextColor(0xffff0000);
            commande_assigned_livreur_txt.setText("aucun livreur assigné.");
        } else {
            commande_assigned_livreur_txt.setTextColor(0xff0000ff);
            commande_assigned_livreur_txt.setText(commande.getAssignedLivreurUID());
        }

        commande_articles_number_txt.setText(String.valueOf(commande.getSizeArticles()));

        commande_delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent=new Intent();
                intent.putExtra("command_uid", commande.getUid());
                setResult(7, intent);

                finish();
            }
        });

        commande_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ViewGroup viewGroup = findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(ClientActivity_Commande_Info_Panel.this).inflate(R.layout.dialog_client_confirm_commande, viewGroup, false);
                AlertDialog.Builder builder = new AlertDialog.Builder(ClientActivity_Commande_Info_Panel.this);
                builder.setView(dialogView);
                final AlertDialog removeCommandeDialog = builder.create();
                removeCommandeDialog.show();

                final TextView status_txt= dialogView.findViewById(R.id.confirmCommande_dialog_status_btn);
                final String livreur_uid_verifyed = commande_uid;

                Button cancel_btn = dialogView.findViewById(R.id.confirmCommande_dialog_no_btn);
                cancel_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        removeCommandeDialog.cancel();
                    }
                });

                Button validate_btn= dialogView.findViewById(R.id.confirmCommande_dialog_yes_btn);
                validate_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    status_txt.setText("confirmation de la commande " + livreur_uid_verifyed + " ...");

                    Intent intent=new Intent();
                    intent.putExtra("command_uid", commande.getUid());
                    setResult(8, intent);

                    finish();
                    }
                });
            }
        });

        commande_asign_livreur_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // TODO: select livreur
                Toast.makeText(getApplicationContext(), "todo...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void initArticlesList(Context context) {
        ArrayList<Article> articles = new ArrayList<Article>();

        adapter = new ArticlesAdapter(articles);
        commande_articles_list_rv.setAdapter(adapter);
        commande_articles_list_rv.setLayoutManager(new LinearLayoutManager(context));
    }


}
