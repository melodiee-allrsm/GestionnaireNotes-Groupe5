package com.example.gestionnairenotes.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionnairenotes.R;
import com.example.gestionnairenotes.model.Note;
import com.example.gestionnairenotes.service.NoteRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    private static final int REQUEST_CREATE_NOTE = 1;
    private static final int REQUEST_EDIT_NOTE   = 2;

    private NoteAdapter adapter;
    private NoteRepository repository;

    private EditText etRecherche;
    private Button btnFavoris;
    private ImageView btnTri;
    private TextView tvCompteurNotes;
    private TextView tvAucuneNote; // Nouvelle variable ajoutée
    private RecyclerView recyclerView; // Passé en variable d'instance pour y accéder partout
    private FloatingActionButton fab;
    private LinearLayout layoutPalette;

    private boolean filtreFavorisActif = false;
    private boolean isPaletteVisible = false;
    private boolean triParTitre = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new NoteRepository(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NoteAdapter(this, List.of(), this);
        recyclerView.setAdapter(adapter);

        etRecherche     = findViewById(R.id.etRecherche);
        btnFavoris      = findViewById(R.id.btnFavoris);
        btnTri          = findViewById(R.id.btnTri);
        tvCompteurNotes = findViewById(R.id.tvCompteurNotes);
        tvAucuneNote    = findViewById(R.id.tvAucuneNote); // Initialisation de la vue
        fab             = findViewById(R.id.fab);
        layoutPalette   = findViewById(R.id.layoutPalette);

        // --- Recherche en temps réel ---
        etRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chargerNotes(s.toString().trim());
            }
        });

        // --- Bouton Favoris ---
        btnFavoris.setOnClickListener(v -> {
            filtreFavorisActif = !filtreFavorisActif;
            mettreAJourBoutonFavoris();
            chargerNotes(etRecherche.getText() != null ? etRecherche.getText().toString().trim() : "");
        });

        // --- Action Bouton de Tri ---
        btnTri.setOnClickListener(v -> {
            triParTitre = !triParTitre;
            if (triParTitre) {
                btnTri.setImageResource(android.R.drawable.ic_menu_sort_alphabetically);
                Toast.makeText(this, "Tri par titre (A-Z)", Toast.LENGTH_SHORT).show();
            } else {
                btnTri.setImageResource(android.R.drawable.ic_menu_sort_by_size);
                Toast.makeText(this, "Tri par date", Toast.LENGTH_SHORT).show();
            }
            chargerNotes(etRecherche.getText() != null ? etRecherche.getText().toString().trim() : "");
        });

        // --- Palette de couleurs ---
        findViewById(R.id.colorVert).setOnClickListener(v -> ouvrirCreation("#219653"));
        findViewById(R.id.colorRouge).setOnClickListener(v -> ouvrirCreation("#EB5757"));
        findViewById(R.id.colorBleu).setOnClickListener(v -> ouvrirCreation("#2F80ED"));
        findViewById(R.id.colorJaune).setOnClickListener(v -> ouvrirCreation("#F2C94C"));
        findViewById(R.id.colorOrange).setOnClickListener(v -> ouvrirCreation("#F2994A"));
        findViewById(R.id.colorGris).setOnClickListener(v -> ouvrirCreation("#828282"));

        // --- FAB ---
        fab.setOnClickListener(v -> {
            if (isPaletteVisible) {
                cacherPalette();
            } else {
                afficherPalette();
            }
        });

        chargerNotes("");
    }

    private void chargerNotes(String query) {
        new Thread(() -> {
            List<Note> notes;

            if (filtreFavorisActif) {
                notes = repository.getFavoris();
            } else if (!query.isEmpty()) {
                notes = repository.searchByTitle(query);
            } else {
                notes = repository.getAll();
            }

            if (triParTitre) {
                Collections.sort(notes, (n1, n2) -> n1.getTitre().compareToIgnoreCase(n2.getTitre()));
            } else {
                Collections.sort(notes, (n1, n2) -> n2.getDate().compareTo(n1.getDate()));
            }

            List<Note> finalNotes = notes;
            runOnUiThread(() -> {
                // Gestion dynamique de l'affichage si la liste est vide
                if (finalNotes == null || finalNotes.isEmpty()) {
                    tvAucuneNote.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvAucuneNote.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                adapter.setNotes(finalNotes);
                mettreAJourCompteur(finalNotes.size());
            });
        }).start();
    }

    private void mettreAJourCompteur(int taille) {
        if (taille <= 1) {
            tvCompteurNotes.setText(taille + " note");
        } else {
            tvCompteurNotes.setText(taille + " notes");
        }
    }

    private void mettreAJourBoutonFavoris() {
        if (filtreFavorisActif) {
            btnFavoris.setText("★ Favoris");
            btnFavoris.setTextColor(Color.parseColor("#E53935"));
        } else {
            btnFavoris.setText("Favoris");
            btnFavoris.setTextColor(Color.parseColor("#000000"));
        }
    }

    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra(EditNoteActivity.extra_note_id, note.getId());
        startActivityForResult(intent, REQUEST_EDIT_NOTE);
    }

    @Override
    public void onNoteDoubleClick(Note note) {
        note.setFavori(!note.isFavori());
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        new Thread(() -> {
            repository.update(note);
        }).start();
    }

    @Override
    public void onNoteLongClick(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la note")
                .setMessage("Voulez-vous vraiment supprimer la note \"" + note.getTitre() + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    new Thread(() -> {
                        repository.delete(note);
                        chargerNotes(etRecherche.getText() != null ? etRecherche.getText().toString().trim() : "");
                    }).start();
                    Toast.makeText(this, "Note supprimée", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        chargerNotes(etRecherche.getText() != null ? etRecherche.getText().toString().trim() : "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            chargerNotes(etRecherche.getText() != null ? etRecherche.getText().toString().trim() : "");
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