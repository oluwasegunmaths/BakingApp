package com.ease.bakingapp;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class WidgetUpdateService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.ease.bakingapp.action.FOO";

    private static final String EXTRA_PARAM1 = "com.ease.bakingapp.extra.PARAM1";
    private static final String DESIRED_RECIPE = "desired recipe";
    private static final String DESIRED_RECIPE_INGREDIENTS = "desired recipe ingredients";

    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateIngredientsList(Context context, String param1) {
        Intent intent = new Intent(context, WidgetUpdateService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        context.startService(intent);
    }

    public static void startActionUpdateIngredientsList(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String desiredRecipe = preferences.getString(DESIRED_RECIPE, "");
        String desiredRecipeIngredients = preferences.getString(DESIRED_RECIPE_INGREDIENTS, "");
        String widgetText;
        if (desiredRecipe.isEmpty()) {
            widgetText = "No favorite recipe selected";
        } else {
            widgetText = desiredRecipe + " ingredients are: \n" + desiredRecipeIngredients;

        }


        Intent intent = new Intent(context, WidgetUpdateService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, widgetText);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                handleActionFoo(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String widgetText) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, BakingAppWidget.class));

        BakingAppWidget.updateAppWidgets(this, appWidgetManager, appWidgetIds, widgetText);

    }


}
