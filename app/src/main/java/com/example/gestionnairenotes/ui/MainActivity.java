package com.example.gestionnairenotes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionnairenotes.R;
import com.example.gestionnairenotes.model.Note;
import com.example.gestionnairenotes.service.NoteRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    // Clé partagée pour passer une note via Intent
    public static final String EXTRA_NOTE_ID = "extra_note_id";

    // Codes de retour pour startActivityForResult
    public static final int REQUEST_CREATE_NOTE = 1;
    public static final int REQUEST_EDIT_NOTE   = 2;

    private NoteRepository repository;
    private NoteAdapter adapter;

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private SearchView searchView;
    private Button btnFavoris;
    private FloatingActionButton fab;

    private LinearLayout layoutPalette;
    private boolean isPaletteVisible = false;

    private boolean showFavorisOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new NoteRepository(this);

        // Références vues
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty      = findViewById(R.id.tvEmpty);
        searchView   = findViewById(R.id.searchView);
        btnFavoris   = findViewById(R.id.btnFavoris);
        fab          = findViewById(R.id.fab);

        // RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Note> notes = repository.getAll();
        adapter = new NoteAdapter(this, notes, this);
        recyclerView.setAdapter(adapter);
        updateEmptyState(notes);

        // Recherche en temps réel (déléguée à Dev 6, mais SearchView configuré ici)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return true;
            }
        });

        // Bouton Favoris — toggle filtre
        btnFavoris.setOnClickListener(v -> {
            showFavorisOnly = !showFavorisOnly;
            if (showFavorisOnly) {
                btnFavoris.setAlpha(1.0f);
                List<Note> favoris = repository.getFavoris();
                adapter.setNotes(favoris);
                updateEmptyState(favoris);
            } else {
                btnFavoris.setAlpha(0.5f);
                List<Note> allNotes = repository.getAll();
                adapter.setNotes(allNotes);
                updateEmptyState(allNotes);
            }
        });
        btnFavoris.setAlpha(0.5f); // inactif par défaut

        layoutPalette = findViewById(R.id.layoutPalette);

        findViewById(R.id.colorVert).setOnClickListener(v -> ouvrirCreation("#219653"));
        findViewById(R.id.colorRouge).setOnClickListener(v -> ouvrirCreation("#EB5757"));
        findViewById(R.id.colorBleu).setOnClickListener(v -> ouvrirCreation("#2F80ED"));
        findViewById(R.id.colorJaune).setOnClickListener(v -> ouvrirCreation("#F2C94C"));
        findViewById(R.id.colorOrange).setOnClickListener(v -> ouvrirCreation("#F2994A"));
        findViewById(R.id.colorGris).setOnClickListener(v -> ouvrirCreation("#828282"));

        fab.setOnClickListener(v -> {
            if (isPaletteVisible) {
                cacherPalette();
            } else {
                afficherPalette();
            }
        });
    }

    // ----- NoteAdapter.OnNoteClickListener -----

    @Override
    public void onNoteClick(Note note) {
        // Clic simple → écran de modification
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, note.getId());
        startActivityForResult(intent, REQUEST_EDIT_NOTE);
    }

    @Override
    public void onNoteDoubleClick(Note note) {
        // Double-clic → toggle favori (Parcours 3)
        note.setFavori(!note.isFavori());
        repository.update(note);
        refreshList();
    }

    // ----- Retour des activités -----

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            refreshList();
        }
    }

    // ----- Méthodes utilitaires -----

    private void refreshList() {
        List<Note> notes = showFavorisOnly ? repository.getFavoris() : repository.getAll();
        adapter.setNotes(notes);
        updateEmptyState(notes);
    }

    private void filterNotes(String query) {
        List<Note> filtered;
        if (query == null || query.trim().isEmpty()) {
            filtered = showFavorisOnly ? repository.getFavoris() : repository.getAll();
        } else {
            filtered = repository.searchByTitle(query.trim());
        }
        adapter.setNotes(filtered);
        updateEmptyState(filtered);
    }

    private void updateEmptyState(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void afficherPalette() {
        layoutPalette.setVisibility(View.VISIBLE);
        layoutPalette.setAlpha(0f);
        layoutPalette.animate().alpha(1f).setDuration(300).start();
        isPaletteVisible = true;
    }

    private void cacherPalette() {
        layoutPalette.animate().alpha(0f).setDuration(300).withEndAction(() ->
                layoutPalette.setVisibility(View.GONE)
        ).start();
        isPaletteVisible = false;
    }

    private void ouvrirCreation(String couleur) {
        cacherPalette();
        Intent intent = new Intent(this, CreateNoteActivity.class);
        intent.putExtra("COULEUR", couleur);
        startActivityForResult(intent, REQUEST_CREATE_NOTE);
    }
}