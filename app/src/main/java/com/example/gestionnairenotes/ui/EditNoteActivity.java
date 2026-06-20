package com.example.gestionnairenotes.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.gestionnairenotes.R;
import com.example.gestionnairenotes.model.Note;
import com.example.gestionnairenotes.service.NoteRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {

    public static final String extra_note_id = "extra_note_id";

    private EditText etTitre, etContenu;
    private NoteRepository repository;
    private Note noteActuelle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        repository = new NoteRepository(this);

        etTitre = findViewById(R.id.etTitreEdit);
        etContenu = findViewById(R.id.etContenuEdit);
        Button btnModifier = findViewById(R.id.btnSauvegarderEdit);
        ImageView btnRetour = findViewById(R.id.btnRetourEdit);

        // Gestion du clic de retour sans modifier
        btnRetour.setOnClickListener(v -> finish());

        int noteId = getIntent().getIntExtra(extra_note_id, -1);

        if (noteId == -1) {
            Toast.makeText(this, "Note introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

                noteActuelle = note;

                etTitre.setText(note.getTitre());
                etContenu.setText(note.getContenu());

                CardView layout = findViewById(R.id.layoutEditNote);
                if (note.getCouleur() != null && !note.getCouleur().isEmpty()) {
                    layout.setCardBackgroundColor(Color.parseColor(note.getCouleur()));
                }
            });
        }).start();

        btnModifier.setOnClickListener(v -> sauvegarderModifications());
    }

    private void sauvegarderModifications() {
        if (noteActuelle == null) {
            Toast.makeText(this, "Erreur lors du chargement de la note", Toast.LENGTH_SHORT).show();
            return;
        }

        String titre = etTitre.getText() != null ? etTitre.getText().toString().trim() : "";
        String contenu = etContenu.getText() != null ? etContenu.getText().toString().trim() : "";

        if (titre.isEmpty()) {
            etTitre.setError("Le titre est obligatoire");
            return;
        }

        noteActuelle.setTitre(titre);
        noteActuelle.setContenu(contenu);
        noteActuelle.setDate(
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date())
        );

        new Thread(() -> {
            repository.update(noteActuelle);
            runOnUiThread(() -> {
                setResult(RESULT_OK);
                Toast.makeText(this, "Note modifiée avec succès", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}