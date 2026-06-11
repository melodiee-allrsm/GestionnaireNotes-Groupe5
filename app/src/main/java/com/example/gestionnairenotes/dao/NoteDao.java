package com.example.gestionnairenotes.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import com.example.gestionnairenotes.model.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE favori = 1 ORDER BY id DESC")
    List<Note> getFavoriteNotes();

    @Query("SELECT * FROM notes WHERE titre LIKE '%' || :search || '%'")
    List<Note> searchNotes(String search);
}
