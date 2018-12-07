package my.edu.um.fsktm.aroundme.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.objects.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class GPlusFragment extends Fragment {

    private GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount account;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private View layout;
    private View progressBar;
    private static final int RC_SIGN_IN = 0;
    private boolean isSignOut = false; // to check whether this fragment should skip sign in page
    private boolean displayedSigningIn = false;

    public GPlusFragment() {
        this(false);
    }

    @SuppressLint("ValidFragment")
    public GPlusFragment(boolean isSignOut) {
        this.isSignOut = isSignOut;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        auth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();

        if (isSignOut) {
            // handle sign out action first, then only create view
            googleSignInClient.signOut()
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toolbar toolbar = getActivity().findViewById(R.id.my_toolbar);
                            toolbar.setVisibility(View.INVISIBLE);
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(getActivity(), "You have been signed out", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            account = GoogleSignIn.getLastSignedInAccount(getActivity());
            if (account != null && !displayedSigningIn) {
                displayedSigningIn = true;
                switchToHomeFragment();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gplus, container, false);
        SignInButton signInButton = v.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }

        });

        layout = v.findViewById(R.id.gplus_layout);
        progressBar = v.findViewById(R.id.listing_progress_bar_in_gplus);

        if (account == null) {
            layout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        } else { // it has account
            layout.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void switchToHomeFragment() {
        // authenticate firebase
        final FragmentManager fm = getActivity().getSupportFragmentManager();

        Toast.makeText(getActivity(), "Signing in", Toast.LENGTH_SHORT).show();

        Log.d("GPlusFragment", String.valueOf(displayedSigningIn));
        Log.d("GPlusFragment", "id_token: " + account.getIdToken());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(account.getId(), account.getDisplayName());

                            User.restoreOrPush(firebaseDatabase.getReference().child("users"), user, true);

                            User.currentUser = user;

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Fragment fragment = new HomeFragment();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    fm.beginTransaction()
                                            .remove(GPlusFragment.this)
                                            .replace(R.id.fragment_container, fragment)
                                            .commit();
                                }
                            }, 100);
                        } else {
                            Toast.makeText(getActivity(), R.string.sign_in_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            layout.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            switchToHomeFragment();
        } catch (ApiException e) {
            Toast.makeText(getActivity(), R.string.sign_in_failed, Toast.LENGTH_LONG).show();
        }
    }

    public static void switchToGPlusFragment(FragmentManager fm, Fragment oldFragment, boolean isSignOut) {
        Fragment fragment = new GPlusFragment(isSignOut);
        Log.d("GPlusFragment", "called twice " + isSignOut);
        FragmentTransaction ft = fm.beginTransaction();

        if (oldFragment != null)
            ft.remove(oldFragment);

        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }
}
