package com.ease.bakingapp.network;

import com.ease.bakingapp.model.Recipe;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class RecipeApiService {
    private static Retrofit retrofit;

    private static void buildRetrofit(String url) {

        retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(url).build();


    }

    public static Call<List<Recipe>> getRecipeCallToEnqueue(String url) {
        buildRetrofit(url);

        RecipeApiService.JsonPlaceHolder request = retrofit.create(RecipeApiService.JsonPlaceHolder.class);
        Call<List<Recipe>> call;
        call = request.getRecipes();
        return call;
    }

    public interface JsonPlaceHolder {
        @GET("baking.json")
        Call<List<Recipe>> getRecipes();


    }

}
