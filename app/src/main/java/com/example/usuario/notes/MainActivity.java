package com.example.usuario.notes;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    public static final String dbName = "Notes.db";
    public static final String tableName = "notes";
    private ListView lv;
    private Button buttonCreate;
    private ArrayList<Note> contents;
    private SQLiteDatabase db;
    private ArrayAdapter<Note> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.notes_view);
        buttonCreate = (Button) findViewById(R.id.button_create_new);
        db = openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (id INT, title VARCHAR(255), content VARCHAR(8000))");
        contents = new ArrayList<>();
        this.fillList(contents);
        db.close();
        adapter = new NoteAdapter(this, contents);
        lv.setAdapter(adapter);
        lv.setLongClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent i = new Intent(getApplicationContext(), NoteActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("note", contents.get(position));
                i.putExtras(b);
                startActivity(i);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete the note?")
                        .setPositiveButton(R.string.positive_confirmation, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteRow(position);
                            }
                        })
                        .setNegativeButton(R.string.negative_confirmation, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                return true;
            }
        });

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), NewNoteActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        db = openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
        contents.clear();
        this.fillList(contents);
        db.close();
        adapter.notifyDataSetChanged();
    }

    private void deleteRow(int position){
        Note n = contents.get(position);
        db = openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
        db.execSQL("DELETE FROM " + tableName + " WHERE id=" + n.getId());
        contents.clear();
        fillList(contents);
        db.close();
        adapter.notifyDataSetChanged();
    }

    private void fillList(List<Note> list){
        if(db.isOpen()){
            Cursor c = db.rawQuery("SELECT * FROM " + tableName, null);
            if(c.getCount() > 0){
                c.moveToFirst();
                while(!c.isAfterLast()) {
                    list.add(new Note(c.getInt(0), c.getString(1), c.getString(2)));
                    c.moveToNext();
                }
            }
            c.close();
        }
    }

    private class NoteAdapter extends ArrayAdapter<Note>{
        ArrayList<Note> notes;
        Context context;

        public NoteAdapter(Context context, ArrayList<Note> notes){
            super(context, R.layout.activity_list_item, notes);
            this.context = context;
            this.notes = notes;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Note n = notes.get(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null)
                convertView = inflater.inflate(R.layout.activity_list_item, parent, false);


            TextView title = (TextView) convertView.findViewById(R.id.list_note_title);
            title.setText(n.getTitle());
            return convertView;
        }
    }
}
