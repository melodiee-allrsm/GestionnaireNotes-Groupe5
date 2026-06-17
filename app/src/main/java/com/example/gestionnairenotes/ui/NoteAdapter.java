package com.example.gestionnairenotes.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionnairenotes.R;
import com.example.gestionnairenotes.model.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(Note note);           // clic simple → édition
        void onNoteDoubleClick(Note note);     // double-clic → toggle favori
    }

    private List<Note> notes;
    private final Context context;
    private final OnNoteClickListener listener;

    // Délai pour distinguer simple/double clic (ms)
    private static final long DOUBLE_CLICK_DELAY = 300;

    public NoteAdapter(Context context, List<Note> notes, OnNoteClickListener listener) {
        this.context = context;
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.tvTitre.setText(note.getTitre());
        holder.tvContenu.setText(note.getContenu());
        holder.tvDate.setText(note.getDate());

        // Couleur de fond dynamique selon la couleur de la note
        try {
            holder.cardNote.setCardBackgroundColor(Color.parseColor(note.getCouleur()));
        } catch (IllegalArgumentException e) {
            holder.cardNote.setCardBackgroundColor(Color.WHITE);
        }

        // Icône favori
        if (note.isFavori()) {
            holder.ivFavori.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.ivFavori.setImageResource(android.R.drawable.btn_star_big_off);
        }

        // Gestion du simple et double clic
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            private int clickCount = 0;
            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void onClick(View v) {
                clickCount++;
                if (clickCount == 1) {
                    handler.postDelayed(() -> {
                        if (clickCount == 1) {
                            // Simple clic → navigation vers édition
                            listener.onNoteClick(note);
                        }
                        clickCount = 0;
                    }, DOUBLE_CLICK_DELAY);
                } else if (clickCount == 2) {
                    handler.removeCallbacksAndMessages(null);
                    clickCount = 0;
                    // Double clic → toggle favori
                    listener.onNoteDoubleClick(note);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    /**
     * Met à jour la liste affichée (recherche, filtre favoris, rechargement)
     */
    public void setNotes(List<Note> newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        CardView cardNote;
        TextView tvTitre, tvContenu, tvDate;
        ImageView ivFavori;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNote  = itemView.findViewById(R.id.cardNote);
            tvTitre   = itemView.findViewById(R.id.tvTitre);
            tvContenu = itemView.findViewById(R.id.tvContenu);
            tvDate    = itemView.findViewById(R.id.tvDate);
            ivFavori  = itemView.findViewById(R.id.ivFavori);
        }
    }
}