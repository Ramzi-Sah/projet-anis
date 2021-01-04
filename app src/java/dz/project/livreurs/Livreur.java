package dz.project.livreurs;

public class Livreur {
    private String uid;
    private String livreur_uid;

    private String user_name = "";
    private String user_email = "";
    private String user_phone = "";

    private String status = "not connected";

    public Livreur(String livreur_uid) {
        this.livreur_uid = livreur_uid;
    }

    public String getLivreur_uid() {
        return livreur_uid;
    }


    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUser_name() {
        return user_name;
    }
    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }


    public String getUser_email() {
        return user_email;
    }
    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_phone() {
        return user_phone;
    }
    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
