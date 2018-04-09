package edu.msoe.hermanb.lab4;

import android.support.design.widget.Snackbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.msoe.hermanb.lab4.JokeListActivity.requestQueue;

/**

 */
public class JokeHelper {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<JokeInfo> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, JokeInfo> ITEM_MAP = new HashMap<>();

    private static void addItem(JokeInfo item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.title, item);
    }

    public static void fetchJokesFromReddit(final JokeListActivity.VolleyCallback callback) {
        String url = "https://www.reddit.com/r/jokes/top/.json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handleJokesJson(response);
                        callback.onSuccess();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onFailure(error);
                    }
                });

        // Access the RequestQueue through your singleton class.
        requestQueue.add(jsonObjectRequest);
    }

    private static void handleJokesJson(JSONObject jokeResponse){
        try {
            JSONArray data = jokeResponse.getJSONObject("data").getJSONArray("children");

            for (int i = 0; i < data.length(); i++) {
                JSONObject currentJoke = data.getJSONObject(i).getJSONObject("data");
                JokeInfo tempJoke = new JokeInfo(currentJoke.getString("title"), currentJoke.getString("selftext"));
                addItem(tempJoke);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class JokeInfo{
        public String title;
        public String detail;

        public JokeInfo(String title, String detail) {
            this.title = title;
            this.detail = detail;
        }
    }
}
