package chain.map.warriors.chainstore.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chain.map.warriors.chainstore.MainActivity;
import chain.map.warriors.chainstore.R;
import chain.map.warriors.chainstore.utils.Navigator;
import chain.map.warriors.chainstore.view.customView.CustomerEditText;
import chain.map.warriors.chainstore.view.customView.TextViewBold;
import chain.map.warriors.chainstore.view.customView.custom_font_nav;
import de.hdodenhof.circleimageview.CircleImageView;

public class StoreMapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{

    private Button mRequest;

    private GoogleMap mMap;

    private SupportMapFragment mapFragment;

    private DrawerLayout mDrawer;

    private NavigationView nvDrawer;

    LocationRequest mLocationRequest;

    Location mLastLocation;

    private LatLng pickupLocation;

    private Boolean requestBol = false;

    Marker pickupMarker;

    private String shipperFoundId;

    //distance around
    private int radius = 1;

    private Boolean shipperFound = false;

    GeoQuery geoQuery;

    final int LOCATION_REQUEST_CODE = 1;

    LinearLayout mShipperInfo;

    private Marker mShipperMarker;

    private DatabaseReference shipperLocationRef;

    private ValueEventListener shipperLocationRefListener;

    private ImageView mShipperProfileImage;

    private TextView mShipperName, mShipperPhone;

    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    private List<Polyline> polylines;

    private LatLng shipperLocation;

    private FusedLocationProviderClient mFusedLocationClient;

    private MenuItem nav_profile, nav_information, nav_setting, nav_logout, nav_ongoing;

    TextViewBold nav_name;

    CircleImageView nav_avatar;

    private CustomerEditText customer_name, customer_phone, customer_address;

    String txt_customer_name, txt_phone_customer, txt_address_customer;

    private static final String uid = FirebaseAuth.getInstance().getUid();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_map_activity);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_store);
        mapFragment.getMapAsync(    this);

        /*Setting action bar*/
        android.support.v7.widget.Toolbar toolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.toolbar_store);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_icon);
        mapping();

        nvDrawer.setNavigationItemSelectedListener(this);

        mRequest.setOnClickListener(this);

    }

    private DatabaseReference reference;
    private ValueEventListener listener;
    public void listenShipper() {
        reference = FirebaseDatabase.getInstance().getReference("shipperAvailable").child("count");
        listener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getshippersAround();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //alert dialog when call shipper, it's provide information of customer
    public void alertSubmit() {
        final PopupWindow window;
        View popUpView = getLayoutInflater().inflate(R.layout.dialog_infor_customer,
                null); // inflating popup layout
        window = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, true); // Creation of popup
        window.setAnimationStyle(R.style.DialogAnimationSlide_Up);
        window.showAtLocation(popUpView, Gravity.BOTTOM, 0, 0);
        Button button=popUpView.findViewById(R.id.btn_close_dialog);
        Button accept_customer = popUpView.findViewById(R.id.btn_accept_dialog);
        final TextViewBold error = popUpView.findViewById(R.id.txt_error_info);
        customer_name = popUpView.findViewById(R.id.customer_name);
        customer_phone = popUpView.findViewById(R.id.customer_phone);
        customer_address = popUpView.findViewById(R.id.customer_address);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                window.dismiss();

            }
        });
        accept_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customer_name.getText().toString().isEmpty() || customer_phone.getText().toString().isEmpty()
                        || customer_address.getText().toString().isEmpty()) {
                    error.setText("Information of customer can not null");
                } else {
                    txt_customer_name = customer_name.getText().toString();
                    txt_phone_customer = customer_phone.getText().toString();
                    txt_address_customer = customer_address.getText().toString();
                    window.dismiss();
                    requestBol = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("storeRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));

                    mRequest.setText("Getting your shipping ...");
                    getClosestShipper();
                }
            }
        });
    }

    public void mapping() {
        mDrawer=(DrawerLayout)findViewById(R.id.drawer_layout_store);
        nvDrawer=(NavigationView)findViewById(R.id.nvView_store);
        mRequest = (Button)findViewById(R.id.store_request);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        mRequest.setTypeface(typeface);
        mShipperInfo = (LinearLayout)findViewById(R.id.shipper_info_book);
        mShipperProfileImage = (ImageView)findViewById(R.id.profile_image_book);
        mShipperName = (TextView)findViewById(R.id.name_shipper_book);
        mShipperPhone = (TextView)findViewById(R.id.phone_shipper_book);
        nav_profile = (MenuItem)findViewById(R.id.nav_profile_store);
        nav_information = (MenuItem)findViewById(R.id.nav_update_store);
        nav_setting = (MenuItem)findViewById(R.id.nav_setting_store);
        nav_logout = (MenuItem)findViewById(R.id.nav_logout_store);
        nav_ongoing = findViewById(R.id.nav_ongoing_store);

        View header = nvDrawer.getHeaderView(0);
        nav_name = (TextViewBold) header.findViewById(R.id.nav_name_store);
        nav_avatar = (CircleImageView) header.findViewById(R.id.nav_avatar_store);
        //set name and image of store
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference mStoreDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Stores").child(uid);
        mStoreDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        nav_name.setText(map.get("name").toString());
                    }

                    if (map.get("profileImageUrl") != null) {
                        Glide.with(getApplicationContext()).load(map.get("profileImageUrl").toString()).into(nav_avatar);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Menu m = nvDrawer.getMenu();
        for (int i=0;i<m.size();i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu!=null && subMenu.size() >0 ) {
                for (int j=0; j <subMenu.size();j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi);
        }
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new custom_font_nav("" , font), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    /*Hàm listen từ các item trong navigation view*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.store_request:
                if (requestBol == false) {
                    alertSubmit();
                }
                else if (mRequest.getText().equals("Shipper's here")) {
                    endRide();
                }
        }
    }

    /*Setting navigation*/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.nav_profile_store:
                //chưa có layout
                break;
            case R.id.nav_update_store:
                break;
            case R.id.nav_setting_store:
                //chưa có layout
                break;
            case R.id.nav_logout_store:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(StoreMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_ongoing_store:
                Navigator.getInstance().startActivity(StoreMapActivity.this, OnGoingActivity.class, new Bundle());
                break;
        }
        return false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); //1000 millions second
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //best performance
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            }
            else {
                checkLocationPermission();
            }
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }



    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                    if(!getshippersAroundStarted)
                        getshippersAround();
                }
            }
        }
    };

    private void getClosestShipper() {
        DatabaseReference shipperLocation = FirebaseDatabase.getInstance().getReference().child("shipperAvailable");

        GeoFire geoFire = new GeoFire(shipperLocation);
        //query around radius
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);

        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!shipperFound && requestBol){
                    DatabaseReference mStoreDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers").child(key);
                    mStoreDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                shipperFound = true;
                                shipperFoundId = dataSnapshot.getKey();

                                DatabaseReference shipperRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers").child(shipperFoundId).child("storeRequest");
                                String storeId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                HashMap map = new HashMap();
                                map.put("storeId", storeId);
                                map.put("name", txt_customer_name);
                                map.put("phone", txt_phone_customer);
                                map.put("destination", txt_address_customer);
                                shipperRef.updateChildren(map);
                                getShipperLocation();
                                getShipperInfo();
//                                for(Marker markerIt : markers){
//                                    if (!markerIt.getTag().toString().isEmpty()) {
//                                        if(!markerIt.getTag().toString().equals(shipperFoundId)){
//                                            markers.remove(markerIt);
//                                            markerIt.remove();
//                                        }
//                                    }
//                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!shipperFound) {
                    radius++;
                    getClosestShipper();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    /*Tìm thông tin shipper*/
    private void getShipperInfo() {
        if (mShipperInfo != null) {
            mShipperInfo.setVisibility(View.VISIBLE);
        }
        DatabaseReference mStoreDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers").child(shipperFoundId);
        mStoreDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mShipperName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null) {
                        mShipperPhone.setText(map.get("phone").toString());
                    }

                    if (map.get("profileImageUrl") != null) {
                        Glide.with(getApplicationContext()).load(map.get("profileImageUrl").toString()).into(mShipperProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*Lấy vị trí shipper*/
    private void getShipperLocation() {
        shipperLocationRef = FirebaseDatabase.getInstance().getReference().child("shipperWorking").child(shipperFoundId).child("l");
        shipperLocationRefListener = shipperLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBol) {
                    List<Object> map = (List<Object>)dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    mRequest.setText("Shipper found");
                    if (map.get(0) != null) { //get location Lat
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) { //get location Lng
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng shipperLatlng = new LatLng(locationLat, locationLng);
                    shipperLocation = new LatLng(locationLat, locationLng);
                    if (mShipperMarker != null) {
                        mShipperMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(shipperLatlng.latitude);
                    loc2.setLongitude(shipperLatlng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    if (distance < 100) {
                        mRequest.setText("Shipper's here");
                        endRide();
                    }
                    else {
                        mRequest.setText("Shipper found: " + String.format("0x%03X", 3).valueOf(distance/1000) + " km");
                    }

                    mShipperMarker = mMap.addMarker(new MarkerOptions().position(shipperLatlng).title("Your Shipper").icon(BitmapDescriptorFactory.fromResource(R.drawable.delivery)));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded(){
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers").child(shipperFoundId).child("storeRequest").child("storeId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide(){
        requestBol = false;
        geoQuery.removeAllListeners();
        shipperLocationRef.removeEventListener(shipperLocationRefListener);
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        if (shipperFoundId != null){
            DatabaseReference ShipperRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers").child(shipperFoundId).child("storeRequest");
            ShipperRef.removeValue();
            shipperFoundId = null;

        }
        shipperFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("storeRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (mShipperMarker != null){
            mShipperMarker.remove();
        }
        mRequest.setText("Call shipper");
        if (mShipperInfo != null) {
            mShipperInfo.setVisibility(View.GONE);
            mShipperName.setText("");
            mShipperPhone.setText("");
            mShipperProfileImage.setImageResource(R.mipmap.ic_launcher_round);
        }
    }


    boolean getshippersAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();
    private void getshippersAround(){
        getshippersAroundStarted = true;
        DatabaseReference shipperLocation = FirebaseDatabase.getInstance().getReference().child("shipperAvailable");

        GeoFire geoFire = new GeoFire(shipperLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 10);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key))
                        return;
                }

                LatLng shipperLocation = new LatLng(location.latitude, location.longitude);

                Marker mShipperMarker = mMap.addMarker(new MarkerOptions().position(shipperLocation).title(key).icon(BitmapDescriptorFactory.fromResource(R.drawable.delivery)));
                mShipperMarker.setTag(key);

                markers.add(mShipperMarker);
            }

            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markers.remove(markerIt);
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(StoreMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(StoreMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("storeRequest").child(uid);
        ref.removeValue();
    }

    boolean twice;
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Toast.makeText(StoreMapActivity.this,R.string.Exit, Toast.LENGTH_SHORT).show();

        if(twice == true)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finishAffinity();

            finish();

            System.exit(0);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                twice = false;
            }
        }, 2000);
        twice = true;
    }

}
