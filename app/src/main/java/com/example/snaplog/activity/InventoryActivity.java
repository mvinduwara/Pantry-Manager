package com.example.snaplog.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snaplog.R;
import com.example.snaplog.adapter.PantryAdapter;
import com.example.snaplog.database.AppDatabase;
import com.example.snaplog.database.PantryItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryActivity extends AppCompatActivity {

    private PantryAdapter adapter;
    private ExecutorService databaseExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        RecyclerView recyclerView = findViewById(R.id.inventoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PantryAdapter();
        recyclerView.setAdapter(adapter);

        databaseExecutor = Executors.newSingleThreadExecutor();
        loadInventory();
    }

    private void loadInventory() {
        databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<PantryItem> items = db.pantryItemDao().getAllItems();
            runOnUiThread(() -> adapter.setItems(items));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }
}