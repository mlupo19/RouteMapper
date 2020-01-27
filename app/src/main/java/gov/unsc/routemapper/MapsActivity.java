package gov.unsc.routemapper;

import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button startButton;
    private Button turnButton;
    private TextView distView;

    private FusedLocationProviderClient fusedLocationClient;

    private ArrayList<LatLng> markers = new ArrayList<>();

    private boolean onRoute = false;
    private float dist = 0;
    private boolean nanners = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        startButton = findViewById(R.id.startButton);
        turnButton = findViewById(R.id.makeTurnButton);
        distView = findViewById(R.id.distLabel);
    }

    public void startButton(View v) {
        if (onRoute) {
            startButton.setText("Start Route");
            finishRoute();
            turnButton.setVisibility(View.INVISIBLE);
            onRoute = false;
        } else {
            startButton.setText("End Route");
            turnButton.setVisibility(View.VISIBLE);
            onRoute = true;
            makeTurn(v);
        }
    }

    private void finishRoute() {

    }

    public void makeTurn(View v) {
        nanners = true;
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

        LocationRequest r = new LocationRequest();
        r.setInterval(10);
        fusedLocationClient.requestLocationUpdates(r, new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locations = locationResult.getLocations();
                if (locations.size() > 0) {
                    Location location = locations.get(locations.size() - 1);
                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.i("nanners", "nanners");

                    if (nanners) {
                        markers.add(loc);
                        mMap.addMarker(new MarkerOptions().position(loc));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(200 + (dist * 10)));
                        if (markers.size() > 1) {
                            dist += Place.dist(markers.get(markers.size() - 2), markers.get(markers.size() - 1));
                            PolylineOptions pol = new PolylineOptions();
                            pol.add(markers.get(markers.size() - 2));
                            pol.add(markers.get(markers.size() - 1));
                            mMap.addPolyline(pol);
                        }
                        distView.setText("Distance: " + dist);
                    }
                }
            }
        }, Looper.myLooper());
    }
}
