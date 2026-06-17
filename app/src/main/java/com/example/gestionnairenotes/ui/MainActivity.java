package com.example.gestionnairenotes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionnairenotes.R;
import com.example.gestionnairenotes.model.Note;
import com.example.gestionnairenotes.service.NoteRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    // Codes de requête pour startActivityForResult
    private static final int REQUEST_CREATE_NOTE = 1;
    private static final int REQUEST_EDIT_NOTE   = 2;

    private NoteAdapter adapter;
    private NoteRepository repository;

    private TextInputEditText etRecherche;
    private Button btnFavoris;
    private FloatingActionButton fab;
    private LinearLayout layoutPalette;

    // On garde en mémoire si le filtre favoris est actif ou non
    private boolean filtreFavorisActif = false;
    private boolean isPaletteVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new NoteRepository(this);

        // Initialisation du RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // L'adapter démarre avec une liste vide, on la remplit juste après
        adapter = new NoteAdapter(this, List.of(), this);
        recyclerView.setAdapter(adapter);

        etRecherche = findViewById(R.id.etRecherche);
        btnFavoris  = findViewById(R.id.btnFavoris);
        fab         = findViewById(R.id.fab);
        layoutPalette = findViewById(R.id.layoutPalette);

        // --- Recherche en temps réel ---
        etRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                chargerNotes(query);
            }
        });

        // --- Bouton Favoris ---
        btnFavoris.setOnClickListener(v -> {
            filtreFavorisActif = !filtreFavorisActif;
            mettreAJourBoutonFavoris();
            chargerNotes(etRecherche.getText() != null ? etRecherche.getText().toString().trim() : "");
        });

        // --- Palette de couleurs (cercles) ---
        // Chaque cercle de couleur ouvre l'écran de création avec la couleur pré-sélectionnée
        findViewById(R.id.colorVert).setOnClickListener(v -> ouvrirCreation("#219653"));
        findViewById(R.id.colorRouge).setOnClickListener(v -> ouvrirCreation("#EB5757"));
        findViewById(R.id.colorBleu).setOnClickListener(v -> ouvrirCreation("#2F80ED"));
        findViewById(R.id.colorJaune).setOnClickListener(v -> ouvrirCreation("#F2C94C"));
        findViewById(R.id.colorOrange).setOnClickListener(v -> ouvrirCreation("#F2994A"));
        findViewById(R.id.colorGris).setOnClickListener(v -> ouvrirCreation("#828282"));

        // --- FAB : affiche ou cache la palette de couleurs ---
        fab.setOnClickListener(v -> {
            if (isPaletteVisible) {
                cacherPalette();
            } else {
                afficherPalette();
            }
        });

        // Chargement initial : toutes les notes
        chargerNotes("");
    }

    /**
     * Recharge la liste dans un thread séparé (Room bloque le main thread).
     * Si le filtre favoris est actif, on ne montre que les notes favorites.
     * Sinon, si une query est saisie, on filtre par titre ; sinon on affiche tout.
     */
    private void chargerNotes(String query) {
        new Thread(() -> {
            List<Note> notes;

            if (filtreFavorisActif) {
                // Mode favoris : on ignore la recherche textuelle pour simplifier
                notes = repository.getFavoris();
            } else if (!query.isEmpty()) {
                // Recherche en cours : on filtre par titre via la requête SQL LIKE
                notes = repository.searchByTitle(query);
            } else {
                // Aucun filtre : toutes les notes
                notes = repository.getAll();
            }

            // Mise à jour de l'UI toujours sur le main thread
            List<Note> finalNotes = notes;
            runOnUiThread(() -> adapter.setNotes(finalNotes));
        }).start();
    }

    /**
     * Met à jour l'apparence du bouton Favoris pour indiquer visuellement l'état actif/inactif.
     * Actif = rouge + texte "★ Favoris ON", inactif = gris + texte "★ Favoris".
     */
    private void mettreAJourBoutonFavoris() {
        if (filtreFavorisActif) {
            btnFavoris.setText("★ Favoris ON");
            btnFavoris.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E53935"))
            );
        } else {
            btnFavoris.setText("★ Favoris");
            btnFavoris.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#BDBDBD"))
            );
        }
    }

    // --- Implémentation de l'interface NoteAdapter.OnNoteClickListener ---

    /**
     * Simple clic → on ouvre l'écran d'édition en passant les données de la note via l'Intent.
     * On passe tout manuellement plutôt que de sérialiser l'objet pour rester simple.
     */
    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra(EditNoteActivity.extra_note_id,     (long) note.getId());
        intent.putExtra(EditNoteActivity.EXTRA_NOTE_TITRE,   note.getTitre());
        intent.putExtra(EditNoteActivity.EXTRA_NOTE_CONTENU, note.getContenu());
        intent.putExtra(EditNoteActivity.EXTRA_NOTE_COULEUR, note.getCouleur());
        intent.putExtra(EditNoteActivity.EXTRA_NOTE_FAVORI,  note.isFavori());
        startActivity(intent);
    }

    /**
     * Double clic → on inverse l'état favori de la note et on recharge la liste.
     * L'animation de l'étoile dans la carte se met à jour automatiquement via setNotes().
     */
    @Override
    public void onNoteDoubleClick(Note note) {
        note.setFavori(!note.isFavori()); // toggle

        new Thread(() -> {
            repository.update(note); // on persist le changement en base
            // Ensuite on recharge pour que l'étoile se mette à jour visuellement
            chargerNotes(etRecherche.getText() != null ? etRecherche.getText().toString().trim() : "");
        }).start();
    }

    /**
     * onResume est appelé automatiquement quand on revient de EditNoteActivity ou CreateNoteActivity.
     * C'est le bon endroit pour recharger la liste et afficher les modifications.
     */
    @Override
    protected void onResume() {
        super.onResume();
        chargerNotes(etRecherche.getText() != null ? etRecherche.getText().toString().trim() : "");
    }

    // --- Gestion du retour des activités ---

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Si une note a été créée ou modifiée, on recharge la liste
        if (resultCode == RESULT_OK) {
            chargerNotes(etRecherche.getText() != null ? etRecherche.getText().toString().trim() : "");
        }
    }

    // --- Palette de couleurs ---

    /**
     * Affiche la palette de couleurs avec une animation de fondu.
     */
    private void afficherPalette() {
        layoutPalette.setVisibility(View.VISIBLE);
        layoutPalette.setAlpha(0f);
        layoutPalette.animate().alpha(1f).setDuration(300).start();
        isPaletteVisible = true;
    }

    /**
     * Cache la palette de couleurs avec une animation de fondu.
     */
    private void cacherPalette() {
        layoutPalette.animate().alpha(0f).setDuration(300).withEndAction(() ->
                layoutPalette.setVisibility(View.GONE)
        ).start();
        isPaletteVisible = false;
    }

    /**
     * Ouvre l'écran de création avec la couleur sélectionnée.
     */
    private void ouvrirCreation(String couleur) {
        cacherPalette();
        Intent intent = new Intent(this, CreateNoteActivity.class);
        intent.putExtra("COULEUR", couleur);
        startActivityForResult(intent, REQUEST_CREATE_NOTE);
    }
}