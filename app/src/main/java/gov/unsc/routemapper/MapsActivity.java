package gov.unsc.routemapper;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Objects;

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
    private HashMap<String, Achievement> achievements;
    private File file;

    /*
        Achievements:
        Achievements are stored in a hashmap with a code, and when an achievement condition is met, the achievement is accessed through its code and updated to show the user got the achievement.
        For data persistence, the hashmap of achievements is stored in a file on the internal storage of the phone.  Every time the activity is paused, the achievement file is saved.
        When the app updates, it checks all the conditions for achievements and updates the achievements accordingly.
        To view achievements, another activity was added and is accessed through the achievements button on the main activity.
        3 linear layouts are used to display the names of the achievements, the icons of the achievements, and the radiobuttons that show whether it has been achieved yet.
        The textviews, imageviews, and radiobuttons are all added programmatically

        Known bugs: on some devices, the achievements names and images may become unaligned
     */

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

        file = new File(getFilesDir(), "achievements.obj");
        loadAchievements();
    }

    private void write() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(openFileOutput(file.getName(), Context.MODE_PRIVATE));
            oos.writeObject(achievements);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createAchievements()  {
        achievements = new HashMap<>();
        achievements.put("5km", new Achievement("5k: Walk 5 kilometers in one trip", R.drawable.five));
        achievements.put("2km", new Achievement("2k: Walk 2 kilometers in one trip", R.drawable.two));
        achievements.put("10km", new Achievement("10k: Walk 10 kilometers in one trip", R.drawable.ten));
        achievements.put("1mk", new Achievement("1 Marker: Place one marker in a run", R.drawable.one_mk));
        achievements.put("5mk", new Achievement("5 Markers: Place five markers in a run", R.drawable.five_mk));
        achievements.put("10mk", new Achievement("10 Markers: Place ten markers in a run", R.drawable.ten_mk));
        write();
    }

    private void loadAchievements() {
        try {
            ObjectInputStream ois = new ObjectInputStream(openFileInput(file.getName()));
            System.out.println("why " + ois.available());
            achievements = (HashMap<String, Achievement>) ois.readObject();
            ois.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        if (achievements == null)
            createAchievements();
        System.out.println(achievements.values());
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
        if (dist >= 2000)
        {
            achievements.get("2km").achieve();
        }
        if (dist >= 5000)
        {
            achievements.get("5km").achieve();
        }
        if (dist >= 10000)
        {
            achievements.get("10km").achieve();
        }
        if (turnCount >= 1)
        {
            achievements.get("1mk").achieve();
        }
        if (turnCount >= 5)
        {
            achievements.get("5mk").achieve();
        }
        if (turnCount >= 10)
        {
            achievements.get("10mk").achieve();
        }
    }

    public void undo(View v) {
        if (markers.size() > 1) {
            markers.remove(markers.size() - 1);
            turnCount--;
            renderMarkersAndLines();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        write();
    }

    public void achieveClick(View view) {
        Intent toAchievementList = new Intent(this, AchievementActivity.class);
        toAchievementList.putExtra("achieves", achievements);
        if (achievements == null)
            System.out.println("Bruh");
        else
            startActivity(toAchievementList);
    }
}
