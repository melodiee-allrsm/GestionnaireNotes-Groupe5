package com.example.gestionnairenotes;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionnairenotes.model.Note;
import com.example.gestionnairenotes.service.NoteRepository;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NoteRepository repository = new NoteRepository(this);

        Note note = new Note("Test", "Ceci est une note de test", "#219653", "11/06/2026", false);
        repository.insert(note);
        Log.d("TEST", "Note insérée !");

        List<Note> notes = repository.getAll();
        Log.d("TEST", "Nombre de notes : " + notes.size());
    }
}