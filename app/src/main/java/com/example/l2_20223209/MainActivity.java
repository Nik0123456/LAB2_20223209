package com.example.l2_20223209;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.l2_20223209.databinding.ActivityMainBinding;
import com.example.l2_20223209.viewmodel.MainViewModel;
import com.example.l2_20223209.network.ConnectivityChecker;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private ConnectivityChecker connectivityChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Configurar View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Configurar insets para Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Inicializar ViewModel y ConnectivityChecker
        viewModel = new MainViewModel();
        connectivityChecker = new ConnectivityChecker(this);
        
        setupViews();
        setupViewModelCallbacks();
    }
    
    private void setupViews() {
        // Configurar el spinner de texto
        setupTextoSpinner();
        
        // Configurar text watchers para los campos de entrada
        setupTextWatchers();
        
        // Configurar click listeners para los botones
        setupClickListeners();
    }
    
    private void setupTextoSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this,
            R.array.texto_options,
            android.R.layout.simple_dropdown_item_1line
        );
        binding.textoSpinner.setAdapter(adapter);
        
        // Limpiar el texto inicial para que solo se muestre el hint
        binding.textoSpinner.setText("", false);
        
        // Configurar listener para el spinner
        binding.textoSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            viewModel.setTextoSeleccionado(selectedItem);
        });
    }
    
    private void setupTextWatchers() {
        // TextWatcher para el campo cantidad
        binding.cantidadEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setCantidad(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // TextWatcher para el campo escribir texto
        binding.escribirTextoEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setTextoEscrito(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupClickListeners() {
        // Click listener para el botón comprobar conexión
        binding.comprobarConexionButton.setOnClickListener(v -> {
            // Deshabilitar el botón temporalmente
            binding.comprobarConexionButton.setEnabled(false);
            binding.comprobarConexionButton.setText("Verificando...");
            
            // Verificar conectividad usando ConnectivityChecker
            boolean hasConnection = connectivityChecker.isConnectedToInternet();
            
            // Restaurar el botón
            binding.comprobarConexionButton.setEnabled(true);
            binding.comprobarConexionButton.setText(getString(R.string.comprobar_conexion));
            
            // Actualizar ViewModel con el resultado
            viewModel.setConexionVerificada(hasConnection);
            
            // Si hay conexión real, hacer verificación en background
            if (hasConnection) {
                viewModel.checkInternetConnection();
            } else {
                Toast.makeText(this, "Sin conexión a internet", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Click listener para el botón comenzar
        binding.comenzarButton.setOnClickListener(v -> {
            if (viewModel.isFormValid() && viewModel.getConnectionStatus()) {
                // Obtener datos del formulario
                MainViewModel.FormData formData = viewModel.getFormData();
                
                // Crear intent para la nueva actividad
                Intent intent = new Intent(MainActivity.this, CatDisplayActivity.class);
                intent.putExtra(CatDisplayActivity.EXTRA_QUANTITY, 
                               Integer.parseInt(formData.cantidad));
                
                // Solo pasar texto si se seleccionó "Sí"
                if ("Sí".equals(formData.textoSeleccionado)) {
                    intent.putExtra(CatDisplayActivity.EXTRA_TEXT, formData.textoEscrito);
                } else {
                    intent.putExtra(CatDisplayActivity.EXTRA_TEXT, ""); // Texto vacío
                }
                
                // Iniciar actividad
                startActivity(intent);
            } else {
                viewModel.showValidationError();
            }
        });
    }
    
    private void setupViewModelCallbacks() {
        // Callback para estado de conexión
        viewModel.setConnectionStatusCallback(isConnected -> {
            runOnUiThread(() -> {
                if (isConnected) {
                    binding.comprobarConexionButton.setIconResource(R.drawable.ic_wifi);
                }
            });
        });
        
        // Callback para mensajes de Toast
        viewModel.setToastMessageCallback(message -> {
            runOnUiThread(() -> {
                if (message != null && !message.isEmpty()) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        // Callback para estado del botón Comenzar
        viewModel.setBeginButtonEnabledCallback(isEnabled -> {
            runOnUiThread(() -> {
                binding.comenzarButton.setEnabled(isEnabled);
            });
        });
        
        // Callback para estado del campo "Escribir texto"
        viewModel.setTextInputEnabledCallback(isEnabled -> {
            runOnUiThread(() -> {
                binding.escribirTextoEditText.setEnabled(isEnabled);
                binding.escribirTextoInputLayout.setEnabled(isEnabled);
                
                // Limpiar el campo si se deshabilita
                if (!isEnabled) {
                    binding.escribirTextoEditText.setText("");
                }
            });
        });
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
}