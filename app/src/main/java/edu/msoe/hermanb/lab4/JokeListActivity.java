package edu.msoe.hermanb.lab4;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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

    /**
     * Single instance of a request queue
     */
    public static RequestQueue requestQueue;

    private static final int READ_AND_SEND_SMS_PERMISSIONS_REQUEST = 1;

    private static final String CHANNEL_ID = "Laugh Every Day";

    private static String currentJokeTitle;
    private static String currentJokeContent;

    public static JokeListActivity inst;

    private static final int PICK_CONTACT = 1;

    private SmsManager smsManager = SmsManager.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joke_list);

        requestQueue = Volley.newRequestQueue(this);

        // Get jokes from reddit (Defauly uses top jokes endpoint)
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
                Snackbar.make(findViewById(R.id.app_bar), "Error: " + error.getLocalizedMessage(), Snackbar.LENGTH_LONG);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        /*
         * Setup floating action buttons on click listeners
         */
        FloatingActionButton fab = findViewById(R.id.new_fab);
        fab.setOnClickListener(createFabOnClickListener(JokeHelper.Endpoint.NEW));


        FloatingActionButton top = findViewById(R.id.top_fab);
        top.setOnClickListener(createFabOnClickListener(JokeHelper.Endpoint.TOP));

        FloatingActionButton hot = findViewById(R.id.hot_fab);
        hot.setOnClickListener(createFabOnClickListener(JokeHelper.Endpoint.HOT));

        FloatingActionButton sendButton = findViewById(R.id.send_message);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendClick(v);
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

        View recyclerView = findViewById(R.id.joke_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        getPermissionToReadAndSendSMS();
    }

    public static JokeListActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    public void createSendNotification(String name, String msg) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Send Message to " + name)
                .setContentText("Joke: " + currentJokeTitle)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Joke: " + currentJokeTitle + " \r\n " + currentJokeContent))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (msg != null) {
            mBuilder.setContentTitle("Message from " + name)
                    .setContentText(msg)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(msg));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            CharSequence cName = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, cName, importance);
            mChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    NOTIFICATION_SERVICE);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannel);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // notificationId is a unique int for each notification that you must define
        Objects.requireNonNull(notificationManager).notify(1, mBuilder.build());
    }

    public void onSendClick(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadAndSendSMS();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
            // Add the buttons
            builder.setTitle("Send Joke: ");
            builder.setMessage(currentJokeTitle);
            builder.setPositiveButton("Select Contact", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button

                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, PICK_CONTACT);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    /**
     * React to contact selection
     */
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT) :
                if (resultCode == Activity.RESULT_OK) {
                    Cursor cursor;
                    try {
                        // Get the URI and query the content provider for the phone number
                        Uri contactUri = data.getData();
                        cursor = getContentResolver().query(Objects.requireNonNull(contactUri), null, null, null, null);
                        Objects.requireNonNull(cursor).moveToFirst();

                        int nameIndex = cursor.getColumnIndex(Phone.DISPLAY_NAME);
                        int numberIndex = cursor.getColumnIndex(Phone.NORMALIZED_NUMBER);

                        if(numberIndex <= 0) {
                            return;
                        }
                        String number = cursor.getString(numberIndex);
                        String name = cursor.getString(nameIndex);

                        String content = currentJokeTitle + " \r\n" + currentJokeContent;
                        if(content.length() < 160) {
                            smsManager.sendTextMessage(number, null, content, null, null);
                        } else {
                            ArrayList<String> msgs = smsManager.divideMessage(content);
                            smsManager.sendMultipartTextMessage(number, null, msgs, null, null);
                        }
                        createSendNotification(name, null);
                        Objects.requireNonNull(cursor).close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * Request permissions
     */
    public void getPermissionToReadAndSendSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_PHONE_STATE)) {
                Toast.makeText(this, "Please allow permission Read Phone State!", Toast.LENGTH_SHORT).show();
            }

            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission Read SMS!", Toast.LENGTH_SHORT).show();
            }

            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.SEND_SMS)) {
                Toast.makeText(this, "Please allow permission Send SMS", Toast.LENGTH_SHORT).show();
            }

            if(shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS)) {
                Toast.makeText(this, "Please allow permission to Receive SMS", Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS},
                    READ_AND_SEND_SMS_PERMISSIONS_REQUEST);
        }
    }

    /**
     * Permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_AND_SEND_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 4
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                //refreshSmsInbox();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Creates on click listener for FABS
     * @param endpoint - which enpoint to hit
     * @return onclicklsitener for the FAB
     */
    private View.OnClickListener createFabOnClickListener(final JokeHelper.Endpoint endpoint) {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                forceFetchOnNextAttempt();
                fetchJokesFromReddit(endpoint, new VolleyCallback() {
                    @Override
                    public void onSuccess() {
                        // Done loading and handling jokes
                        View recyclerView = findViewById(R.id.joke_list);
                        assert recyclerView != null;
                        setupRecyclerView((RecyclerView) recyclerView);
                    }

                    @Override
                    public void onFailure(VolleyError error){
                        Snackbar.make(view, "Error: " + error.getLocalizedMessage(), Snackbar.LENGTH_LONG);
                    }
                });
            }
        };
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, JokeHelper.ITEMS, mTwoPane));
    }

    /**
     * Interface used to handle callbacks from Volley fetches
     */
    public interface VolleyCallback{
        void onSuccess();
        void onFailure(VolleyError error);
    }

    /**
     * RecycleViewAdaptor for Jokes
     */
    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final JokeListActivity mParentActivity;
        private final List<JokeHelper.JokeInfo> mValues;
        private final boolean mTwoPane;

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) { // When a joke from the list is clicked on
                JokeHelper.JokeInfo item = (JokeHelper.JokeInfo) view.getTag();

                currentJokeTitle = item.title;
                currentJokeContent = item.detail;

                if (mTwoPane) { // If in two pane mode then create fragment
                    Bundle arguments = new Bundle();
                    arguments.putString(JokeDetailFragment.ARG_ITEM_ID, item.title);
                    JokeDetailFragment fragment = new JokeDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.joke_detail_container, fragment)
                            .commit();
                } else { // Else create a new activity
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

        @NonNull
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
