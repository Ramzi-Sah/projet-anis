package dz.project.commandes;

public class Article {
    private String uid;

    private String title;
    private float quantity = 0;

    public Article(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public float getQuantity() {
        return quantity;
    }
    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

}
