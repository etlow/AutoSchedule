package teamget.autoschedule;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Consumer;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import teamget.autoschedule.schedule.Timetable;

class TimetableRecyclerAdapter extends RecyclerView.Adapter<TimetableRecyclerAdapter.MyViewHolder> {
    private List<Timetable> timetables;
    private Consumer<Integer> onButtonClick;
    private TimetableFiller filler;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout constraintLayout;
        private MyViewHolder(ConstraintLayout l) {
            super(l);
            constraintLayout = l;
        }
    }

    TimetableRecyclerAdapter(List<Timetable> t, Consumer<Integer> c) {
        timetables = t;
        onButtonClick = c;
        filler = new TimetableFiller(TimetableFiller.HORIZONTAL);
    }

    @Override
    @NonNull
    public TimetableRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                    int viewType) {
        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.timetable_choice, parent, false);

        MyViewHolder viewHolder = new MyViewHolder(constraintLayout);
        constraintLayout.getViewById(R.id.button)
                .setOnClickListener(view -> onButtonClick.accept(viewHolder.getAdapterPosition()));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {
        ConstraintLayout constraintLayout = viewHolder.constraintLayout;
        GridLayout gridLayout = constraintLayout.findViewById(R.id.gridLayout);
        filler.setGridLayout(gridLayout).fill(timetables.get(position));

        String text = String.format(constraintLayout.getContext().getString(R.string
                        .top5timetables_button_selecttimetable), position + 1);
        ((Button) constraintLayout.getViewById(R.id.button)).setText(text);
    }

    @Override
    public int getItemCount() {
        return timetables.size();
    }
}
