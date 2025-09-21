package com.example.l2_20223209;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.l2_20223209.data.HistoryManager;
import com.example.l2_20223209.databinding.ActivityCatDisplayBinding;
import com.example.l2_20223209.viewmodel.CatDisplayViewModel;

public class CatDisplayActivity extends AppCompatActivity {

    public static final String EXTRA_QUANTITY = "extra_quantity";
    public static final String EXTRA_TEXT = "extra_text";

    private ActivityCatDisplayBinding binding;
    private CatDisplayViewModel viewModel;
    private HistoryManager historyManager;
    private int quantity;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityCatDisplayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar ViewModel
        viewModel = new CatDisplayViewModel();
        
        // Inicializar HistoryManager
        historyManager = HistoryManager.getInstance(this);

        // Obtener datos del Intent
        Intent intent = getIntent();
        quantity = intent.getIntExtra(EXTRA_QUANTITY, 1);
        text = intent.getStringExtra(EXTRA_TEXT);

        setupViews(quantity);
        setupViewModelCallbacks();
        
        // Inicializar ViewModel con datos
        viewModel.initialize(quantity, text);
        
        setupClickListeners();
    }

    private void setupViews(int quantity) {
        // Configurar texto de cantidad
        binding.quantityText.setText("Cantidad = " + quantity);
        
        // Configurar estado inicial del botón
        binding.nextButton.setEnabled(false);
        binding.nextButton.setBackgroundTintList(
            getColorStateList(R.color.button_disabled_color)
        );
    }

    private void setupViewModelCallbacks() {
        // Callback para actualización del timer
        viewModel.setTimerUpdateCallback(timeSeconds -> {
            runOnUiThread(() -> {
                int minutes = timeSeconds / 60;
                int seconds = timeSeconds % 60;
                String timeText = String.format("%02d:%02d", minutes, seconds);
                binding.timerText.setText(timeText);
            });
        });

        // Callback para cargar nueva imagen
        viewModel.setImageUrlCallback(imageUrl -> {
            runOnUiThread(() -> {
                loadImage(imageUrl);
            });
        });

        // Callback para habilitar botón siguiente
        viewModel.setNextButtonEnabledCallback(isEnabled -> {
            runOnUiThread(() -> {
                binding.nextButton.setEnabled(isEnabled);
                if (isEnabled) {
                    binding.nextButton.setBackgroundTintList(
                        getColorStateList(R.color.button_enabled_color)
                    );
                } else {
                    binding.nextButton.setBackgroundTintList(
                        getColorStateList(R.color.button_disabled_color)
                    );
                }
            });
        });

        // Callback para mostrar errores
        viewModel.setErrorCallback(errorMessage -> {
            runOnUiThread(() -> {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                showErrorState();
            });
        });
    }

    private void loadImage(String imageUrl) {
        Log.d("CatDisplay", "Cargando imagen: " + imageUrl);
        
        // Mostrar loading
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        binding.catImage.setVisibility(View.GONE);
        binding.errorLayout.setVisibility(View.GONE);

        // Cargar imagen con Glide con configuración optimizada
        Glide.with(this)
                .load(imageUrl)
                .timeout(8000) // Timeout de 8 segundos
                .centerCrop() // Ajustar imagen al contenedor
                .into(binding.catImage);
                
        // Simular carga exitosa después de un breve delay para permitir que Glide procese
        binding.catImage.postDelayed(() -> {
            Log.d("CatDisplay", "Imagen procesada para: " + imageUrl);
            showImageLoaded();
            viewModel.onImageLoadSuccess();
        }, 1500); // Dar más tiempo para la carga
    }

    private void showImageLoaded() {
        binding.loadingIndicator.setVisibility(View.GONE);
        binding.catImage.setVisibility(View.VISIBLE);
        binding.errorLayout.setVisibility(View.GONE);
    }

    private void showErrorState() {
        binding.loadingIndicator.setVisibility(View.GONE);
        binding.catImage.setVisibility(View.GONE);
        binding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        binding.nextButton.setOnClickListener(v -> {
            if (viewModel.isTimerFinished()) {
                // Guardar esta interacción en el historial
                historyManager.addInteraction(text, quantity);
                
                // Navegar a la pantalla de historial
                Intent intent = new Intent(CatDisplayActivity.this, HistoryActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.resumeTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.pauseTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            viewModel.cleanup();
        }
        if (binding != null) {
            binding = null;
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // El timer continúa ejecutándose durante rotaciones de pantalla
        // No necesitamos hacer nada especial aquí ya que el ViewModel maneja el estado
    }
}