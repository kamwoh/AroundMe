package my.edu.um.fsktm.aroundme;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.signin.SignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * A simple {@link Fragment} subclass.
 */
public class GPlusFragment extends Fragment {

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mAccount;
    private static final int RC_SIGN_IN = 0;
    private boolean isSignOut = false; // to check whether this fragment should skip sign in page

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
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        if (isSignOut) {
            // handle sign out action first, then only create view
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getActivity(), "You have been signed out", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            mAccount = GoogleSignIn.getLastSignedInAccount(getActivity());

            if (mAccount != null) {
                switchToHomeFragment(mAccount);
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
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }

        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void switchToHomeFragment(GoogleSignInAccount mAccount) {
        FragmentManager fm = getActivity().getSupportFragmentManager();

        Fragment fragment = new HomeFragment();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        /*

        {
        "id":"117953961621286138844",
        "email":"kamwoh@gmail.com",
        "expirationTime":1542895292,
        "obfuscatedIdentifier":"B7C8B35B39D554443CA4B749D2F5CC2B",
        "grantedScopes":
        ["email",
        "https:\/\/www.googleapis.com\/auth\/plus.me",
        "https:\/\/www.googleapis.com\/auth\/userinfo.email",
        "https:\/\/www.googleapis.com\/auth\/userinfo.profile",
        "openid",
        "profile"]
        }

        */
        try {
            mAccount = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            switchToHomeFragment(mAccount);
            Log.d("Success", "signInResult: success" + mAccount.toJson());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Error", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(getActivity(), R.string.sign_in_failed, Toast.LENGTH_LONG).show();
        }
    }
}
