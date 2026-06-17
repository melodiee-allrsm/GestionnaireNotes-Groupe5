package com.example.gestionnairenotes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionnairenotes.R;
import com.example.gestionnairenotes.model.Note;
import com.example.gestionnairenotes.service.NoteRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {

    // Clé utilisée pour transmettre la note via l'Intent (doit être identique à celle de MainActivity)
    public static final String EXTRA_NOTE_ID    = "note_id";
    public static final String EXTRA_NOTE_TITRE   = "note_titre";
    public static final String EXTRA_NOTE_CONTENU = "note_contenu";
    public static final String EXTRA_NOTE_COULEUR = "note_couleur";
    public static final String EXTRA_NOTE_FAVORI  = "note_favori";

    private TextInputEditText etTitre, etContenu;
    private NoteRepository repository;

    // On garde en mémoire la note qu'on est en train de modifier
    private Note noteActuelle;

    // Couleur sélectionnée par l'utilisateur (blanc par défaut)
    private String couleurSelectionnee = "#FFFFFF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        repository = new NoteRepository(this);

        // Récupération des vues
        etTitre  = findViewById(R.id.etTitreEdit);
        etContenu = findViewById(R.id.etContenuEdit);
        Button btnSauvegarder = findViewById(R.id.btnSauvegarderEdit);

        // --- Pré-remplissage du formulaire avec les données existantes ---
        // On récupère les données passées par MainActivity via l'Intent
        Intent intent = getIntent();
        long   noteId      = intent.getLongExtra(EXTRA_NOTE_ID, -1);
        String titrePasse  = intent.getStringExtra(EXTRA_NOTE_TITRE);
        String contenuPasse = intent.getStringExtra(EXTRA_NOTE_CONTENU);
        String couleurPasse = intent.getStringExtra(EXTRA_NOTE_COULEUR);
        boolean favoriPasse = intent.getBooleanExtra(EXTRA_NOTE_FAVORI, false);

        // On reconstruit l'objet Note pour pouvoir l'updater plus tard
        noteActuelle = new Note(titrePasse, contenuPasse, couleurPasse != null ? couleurPasse : "#FFFFFF");
        noteActuelle.setId((int) noteId);
        noteActuelle.setFavori(favoriPasse);

        // On injecte les valeurs dans les champs
        etTitre.setText(titrePasse);
        etContenu.setText(contenuPasse);

        // On initialise la couleur sélectionnée avec celle de la note
        if (couleurPasse != null) couleurSelectionnee = couleurPasse;

        // --- Gestion des boutons de couleur ---
        configurerBoutonCouleur(R.id.btnCouleurBlanc);
        configurerBoutonCouleur(R.id.btnCouleurJaune);
        configurerBoutonCouleur(R.id.btnCouleurVert);
        configurerBoutonCouleur(R.id.btnCouleurRose);

        // --- Sauvegarde : on appelle NoteRepository.update() ---
        btnSauvegarder.setOnClickListener(v -> sauvegarderModifications());
    }

    /**
     * Attache un listener à un bouton couleur.
     * Quand l'utilisateur appuie dessus, on mémorise la couleur hex stockée dans son tag.
     */
    private void configurerBoutonCouleur(int buttonId) {
        Button btn = findViewById(buttonId);
        btn.setOnClickListener(v -> {
            // Le tag du bouton contient directement la couleur hex (#FFF9C4, etc.)
            couleurSelectionnee = (String) v.getTag();
            Toast.makeText(this, "Couleur sélectionnée", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Valide le formulaire, met à jour la note en base puis retourne à la liste.
     */
    private void sauvegarderModifications() {
        String titre  = etTitre.getText() != null ? etTitre.getText().toString().trim() : "";
        String contenu = etContenu.getText() != null ? etContenu.getText().toString().trim() : "";

        // Validation basique : le titre ne peut pas être vide
        if (titre.isEmpty()) {
            etTitre.setError("Le titre est obligatoire");
            return;
        }

        // On met à jour les champs de la note qu'on avait mémorisée
        noteActuelle.setTitre(titre);
        noteActuelle.setContenu(contenu);
        noteActuelle.setCouleur(couleurSelectionnee);

        // On horodate la modification avec la date du jour
        String dateAujourdhui = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        noteActuelle.setDate(dateAujourdhui);

        // Appel repository en thread séparé (Room interdit les accès réseau/BDD sur le main thread)
        new Thread(() -> {
            repository.update(noteActuelle);
            // Retour sur le thread principal pour afficher le toast et fermer l'activité
            runOnUiThread(() -> {
                Toast.makeText(this, "Note modifiée ✓", Toast.LENGTH_SHORT).show();
                finish(); // Retour automatique à MainActivity (la liste)
            });
        }).start();
    }
}