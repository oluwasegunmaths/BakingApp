package com.ease.bakingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ease.bakingapp.model.Ingredient;
import com.ease.bakingapp.model.Recipe;
import com.ease.bakingapp.model.Step;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.ease.bakingapp.MainActivity.STEPS_LIST;
import static com.ease.bakingapp.MainActivity.isThereConnection;

public class RecipeDetailActivity extends AppCompatActivity implements RecipeAdapter.StepItemClickListener {

    public static final String STEP_POSITION = "step position";
    private static final String DESIRED_RECIPE = "desired recipe";
    private static final String DESIRED_RECIPE_INGREDIENTS = "desired recipe ingredients";
    private static final String POSITION_BEING_PLAYED = "position being played";
    protected static boolean isTablet;
    private static String ingerdientsText;
    private RecyclerView recycler;
    private RecipeAdapter adapter;
    private ArrayList<Step> steps;
    private SimpleExoPlayerView videoView;
    private TextView stepDescriptionTextView;
    private SimpleExoPlayer mExoPlayer;
    private Recipe recipe;
    private SharedPreferences preferences;
    private boolean isFavorite;
    private int stepPositionBeingPlayedInTabletLayout;
    private TextView videoTextView;

    public static String getIngredientsConcatenated(ArrayList<Ingredient> ingredients) {
        if (ingredients != null && !ingredients.isEmpty()) {
            ingerdientsText = "";
            for (Ingredient ingredient : ingredients) {
                ingerdientsText = ingerdientsText + "\n" + ingredient.getQuantity() + " " + ingredient.getMeasure() + " of " + ingredient.getIngredientName() + ",";

            }
            ingerdientsText = ingerdientsText.substring(0, ingerdientsText.length() - 1);
            return ingerdientsText;
        } else {

            return null;

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recipe_detail);

        recipe = getIntent().getParcelableExtra(MainActivity.PARCEL_TO_INTENT);
        ArrayList<Ingredient> ingredients = getIntent().getParcelableArrayListExtra(MainActivity.INGREDIENTS_LIST);
        steps = getIntent().getParcelableArrayListExtra(STEPS_LIST);


        if (recipe != null) {
            if (findViewById(R.id.tablet_linear_layout) != null) {
                isTablet = true;
                videoView = findViewById(R.id.videoView);
                videoTextView = findViewById(R.id.exoplayer_fragment_TextView);

                stepDescriptionTextView = findViewById(R.id.step_description_text);

                if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_BEING_PLAYED)) {
                    stepPositionBeingPlayedInTabletLayout = savedInstanceState.getInt(POSITION_BEING_PLAYED);
                    stepDescriptionTextView.setText(steps.get(stepPositionBeingPlayedInTabletLayout).getDescription());
                    initializePlayer(Uri.parse(steps.get(stepPositionBeingPlayedInTabletLayout).getVideoURL()));

                } else {
                    stepPositionBeingPlayedInTabletLayout = 0;
                    stepDescriptionTextView.setText(steps.get(0).getDescription());
                    initializePlayer(Uri.parse(steps.get(0).getVideoURL()));

                }

            }
            populateUi(recipe, ingredients);

            setTitle(recipe.getName());
            recycler = findViewById(R.id.steps_recycler);
            setUpRecycler();
            adapter.setSteps(steps);
            checkIfRecipeIsFavorite();

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        if (isTablet) {
            outState.putInt(POSITION_BEING_PLAYED, stepPositionBeingPlayedInTabletLayout);
        }

    }

    private void checkIfRecipeIsFavorite() {
        preferences = PreferenceManager.getDefaultSharedPreferences(RecipeDetailActivity.this);
        String desiredRecipe = preferences.getString(DESIRED_RECIPE, "");
        if (desiredRecipe.contentEquals(recipe.getName())) {
            isFavorite = true;
            invalidateOptionsMenu();
        }

    }

    private void updateSharedPreferencesToStoreNameAndIngredientsOfThisRecipeSoAsToShowInWidget(boolean isBecomingFavorite) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        if (isBecomingFavorite) {
            preferencesEditor.putString(DESIRED_RECIPE, recipe.getName());
            preferencesEditor.putString(DESIRED_RECIPE_INGREDIENTS, ingerdientsText);
        } else {
            preferencesEditor.putString(DESIRED_RECIPE, "");
            preferencesEditor.putString(DESIRED_RECIPE_INGREDIENTS, "");

        }

        preferencesEditor.apply();


    }

    private void loadImageUsingPicassoAccordingToBuildVersion(Recipe recipe, ImageView posterView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable errorDrawable = getDrawable(R.drawable.ic_action_error);
            if (errorDrawable != null) {
                Picasso.get().load(recipe.getImageUrl()).error(errorDrawable).into(posterView);
            } else {
                Picasso.get().load(recipe.getImageUrl()).into(posterView);

            }
        } else {
            Picasso.get().load(recipe.getImageUrl()).into(posterView);

        }

        Picasso.get().load(recipe.getImageUrl()).into(posterView);
    }

    private void populateUi(Recipe recipe, ArrayList<Ingredient> ingredients) {
        ImageView recipeDetailImage = findViewById(R.id.recipe_detail_imageView);
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            recipeDetailImage.setVisibility(View.VISIBLE);
            loadImageUsingPicassoAccordingToBuildVersion(recipe, recipeDetailImage);
        }
        TextView ingredientTextView = findViewById(R.id.ingredients_textView);
        ingredientTextView.setText(getIngredientsConcatenated(ingredients));
    }

    private void setUpRecycler() {
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);

        adapter = new RecipeAdapter(this, this);


        recycler.setAdapter(adapter);
    }

    @Override
    public void onStepItemClick(int position) {

        if (isTablet) {
            stepPositionBeingPlayedInTabletLayout = position;
            stepDescriptionTextView.setText(steps.get(position).getDescription());
            releasePlayer();
            initializePlayer(Uri.parse(steps.get(position).getVideoURL()));
        } else {
            Intent intent = new Intent(this, StepDetailActivity.class);
            intent.putExtra(STEP_POSITION, position);
            intent.putParcelableArrayListExtra(STEPS_LIST, steps);

            startActivity(intent);
        }
    }


    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(true);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        MenuItem item = menu.findItem(R.id.action_favorite);
        if (isFavorite) {
            item.setIcon(R.drawable.ic_favorite);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                if (!isFavorite) {
                    updateSharedPreferencesToStoreNameAndIngredientsOfThisRecipeSoAsToShowInWidget(true);


                    isFavorite = true;

                    item.setIcon(R.drawable.ic_favorite);
                    WidgetUpdateService.startActionUpdateIngredientsList(this, recipe.getName() + " ingredients are: \n" + ingerdientsText);

                    Toast.makeText(this, recipe.getName() + " is your favorite recipe ", Toast.LENGTH_SHORT).show();

                } else {
                    updateSharedPreferencesToStoreNameAndIngredientsOfThisRecipeSoAsToShowInWidget(false);

                    isFavorite = false;

                    item.setIcon(R.drawable.ic_not_favorite);
                    WidgetUpdateService.startActionUpdateIngredientsList(this, "No favorite recipe selected");

                    Toast.makeText(this, recipe.getName() + " is no longer your favorite ", Toast.LENGTH_SHORT).show();

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializePlayer(Uri mediaUri) {
        if (mediaUri != null && !mediaUri.toString().isEmpty()) {
            if (isThereConnection(this)) {
                if (mExoPlayer == null) {
                    TrackSelector trackSelector = new DefaultTrackSelector();
                    LoadControl loadControl = new DefaultLoadControl();
                    mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

                    videoView.setPlayer(mExoPlayer);
                    String userAgent = Util.getUserAgent(this, "BakingApp");
                    MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(this, userAgent), new DefaultExtractorsFactory(), null, null);
                    mExoPlayer.prepare(mediaSource);
                    mExoPlayer.setPlayWhenReady(true);
                }
                videoTextView.setVisibility(View.GONE);

            } else {
                videoTextView.setVisibility(View.VISIBLE);
                videoTextView.setText(getText(R.string.no_network));
            }
        } else {
            if (steps.get(stepPositionBeingPlayedInTabletLayout).getThumbnailURL().endsWith(".mp4")) {
                initializePlayer(Uri.parse(steps.get(stepPositionBeingPlayedInTabletLayout).getThumbnailURL()));
            } else {
                videoTextView.setVisibility(View.VISIBLE);
                videoTextView.setText(getText(R.string.no_video_available));
            }
        }
    }

}
