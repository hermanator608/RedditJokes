package edu.msoe.hermanb.lab4;

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
 * Class used to get jokes from reddit using Volley
 */
class JokeHelper {

    /**
     * Endpoints for www.reddit.com/r/jokes/
     */
    enum Endpoint{
        TOP ("top"),
        HOT (""),
        NEW ("new"),
        CONTROVERSIAL ("controversial");

        private final String ep;
        Endpoint(String ep) {
            this.ep = ep;
        }
    }

    // List of Jokes
    static final List<JokeInfo> ITEMS = new ArrayList<>();
    // Map of jokes by joke title. Used to easily pass jokes between fragments/activities
    static final Map<String, JokeInfo> ITEM_MAP = new HashMap<>();

    // Used to prevent fetching data multiple times
    private static boolean fetched = false;

    /**
     * Add Joke to both List and Map
     * @param item - JokeInfo to be added
     */
    private static void addItem(JokeInfo item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.title, item);
    }

    /**
     * This method actually requests jokes from reddit
     * @param endpoint - What endpoint to hit
     * @param callback - Callback to be used on success/failure
     */
    static void fetchJokesFromReddit(Endpoint endpoint, final JokeListActivity.VolleyCallback callback) {
        if (!fetched) {
            ITEMS.clear();
            ITEM_MAP.clear();

            String url = String.format("https://www.reddit.com/r/jokes/%s/.json", endpoint.ep);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleJokesJson(response);
                            fetched = true;
                            callback.onSuccess();
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            fetched = false;
                            callback.onFailure(error);
                        }
                    });

            // Access the RequestQueue using single request queue object
            requestQueue.add(jsonObjectRequest);
        }
        else { // Already loaded
            callback.onSuccess();
        }
    }

    // Force pull nect time fetch is called
    static void forceFetchOnNextAttempt() {
        fetched = false;
    }

    /**
     * Parse JSON from Reddit and get info we want
     * @param jokeResponse - json from Reddit
     */
    private static void handleJokesJson(JSONObject jokeResponse){
        try {
            JSONArray data = jokeResponse.getJSONObject("data").getJSONArray("children");

            for (int i = 0; i < data.length(); i++) {
                JSONObject currentJoke = data.getJSONObject(i).getJSONObject("data");
                JokeInfo tempJoke = new JokeInfo(currentJoke.getString("title"), currentJoke.getString("selftext"), currentJoke.getInt("ups"));
                addItem(tempJoke);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Class used to store title, detail, and ups for a reddit joke
     */
    public static class JokeInfo{
        public String title;
        public String detail;
        public int ups;

        public JokeInfo(String title, String detail, int ups) {
            this.title = title;
            this.detail = detail;
            this.ups = ups;
        }
    }
}
