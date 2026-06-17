// package com.example.gestionnairenotes.ui;

// import android.content.Context;
// import android.graphics.Color;
// import android.os.Handler;
// import android.os.Looper;
// import android.view.LayoutInflater;
// import android.view.View;
// import android.view.ViewGroup;
// import android.widget.ImageView;
// import android.widget.TextView;

// import androidx.annotation.NonNull;
// import androidx.cardview.widget.CardView;
// import androidx.recyclerview.widget.RecyclerView;

// import com.example.gestionnairenotes.R;
// import com.example.gestionnairenotes.model.Note;

// import java.util.List;

// public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

//     public interface OnNoteClickListener {
//         void onNoteClick(Note note);           // clic simple → édition
//         void onNoteDoubleClick(Note note);     // double-clic → toggle favori
//     }

//     private List<Note> notes;
//     private final Context context;
//     private final OnNoteClickListener listener;

//     // Délai pour distinguer simple/double clic (ms)
//     private static final long DOUBLE_CLICK_DELAY = 300;

//     public NoteAdapter(Context context, List<Note> notes, OnNoteClickListener listener) {
//         this.context = context;
//         this.notes = notes;
//         this.listener = listener;
//     }

//     @NonNull
//     @Override
//     public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//         View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
//         return new NoteViewHolder(view);
//     }

//     @Override
//     public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
//         Note note = notes.get(position);

//         holder.tvTitre.setText(note.getTitre());
//         holder.tvContenu.setText(note.getContenu());
//         holder.tvDate.setText(note.getDate());

//         // Couleur de fond dynamique selon la couleur de la note
//         try {
//             holder.cardNote.setCardBackgroundColor(Color.parseColor(note.getCouleur()));
//         } catch (IllegalArgumentException e) {
//             holder.cardNote.setCardBackgroundColor(Color.WHITE);
//         }

//         // Icône favori
//         if (note.isFavori()) {
//             holder.ivFavori.setImageResource(android.R.drawable.btn_star_big_on);
//         } else {
//             holder.ivFavori.setImageResource(android.R.drawable.btn_star_big_off);
//         }

//         // Gestion du simple et double clic
//         holder.itemView.setOnClickListener(new View.OnClickListener() {
//             private int clickCount = 0;
//             private final Handler handler = new Handler(Looper.getMainLooper());

//             @Override
//             public void onClick(View v) {
//                 clickCount++;
//                 if (clickCount == 1) {
//                     handler.postDelayed(() -> {
//                         if (clickCount == 1) {
//                             // Simple clic → navigation vers édition
//                             listener.onNoteClick(note);
//                         }
//                         clickCount = 0;
//                     }, DOUBLE_CLICK_DELAY);
//                 } else if (clickCount == 2) {
//                     handler.removeCallbacksAndMessages(null);
//                     clickCount = 0;
//                     // Double clic → toggle favori
//                     listener.onNoteDoubleClick(note);
//                 }
//             }
//         });
//     }

//     @Override
//     public int getItemCount() {
//         return notes != null ? notes.size() : 0;
//     }

//     /**
//      * Met à jour la liste affichée (recherche, filtre favoris, rechargement)
//      */
//     public void setNotes(List<Note> newNotes) {
//         this.notes = newNotes;
//         notifyDataSetChanged();
//     }

//     static class NoteViewHolder extends RecyclerView.ViewHolder {
//         CardView cardNote;
//         TextView tvTitre, tvContenu, tvDate;
//         ImageView ivFavori;

//         NoteViewHolder(@NonNull View itemView) {
//             super(itemView);
//             cardNote  = itemView.findViewById(R.id.cardNote);
//             tvTitre   = itemView.findViewById(R.id.tvTitre);
//             tvContenu = itemView.findViewById(R.id.tvContenu);
//             tvDate    = itemView.findViewById(R.id.tvDate);
//             ivFavori  = itemView.findViewById(R.id.ivFavori);
//         }
//     }
// }

package com.example.gestionnairenotes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionnairenotes.R;
import com.example.gestionnairenotes.model.Note;
import com.example.gestionnairenotes.service.NoteRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    private NoteAdapter adapter;
    private NoteRepository repository;

    private TextInputEditText etRecherche;
    private Button btnFavoris;

    // On garde en mémoire si le filtre favoris est actif ou non
    private boolean filtreFavorisActif = false;

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

        // --- Recherche en temps réel ---
        // À chaque frappe au clavier, on relance le filtre sans attendre que
        // l'utilisateur valide (c'est le TextWatcher qui s'en occupe)
        etRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // On délègue la recherche au repository qui interroge Room
                String query = s.toString().trim();
                chargerNotes(query);
            }
        });

        // --- Bouton Favoris ---
        // Chaque appui inverse l'état du filtre et recharge la liste correspondante
        btnFavoris.setOnClickListener(v -> {
            filtreFavorisActif = !filtreFavorisActif;
            mettreAJourBoutonFavoris();
            chargerNotes(etRecherche.getText() != null ? etRecherche.getText().toString().trim() : "");
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
        intent.putExtra(EditNoteActivity.EXTRA_NOTE_ID,     (long) note.getId());
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
     * onResume est appelé automatiquement quand on revient de EditNoteActivity.
     * C'est le bon endroit pour recharger la liste et afficher les modifs.
     */
    @Override
    protected void onResume() {
        super.onResume();
        chargerNotes(etRecherche.getText() != null ? etRecherche.getText().toString().trim() : "");
    }
}