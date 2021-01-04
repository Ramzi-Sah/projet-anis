package dz.project.commandes;

import java.util.ArrayList;

public class Commande {
    public String uid;

    private String dateCreated = "";
    private String assignedLivreurUID = "";

    private ArrayList<Article> articles;

    private String status = "null";

    public Commande(String uid) {
        this.uid = uid;
        articles = new ArrayList<Article>();
    }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getAssignedLivreurUID() {
        return assignedLivreurUID;
    }
    public void setAssignedLivreurUID(String assignedLivreurUID) {
        this.assignedLivreurUID = assignedLivreurUID;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public void clearArticles() {
        articles.clear();
    }
    public void addArticle(Article article) {
        articles.add(article);
    }
    public int getSizeArticles() {
        return articles.size();
    }

    public Article getArticle(int i) {
        return articles.get(i);
    }

    public ArrayList<Article> getArticles() {
        return articles;
    }



}
