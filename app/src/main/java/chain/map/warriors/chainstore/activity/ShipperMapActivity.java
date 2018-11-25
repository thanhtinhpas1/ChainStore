package chain.map.warriors.chainstore.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chain.map.warriors.chainstore.MainActivity;
import chain.map.warriors.chainstore.R;
import chain.map.warriors.chainstore.base.BaseActivity;

public class ShipperMapActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, RoutingListener, GpsStatus.Listener{

    private GoogleMap mMap;

    private Boolean isSwitchChecked = false;

    private float rideDistance;

    private static int status = 0;

    GoogleApiClient mGoogleApiClient;

    Location mLastLocation;

    LocationRequest mLocationRequest;

    private String storeId = "", destination, destinationFound;

    private SupportMapFragment mapFragment;

    private LinearLayout mStoreInfo, mPlaces;

    private ImageView mStoreProfileImage;

    private TextView mStoreName, mStorePhone, mStoreDestination;

    Marker pickupMarker;

    private DatabaseReference assignedStorePickupLocationRef;

    private ValueEventListener assignedStorePickupLocationRefListener;

    final int LOCATION_REQUEST_CODE = 1;

    private List<Polyline> polylines;

    private LatLng destinationLatLng, pickupLocation;

    private FusedLocationProviderClient mFusedLocationClient;

    private Switch mWorkingSwitch;

    private DrawerLayout mDrawer;

    private NavigationView nvDrawer;

    private Toolbar toolbar;

    private static int count_shipper = 0;

    Button accept, cancel;

    private static final String uid = FirebaseAuth.getInstance().getUid().toString();

    private LocationManager locationManager;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_shipper_map_activity;
    }

    @Override
    public void loadControl(Bundle savedInstanceState) {
        checkLocationPermission();
        /*Check GPS enable*/
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        checkGpsEnable(locationManager);
        locationManager.addGpsStatusListener(this);

        onBackPressed();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_shipper);
        mapFragment.getMapAsync(this);
        mapping();
        polylines = new ArrayList<>();
        setupNavDrawer();
        nvDrawer.setNavigationItemSelectedListener(this);

        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isSwitchChecked = true;
                    connectShipper();
                } else {
                    isSwitchChecked = false;
                    disconnectShipper();
                }
            }
        });

        getAssignedStore();
    }

    @Override
    public int getFragmentContainerViewId() {
        return 0;
    }

    public void setupNavDrawer() {
        /*Setting action bar*/
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_icon);
    }

    public void mapping() {
        mStoreInfo = findViewById(R.id.store_info_request);
        mStoreProfileImage = findViewById(R.id.store_avatar_request);
        mStoreName = findViewById(R.id.store_name_request);
        mStorePhone = findViewById(R.id.store_phone_request);
        mStoreDestination = findViewById(R.id.store_destination_request);
        mWorkingSwitch = findViewById(R.id.workingSwitch);
        mDrawer = findViewById(R.id.drawer_layout_shipper);
        nvDrawer = findViewById(R.id.nvView_shipper);
        toolbar = findViewById(R.id.toolbar_shipper);
        accept = findViewById(R.id.btn_accept_request);
        cancel = findViewById(R.id.btn_cancel_request);
        mPlaces = findViewById(R.id.places);
    }

    //listen when item in navigation view was clicked
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_profile_shipper:

                break;
            case R.id.nav_update_shipper:

                break;
            case R.id.nav_setting_shipper:

                break;
            case R.id.nav_logout_shipper:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ShipperMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }

    //open navigation view when icon was clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);

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
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                checkLocationPermission();
            }
        }
        checkLocationPermission();
        checkGpsEnable(locationManager);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    //    Check permission of location in device
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(ShipperMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(ShipperMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    //Get all about store - location, destination, info
    private void getAssignedStore() {
        String shipperId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedStoreRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers").child(shipperId).child("storeRequest").child("storeId");
        assignedStoreRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    storeId = dataSnapshot.getValue().toString();

                    getAssignedStorePickupLocation();
                    getAssignedStoreDestination();
                    getAssignedStoreInfo();
                    accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            accept.setVisibility(View.GONE);
                            cancel.setVisibility(View.GONE);
                            mPlaces.setVisibility(View.VISIBLE);
                            PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
                            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build();
                            autocompleteFragment.setFilter(typeFilter);
                            autocompleteFragment.setText(destination);
                            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                                @Override
                                public void onPlaceSelected(Place place) {
                                    // TODO: Get info about the selected place.
                                    destinationFound = place.getAddress().toString();
                                    mStoreDestination.setText(destinationFound);
                                    destinationLatLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                                    mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.cart)));
                                    getRouteToMarker(destinationLatLng);
                                }

                                @Override
                                public void onError(Status status) {
                                    // TODO: Handle the error.
                                }
                            });
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(ShipperMapActivity.this)
                                    .setMessage("Are you sure you want to cancel request ?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mStoreInfo.setVisibility(View.GONE);
                                            mStoreName.setText("");
                                            mStorePhone.setText("");
                                            mStoreDestination.setText("Destination: --");
                                            mStoreProfileImage.setImageResource(R.drawable.avatar_user);
                                            DatabaseReference request_cancel = FirebaseDatabase.getInstance().getReference("Users").child("Shippers").child(uid).child("storeRequest");
                                            request_cancel.removeValue();
                                            storeId = "";
                                        }
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }
                    });
                } else {
//                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getAssignedStorePickupLocation() {
        assignedStorePickupLocationRef = FirebaseDatabase.getInstance().getReference().child("storeRequest").child(storeId).child("l");
        assignedStorePickupLocationRefListener = assignedStorePickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLocation = new LatLng(locationLat, locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup location").icon(BitmapDescriptorFactory.fromResource(R.drawable.cart)));
                    //   getRouteToMarker(destinationLatLng);
                } else {
                    storeId = "";
                    if (pickupMarker != null) {
                        pickupMarker.remove();
                    }
                    if (assignedStorePickupLocationRefListener != null) {
                        assignedStorePickupLocationRef.removeEventListener(assignedStorePickupLocationRefListener);
                    }
                    mStoreInfo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedStoreInfo() {
        mStoreInfo.setVisibility(View.VISIBLE);
        DatabaseReference mStoreDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Stores").child(storeId);
        mStoreDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mStoreName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null) {
                        mStorePhone.setText(map.get("phone").toString());
                    }

                    if (map.get("profileImageUrl") != null) {
                        Glide.with(getApplicationContext()).load(map.get("profileImageUrl").toString()).into(mStoreProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedStoreDestination() {
        String shipperId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedStoreRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers").child(shipperId).child("storeRequest").child("destination");
        assignedStoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot != null) {
                        destination = dataSnapshot.getValue().toString();
                        mStoreDestination.setText(destination);
                    } else {
                        mStoreDestination.setText("Destination: --");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void connectShipper() {
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
        final DatabaseReference addNewShipper = FirebaseDatabase.getInstance().getReference("shipperAvailable").child("count");
        count_shipper++;
        addNewShipper.setValue(String.valueOf(count_shipper));
    }

    private void disconnectShipper() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("shipperAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (count_shipper > 0) {
                    count_shipper--;
                    final DatabaseReference addNewShipper = FirebaseDatabase.getInstance().getReference("shipperAvailable").child("count");
                    addNewShipper.setValue(String.valueOf(count_shipper));
                }
            }
        });

    }

    //Setting when have change from location of device
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    if (!storeId.equals("") && mLastLocation != null && location != null) {
                        rideDistance += mLastLocation.distanceTo(location) / 1000;
                    }
                    mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

//                    if (pickupMarker != null) {
//                        pickupMarker.remove();
//                    }
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("shipperAvailable");
                    final DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("shipperWorking");
                    GeoFire geoFireWorking = new GeoFire(refWorking);
                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    switch (storeId) {
                        case "":
                            if (isSwitchChecked) {
                                geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {

                                    }
                                });
                                geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                    }
                                });
                            }
                            break;
                        default:
                            geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }
                            });
                            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }
                            });
                            break;
                    }
                }
            }
        }
    };

    private void endRide() {
        // mRideStatus.setText("picked store");
        erasePolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference shipperRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers").child(userId).child("storeRequest");
        shipperRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("storeRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(storeId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
        storeId = "";
        rideDistance = 0;
        status = 0;

        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (assignedStorePickupLocationRefListener != null) {
            assignedStorePickupLocationRef.removeEventListener(assignedStorePickupLocationRefListener);
        }
        mStoreInfo.setVisibility(View.GONE);
        mStoreName.setText("");
        mStorePhone.setText("");
        mStoreDestination.setText("Destination: --");
        mStoreProfileImage.setImageResource(R.drawable.avatar_user);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }

    private void getRouteToMarker(LatLng destination) {
        Routing routing = new Routing.Builder()
                .key("AIzaSyDzGTfU97JHRG9n2U29EAojIhFll2RE09I")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), destination)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int closest) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng now = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        builder.include(now);

        builder.include(pickupLocation);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width * 0.2);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cameraUpdate);

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        final int mainColor = R.color.colorPrimary;
        final int color = R.color.gray;
        PolylineOptions polyOptionsMain = new PolylineOptions();
        polyOptionsMain.color(getResources().getColor(mainColor));
        polyOptionsMain.width(15);
        polyOptionsMain.addAll(route.get(0).getPoints());
        Polyline polylineMain = mMap.addPolyline(polyOptionsMain);
        polylines.add(polylineMain);

        //   add route(s) to the map.
        for (int i = 1; i < route.size(); i++) {
            //In case of more than 5 alternative routes
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(color));
            polyOptions.width(20);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
        }
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                for (Polyline a : polylines) {
                    a.setColor(color);
                }
                polyline.setColor(getResources().getColor(mainColor));
            }
        });
    }

    private void erasePolylines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); //1000 millions second
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //best performance

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ShipperMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {

            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11)); //1-21
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("shipperAvailable");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            switch (storeId) {
                case "":
                    if (isSwitchChecked) {
                        geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()),
                                new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {

                                    }
                                });
                    }
                    break;
                default:
                    geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });

                    break;
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("shipperAvailable");
        GeoFire geoFireAvailable = new GeoFire(refAvailable);
        geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                }
        );
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(ShipperMapActivity.this);
            dialog.setMessage(ShipperMapActivity.this.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(ShipperMapActivity.this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    ShipperMapActivity.this.startActivity(myIntent);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //get gps
                }
            });
            dialog.setNegativeButton(ShipperMapActivity.this.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }
    }


    @Override
    public void onGpsStatusChanged(int event) {

    }

    boolean twice;
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Toast.makeText(ShipperMapActivity.this,R.string.Exit, Toast.LENGTH_SHORT).show();

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