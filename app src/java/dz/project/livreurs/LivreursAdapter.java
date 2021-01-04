package dz.project.livreurs;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dz.project.ClientActivity_LivreursFragment;
import dz.project.R;

public class LivreursAdapter extends RecyclerView.Adapter<LivreursAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView livreur_username_txt;
        TextView livreur_uid_txt;
        TextView livreur_phone_txt;
        TextView livreur_email_txt;
        TextView livreur_status_txt;
        CardView livreur_remove_uid_btn;

        ViewHolder(View itemView) {
            super(itemView);

            livreur_username_txt = itemView.findViewById(R.id.livreur_user_name_txt);
            livreur_uid_txt = itemView.findViewById(R.id.livreur_livreur_uid_txt);
            livreur_phone_txt = itemView.findViewById(R.id.livreur_phone_number_txt);
            livreur_email_txt = itemView.findViewById(R.id.livreur_email_txt);
            livreur_status_txt = itemView.findViewById(R.id.livreur_livreur_status_txt);
            livreur_remove_uid_btn = itemView.findViewById(R.id.livreur_remove_uid_btn);

        }
    }

    private List<Livreur> livreurs;
    public LivreursAdapter(List<Livreur> livreurs) {
        this.livreurs = livreurs;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public LivreursAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.list_item_livreur, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LivreursAdapter.ViewHolder viewHolder, int position) {
        final Livreur livreur = livreurs.get(position);

        final TextView uid_txt = viewHolder.livreur_username_txt;
        final TextView livreur_uid_txt = viewHolder.livreur_uid_txt;
        final TextView livreur_phone_txt = viewHolder.livreur_phone_txt;
        final TextView livreur_email_txt = viewHolder.livreur_email_txt;
        final TextView livreur_status_txt = viewHolder.livreur_status_txt;
        final CardView livreur_remove_uid_btn = viewHolder.livreur_remove_uid_btn;

        uid_txt.setText(livreur.getUser_name());
        livreur_uid_txt.setText(livreur.getLivreur_uid());
        livreur_phone_txt.setText(livreur.getUser_phone());
        livreur_email_txt.setText(livreur.getUser_email());

        String livreur_status = livreur.getStatus();
        switch (livreur_status) {
            case "NOT_ACTIVE":
                livreur_status_txt.setTextColor(0xffff0000);
                livreur_status_txt.setText("non actif");
                break;
            default:
                livreur_status_txt.setText(livreur_status);
                break;
        }

        livreur_remove_uid_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ClientActivity_LivreursFragment.removeLivreur(livreur.getLivreur_uid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return livreurs.size();
    }

    public void setLivreur(ArrayList<Livreur> livreurs) {
        this.livreurs = livreurs;
    }
}
