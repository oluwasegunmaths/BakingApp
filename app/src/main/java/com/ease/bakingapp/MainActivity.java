package com.ease.bakingapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.IdlingResource;

import com.ease.bakingapp.model.Recipe;
import com.ease.bakingapp.network.RecipeApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.ItemClickListener {

    public static final String PARCEL_TO_INTENT = "parcel to intent";
    public static final String INGREDIENTS_LIST = "ingredients list";
    public static final String STEPS_LIST = "steps list";
    private static final String RECIPE_DOWNLOADED = "recipe downloaded";
    private static final String BASE_URL = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/";
    private TextView messageText;
    private RecyclerView recycler;
    private ProgressBar progressBar;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private BakingAppIdlingResource mIdlingResource;

    public static boolean isThereConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getIdlingResource();

        initViews();
        setUpRecycler();
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(RECIPE_DOWNLOADED)) {

                recipeList = savedInstanceState.getParcelableArrayList(RECIPE_DOWNLOADED);
                if (recipeList != null && !recipeList.isEmpty()) {

                    adapter.setReviews(recipeList);
                    progressBar.setVisibility(View.GONE);
                    messageText.setVisibility(View.GONE);
                } else {
                    loadReviews(BASE_URL);

                }
            } else {
                loadReviews(BASE_URL);

            }
        } else {
            loadReviews(BASE_URL);

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        if (recipeList != null && !recipeList.isEmpty()) {
            ArrayList<Recipe> recipeArrayList = new ArrayList<Recipe>(recipeList);
            outState.putParcelableArrayList(RECIPE_DOWNLOADED, recipeArrayList);
        }
    }

    private void loadReviews(final String id) {
        if (isThereConnection(this)) {
            AppExecutors.getInstance().networkIO().execute(new Runnable() {
                @Override
                public void run() {

                    Call<List<Recipe>> call = RecipeApiService.getRecipeCallToEnqueue(id);

                    call.enqueue(new Callback<List<Recipe>>() {
                        @Override
                        public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {

                            List<Recipe> jsonResponse = response.body();

                            adapter.setReviews(jsonResponse);
                            recipeList = jsonResponse;

                            if (mIdlingResource != null) {


                                mIdlingResource.setIdleState(true);

                            }


                            progressBar.setVisibility(View.GONE);
                            messageText.setVisibility(View.GONE);


                        }

                        @Override
                        public void onFailure(Call<List<Recipe>> call, Throwable t) {
                            messageText.setVisibility(View.VISIBLE);
                            messageText.setText(getText(R.string.error_loading_recipes));
                            progressBar.setVisibility(View.GONE);


                        }
                    });
                }
            });
        } else {
            messageText.setVisibility(View.VISIBLE);
            messageText.setText(getText(R.string.no_network));
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setUpRecycler() {
        recycler.setLayoutManager(new GridLayoutManager(this, numberOfColumns()));

        adapter = new RecipeAdapter(this, this);
        recycler.setAdapter(adapter);
    }

    private void initViews() {
        messageText = findViewById(R.id.review_textview);
        recycler = findViewById(R.id.review_recycler);
        progressBar = findViewById(R.id.reviewprogressBar);
    }

    @Override
    public void onItemClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(PARCEL_TO_INTENT, recipe);
        intent.putParcelableArrayListExtra(INGREDIENTS_LIST, recipe.getIngredients());
        intent.putParcelableArrayListExtra(STEPS_LIST, recipe.getSteps());

        startActivity(intent);
    }

    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthDivider = 800;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns > 0) {
            return nColumns;
        } else {
            return 1;
        }
    }

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new BakingAppIdlingResource();
        }
        mIdlingResource.setIdleState(false);

        return mIdlingResource;
    }

}
