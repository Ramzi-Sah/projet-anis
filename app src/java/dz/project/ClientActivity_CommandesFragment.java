package dz.project;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import dz.project.commandes.Commande;
import dz.project.commandes.CommandesAdapter;

public class ClientActivity_CommandesFragment extends Fragment {

    public ClientActivity_CommandesFragment() {}

    CommandesAdapter adapter;

    TextView status_txt;
    RecyclerView commande_rv;
    FloatingActionButton add_commande_fabtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // view
        View v = inflater.inflate(R.layout.activity_client_main_commandes_fragment, container, false);

        // status text
        status_txt = v.findViewById(R.id.client_main_commandes_fragment_txt);

        // add livreur fab
        add_commande_fabtn = v.findViewById(R.id.client_main_commandes_fragment_add_commande_faabtn);
        add_commande_fabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                listner.ClientActivity_CommandesFragment_onInputSent("ADD_COMMANDE", "");
            }
        });

        // commande list
        commande_rv = v.findViewById(R.id.client_main_commandes_fragment_rv);

        // update data
        listner.ClientActivity_CommandesFragment_onInputSent("SETUP", "");

        return v;
    }

    //----------------------------------------------------------------------------------------------
    private static ClientActivity_CommandesFragment_Listner listner;
    public interface ClientActivity_CommandesFragment_Listner {
        void ClientActivity_CommandesFragment_onInputSent(String input, String data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ClientActivity_CommandesFragment_Listner) {
            listner = (ClientActivity_CommandesFragment_Listner) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ClientActivity_CommandesFragment_Listner.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listner = null;
    }

    //----------------------------------------------------------------------------------------------
    public void setStatusText(String statusText) {
        if (statusText.equals("")) {
            status_txt.setVisibility(View.GONE);
        } else {
            status_txt.setVisibility(View.VISIBLE);
        }
        status_txt.setText(statusText);
    }


    public void initCommandesList(Context context) {
        ArrayList<Commande> commandes = new ArrayList<Commande>();

        adapter = new CommandesAdapter(commandes);
        commande_rv.setAdapter(adapter);
        commande_rv.setLayoutManager(new LinearLayoutManager(context));
    }

    public void setCommandesList(ArrayList<Commande> commandes) {
        adapter.setCommandes(commandes);
        adapter.notifyItemInserted(0);
    }

    public static void openCommandeInfoPanel(String commandeUid) {
        listner.ClientActivity_CommandesFragment_onInputSent("COMMANDE_INFO", commandeUid);
    }



}