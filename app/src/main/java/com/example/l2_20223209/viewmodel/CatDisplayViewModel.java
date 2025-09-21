package com.example.l2_20223209.viewmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CatDisplayViewModel {
    
    private static final int SECONDS_PER_IMAGE = 4;
    
    private ScheduledExecutorService timerExecutor;
    private ExecutorService networkExecutor;
    
    // Callbacks para comunicación con la UI
    private Consumer<Integer> timerUpdateCallback;
    private Consumer<String> imageUrlCallback;
    private Consumer<Boolean> nextButtonEnabledCallback;
    private Consumer<String> errorCallback;
    private Consumer<Integer> currentImageIndexCallback;
    
    // Estado del ViewModel
    private int totalImages;
    private int totalTimeSeconds;
    private int currentTimeSeconds;
    private int currentImageIndex;
    private boolean isTimerFinished;
    private String textOverlay;
    private List<String> imageUrls;
    private boolean isTimerRunning;
    
    public CatDisplayViewModel() {
        timerExecutor = Executors.newSingleThreadScheduledExecutor();
        networkExecutor = Executors.newFixedThreadPool(2);
        imageUrls = new ArrayList<>();
        isTimerRunning = false;
    }
    
    // Configurar callbacks
    public void setTimerUpdateCallback(Consumer<Integer> callback) {
        this.timerUpdateCallback = callback;
    }
    
    public void setImageUrlCallback(Consumer<String> callback) {
        this.imageUrlCallback = callback;
    }
    
    public void setNextButtonEnabledCallback(Consumer<Boolean> callback) {
        this.nextButtonEnabledCallback = callback;
    }
    
    public void setErrorCallback(Consumer<String> callback) {
        this.errorCallback = callback;
    }
    
    public void setCurrentImageIndexCallback(Consumer<Integer> callback) {
        this.currentImageIndexCallback = callback;
    }
    
    // Inicializar el ViewModel con datos de la pantalla anterior
    public void initialize(int quantity, String text) {
        this.totalImages = quantity;
        this.textOverlay = text != null ? text : "";
        this.totalTimeSeconds = quantity * SECONDS_PER_IMAGE;
        this.currentTimeSeconds = totalTimeSeconds;
        this.currentImageIndex = 0;
        this.isTimerFinished = false;
        
        // Generar URLs de imágenes
        generateImageUrls();
        
        // Iniciar timer si no está corriendo
        if (!isTimerRunning) {
            startTimer();
        }
        
        // Cargar primera imagen
        loadCurrentImage();
    }

    public void generateNewUrls() {
        generateImageUrls();
    }

    private void generateImageUrls() {
        imageUrls.clear();
        
        // Usar URL más simple y confiable de CATAAS
        String baseUrl = "https://cataas.com/cat";
        
        // Si hay texto, agregarlo a la URL, pero simplificado
        if (!textOverlay.isEmpty() && !textOverlay.trim().isEmpty()) {
            // Simplificar el texto para la URL
            String simpleText = textOverlay.trim().replaceAll("[^a-zA-Z0-9\\s]", "").replace(" ", "%20");
            if (simpleText.length() > 0 && simpleText.length() <= 50) {
                baseUrl = "https://cataas.com/cat/says/" + simpleText;
            }
        }
        
        for (int i = 0; i < totalImages; i++) {
            // Agregar parámetros para obtener imágenes diferentes
            String url = baseUrl + "?width=400&height=400&r=" + (System.currentTimeMillis() + i);
            imageUrls.add(url);
            
            // También agregar URLs de respaldo simples
            if (i < 3) {
                imageUrls.add("https://cataas.com/cat?width=400&height=400&r=" + (System.currentTimeMillis() + i + 1000));
            }
        }
    }
    
    public void startTimer() {
        if (isTimerRunning || isTimerFinished) {
            return;
        }
        
        isTimerRunning = true;
        
        timerExecutor.scheduleAtFixedRate(() -> {
            if (currentTimeSeconds > 0) {
                // Actualizar timer
                if (timerUpdateCallback != null) {
                    timerUpdateCallback.accept(currentTimeSeconds);
                }
                
                // Verificar si es momento de cambiar imagen
                int expectedImageIndex = (totalTimeSeconds - currentTimeSeconds) / SECONDS_PER_IMAGE;
                if (expectedImageIndex != currentImageIndex && expectedImageIndex < totalImages) {
                    currentImageIndex = expectedImageIndex;
                    loadCurrentImage();
                    
                    if (currentImageIndexCallback != null) {
                        currentImageIndexCallback.accept(currentImageIndex);
                    }
                }
                
                currentTimeSeconds--;
            } else {
                // Timer terminado
                isTimerFinished = true;
                isTimerRunning = false;
                
                if (timerUpdateCallback != null) {
                    timerUpdateCallback.accept(0);
                }
                
                if (nextButtonEnabledCallback != null) {
                    nextButtonEnabledCallback.accept(true);
                }
                
                // Cancelar timer
                timerExecutor.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    private void loadCurrentImage() {
        if (currentImageIndex < imageUrls.size() && imageUrlCallback != null) {
            String imageUrl = imageUrls.get(currentImageIndex);
            imageUrlCallback.accept(imageUrl);
        }
    }
    
    public void pauseTimer() {
        // El timer continúa corriendo en background para cumplir con el requisito
        // de que no se detenga aunque se navegue entre vistas
    }
    
    public void resumeTimer() {
        // Si el timer se detuvo por alguna razón, reiniciarlo
        if (!isTimerRunning && !isTimerFinished && currentTimeSeconds > 0) {
            timerExecutor = Executors.newSingleThreadScheduledExecutor();
            startTimer();
        }
    }
    
    // Getters para el estado actual
    public int getCurrentTimeSeconds() {
        return currentTimeSeconds;
    }
    
    public int getCurrentImageIndex() {
        return currentImageIndex;
    }
    
    public int getTotalImages() {
        return totalImages;
    }
    
    public boolean isTimerFinished() {
        return isTimerFinished;
    }
    
    public String getFormattedTime() {
        int minutes = currentTimeSeconds / 60;
        int seconds = currentTimeSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    public void cleanup() {
        if (timerExecutor != null && !timerExecutor.isShutdown()) {
            timerExecutor.shutdown();
        }
        if (networkExecutor != null && !networkExecutor.isShutdown()) {
            networkExecutor.shutdown();
        }
    }
    
    // Método para obtener la URL de la imagen actual
    public String getCurrentImageUrl() {
        if (currentImageIndex < imageUrls.size()) {
            return imageUrls.get(currentImageIndex);
        }
        return null;
    }
    
    // Método para manejar errores de carga de imagen
    public void onImageLoadError() {
        if (errorCallback != null) {
            errorCallback.accept("Error al cargar imagen de gato");
        }
    }
    
    // Método para cuando se carga exitosamente una imagen
    public void onImageLoadSuccess() {
        // Imagen cargada exitosamente, no se necesita acción especial
    }
}