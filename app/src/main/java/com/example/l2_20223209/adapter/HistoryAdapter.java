package com.example.l2_20223209.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.l2_20223209.R;
import com.example.l2_20223209.data.HistoryManager;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryManager.Interaction> interactions = new ArrayList<>();

    public void setInteractions(List<HistoryManager.Interaction> interactions) {
        this.interactions = interactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_interaction, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryManager.Interaction interaction = interactions.get(position);
        holder.bind(interaction);
    }

    @Override
    public int getItemCount() {
        return interactions.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView interactionTitle;
        private TextView interactionText;
        private TextView interactionQuantity;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            interactionTitle = itemView.findViewById(R.id.interaction_title);
            interactionText = itemView.findViewById(R.id.interaction_text);
            interactionQuantity = itemView.findViewById(R.id.interaction_quantity);
        }

        public void bind(HistoryManager.Interaction interaction) {
            // Título de la interacción
            String title = "Interaccion" + interaction.getInteractionNumber() + ": " + 
                          interaction.getQuantityText();
            interactionTitle.setText(title);
            
            // Texto usado
            interactionText.setText(interaction.getFormattedText());
            
            // Cantidad
            interactionQuantity.setText(interaction.getQuantityText());
        }
    }
}