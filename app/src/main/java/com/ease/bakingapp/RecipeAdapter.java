package com.ease.bakingapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ease.bakingapp.model.Recipe;
import com.ease.bakingapp.model.Step;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private final Context context;
    protected boolean isDisplayingSteps;
    private Step selected;
    private StepItemClickListener stepItemClickListener;
    private List<Recipe> reviewList;
    private ItemClickListener itemClickListener;
    private List<Step> stepList;

    public RecipeAdapter(Context c, ItemClickListener ClickListener) {
        context = c;
        this.itemClickListener = ClickListener;

    }

    public RecipeAdapter(Context c, StepItemClickListener ClickListener) {
        context = c;
        this.stepItemClickListener = ClickListener;
        isDisplayingSteps = true;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recipe_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isDisplayingSteps) {
            if (RecipeDetailActivity.isTablet) {
                if (getSelected() == stepList.get(position)) {
                    holder.constraintLayout.setBackgroundColor(Color.GREEN);
                } else {
                    holder.constraintLayout.setBackgroundColor(Color.WHITE);

                }
            }
            holder.name.setText(stepList.get(position).getShortDescription());


            if (stepList.get(position).getThumbnailURL().isEmpty()) {
                holder.imageView.setVisibility(View.GONE);

            } else {
                holder.imageView.setVisibility(View.VISIBLE);

                loadImageUsingPicassoAccordingToBuildVersion(stepList.get(position), holder.imageView);

            }
        } else {
            holder.name.setText(reviewList.get(position).getName());

        }


    }

    private void loadImageUsingPicassoAccordingToBuildVersion(Step step, ImageView posterView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable errorDrawable = context.getDrawable(R.drawable.ic_action_error);
            if (errorDrawable != null) {
                Picasso.get().load(step.getThumbnailURL()).error(errorDrawable).into(posterView);
            } else {
                Picasso.get().load(step.getThumbnailURL()).into(posterView);

            }
        } else {
            Picasso.get().load(step.getThumbnailURL()).into(posterView);

        }

    }

    @Override
    public int getItemCount() {
        if (isDisplayingSteps) {
            if (null == stepList) return 0;

            return stepList.size();
        } else {
            if (null == reviewList) return 0;

            return reviewList.size();
        }
    }

    public void setReviews(List<Recipe> reviews) {
        reviewList = reviews;
        notifyDataSetChanged();
    }

    public void setSteps(List<Step> steps) {
        stepList = steps;
        notifyDataSetChanged();
    }

    public Step getSelected() {
        return selected;
    }

    public void setSelected(Step selected) {
        this.selected = selected;
    }

    public interface ItemClickListener {
        void onItemClick(Recipe recipe);

    }

    public interface StepItemClickListener {
        void onStepItemClick(int position);

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        ImageView imageView;
        ConstraintLayout constraintLayout;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.list_item_name);
            imageView = itemView.findViewById(R.id.list_imageView);
            constraintLayout = itemView.findViewById(R.id.constraint);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (isDisplayingSteps) {
                stepItemClickListener.onStepItemClick(getAdapterPosition());
                if (RecipeDetailActivity.isTablet) {
                    setSelected(stepList.get(getAdapterPosition()));

                    notifyDataSetChanged();
                }
            } else {
                itemClickListener.onItemClick(reviewList.get(getAdapterPosition()));

            }

        }
    }
}
