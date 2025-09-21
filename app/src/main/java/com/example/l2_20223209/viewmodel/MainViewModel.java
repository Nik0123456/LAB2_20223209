package com.example.l2_20223209.viewmodel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MainViewModel {
    
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    
    // Callbacks para comunicarse con la UI
    private Consumer<Boolean> connectionStatusCallback;
    private Consumer<String> toastMessageCallback;
    private Consumer<Boolean> beginButtonEnabledCallback;
    private Consumer<Boolean> textInputEnabledCallback;
    
    // Variables para almacenar el estado
    private boolean connectionStatus = false;
    private boolean isBeginButtonEnabled = false;
    private boolean isTextInputEnabled = false;
    
    // Variables para almacenar los datos del formulario
    private String cantidad = "";
    private String textoSeleccionado = "Elegir";
    private String textoEscrito = "";
    private boolean conexionVerificada = false;
    
    // Métodos para configurar callbacks
    public void setConnectionStatusCallback(Consumer<Boolean> callback) {
        this.connectionStatusCallback = callback;
    }
    
    public void setToastMessageCallback(Consumer<String> callback) {
        this.toastMessageCallback = callback;
    }
    
    public void setBeginButtonEnabledCallback(Consumer<Boolean> callback) {
        this.beginButtonEnabledCallback = callback;
    }
    
    public void setTextInputEnabledCallback(Consumer<Boolean> callback) {
        this.textInputEnabledCallback = callback;
    }
    
    // Getters para el estado actual
    public boolean getConnectionStatus() {
        return connectionStatus;
    }
    
    public boolean getIsBeginButtonEnabled() {
        return isBeginButtonEnabled;
    }
    
    public boolean getIsTextInputEnabled() {
        return isTextInputEnabled;
    }
    
    // Métodos para actualizar datos del formulario
    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
        validateForm();
    }
    
    public void setTextoSeleccionado(String textoSeleccionado) {
        this.textoSeleccionado = textoSeleccionado;
        updateTextInputVisibility();
        validateForm();
    }
    
    public void setTextoEscrito(String textoEscrito) {
        this.textoEscrito = textoEscrito;
        validateForm();
    }
    
    public void setConexionVerificada(boolean verificada) {
        this.conexionVerificada = verificada;
        this.connectionStatus = verificada;
        
        if (connectionStatusCallback != null) {
            connectionStatusCallback.accept(verificada);
        }
        validateForm();
    }
    
    private void updateTextInputVisibility() {
        boolean shouldEnable = "Sí".equals(textoSeleccionado);
        this.isTextInputEnabled = shouldEnable;
        
        if (textInputEnabledCallback != null) {
            textInputEnabledCallback.accept(shouldEnable);
        }
        
        // Si se cambia a "No", limpiar el texto escrito
        if (!shouldEnable) {
            textoEscrito = "";
        }
    }
    
    private void validateForm() {
        boolean isValid = isFormValid();
        boolean shouldEnableButton = isValid && conexionVerificada;
        
        this.isBeginButtonEnabled = shouldEnableButton;
        
        if (beginButtonEnabledCallback != null) {
            beginButtonEnabledCallback.accept(shouldEnableButton);
        }
    }
    
    public boolean isFormValid() {
        // Validar que la cantidad no esté vacía
        if (cantidad.trim().isEmpty()) {
            return false;
        }
        
        // Si se seleccionó "Sí" en texto, debe haber texto escrito
        if ("Sí".equals(textoSeleccionado) && textoEscrito.trim().isEmpty()) {
            return false;
        }
        
        // Debe haber seleccionado una opción válida (no "Elegir")
        if ("Elegir".equals(textoSeleccionado)) {
            return false;
        }
        
        return true;
    }
    
    public void showValidationError() {
        String errorMessage = "";
        
        if (cantidad.trim().isEmpty()) {
            errorMessage = "La cantidad no puede estar vacía";
        } else if ("Elegir".equals(textoSeleccionado)) {
            errorMessage = "Debe seleccionar una opción en Texto";
        } else if ("Sí".equals(textoSeleccionado) && textoEscrito.trim().isEmpty()) {
            errorMessage = "Debe escribir texto cuando selecciona Sí";
        } else if (!conexionVerificada) {
            errorMessage = "Debe comprobar la conexión antes de comenzar";
        }
        
        if (toastMessageCallback != null && !errorMessage.isEmpty()) {
            toastMessageCallback.accept(errorMessage);
        }
    }
    
    public void checkInternetConnection() {
        executor.execute(() -> {
            try {
                // Simulamos una verificación de conectividad
                Thread.sleep(1000); // Simular delay de red
                
                // Aquí iría la lógica real de verificación de internet
                boolean hasConnection = checkRealConnection();
                
                // Actualizar estado
                setConexionVerificada(hasConnection);
                
                String message = hasConnection ? "Conexión exitosa" : "Sin conexión a internet";
                if (toastMessageCallback != null) {
                    toastMessageCallback.accept(message);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (toastMessageCallback != null) {
                    toastMessageCallback.accept("Error al verificar conexión");
                }
            }
        });
    }
    
    private boolean checkRealConnection() {
        // Aquí implementarías la verificación real de internet
        // Por ahora, simularemos que siempre hay conexión
        return true;
    }
    
    public void beginProcess() {
        if (!isFormValid() || !conexionVerificada) {
            showValidationError();
            return;
        }
        
        if (toastMessageCallback != null) {
            toastMessageCallback.accept("Comenzando proceso...");
        }
    }
    
    // Método para obtener los datos del formulario
    public FormData getFormData() {
        return new FormData(cantidad, textoSeleccionado, textoEscrito);
    }
    
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
    // Clase para encapsular los datos del formulario
    public static class FormData {
        public final String cantidad;
        public final String textoSeleccionado;
        public final String textoEscrito;
        
        public FormData(String cantidad, String textoSeleccionado, String textoEscrito) {
            this.cantidad = cantidad;
            this.textoSeleccionado = textoSeleccionado;
            this.textoEscrito = textoEscrito;
        }
    }
}