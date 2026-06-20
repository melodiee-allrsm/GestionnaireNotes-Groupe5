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
        void onNoteClick(Note note);           // Clic simple -> Édition
        void onNoteDoubleClick(Note note);     // Double clic -> Favori
        void onNoteLongClick(Note note);       // Clic long -> Suppression
    }

    private List<Note> notes;
    private final Context context;
    private final OnNoteClickListener listener;
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

        try {
            holder.cardNote.setCardBackgroundColor(Color.parseColor(note.getCouleur()));
        } catch (IllegalArgumentException e) {
            holder.cardNote.setCardBackgroundColor(Color.WHITE);
        }

        if (note.isFavori()) {
            holder.ivFavori.setVisibility(View.VISIBLE);
        } else {
            holder.ivFavori.setVisibility(View.INVISIBLE);
        }

        // --- Gestion des Clics Simples et Doubles ---
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            private int clickCount = 0;
            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void onClick(View v) {
                clickCount++;
                if (clickCount == 1) {
                    handler.postDelayed(() -> {
                        if (clickCount == 1) {
                            if (listener != null) listener.onNoteClick(note);
                        }
                        clickCount = 0;
                    }, DOUBLE_CLICK_DELAY);
                } else if (clickCount == 2) {
                    handler.removeCallbacksAndMessages(null);
                    clickCount = 0;
                    if (listener != null) listener.onNoteDoubleClick(note);
                }
            }
        });

        // --- Gestion du Clic Long pour Supprimer ---
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onNoteLongClick(note);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

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

            // Empêche la CardView de bloquer le clic long sur l'item complet
            cardNote.setLongClickable(false);
        }
    }
}