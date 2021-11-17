package edu.uark.dlb018.bruhapi.user;

public class User {
    private String Username;
    private String Email;
    private String Password;
    private String PoliticalLeaning;
    private String Age;
    private String Race;
    private String Nationality;
    private String Religion;
    private String ParentalStatus;
    private String Gender;

    public User(String un, String email, String pw){
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

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getRace() {
        return Race;
    }

    public void setRace(String race) {
        Race = race;
    }

    public String getNationality() {
        return Nationality;
    }

    public void setNationality(String nationality) {
        Nationality = nationality;
    }

    public String getReligion() {
        return Religion;
    }

    public void setReligion(String religion) {
        Religion = religion;
    }

    public String getParentalStatus() {
        return ParentalStatus;
    }

    public void setParentalStatus(String parentalStatus) {
        ParentalStatus = parentalStatus;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }
}
