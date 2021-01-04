package dz.project.commandes;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dz.project.ClientActivity_CommandesFragment;
import dz.project.R;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView article_uid_txt;
//        TextView commande_date_created_txt;
//        TextView commande_status_txt;
//        TextView commande_assigned_livreur_txt;
//        TextView commande_articles_number_txt;
////        TextView livreur_status_txt;
//        CardView commande_card_btn;

        ViewHolder(View itemView) {
            super(itemView);

            article_uid_txt = itemView.findViewById(R.id.article_uid_txt);
//            commande_date_created_txt = itemView.findViewById(R.id.commande_date_created_txt);
//            commande_status_txt = itemView.findViewById(R.id.commande_status_txt);
//            commande_assigned_livreur_txt = itemView.findViewById(R.id.commande_assigned_livreur_txt);
//            commande_articles_number_txt = itemView.findViewById(R.id.commande_articles_number_txt);
////            livreur_status_txt = itemView.findViewById(R.id.livreur_livreur_status_txt);
//            commande_card_btn = itemView.findViewById(R.id.commande_card_btn);

        }
    }

    private List<Article> articles;
    public ArticlesAdapter(List<Article> articles) {
        this.articles = articles;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public ArticlesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.list_item_article, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ArticlesAdapter.ViewHolder viewHolder, int position) {
        final Article article = articles.get(position);

        final TextView article_uid_txt = viewHolder.article_uid_txt;
//        final TextView commande_date_created_txt = viewHolder.commande_date_created_txt;
//        final TextView commande_status_txt = viewHolder.commande_status_txt;
//        final TextView commande_assigned_livreur_txt = viewHolder.commande_assigned_livreur_txt;
//        final TextView commande_articles_number_txt = viewHolder.commande_articles_number_txt;
////        final TextView livreur_status_txt = viewHolder.livreur_status_txt;
//        final CardView commande_card_btn = viewHolder.commande_card_btn;
//
        article_uid_txt.setText(article.getUid());
//        commande_date_created_txt.setText(commande.getDateCreated());
//
//        switch (commande.getStatus()) {
//            case "null":
//                commande_status_txt.setText("non confirmée");
//                commande_status_txt.setTextColor(0xffff0000);
//                break;
//            case "NOT_CONFIRMED":
//                commande_status_txt.setText("non confirmée");
//                commande_status_txt.setTextColor(0xffff0000);
//                break;
//            case "WAITING_LIVREUR_CONFIRMATION":
//                commande_status_txt.setText("en attente du livreur");
//                commande_status_txt.setTextColor(0xff0000ff);
//                break;
//            case "CONFIRMED":
//                commande_status_txt.setText("confirmée");
//                commande_status_txt.setTextColor(0x0000ff00);
//                break;
//        }
//
//
//        if (commande.getAssignedLivreurUID().equals("NO_LIVREUR_ASSIGNED")) {
//            commande_assigned_livreur_txt.setTextColor(0xffff0000);
//            commande_assigned_livreur_txt.setText("aucun livreur assigné.");
//        } else {
//            commande_assigned_livreur_txt.setTextColor(0xff0000ff);
//            commande_assigned_livreur_txt.setText(commande.getAssignedLivreurUID());
//        }
//
//        commande_articles_number_txt.setText(String.valueOf(commande.getSizeArticles()));

/*
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
*/
//        commande_card_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view)
//            {
//                ClientActivity_CommandesFragment.openCommandeInfoPanel(commande.getUid());
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setArticles(ArrayList<Article> articles) {
        this.articles = articles;
    }
}
