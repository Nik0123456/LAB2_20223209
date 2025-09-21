package com.example.l2_20223209.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryManager {
    private static final String PREFS_NAME = "telecat_history";
    private static final String HISTORY_KEY = "interaction_history";
    private static HistoryManager instance;
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    
    private HistoryManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    public static synchronized HistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryManager(context);
        }
        return instance;
    }
    
    // Clase para representar una interacción
    public static class Interaction {
        private String text;
        private int quantity;
        private long timestamp;
        private int interactionNumber;
        
        public Interaction(String text, int quantity, int interactionNumber) {
            this.text = text != null ? text : "";
            this.quantity = quantity;
            this.timestamp = System.currentTimeMillis();
            this.interactionNumber = interactionNumber;
        }
        
        // Getters
        public String getText() { return text; }
        public int getQuantity() { return quantity; }
        public long getTimestamp() { return timestamp; }
        public int getInteractionNumber() { return interactionNumber; }
        
        public String getFormattedText() {
            return text.isEmpty() ? "Sin texto" : text;
        }
        
        public String getQuantityText() {
            return quantity + " imagen" + (quantity != 1 ? "es" : "");
        }
    }
    
    // Agregar nueva interacción
    public void addInteraction(String text, int quantity) {
        List<Interaction> history = getHistory();
        int interactionNumber = history.size() + 1;
        
        Interaction newInteraction = new Interaction(text, quantity, interactionNumber);
        history.add(newInteraction);
        
        saveHistory(history);
    }
    
    // Obtener historial completo
    public List<Interaction> getHistory() {
        String historyJson = prefs.getString(HISTORY_KEY, "[]");
        Type listType = new TypeToken<List<Interaction>>(){}.getType();
        List<Interaction> history = gson.fromJson(historyJson, listType);
        return history != null ? history : new ArrayList<>();
    }
    
    // Guardar historial
    private void saveHistory(List<Interaction> history) {
        String historyJson = gson.toJson(history);
        prefs.edit().putString(HISTORY_KEY, historyJson).apply();
    }
    
    // Limpiar historial (para reiniciar el juego)
    public void clearHistory() {
        prefs.edit().remove(HISTORY_KEY).apply();
    }
    
    // Método adicional: obtener si hay historial
    public boolean hasHistory() {
        return getTotalInteractions() > 0;
    }
    
    // Obtener número total de interacciones
    public int getTotalInteractions() {
        return getHistory().size();
    }
    
    // Obtener total de imágenes vistas
    public int getTotalImagesViewed() {
        List<Interaction> history = getHistory();
        int total = 0;
        for (Interaction interaction : history) {
            total += interaction.getQuantity();
        }
        return total;
    }
}