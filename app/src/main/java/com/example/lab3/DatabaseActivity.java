package com.example.lab3;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class DatabaseActivity extends ListActivity {
    private DBHelper dbHelper; // Ваш помощник для работы с базой данных
    private SimpleCursorAdapter adapter; // Адаптер для связывания данных с ListView



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database); // Убедитесь, что вы установили правильный макет

        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = dbHelper.getAllData();
        if(cursor.getCount() == 0) {
            Toast.makeText(DatabaseActivity.this,"Нет данных",Toast.LENGTH_LONG).show();
            return;
        }

        String[] columns = new String[] { "name", "data" };
        int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, columns, to, 0);
        setListAdapter(adapter);

        ListView listView = getListView();
        registerForContextMenu(listView);

        final EditText etName = (EditText) findViewById(R.id.etName);
        final EditText etData = (EditText) findViewById(R.id.etData);
        Button btnAdd = (Button) findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                String data = etData.getText().toString();
                dbHelper.insertData(name, data);

                // Обновляем список после добавления
                Cursor cursor = dbHelper.getAllData();
                adapter.changeCursor(cursor);
                etName.setText("");
                etData.setText("");
            }
        });
    }

    // Создаем контекстное меню
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, "Изменить");
        menu.add(0, v.getId(), 0, "Удалить");
    }

    // Обрабатываем выбор пункта контекстного меню
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getTitle() == "Изменить") {
            String id = String.valueOf(info.id);
            showUpdateDialog(id);
        } else if (item.getTitle() == "Удалить") {
            // Получаем id элемента, который нужно удалить
            String id = String.valueOf(info.id);
            // Удаляем элемент
            dbHelper.deleteData(id);
            // Обновляем список
            Cursor cursor = dbHelper.getAllData();
            adapter.changeCursor(cursor);
        } else {
            return false;
        }
        return true;
    }

    private void showUpdateDialog(final String id) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final EditText newNameInput = new EditText(this);
        newNameInput.setHint("Введите новое имя");
        final EditText newDataInput = new EditText(this);
        newDataInput.setHint("Введите новые данные");

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(newNameInput);
        layout.addView(newDataInput);
        dialogBuilder.setView(layout);

        dialogBuilder.setPositiveButton("Изменить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newName = newNameInput.getText().toString();
                String newData = newDataInput.getText().toString();
                dbHelper.updateData(id, newName, newData);
                // Обновляем список после изменения
                Cursor cursor = dbHelper.getAllData();
                adapter.changeCursor(cursor);
            }
        });
        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Ничего не делаем
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
}
