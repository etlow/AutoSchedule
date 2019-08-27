package teamget.autoschedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseUiException;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

class AuthFragment extends Fragment {
    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "AuthFragment";
    private static final String FRAGMENT_TAG = "AuthFragment";
    private TimetablePreferences timetablePref = TimetablePreferences.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FragmentTransaction transaction;
    private Runnable signInSuccess;
    private Consumer<Integer> showSnack;

    static AuthFragment getInstance(FragmentManager manager) {
        AuthFragment frag = (AuthFragment) manager.findFragmentByTag(FRAGMENT_TAG);
        if (frag == null) {
            frag = new AuthFragment();
            frag.transaction = manager.beginTransaction().add(frag, FRAGMENT_TAG);
            frag.transaction.commit();
        }
        return frag;
    }

    private void runAfterAdded(Runnable runnable) {
        if (isAdded()) runnable.run();
        else transaction.runOnCommit(runnable);
    }

    void checkSignInAndContinue(Runnable onSuccess, Consumer<Integer> failSnack) {
        if (auth.getCurrentUser() != null) {
            // already signed in, exited before making timetable
            runAfterAdded(() -> timetablePref.downloadData(getContext())
                    .addOnSuccessListener(snapshots -> onSuccess.run())
                    .addOnFailureListener(e -> failSnack.accept(R.string.data_error)));
        } else {
            // not signed in
            startSignIn(onSuccess, failSnack);
        }
    }

    void startSignIn(Runnable onSuccess, Consumer<Integer> failSnack) {
        signInSuccess = onSuccess;
        showSnack = failSnack;

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null || auth.getCurrentUser().isAnonymous()) {
            // not signed in
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    // new AuthUI.IdpConfig.FacebookBuilder().build(),
                    // new AuthUI.IdpConfig.TwitterBuilder().build(),
                    new AuthUI.IdpConfig.AnonymousBuilder().build());

            // Create and launch sign-in intent
            runAfterAdded(() -> startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .enableAnonymousUsersAutoUpgrade()
                            .setAvailableProviders(providers)
                            .setLogo(R.drawable.autoschedule_logo)
                            .build(),
                    RC_SIGN_IN));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
                timetablePref.downloadData(getContext())
                        .addOnSuccessListener(snapshots -> signInSuccess.run())
                        .addOnFailureListener(e -> showSnack.accept(R.string.data_error));
            } else {
                // Sign in failed
                IdpResponse response = IdpResponse.fromResultIntent(data);

                if (response == null) {
                    // User pressed back button
                    showSnack.accept(R.string.sign_in_cancelled);
                    return;
                }

                FirebaseUiException exception = response.getError();
                assert exception != null;
                if (exception.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnack.accept(R.string.no_internet_connection);
                    return;
                }

                if (exception.getErrorCode() == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT) {
                    // Store relevant anonymous user data
                    timetablePref.deleteAllDocuments(auth.getUid());

                    // Get the non-anonymous credential from the response
                    AuthCredential nonAnonymousCredential = response.getCredentialForLinking();
                    // Sign in with credential
                    assert nonAnonymousCredential != null;
                    FirebaseAuth.getInstance().signInWithCredential(nonAnonymousCredential)
                            .addOnSuccessListener(result -> {
                                // Copy over anonymous user data to signed in user
                                timetablePref.downloadData(getContext())
                                        .addOnSuccessListener(snapshots -> signInSuccess.run())
                                        .addOnFailureListener(e ->
                                                showSnack.accept(R.string.data_error));
                            });
                    return;
                }

                showSnack.accept(R.string.unknown_error);
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    Task<Void> signOut(Context context) {
        return AuthUI.getInstance().signOut(context)
                .addOnCompleteListener(v -> timetablePref.clearData(context));
    }

    Task<Void> deleteAccount(Context context) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return Tasks.call(() -> {
                timetablePref.clearData(context);
                return null;
            });
        } else {
            return timetablePref.deleteAllDocuments(user.getUid())
                    .onSuccessTask(task -> user.delete())
                    .addOnSuccessListener(v -> timetablePref.clearData(context));
        }
    }

}
