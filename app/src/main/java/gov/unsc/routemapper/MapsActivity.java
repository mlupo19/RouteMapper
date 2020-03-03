package gov.unsc.routemapper;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button startButton;
    private Button turnButton;
    private Button undoButton;
    private TextView distView;

    private FusedLocationProviderClient fusedLocationClient;

    // Markers of turns
    private ArrayList<MarkerOptions> markers = new ArrayList<>();

    private boolean onRoute = false;
    // total distance of route
    private float dist = 0;
    // flag for turn button
    private boolean nanners = false;
    // flag for end route button
    private boolean endOfNanners = false;
    // flag for start route button
    private boolean startOfNanners = false;
    private boolean init = true;
    private int turnCount = 0;
    private File file;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private HashMap<String, Boolean> achievements;

    /*
        This app works by pressing the start route button, which places a marker at your starting position and the turn and undo buttons become available.
        When you want to make a turn, press the turn button and a marker and line will be drawn.  To see labels of any marker, click on it.
        The bottom shows the total distance travelled on the route.  To end your trip, press the end route button which adds the final marker and you can see your route, markers, and distance.
        Pressing the start route button again will reset the route and the distance.

        Some google maps bugs have been experienced, but most are not repeatable and have to do with bad connection
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        startButton = findViewById(R.id.startButton);
        turnButton = findViewById(R.id.makeTurnButton);
        undoButton = findViewById(R.id.undoButton);
        distView = findViewById(R.id.distLabel);

        file = new File("achievements");
        boolean exists = file.exists();
        if (!exists) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            ois = new ObjectInputStream(new FileInputStream(file));
            if (exists)
                loadAchievements();
            else
                createAchievements();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createAchievements() throws IOException {
        achievements = new HashMap<>();
        oos.writeObject(achievements);
    }

    private void loadAchievements() throws IOException, ClassNotFoundException {
        achievements = (HashMap<String, Boolean>) ois.readObject();
        
    }

    public void startButton(View v) {
        if (onRoute) {
            startButton.setText("Start Route");
            endOfNanners = true;
            turnButton.setVisibility(View.GONE);
            undoButton.setVisibility(View.GONE);
            onRoute = false;
            turnCount = 0;
        } else {
            dist = 0;
            startButton.setText("End Route");
            turnButton.setVisibility(View.VISIBLE);
            undoButton.setVisibility(View.VISIBLE);
            onRoute = true;
            startOfNanners = true;
        }
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

        LocationRequest r = LocationRequest.create();
        r.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        r.setInterval(1000);
        fusedLocationClient.requestLocationUpdates(r, new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {

                List<Location> locations = locationResult.getLocations();
                if (locations.size() > 0) {
                    Location location = locations.get(locations.size() - 1);
                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    if (init) {
                        init = false;
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(200));
                    }

                    if (startOfNanners) {
                        markers.add(new MarkerOptions().position(loc).title("Start"));
                        startOfNanners = false;
                        renderMarkersAndLines();
                    }

                    if (nanners) {
                        markers.add(new MarkerOptions().position(loc).title("Turn " + (++turnCount)));
                        nanners = false;
                        renderMarkersAndLines();
                    }

                    if (endOfNanners) {
                        markers.add(new MarkerOptions().position(loc).title("End"));
                        endOfNanners = false;
                        renderMarkersAndLines();
                    }
                }
            }
        }, Looper.myLooper());
    }

    @SuppressLint("SetTextI18n")
    private void renderMarkersAndLines() {
        mMap.clear();

        MarkerOptions prev = null;
        for (MarkerOptions mo : markers) {
            mMap.addMarker(mo);
            if (prev == null) {
                prev = mo;
                continue;
            }
            dist += Place.dist(prev.getPosition(), mo.getPosition());
            PolylineOptions pol = new PolylineOptions();
            pol.add(prev.getPosition());
            pol.add(mo.getPosition());
            mMap.addPolyline(pol);
            prev = mo;
        }
        if (prev != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(prev.getPosition()));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(200 + (dist * 10)));
        distView.setText("Distance: " + String.format(Locale.US, "%.2f", dist) + " meters");

        if (!onRoute)
            markers.clear();
    }

    public void undo(View v) {
        if (markers.size() > 1) {
            markers.remove(markers.size() - 1);
            turnCount--;
            renderMarkersAndLines();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            oos.close();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
