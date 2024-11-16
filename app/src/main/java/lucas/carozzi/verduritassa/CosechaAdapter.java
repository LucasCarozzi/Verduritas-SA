package lucas.carozzi.verduritassa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CosechaAdapter extends RecyclerView.Adapter<CosechaAdapter.CosechaViewHolder> {
    private List<Cosecha> cosechas;
    private OnCosechaClickListener listener;

    public interface OnCosechaClickListener {
        void onMenuClick(View view, Cosecha cosecha);
    }

    public CosechaAdapter(List<Cosecha> cosechas, OnCosechaClickListener listener) {
        this.cosechas = cosechas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CosechaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.items_cosecha, parent, false);
        return new CosechaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CosechaViewHolder holder, int position) {
        holder.bind(cosechas.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return cosechas != null ? cosechas.size() : 0;
    }

    public void updateCosechas(List<Cosecha> nuevasCosechas) {
        this.cosechas = nuevasCosechas;
        notifyDataSetChanged();
    }

    public void removeCosecha(Cosecha cosecha) {
        int position = cosechas.indexOf(cosecha);
        if (position != -1) {
            cosechas.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class CosechaViewHolder extends RecyclerView.ViewHolder {
        private final TextView cropName;
        private final TextView harvestDate;
        private final ImageButton menuButton;

        CosechaViewHolder(View itemView) {
            super(itemView);
            cropName = itemView.findViewById(R.id.cropName);
            harvestDate = itemView.findViewById(R.id.harvestDate);
            menuButton = itemView.findViewById(R.id.menuButton);
        }

        void bind(final Cosecha cosecha, final OnCosechaClickListener listener) {
            cropName.setText(cosecha.getCropName());
            harvestDate.setText(cosecha.getHarvestDate());

            if (listener != null) {
                menuButton.setOnClickListener(v -> listener.onMenuClick(menuButton, cosecha));
            }
        }
    }
}
