package edu.uark.dlb018.bruhapi;

public class User {
    private String Username;
    private String Email;
    private String Password;
    private String PoliticalLeaning;

    User(String un, String email, String pw){
        Username = un;
        Email = email;
        Password = pw;
    }

    public String getUsername() {
        return Username;
    }

    public String getEmail() {
        return Email;
    }

    public String getPassword() {
        return Password;
    }

    public String getPoliticalLeaning() { return PoliticalLeaning; }

    public void setPoliticalLeaning(String pl) { PoliticalLeaning = pl; }
}
