package edu.msoe.hermanb.lab4;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

import static edu.msoe.hermanb.lab4.JokeHelper.fetchJokesFromReddit;
import static edu.msoe.hermanb.lab4.JokeHelper.forceFetchOnNextAttempt;

/**
 * An activity representing a list of Jokes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link JokeDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class JokeListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    public static RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joke_list);

        requestQueue = Volley.newRequestQueue(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = findViewById(R.id.new_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forceFetchOnNextAttempt();
                fetchJokesFromReddit(JokeHelper.Endpoint.NEW, new VolleyCallback() {
                    @Override
                    public void onSuccess() {
                        // Done loading and handling jokes
                        View recyclerView = findViewById(R.id.joke_list);
                        assert recyclerView != null;
                        setupRecyclerView((RecyclerView) recyclerView);
                    }

                    @Override
                    public void onFailure(VolleyError error){

                    }
                });
            }
        });

        FloatingActionButton top = findViewById(R.id.top_fab);
        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forceFetchOnNextAttempt();
                fetchJokesFromReddit(JokeHelper.Endpoint.TOP, new VolleyCallback() {
                    @Override
                    public void onSuccess() {
                        // Done loading and handling jokes
                        View recyclerView = findViewById(R.id.joke_list);
                        assert recyclerView != null;
                        setupRecyclerView((RecyclerView) recyclerView);
                    }

                    @Override
                    public void onFailure(VolleyError error){

                    }
                });
            }
        });

        FloatingActionButton contra = findViewById(R.id.controversial_fab);
        contra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forceFetchOnNextAttempt();
                fetchJokesFromReddit(JokeHelper.Endpoint.CONTROVERSIAL, new VolleyCallback() {
                    @Override
                    public void onSuccess() {
                        // Done loading and handling jokes
                        View recyclerView = findViewById(R.id.joke_list);
                        assert recyclerView != null;
                        setupRecyclerView((RecyclerView) recyclerView);
                    }

                    @Override
                    public void onFailure(VolleyError error){

                    }
                });
            }
        });

        FloatingActionButton hot = findViewById(R.id.hot_fab);
        hot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forceFetchOnNextAttempt();
                fetchJokesFromReddit(JokeHelper.Endpoint.HOT, new VolleyCallback() {
                    @Override
                    public void onSuccess() {
                        // Done loading and handling jokes
                        View recyclerView = findViewById(R.id.joke_list);
                        assert recyclerView != null;
                        setupRecyclerView((RecyclerView) recyclerView);
                    }

                    @Override
                    public void onFailure(VolleyError error){

                    }
                });
            }
        });

        if (findViewById(R.id.joke_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // Or in any landscape mode
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        fetchJokesFromReddit(JokeHelper.Endpoint.TOP, new VolleyCallback() {
            @Override
            public void onSuccess() {
                // Done loading and handling jokes
                View recyclerView = findViewById(R.id.joke_list);
                assert recyclerView != null;
                setupRecyclerView((RecyclerView) recyclerView);
            }

            @Override
            public void onFailure(VolleyError error){

            }
        });

        View recyclerView = findViewById(R.id.joke_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, JokeHelper.ITEMS, mTwoPane));
    }

    public interface VolleyCallback{
        void onSuccess();
        void onFailure(VolleyError error);
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final JokeListActivity mParentActivity;
        private final List<JokeHelper.JokeInfo> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JokeHelper.JokeInfo item = (JokeHelper.JokeInfo) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(JokeDetailFragment.ARG_ITEM_ID, item.title);
                    JokeDetailFragment fragment = new JokeDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.joke_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, JokeDetailActivity.class);
                    intent.putExtra(JokeDetailFragment.ARG_ITEM_ID, item.title);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(JokeListActivity parent,
                                      List<JokeHelper.JokeInfo> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.joke_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).title);
            holder.mContentView.setText(String.format(Locale.ENGLISH,"Upvotes: %d", mValues.get(position).ups));

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = view.findViewById(R.id.id_text);
                mContentView = view.findViewById(R.id.content);
            }
        }
    }
}
