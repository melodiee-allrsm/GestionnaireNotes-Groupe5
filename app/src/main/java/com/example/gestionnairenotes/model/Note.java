package com.example.gestionnairenotes.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String titre;
    private String contenu;
    private String couleur;
    private String date;
    private boolean favori;

    public Note(String titre,
                String contenu,
                String couleur,
                String date,
                boolean favori) {

        this.titre = titre;
        this.contenu = contenu;
        this.couleur = couleur;
        this.date = date;
        this.favori = favori;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public String getContenu() {
        return contenu;
    }

    public String getCouleur() {
        return couleur;
    }

    public String getDate() {
        return date;
    }

    public boolean isFavori() {
        return favori;
    }

    public void setFavori(boolean favori) {
        this.favori = favori;
    }
}
