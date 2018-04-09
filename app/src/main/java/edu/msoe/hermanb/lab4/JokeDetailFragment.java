package edu.msoe.hermanb.lab4;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Joke detail screen.
 * This fragment is either contained in a {@link JokeListActivity}
 * in two-pane mode (on tablets/landscape mode) or a {@link JokeDetailActivity}
 * on handsets.
 */
public class JokeDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     *
     */
    private JokeHelper.JokeInfo mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public JokeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = JokeHelper.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.joke_detail, container, false);

        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.joke_detail_title)).setText(mItem.title);
            ((TextView) rootView.findViewById(R.id.joke_detail_content)).setText(mItem.detail);
        }

        return rootView;
    }
}
