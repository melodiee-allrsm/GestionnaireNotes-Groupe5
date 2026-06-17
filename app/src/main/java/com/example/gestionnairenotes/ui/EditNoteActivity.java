package com.example.gestionnairenotes.ui;

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

    // Clés pour passer les données de la note via Intent (partagées avec MainActivity)
    public static final String extra_note_id     = "extra_note_id";
    public static final String EXTRA_NOTE_TITRE   = "note_titre";
    public static final String EXTRA_NOTE_CONTENU = "note_contenu";
    public static final String EXTRA_NOTE_COULEUR = "note_couleur";
    public static final String EXTRA_NOTE_FAVORI  = "note_favori";

    private TextInputEditText etTitre, etContenu;
    private NoteRepository repository;
    private Note noteActuelle;
    private String couleurSelectionnee = "#FFFFFF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        repository = new NoteRepository(this);

        etTitre   = findViewById(R.id.etTitreEdit);
        etContenu = findViewById(R.id.etContenuEdit);
        Button btnSauvegarder = findViewById(R.id.btnSauvegarderEdit);

        // Récupération de l'ID passé par MainActivity
        int noteId = getIntent().getIntExtra("extra_note_id", -1);

        if (noteId == -1) {
            Toast.makeText(this, "Note introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Chargement de la note depuis Room (thread séparé obligatoire)
        new Thread(() -> {
            Note note = repository.getAll()
                    .stream()
                    .filter(n -> n.getId() == noteId)
                    .findFirst()
                    .orElse(null);

            runOnUiThread(() -> {
                if (note == null) {
                    Toast.makeText(this, "Note introuvable", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // On mémorise la note pour la modifier plus tard
                noteActuelle = note;
                couleurSelectionnee = note.getCouleur() != null ? note.getCouleur() : "#FFFFFF";

                // Pré-remplissage des champs avec les données existantes
                etTitre.setText(note.getTitre());
                etContenu.setText(note.getContenu());
            });
        }).start();

        // Boutons de couleur — le tag contient la valeur hex
        configurerBoutonCouleur(R.id.btnCouleurBlanc);
        configurerBoutonCouleur(R.id.btnCouleurJaune);
        configurerBoutonCouleur(R.id.btnCouleurVert);
        configurerBoutonCouleur(R.id.btnCouleurRose);

        btnSauvegarder.setOnClickListener(v -> sauvegarderModifications());
    }

    private void configurerBoutonCouleur(int buttonId) {
        Button btn = findViewById(buttonId);
        btn.setOnClickListener(v -> {
            couleurSelectionnee = (String) v.getTag();
            Toast.makeText(this, "Couleur sélectionnée", Toast.LENGTH_SHORT).show();
        });
    }

    private void sauvegarderModifications() {
        String titre   = etTitre.getText() != null ? etTitre.getText().toString().trim() : "";
        String contenu = etContenu.getText() != null ? etContenu.getText().toString().trim() : "";

        if (titre.isEmpty()) {
            etTitre.setError("Le titre est obligatoire");
            return;
        }

        // On met à jour les champs via les setters qu'on vient d'ajouter
        noteActuelle.setTitre(titre);
        noteActuelle.setContenu(contenu);
        noteActuelle.setCouleur(couleurSelectionnee);
        noteActuelle.setDate(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        new Thread(() -> {
            repository.update(noteActuelle);
            runOnUiThread(() -> {
                // RESULT_OK indique à MainActivity de rafraîchir la liste
                setResult(RESULT_OK);
                Toast.makeText(this, "Note modifiée ✓", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}