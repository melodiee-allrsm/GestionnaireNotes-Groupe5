package com.example.gestionnairenotes.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // Import ajouté pour la CardView

import com.example.gestionnairenotes.R;
import com.example.gestionnairenotes.model.Note;
import com.example.gestionnairenotes.service.NoteRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText etTitre, etContenu;
    private CardView layoutCreateNote; // Modifié : LinearLayout -> CardView
    private NoteRepository repository;
    private String couleurChoisie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        etTitre          = findViewById(R.id.etTitre);
        etContenu        = findViewById(R.id.etContenu);
        layoutCreateNote = findViewById(R.id.layoutCreateNote); // Récupère le CardView du XML
        Button btnCreer  = findViewById(R.id.btnCreer);

        repository = new NoteRepository(this);

        // Couleur reçue du sélecteur de couleurs
        couleurChoisie = getIntent().getStringExtra("COULEUR");
        if (couleurChoisie != null && !couleurChoisie.isEmpty()) {
            // Modifié : Utilisation de setCardBackgroundColor pour préserver les bords arrondis
            layoutCreateNote.setCardBackgroundColor(Color.parseColor(couleurChoisie));
        } else {
            // Optionnel : Couleur par défaut (ex: Gris clair) si aucune couleur n'est reçue
            layoutCreateNote.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
        }

        btnCreer.setOnClickListener(v -> creerNote());
    }

    private void creerNote() {
        String titre   = etTitre.getText().toString().trim();
        String contenu = etContenu.getText().toString().trim();

        // Validation obligatoire
        if (titre.isEmpty()) {
            etTitre.setError("Le titre est obligatoire");
            etTitre.requestFocus();
            return;
        }
        if (contenu.isEmpty()) {
            etContenu.setError("Le contenu est obligatoire");
            etContenu.requestFocus();
            return;
        }

        // Date automatique
        String date = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
                .format(new Date());

        // Créer et sauvegarder la note
        Note note = new Note(titre, contenu, couleurChoisie, date, false);
        repository.insert(note);

        Toast.makeText(this, "Note créée !", Toast.LENGTH_SHORT).show();

        setResult(RESULT_OK);
        finish();
    }
}