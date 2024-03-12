package com.oopsipushedtomain;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class TestingActivity extends AppCompatActivity {
    /**
     * A reference to the Firestore database
     */
    private FirebaseFirestore db;
    private Spinner dropdown;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_page);

        dropdown = findViewById(R.id.selectionSpinner);
        String[] collections = new String[]{"announcements", "events", "images", "qrcodes", "users", "testwipe"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, collections);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        // Initialize Firestore instance and get the event's announcements
        db = FirebaseFirestore.getInstance();
        setListeners();
    }

    private void setListeners() {
        Button wipeCollectionButton = findViewById(R.id.btnWipeCollection);
        String collection = dropdown.getSelectedItem().toString();
        wipeCollectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(TestingActivity.this)
                        .setTitle("Wipe collection")
                        .setMessage(String.format(
                                "Are you sure you want to delete all documents in the %s collection?",
                                collection))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCollection(db.collection(dropdown.getSelectedItem().toString()));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
    }

    /**
     * Delete all documents in a collection
     */
    private void deleteCollection(CollectionReference collection) {
        collection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("testing", document.getId() + " => " + document.getData());
                                document.getReference().delete();
                            }
                            Toast.makeText(
                                    getApplicationContext(),
                                    String.format("Finished wiping %s collection", collection),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Log.d("testing", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
