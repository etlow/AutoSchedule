package teamget.autoschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    // First launch welcome screen
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonClick(View v) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
            downloadAndContinue();
        } else {
            // not signed in
            startSignIn();
        }
    }

    private void startSignIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // not signed in
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    // new AuthUI.IdpConfig.FacebookBuilder().build(),
                    // new AuthUI.IdpConfig.TwitterBuilder().build(),
                    new AuthUI.IdpConfig.AnonymousBuilder().build());

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .enableAnonymousUsersAutoUpgrade()
                            .setAvailableProviders(providers)
                            .setLogo(R.drawable.autoschedule_logo)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                downloadAndContinue();
                // startActivity(SignedInActivity.createIntent(this, response));
                // finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    // showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                /*
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    // showSnackbar(R.string.no_internet_connection);
                    return;
                }
                */

                // showSnackbar(R.string.unknown_error);
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    private void downloadAndContinue() {
        String uid = FirebaseAuth.getInstance().getUid();
        assert uid != null;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(uid)
                .collection("data").document("ChosenTimetable");
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    Map<String, ?> map = document.getData();
                    assert map != null;
                    setChosenTimetable(map);
                    Intent intent = new Intent(this, ChosenTimetable.class);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "No such document");
                    Intent intent = new Intent(this, SemesterSelection.class);
                    startActivity(intent);
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void setChosenTimetable(Map<String, ?> map) {
        SharedPreferences pref = getSharedPreferences("ChosenTimetable", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                editor.putString(entry.getKey(), (String) value);
            } else if (value instanceof Integer) {
                editor.putInt(entry.getKey(), (Integer) value);
            } else if (value instanceof List) {
                List list = (List) value;
                Set<String> set = new HashSet<>();
                for (Object str : list) {
                    set.add((String) str);
                }
                editor.putStringSet(entry.getKey(), set);
            }
        }
        editor.apply();
    }
}
