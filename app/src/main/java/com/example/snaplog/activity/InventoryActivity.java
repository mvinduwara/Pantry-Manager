package com.example.snaplog.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snaplog.BuildConfig;
import com.example.snaplog.R;
import com.example.snaplog.adapter.PantryAdapter;
import com.example.snaplog.database.AppDatabase;
import com.example.snaplog.database.PantryItem;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryActivity extends AppCompatActivity {

    private PantryAdapter adapter;
    private ExecutorService databaseExecutor;
    private TextView recipeResultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        recipeResultTextView = findViewById(R.id.recipeResultTextView);
        Button generateRecipeButton = findViewById(R.id.generateRecipeButton);

        RecyclerView recyclerView = findViewById(R.id.inventoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PantryAdapter();
        recyclerView.setAdapter(adapter);

        databaseExecutor = Executors.newSingleThreadExecutor();
        loadInventory();

        generateRecipeButton.setOnClickListener(v -> generateRecipeWithAI());

        new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                PantryItem itemToDelete = adapter.getItemAt(position);

                databaseExecutor.execute(() -> {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    db.pantryItemDao().delete(itemToDelete);
                    loadInventory();
                });

                Toast.makeText(InventoryActivity.this, "Item consumed and removed!", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void loadInventory() {
        databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<PantryItem> items = db.pantryItemDao().getAllItems();
            runOnUiThread(() -> adapter.setItems(items));
        });
    }

    private void generateRecipeWithAI() {
        recipeResultTextView.setText("Chef Gemini is thinking...");

        databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<PantryItem> items = db.pantryItemDao().getAllItems();

            if (items.isEmpty()) {
                runOnUiThread(() -> recipeResultTextView.setText("Your pantry is empty! Scan some items first."));
                return;
            }

            StringBuilder promptBuilder = new StringBuilder("I have the following ingredients in my pantry: ");
            for (PantryItem item : items) {
                promptBuilder.append(item.name).append(", ");
            }
            promptBuilder.append("I am an amateur cook. Give me one creative, easy-to-make recipe using primarily these ingredients. Format it with a title, ingredients list, and clear numbered steps.");

            String finalPrompt = promptBuilder.toString();

            GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", BuildConfig.GEMINI_API_KEY);
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            Content content = new Content.Builder().addText(finalPrompt).build();
            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    runOnUiThread(() -> recipeResultTextView.setText(result.getText()));
                }

                @Override
                public void onFailure(Throwable t) {
                    runOnUiThread(() -> recipeResultTextView.setText("Failed to generate recipe: " + t.getMessage()));
                }
            }, ContextCompat.getMainExecutor(InventoryActivity.this));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }
}