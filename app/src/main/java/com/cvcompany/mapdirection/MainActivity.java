package com.cvcompany.mapdirection;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION =100 ;
    private GoogleMap mGoogleMap;
    @BindView(R.id.etFind)
    EditText etFind;
    private GoogleApiClient mGoogleApiClient;
    private String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (googleServiceAvailble()) {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
            Toast.makeText(this, "Perfect", Toast.LENGTH_SHORT).show();
            initMap();
        } else {
            // No Google Maps Layout
        }
    }



    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;

        if(mGoogleMap!=null){
            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    MainActivity.this.setMarker("Local",latLng.latitude, latLng.longitude);
                }
            });
            mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Geocoder gc = new Geocoder(MainActivity.this);
                    LatLng ll  =marker.getPosition();
                    List<Address> list=null;

                    try {
                      list  = gc.getFromLocation(ll.latitude, ll.longitude, 1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address mAddress= list.get(0);
                    marker.setTitle(mAddress.getLocality());
                    marker.showInfoWindow();


                }
            });
            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window, null);
                    TextView txtLocality  =v.findViewById(R.id.txtTxt4);
                    TextView txtLat  =v.findViewById(R.id.txtTxt3);
                    TextView txtLng  =v.findViewById(R.id.txtTxt2);
                    TextView txtSniper  =v.findViewById(R.id.txtTxt1);
                    LatLng mlLatLng = marker.getPosition();
                    txtLocality.setText(marker.getTitle());
                    txtLat.setText("Latitude: "+mlLatLng.latitude);
                    txtLng.setText("Longitude: "+mlLatLng.longitude);
                    txtSniper.setText(marker.getSnippet());
                    return v;
                }
            });
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

    }
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();



            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }
    private void gotoLocationZoom(double v, double v1, float zoom) {
        LatLng mLatLng = new LatLng(v, v1);
        //mGoogleMap.addMarker(new MarkerOptions().position(mLatLng).title("HCM"));
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(mLatLng, zoom);
        mGoogleMap.moveCamera(update);

    }

    private void gotoLocation(double v, double v1) {
        LatLng mLatLng = new LatLng(v, v1);
        CameraUpdate update = CameraUpdateFactory.newLatLng(mLatLng);
        mGoogleMap.moveCamera(update);
    }

    public boolean googleServiceAvailble() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS)
            return true;
        else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
        } else
            Toast.makeText(this, "Cant connect to play services", Toast.LENGTH_SHORT).show();
        return false;
    }
private Marker mMarker;
    @OnClick(R.id.btnFind)
    public void btnFind() {
        String location = etFind.getText().toString().trim();
        Geocoder mGeocoder = new Geocoder(this);
        List<Address> mAddresses = null;
        try {
            mAddresses = mGeocoder.getFromLocationName(location, 1);
            Address mAddress = mAddresses.get(0);
            String locality = mAddress.getLocality();
            Toast.makeText(this, "" + locality, Toast.LENGTH_SHORT).show();

            double lat = mAddress.getLatitude();
            double lng = mAddress.getLongitude();
            gotoLocationZoom(lat, lng, 15);
            setMarker(locality, lat, lng);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    private Circle mCircle=null;
//    Marker marker1, marker2;
//    Polyline mPolyline;
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    static  final int POLYGON_POINTS = 5;
    Polygon mPolygon;
    private void setMarker(String locality, double lat, double lng) {
//        if(mMarker!=null){
//           removeEverything();
//        }
        if(markers.size()==POLYGON_POINTS){
            removeEverything();
        }
        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .position(new LatLng(lat,lng))
                .snippet("iam here")
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
//        if(marker1==null){
//            marker1 = mGoogleMap.addMarker(options);
//        }else if(marker2==null){
//            marker2  =mGoogleMap.addMarker(options);
//            drawline();
//        }else{
//            removeEverything();
//            marker1  = mGoogleMap.addMarker(options);
//        }
     //   mMarker =mGoogleMap.addMarker(options);
      //  mCircle = drawCircle(new LatLng(lat, lng));
        markers.add(mGoogleMap.addMarker(options));
        if(markers.size()==POLYGON_POINTS){
            drawPolygon();
        }
    }

    private void drawPolygon() {
        PolygonOptions options = new PolygonOptions()
                .fillColor(0x330000FF)
                .strokeWidth(3)
                .strokeColor(Color.RED);
       for(Marker marker: markers){
           options.add(marker.getPosition());
       }
       mPolygon = mGoogleMap.addPolygon(options);
    }




//    private void drawline() {
//        PolylineOptions options = new PolylineOptions()
//                .add(marker1.getPosition())
//                .add(marker2.getPosition())
//                .color(Color.BLUE)
//                .width(3);
//        mPolyline = mGoogleMap.addPolyline(options);
//    }

    private Circle drawCircle(LatLng latLng) {
        CircleOptions options = new CircleOptions()
                .center(latLng)
                .radius(1000)
                .fillColor(0x33FF0000)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);
        return mGoogleMap.addCircle(options);
    }

    private void removeEverything(){
//        mMarker.remove();
//        mMarker=null;
//        mCircle.remove();
//        mCircle=null;
//        marker1.remove();
//        marker1=null;
//        marker2.remove();
//        marker2=null;
//        mPolyline.remove();
//        mPolyline=null;
          for(Marker marker: markers){
              marker.remove();

          }
          markers.clear();
        mPolygon.remove();
        mPolygon=null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapTypeNone:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeTerrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeSatllite:
                mGoogleMap.setMapType((GoogleMap.MAP_TYPE_SATELLITE));
                break;
            case R.id.mapTypeHybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private LocationRequest mLocationRequest;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,  this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location==null)
            Toast.makeText(this, "Cant get current location", Toast.LENGTH_SHORT).show();
        else{
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,15);
            mGoogleMap.animateCamera(update);
        }

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }
}
