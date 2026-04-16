package com.esports.models;

public class Sujet {
    private int id;
    private String titre;
    private String contenu;

    // Empty constructor needed for DAO
    public Sujet() {}

    // Full constructor
    public Sujet(int id, String titre, String contenu) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
}