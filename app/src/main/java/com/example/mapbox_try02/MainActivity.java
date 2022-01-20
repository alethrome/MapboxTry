package com.example.mapbox_try02;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationContract;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Callback<DirectionsResponse>, PermissionsListener {

    MapView mapView;
    public MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private CarmenFeature home;
    private CarmenFeature work;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    private static final int REQUEST_CODE = 5678;
    String address;
    Point origin = Point.fromLngLat(90.399452, 23.777176);
    Point destination = Point.fromLngLat(90.399452, 23.777176);
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";
    private MapboxDirections client;
    int c = 0;
    MapboxNavigation navigation;
    double distance;
    String st;
    String startLocation="";
    String endLocation="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, "pk.eyJ1IjoiYWFhYWRpYXoiLCJhIjoiY2t5aTNzeGd5MjhpZzJ4cGIzMGsxYW1rbCJ9.uMZy0SCPJduUtheZlsln0Q");

        setContentView(R.layout.activity_main);
        navigation = new MapboxNavigation(this, "pk.eyJ1IjoiYWFhYWRpYXoiLCJhIjoiY2t5aTNzeGd5MjhpZzJ4cGIzMGsxYW1rbCJ9.uMZy0SCPJduUtheZlsln0Q");
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void OnMapReady(final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override

            public void  onStyleLoaded(@NonNull Style style) {

                enableLocationComponent(style); // function to show user's location
                initSearchFab(); // function to initialize location search

                addUserLocations(); // function to add default locations in autocomplete location search
                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24, null);
                Bitmap nBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
                // Add the symbol layer icon to map for future use
                style.addImage(symbolIconId, nBitmap);

                // Create an empty  GeoJSON source using the empty feature collection
                setUpSource(style);

                // Setup a new symbol lar for displaying  the searched location's feature coordinates
                setUpLayer(style);

                initSource(style);

                initLayers(style);
                //
                //
                //

                mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    LatLng source;

                    @Override
                    public boolean onMapClick(@NonNull LatLng point) {

                        if (c == 0) { // c is used to count clicks on the map
                            origin = Point.fromLngLat(point.getLongitude(), point.getLatitude());
                            source = point;
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(point);
                            markerOptions.title("Source");
                            mapboxMap.addMarker(markerOptions);
                            reverseGeocodeFunc(point, c); // to get location details, place name form latitude longitude
                        }
                        if (c == 1) { // if c==1 then destination
                            destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
                            getRoute(mapboxMap, origin, destination);
                            MarkerOptions markerOptions2 = new MarkerOptions();
                            markerOptions2.position(point);
                            markerOptions2.title("destination");
                            mapboxMap.addMarker(markerOptions2);
                            reverseGeocodeFunc(point,c);
                            getRoute(mapboxMap, origin, destination); // then we show the route using polylines
                            //double d = point.distanceTo(source);
                        }

                        if (c > 1) {
                            c = 0;
                            recreate(); // more than 2 clicks will restart the activity

                        }
                        c++;
                        return true;
                    }
                });
            }
        });
    }

    private void reverseGeocodeFunc(LatLng point, int c) { // for getting place name
        MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                .accessToken("pk.eyJ1IjoiYWFhYWRpYXoiLCJhIjoiY2t5aTNzeGd5MjhpZzJ4cGIzMGsxYW1rbCJ9.uMZy0SCPJduUtheZlsln0Q")
                .query(Point.fromLngLat(point.getLongitude(), point.getLatitude()))
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS) // can use TYPE_ADDRESS for address or TYPE_POI for places of interest
                .build();
        reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                List<CarmenFeature> results = response.body().features();

                if(results.size() > 0) {
                    CarmenFeature feature;
                    // Log the first results Point
                    Point firstResultPoint = results.get(0).center();
                    feature = results.get(0);
                    if (c==0) //we show source location
                    {
                        startLocation+=feature.placeName();
                        TextView tv = findViewById(R.id.s);
                        tv.setText(startLocation);
                    }
                    if(c==1)// we show destination
                    {
                        endLocation += feature.placeName();
                        TextView tv2 = findViewById(R.id.d);
                        tv2.setText(endLocation);
                    }

                    Toast.makeText(MainActivity.this, "" + feature.placeName(), Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(MainActivity.this, "Not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
    private void initLayers(@NonNull Style loadedMapStyle) { // setting up design layer
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);
//Add the linelayer to the map. This layer will display the directions route
        routeLayer.setProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#009688"))
        );
        loadedMapStyle.addLayer(routeLayer);

        //Add the red marker icon image to the map
        loadedMapStyle.addImage(RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.marker)));

        //Add the red marker icon Symbollayer to the map
        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID,ICON_SOURCE_ID).withProperties(
                iconImage(RED_PIN_ICON_ID),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -9f})
        ));

    }
    private void initSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID));

        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[]{
                Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
                Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))
        }));
        loadedMapStyle.addSource(iconGeoJsonSource);
    }
    //function to show route between two points
    private void getRoute(final MapboxMap mapboxMap, Point origin, final Point destination) {
        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken("pk.eyJ1IjoiYWFhYWRpYXoiLCJhIjoiY2t5aTNzeGd5MjhpZzJ4cGIzMGsxYW1rbCJ9.uMZy0SCPJduUtheZlsln0Q")
                .build();

        client.enqueueCall(this); //xxxx
    }
    //navigation function, use your own token everytime
    private void navigationRoute()
    {
        NavigationRoute.builder(this)
                .accessToken("pk.eyJ1IjoiYWFhYWRpYXoiLCJhIjoiY2t5aTNzeGd5MjhpZzJ4cGIzMGsxYW1rbCJ9.uMZy0SCPJduUtheZlsln0Q")
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null) {
                            Toast.makeText(MainActivity.this, "No Routes found. Make sure to set right user and access token", Toast.LENGTH_LONG).show();
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Toast.makeText(MainActivity.this, "No routes found", Toast.LENGTH_LONG).show();
                            return;
                        }
                        DirectionsRoute route =  response.body().routes().get(0); //getting route
                        boolean simulateRoute = true;

                        //create a NavigationLauncherOptions object to package everything together
                        //launching navigation for the route
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(route)
                                .shouldSimulateRoute(simulateRoute)
                                .build();
                        //Call this method with Context from within an Activity
                        NavigationLauncher.startNavigation(MainActivity.this, options);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                    }
                });
    }
    //this function is called from here xxxx
    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        //you can get the generic HTTP info about the response
        if (response.body() == null) {
            Toast.makeText(MainActivity.this, "No routes found make sure to  set right user and access token", Toast.LENGTH_LONG).show();
            return;
        } else if (response.body().routes().size() < 1) {
            Toast.makeText(MainActivity.this, "No routes found", Toast.LENGTH_LONG).show();
        }

        //Get the directions route
        final DirectionsRoute currentRoute = response.body().routes().get(0); // getting route we use two routes
        //first one was used for navigation. this one is when we click destination, we get the route. this is using mapbox direction

        distance = currentRoute.distance() / 1000; // route distance in km
        st = String.format("%.2f K.M", distance);
        TextView dv = findViewById(R.id.distanceView);
        dv.setText(st);


        if (mapboxMap != null) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void OnStyleLoaded(@NonNull Style style) {

                    //Retrieve and update the source designated for showing the directions route
                    GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);

                    //Create a lineString with the directions route's geometry and
                    //reset the GeoJSON source for the route linelayer source
                    if (source != null) {
                        source.setGeoJson(LineString.fromPolyline(currentRoute.geometry(), Constants.PRECISION_6));
                    }
                }
            });
        }
    }
    @Override
    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

    }

    public void confirmed(View view) {
        navigationRoute(); // if clicked then navigation starts
    }
    //PlaceAutocomplete initialize
    private void initSearchFab() {
        findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete().IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : "pk.eyJ1IjoiYWFhYWRpYXoiLCJhIjoiY2t5aTNzeGd5MjhpZzJ4cGIzMGsxYW1rbCJ9.uMZy0SCPJduUtheZlsln0Q")
                        .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#EEEEEE"))
                            .limit(10)
                            .addInjectedFeature(home)//with 2 location 1st one
                            .addInjectedFeature(work)//2nd one
                            .build(PlaceOptions.MODE_CARDS))
                        .build(MainActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);// here oooo
            }
        });
    }
    //those locations are initialized here
    private void addUserLocations() {
        home = CarmenFeature.builder().text("Mapbox SF Office")
                .geometry(Point.fromLngLat(-122.3954485, 37.7912561))
                .placeName("50 Beale St., Francisco, CA")
                .id("mapbox-sf")
                .properties(new JsonObject())
                .build();

        work = CarmenFeature.builder().text("Mapbox DC Office")
                .placeName("740 15th Street NW, Washingtion DC")
                .geometry(Point.fromLngLat(-77.0338348, 38.899750))
                .id("mapbox-dc")
                .properties(new JsonObject())
                .build();
    }

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    private void setUpLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[]{0f, -0f})
        ));
    }
//placeautocomplete search activity from oooo
    //to here
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            //Retrieve  selected  location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            //Create a new FeatureCollection and add a new feature to it using selectedCarmenFeature above.
            //Then retrieve  and update  the source designated for showing  a selected  location's  symbol layer icon

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source =  style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}
                        ));
                    }
                    //Move map camera  to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                        ((Point) selectedCarmenFeature.geometry()).longitude()))
                            .zoom(14)
                            .build()), 4000);
                }
            }
        }
    }
//function to show users initial location
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are  enabled  and if not request
        if (PermissionsManager.areLocationPermissionsGranted(MainActivity.this)) {
            //Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            //Activate  with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(MainActivity.this, loadedMapStyle).build()
            );

            //Enable to make component visible
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationComponent.setLocationComponentEnabled(true);

            //Set  the component's  camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);// if user moves the icon will move

            //Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int [] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            finish();
        }
    }

    //Add the mapView lifecycle  to the activity's  lifecycle  methods
    //not essential functions
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        navigation.onDestroy();
        mapView.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}