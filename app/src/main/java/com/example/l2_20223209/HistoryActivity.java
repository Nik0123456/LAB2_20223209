package com.example.l2_20223209;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.l2_20223209.adapter.HistoryAdapter;
import com.example.l2_20223209.data.HistoryManager;
import com.example.l2_20223209.databinding.ActivityHistoryBinding;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private HistoryManager historyManager;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar HistoryManager
        historyManager = HistoryManager.getInstance(this);

        setupRecyclerView();
        loadHistory();
        setupClickListeners();
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.historyRecyclerView.setAdapter(adapter);
    }

    private void loadHistory() {
        List<HistoryManager.Interaction> history = historyManager.getHistory();
        
        if (history.isEmpty()) {
            // Mostrar estado vacío
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.historyRecyclerView.setVisibility(View.GONE);
        } else {
            // Mostrar historial
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.historyRecyclerView.setVisibility(View.VISIBLE);
            adapter.setInteractions(history);
        }
    }

    private void setupClickListeners() {
        binding.playAgainButton.setOnClickListener(v -> {
            showPlayAgainDialog();
        });
    }

    private void showPlayAgainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Estás seguro?");
        builder.setMessage("¿Estás seguro que deseas volver a jugar?");
        builder.setIcon(R.drawable.ic_warning);
        
        // Botón "Sí"
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Volver al menú principal SIN borrar el historial
                Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        
        // Botón "No"
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Personalizar colores de los botones
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.accent_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.on_surface_variant_color));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}