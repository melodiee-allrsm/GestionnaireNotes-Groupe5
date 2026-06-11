package com.example.gestionnairenotes.service;

import android.content.Context;

import com.example.gestionnairenotes.dao.NoteDao;
import com.example.gestionnairenotes.database.NoteDatabase;
import com.example.gestionnairenotes.model.Note;

import java.util.List;

public class NoteRepository {

    private final NoteDao noteDao;

    public NoteRepository(Context context) {
        NoteDatabase db = NoteDatabase.getInstance(context);
        this.noteDao = db.noteDao();
    }

    public void insert(Note note) {
        noteDao.insert(note);
    }

    public void update(Note note) {
        noteDao.update(note);
    }

    public void delete(Note note) {
        noteDao.delete(note);
    }

    public List<Note> getAll() {
        return noteDao.getAllNotes();
    }

    public List<Note> getFavoris() {
        return noteDao.getFavoriteNotes();
    }

    public List<Note> searchByTitle(String query) {
        return noteDao.searchNotes(query);
    }
}