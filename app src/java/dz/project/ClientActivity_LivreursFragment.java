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

import dz.project.livreurs.Livreur;
import dz.project.livreurs.LivreursAdapter;

public class ClientActivity_LivreursFragment extends Fragment {

    public ClientActivity_LivreursFragment() {}

    LivreursAdapter adapter;

    RecyclerView livreurs_rv;
    FloatingActionButton add_livreur_fabtn;
    TextView test_txt;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // view
        View v = inflater.inflate(R.layout.activity_client_main_livreur_fragment, container, false);

        // status text
        test_txt = v.findViewById(R.id.client_main_livreur_fragment_txt);

        // add livreur fab
        add_livreur_fabtn = v.findViewById(R.id.client_main_livreur_fragment_add_livreur_faabtn);
        add_livreur_fabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                listner.ClientActivity_LivreursFragment_onInputSent("ADD_LIVREUR");
            }
        });

        // livreurs list
        livreurs_rv = v.findViewById(R.id.client_main_livreur_fragment_rv);

        // update data
        listner.ClientActivity_LivreursFragment_onInputSent("SETUP");

        return v;
    }

    //----------------------------------------------------------------------------------------------
    private static ClientActivity_LivreursFragment_Listner listner;
    public interface ClientActivity_LivreursFragment_Listner {
        void ClientActivity_LivreursFragment_onInputSent(String input);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ClientActivity_LivreursFragment_Listner) {
            listner = (ClientActivity_LivreursFragment_Listner) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ClientActivity_LivreursFragment_Listner.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listner = null;
    }

    //----------------------------------------------------------------------------------------------
    public void setText(String statusText) {
        if (statusText.equals("")) {
            test_txt.setVisibility(View.GONE);
        } else {
            test_txt.setVisibility(View.VISIBLE);
        }
        test_txt.setText(statusText);
    }

    public void initLivreursList(Context context) {
        ArrayList<Livreur> livreurs = new ArrayList<Livreur>();

        adapter = new LivreursAdapter(livreurs);
        livreurs_rv.setAdapter(adapter);
        livreurs_rv.setLayoutManager(new LinearLayoutManager(context));
    }

    public void setLivreurList(ArrayList<Livreur> livreurs) {
        adapter.setLivreur(livreurs);
        adapter.notifyItemInserted(0);
    }

    public static void removeLivreur(String livreur_uid) {
        listner.ClientActivity_LivreursFragment_onInputSent("REMOVE_LIVREUR:" + livreur_uid);
    }

}