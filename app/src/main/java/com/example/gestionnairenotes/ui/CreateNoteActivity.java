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

public class CreateNoteActivity extends AppCompatActivity {

    private EditText etTitre, etContenu;
    private CardView layoutCreateNote;
    private NoteRepository repository;
    private String couleurChoisie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        etTitre          = findViewById(R.id.etTitre);
        etContenu        = findViewById(R.id.etContenu);
        layoutCreateNote = findViewById(R.id.layoutCreateNote);
        Button btnCreer  = findViewById(R.id.btnCreer);
        ImageView btnRetour = findViewById(R.id.btnRetourCreate);

        // Gestion du clic de retour sans sauvegarder
        btnRetour.setOnClickListener(v -> finish());

        repository = new NoteRepository(this);

        couleurChoisie = getIntent().getStringExtra("COULEUR");
        if (couleurChoisie != null && !couleurChoisie.isEmpty()) {
            layoutCreateNote.setCardBackgroundColor(Color.parseColor(couleurChoisie));
        } else {
            layoutCreateNote.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
        }

        btnCreer.setOnClickListener(v -> creerNote());
    }

    private void creerNote() {
        String titre   = etTitre.getText().toString().trim();
        String contenu = etContenu.getText().toString().trim();

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

        String date = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
                .format(new Date());

        Note note = new Note(titre, contenu, couleurChoisie, date, false);

        new Thread(() -> {
            repository.insert(note);
            runOnUiThread(() -> {
                Toast.makeText(this, "Note créée !", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }).start();
    }
}