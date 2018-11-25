package chain.map.warriors.chainstore;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import chain.map.warriors.chainstore.activity.RegisterActivity;
import chain.map.warriors.chainstore.activity.ShipperMapActivity;
import chain.map.warriors.chainstore.activity.StoreMapActivity;
import chain.map.warriors.chainstore.base.BaseActivity;
import chain.map.warriors.chainstore.utils.Navigator;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private EditText mEmail, mPassword;
    private Button mLogin, mRegistration;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private DatabaseReference databaseReference;

    @Override
    public int getLayoutResource() {
        return R.layout.login;
    }

    @Override
    public void loadControl(Bundle savedInstanceState) {
        startFirstFragment();
        mEmail = (EditText)findViewById(R.id.lEditEmail);
        mPassword = (EditText)findViewById(R.id.lEditPassword);
        mLogin = (Button)findViewById(R.id.btnLogin);
        mRegistration = (Button)findViewById(R.id.btnLinkToRegisterScreen);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        mLogin.setTypeface(typeface);
        mRegistration.setTypeface(typeface);
        mEmail.setTypeface(typeface);
        mAuth = FirebaseAuth.getInstance();
        mLogin.setOnClickListener(this);
        mRegistration.setOnClickListener(this);

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    final String uid = user.getUid();
                    Login(uid);
                    return;
                }
            }
        };
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                if (!email.isEmpty() && !password.isEmpty()) {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Sign in error", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                final String uid = user.getUid();
                                databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers");
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(uid)) {
                                            Navigator.getInstance().startActivity(MainActivity.this, ShipperMapActivity.class, new Bundle());
                                        }
                                        else {
                                            Navigator.getInstance().startActivity(MainActivity.this, StoreMapActivity.class, new Bundle());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(this, "Username and password can't be empty!", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btnLinkToRegisterScreen:
                Navigator.getInstance().startActivity(MainActivity.this, RegisterActivity.class, new Bundle());
                break;
        }
    }

    @Override
    public int getFragmentContainerViewId() {
        return R.id.fragment_registers;
    }

    public void Login(final String uid) {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid)) {
                    Navigator.getInstance().startActivity(MainActivity.this, ShipperMapActivity.class, new Bundle());
                }
                else {
                    Navigator.getInstance().startActivity(MainActivity.this, StoreMapActivity.class, new Bundle());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void checkGpsEnable(LocationManager lm) {
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setMessage(MainActivity.this.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(MainActivity.this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    MainActivity.this.startActivity(myIntent);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //get gps
                }
            });
            dialog.setNegativeButton(MainActivity.this.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }
    }
}
