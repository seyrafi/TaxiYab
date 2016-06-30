// GPS ref: https://developer.android.com/training/location/retrieve-current.html
// GPS ref: https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.ConnectionCallbacks.html#constants
// Place API ref: http://www.startingandroid.com/google-places-api-tutorial-for-android/

// Google Map Bug: http://www.aphex.cx/the_google_maps_api_is_broken_on_android_5_here_s_a_workaround_for_multiple_map_fragments/
// Google Map Bug: https://github.com/cjmartin2/OneWarmCoat-android/commit/70bdc7a0db1708ab931a81f107b248298a1cfc39

package com.taxiyab;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.taxiyab.Model.DestStructureGooglePlace;
import com.taxiyab.Model.DestStructureLine;
import com.taxiyab.Model.DestStructureBase;
import com.taxiyab.Model.LineInfo;
import com.taxiyab.Model.SpinnerModel;
import com.taxiyab.Service.MyAsyncTask;
import com.taxiyab.Service.StoppableRunnable;
import com.taxiyab.adapter.DestSearchExpandableListAdapter;
import com.taxiyab.adapter.PickupTimeSpinnerAdapter;
import com.taxiyab.common.Constants;
import com.taxiyab.common.MapLib;
import com.taxiyab.common.MyToast;
import com.taxiyab.common.States;
import com.taxiyab.common.Tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
//import com.google.android.gms.location.LocationServices;

/**
 * Created by MehrdadS on 6/17/2016.
 */
public class FragmentMap extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    View rootView;
    public static FragmentMap This;
    Context context;

    MapFragment mapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient = null;
    private View mapFragmentView;
    private ImageButton btnStartingPoint;
    private ImageButton btnEndingPoint;
    private AutoCompleteTextView txtInlineSearchPlaces;
    private LinearLayout panelPlacesSearchBox;
    private LatLng startingPoint = null;
    private LatLng endingPoint = null;
    private Marker endingMarker = null;
    private Marker startingMarker = null;
    private LinearLayout panelMapFragment;
    private int selectedLine = -1;

    private int panelInfoHeight = 0;
    private int btnContinueHeight = 0;


    DestSearchExpandableListAdapter destListAdapter;
    List<String> listDataHeader;
    List<Pair<String, List<DestStructureBase>>> listDataChild;
    StoppableRunnable googleSearchRunnable;

    private Dialog dlgDestSearch;
    private Dialog dlgPassengerCount;
    private Dialog dlgPickupTime;

    private LinearLayout panelInfo;
    private TextView txtDest;
    private TextView txtSrc;
    private Button btnContinue;

    private int mapHeightLast = 0;
    private LatLng lastCameraPosition = null;
    private float lastZoom = 15;

    public FragmentMap() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        This = this;
        context = getActivity();
        MainActivity.program_state = States.STATE_MAP_MAP_STARTED;

        // FragmentMap attach
        attachMapFragment();

        btnStartingPoint = (ImageButton) rootView.findViewById(R.id.btnStartingPoint);
        btnEndingPoint = (ImageButton) rootView.findViewById(R.id.btnEndingPoint);
        txtInlineSearchPlaces = (AutoCompleteTextView) rootView.findViewById(R.id.txtInlineSearchPlaces);
        mapFragmentView = (View) getChildFragmentManager().findFragmentById(R.id.map).getView();
        panelPlacesSearchBox = (LinearLayout) rootView.findViewById(R.id.panelPlacesSearchBox);
        panelMapFragment = (LinearLayout)rootView.findViewById(R.id.panelMapFragment);
        panelInfo = (LinearLayout)rootView.findViewById(R.id.panelInfo);
        txtDest = (TextView)rootView.findViewById(R.id.txtDest);
        txtSrc= (TextView)rootView.findViewById(R.id.txtSrc);
        btnContinue = (Button)rootView.findViewById(R.id.btnContinue);

        setSrcText(null);
        setDestText(null);

        ViewTreeObserver vto = mapFragmentView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //int mapHeight = ((RelativeLayout)(panelMapFragment.getParent())).getHeight();
                int mapHeight = mapFragmentView.getHeight();
                boolean layoutSizeChanged = false;
                if (mapHeightLast != mapHeight)
                    layoutSizeChanged = true;

                int btnHeight = Math.max(btnStartingPoint.getHeight(), btnEndingPoint.getHeight());
                int btnTop = mapHeight / 2 - btnHeight;

                btnEndingPoint.setTop(btnTop);
                btnEndingPoint.setBottom(btnTop + btnHeight);
                btnStartingPoint.setTop(btnTop);
                btnStartingPoint.setBottom(btnTop + btnHeight);

                int txtPanelPlacesSearchBox = panelPlacesSearchBox.getHeight();
                int topOffset = 25;
                panelPlacesSearchBox.setTop(topOffset);
                panelPlacesSearchBox.setBottom(topOffset + txtPanelPlacesSearchBox);

                if (layoutSizeChanged) {
                    btnContinue.setWidth(mapFragmentView.getWidth()/2);
                    btnContinueHeight = btnContinue.getHeight();
                    btnContinue.setTop(mapHeight);
                    btnContinue.setBottom(mapHeight + btnContinueHeight);

                    panelInfoHeight = panelInfo.getHeight();
                    panelInfo.setTop(mapHeight);
                    panelInfo.setBottom(mapHeight + panelInfoHeight);
                }


            }
        });


        txtInlineSearchPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestSearchDialog(txtInlineSearchPlaces);
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addConnectionCallbacks(this)
                    //.addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        btnEndingPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                showDestSearchDialog(btnEndingPoint);
            }
        });

        btnStartingPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                // Choose the starting point
                startingPoint = mMap.getCameraPosition().target;

                showStartingEndingMarker();

                btnStartingPoint.setVisibility(View.INVISIBLE);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(startingPoint);
                builder.include(endingPoint);
                LatLngBounds bounds = builder.build();

                int padding = mapFragmentView.getHeight() / 5; // offset from edges of the map in pixels
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding)); // Make all markers visible

                MainActivity.menu.findViewById(R.id.mnBack).setVisibility(View.VISIBLE);
                MainActivity.program_state = States.STATE_MAP_BOTH_WAYS_SELECTED;

                MapLib.MapReverseGeocodingAsync(context, startingPoint, new MapLib.ReverseGeocodingHandler() {
                    @Override
                    public void handle(Address addressInfo) {
                        String city = addressInfo.getLocality();
                        String knownName = addressInfo.getFeatureName();
                        if (knownName != "" && city != "")
                            setSrcText( city + " - " + knownName);
                        else
                            setSrcText(city + knownName);
                    }
                });

                /*BounceInterpolator bounceInterpolator1 = new BounceInterpolator();
                ObjectAnimator anim1 = ObjectAnimator.ofFloat(panelInfo, "translationY", -panelInfoHeight, -panelInfoHeight-panelInfoHeight );
                anim1.setInterpolator(bounceInterpolator1);
                anim1.setDuration(1100).start();*/


                BounceInterpolator bounceInterpolator2 = new BounceInterpolator();
                ObjectAnimator anim2 = ObjectAnimator.ofFloat(btnContinue, "translationY", 0f, -panelInfoHeight-btnContinueHeight );
                anim2.setInterpolator(bounceInterpolator2);
                anim2.setDuration(1100).start();

                btnContinue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPassengerCountDialog();
                    }
                });
                //showPassengerCountDialog();

                //MyToast.makeText(context, "+" + latLng.latitude + " " + latLng.longitude+ "\n zoom: "+ cam.zoom, MyToast.LENGTH_SHORT).show();

                /*BounceInterpolator bounceInterpolator = new BounceInterpolator();
                ObjectAnimator anim = ObjectAnimator.ofFloat(txtInlineSearchPlaces, "translationY", 0f, -200 );
                anim.setInterpolator(bounceInterpolator);
                anim.setDuration(1100).start();*/
            }
        });

        /*GoogleMap googleMap;
        googleMap = ((MapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        //googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        //googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setTrafficEnabled(true);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                MyToast.makeText(context, marker.getPosition().latitude + " " + marker.getPosition().longitude, MyToast.LENGTH_SHORT);
                return false;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MyToast.makeText(context, "+" + latLng.latitude + " " + latLng.longitude, MyToast.LENGTH_SHORT);
                //return false;
            }
        });

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return rootView;
        googleMap.setMyLocationEnabled(true);*/
        return rootView;
    }

    private void doSelectEndingPoint(DestStructureBase dest) {
        // Choose the ending point
        LatLng newPoint = mMap.getCameraPosition().target;
        LatLng additionalPoint = null;
        if (dest == null) {
            endingPoint = mMap.getCameraPosition().target;
            newPoint = new LatLng(endingPoint.latitude - 0.01, endingPoint.longitude);
            final LatLng latLng = mMap.getCameraPosition().target;
            MapLib.MapReverseGeocodingAsync(context, latLng, new MapLib.ReverseGeocodingHandler() {
                @Override
                public void handle(Address addressInfo) {
                    String city = addressInfo.getLocality();
                    String knownName = addressInfo.getFeatureName();
                    if (knownName == null)
                        knownName = "";
                    if (city == null)
                        city = "";
                    if (knownName != "" && city != "")
                        setDestText( city + " - " + knownName);
                    else
                        setDestText(city + knownName);
                }
            });
        }else {
            if (dest.objectType == DestStructureBase.DestStructureType.LOCAL_LINES_DEST) {
                DestStructureLine dst = (DestStructureLine)dest;
                endingPoint = dst.dstLatLng;
                setDestText(dst.dest);
                double lat = (dst.dstLatLng.latitude + dst.srcLatLng.latitude)/2;
                double lng = (dst.dstLatLng.longitude + dst.srcLatLng.longitude)/2;
                newPoint = new LatLng(lat, lng);
                additionalPoint = dst.srcLatLng;
            }// TODO: else ...
        }
        showStartingEndingMarker();

        btnEndingPoint.setVisibility(View.INVISIBLE);
        btnStartingPoint.setVisibility(View.VISIBLE);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(endingPoint);
        builder.include(newPoint);
        if (additionalPoint != null)
            builder.include(additionalPoint);
        LatLngBounds bounds = builder.build();

        int padding = mapFragmentView.getHeight() / 5; // offset from edges of the map in pixels
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding)); // Make all markers visible


        /*CameraPosition cameraPosition = CameraPosition.builder()
                .target(newPoint)
                .zoom(14)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/

        MainActivity.menu.findViewById(R.id.mnBack).setVisibility(View.VISIBLE);
        MainActivity.program_state = States.STATE_MAP_ENDING_POINT_SELECTED;
        //MyToast.makeText(context, "+" + latLng.latitude + " " + latLng.longitude+ "\n zoom: "+ cam.zoom, MyToast.LENGTH_SHORT).show();

        panelInfo.setVisibility(View.VISIBLE);

        BounceInterpolator bounceInterpolator = new BounceInterpolator();
        ObjectAnimator anim = ObjectAnimator.ofFloat(panelInfo, "translationY", 0f, -panelInfoHeight );
        anim.setInterpolator(bounceInterpolator);
        anim.setDuration(1100).start();
    }


    private void setDestText(String destName) {
        if (destName == null)
            txtDest.setText("مقصد: ...");
        else
            txtDest.setText("مقصد: " + destName);
    }

    private void setSrcText(String srcName) {
        if (srcName == null)
            txtSrc.setText("مبدا: ...");
        else
            txtSrc.setText("مبدا: " + srcName);
    }


    private void showStartingEndingMarker(){
        if (endingPoint != null) {
            if (endingMarker != null)
                endingMarker.remove();
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_blue_end);
            endingMarker = mMap.addMarker(new MarkerOptions().position(endingPoint).alpha((float) 0.9).title("مقصد"));
            endingMarker.showInfoWindow();
            endingMarker.setIcon(icon);
            endingMarker.setAnchor(Constants.ic_pin_blue_end_anchorU, Constants.ic_pin_blue_end_anchorV);
            endingMarker.setSnippet(""); // Name of the place
            endingMarker.setInfoWindowAnchor((float) 0.5, 0);
        }
        if (startingPoint != null){
            if (startingMarker != null)
                startingMarker.remove();
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_green_start);
            startingMarker = mMap.addMarker(new MarkerOptions().position(startingPoint).alpha((float) 0.9).title("مبدا"));
            startingMarker.showInfoWindow();
            startingMarker.setIcon(icon);
            startingMarker.setAnchor(Constants.ic_pin_green_start_anchorU, Constants.ic_pin_green_start_anchorV);
            startingMarker.setSnippet(""); //
            startingMarker.setInfoWindowAnchor((float) 0.5, 0);
        }
    }

    private void attachMapFragment() {
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_SATELLITE)
                .mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(true)
                .rotateGesturesEnabled(true)
                .tiltGesturesEnabled(false);
                /*.mapToolbarEnabled(false)
                .scrollGesturesEnabled(false)
                .zoomControlsEnabled(false)
                .zoomGesturesEnabled(false);*/

        mapFragment = MapFragment.newInstance(options);
        mapFragment.getMapAsync(this);
        getFragmentManager().beginTransaction().add(R.id.map, mapFragment).commit();
    }


    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.e(getClass().getSimpleName(), "OnPause.");
        super.onPause();

     //   callOnViewPagerHide(mViewPager.getCurrentItem());
        mapFragment.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapFragment != null) {
            mapFragment.onResume();
        } else {
            attachMapFragment();
        }
    }

    /*    @Override
        public void onResume() {
            mapFragmentView.Res
            super.onResume();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mGoogleApiClient.onDestroy();
        }

        @Override
        public void onPause() {
            super.onPause();
            mGoogleApiClient.pause();
        }

        @Override
        public void onLowMemory() {
            super.onLowMemory();
            mGoogleApiClient.onLowMemory();
        }*/
    private void animateToLocation(LatLng latLng, GoogleMap.CancelableCallback callback) {
        // Will set to position the current user location if available
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        if (latLng == null) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation !=  null)
                latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
        if (latLng != null) {
            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(latLng)
                    .zoom(lastZoom)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), callback);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        animateToLocation(lastCameraPosition, null);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Called when the client is temporarily in a disconnected state. This can happen if there
        // is a problem with the remote service (e.g. a crash or resource problem causes it to be
        // killed by the system). When called, all requests have been canceled and no outstanding
        // listeners will be executed. GoogleApiClient will automatically attempt to restore the connection.
        // Applications should disable UI components that require the service, and wait for a call to
        // onConnected(Bundle) to re-enable them.
        MyToast.makeText(context, "خطا در اتصال به سرور نقشه", MyToast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        //Called when there was an error connecting the client to the service.
        MyToast.makeText(context, "خطا در اتصال به سرور نقشه", MyToast.LENGTH_SHORT).show();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!(ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))
            googleMap.setMyLocationEnabled(true);


        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                lastCameraPosition = cameraPosition.target;
                lastZoom = cameraPosition.zoom;
                if (selectedLine == -1){
                    LatLng latLng = mMap.getCameraPosition().target;
                    List<LineInfo> linesInfo = MainActivity.getDB().findNearestLines(latLng.latitude, latLng.longitude, Constants.DISTANCE_THRESHOLD, 8, true);
                    int boldLineId = Integer.MIN_VALUE;
                    if (linesInfo.size() > 0)
                        boldLineId = linesInfo.get(0).lineId;
                    MapLib.MapDrawLines(mMap, linesInfo, boldLineId);
                }else{
                    List<LineInfo> linesInfo = MainActivity.getDB().getLines(selectedLine);
                    MapLib.MapDrawLines(mMap, linesInfo, selectedLine);
                }
                showStartingEndingMarker();
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                /*if (MainActivity.program_state == States.STATE_MAP_MAP_STARTED) {
                    List<LineInfo> linesInfo = MainActivity.getDB().findNearestLines(latLng.latitude, latLng.longitude, Constants.DISTANCE_THRESHOLD, 8, true);
                    mMap.clear();
                    if (linesInfo.size() > 0)
                        MapLib.MapDrawLines(mMap, linesInfo, linesInfo.get(0).lineId);
                }*/
                //CameraPosition cam = mMap.getCameraPosition();
                //MyToast.makeText(context, linesInfo.get(0).lineId + "\n" + latLng.latitude + " " + latLng.longitude + "\n zoom: " + cam.zoom, MyToast.LENGTH_SHORT).show();
            }
        });

        /*// Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

    }

    public void backToStep1() {
        setDestText(null);

        btnStartingPoint.setVisibility(View.INVISIBLE);
        MainActivity.menu.findViewById(R.id.mnBack).setVisibility(View.GONE);
        MainActivity.program_state = States.STATE_MAP_MAP_STARTED;

        BounceInterpolator bounceInterpolator = new BounceInterpolator();
        ObjectAnimator anim = ObjectAnimator.ofFloat(panelInfo, "translationY", -panelInfoHeight, 0f);
        anim.setInterpolator(bounceInterpolator);
        anim.setDuration(1100).start();


        animateToLocation(endingPoint, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                fadeIn.setDuration(500);

                btnEndingPoint.setVisibility(View.VISIBLE);
                btnEndingPoint.setAnimation(fadeIn);

                endingPoint = null;
                endingMarker.remove();
                endingMarker = null;

                selectedLine = -1;
            }

            @Override
            public void onCancel() {
            }
        });
    }

    public void backToStep2() {
        MainActivity.program_state = States.STATE_MAP_ENDING_POINT_SELECTED;
        setSrcText(null);
        startingPoint = null;
        startingMarker.remove();
        startingMarker = null;

        BounceInterpolator bounceInterpolator2 = new BounceInterpolator();
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(btnContinue, "translationY", -panelInfoHeight-btnContinueHeight, 0f );
        anim2.setInterpolator(bounceInterpolator2);
        anim2.setDuration(1100).start();


        LatLng newPoint = new LatLng(endingPoint.latitude + 0.01, endingPoint.longitude);

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(newPoint)
                .zoom(14)
                .build();

        /*makeStartingPointVisibleRunnable = new Runnable(){
            @Override
            public void run() {
                try {
                    makeStartingPointVisible();
                }catch (Exception ex){}
            }
        };
        new Handler().postDelayed(
                makeStartingPointVisibleRunnable, 2000);*/

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                makeStartingPointVisible();
            }

            @Override
            public void onCancel() {
                makeStartingPointVisible();
            }
        });
    }

    private Runnable makeStartingPointVisibleRunnable = null;
    private void makeStartingPointVisible(){
        /*if (makeStartingPointVisibleRunnable == null)
            return;*/
        makeStartingPointVisibleRunnable = null;
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(500);

        btnStartingPoint.setVisibility(View.VISIBLE);
        btnStartingPoint.setAnimation(fadeIn);
    }


    private ImageButton btnPassengerCountSelected = null;
    private void showPassengerCountDialog(){

        dlgPassengerCount = new Dialog(getActivity(),
                //R.style.DialogTranslucentNoTitleBar);
                android.R.style.Theme_Translucent_NoTitleBar);
        //android.R.style.Theme_DeviceDefault_Light);
        dlgPassengerCount.setContentView(R.layout.dialog_set_passenger_count);
        dlgPassengerCount.setTitle("جستجوی مقصد");
        dlgPassengerCount.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlgPassengerCount.setCancelable(true);
        // dlgLogin.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //final TextView txtLoginFormUsername = (TextView) dlgDestSearch.findViewById(R.id.txtLoginFormUsername);
        final ImageButton btnPassengerCount1 = (ImageButton) dlgPassengerCount.findViewById(R.id.btnPassengerCount1);
        final ImageButton btnPassengerCount2 = (ImageButton) dlgPassengerCount.findViewById(R.id.btnPassengerCount2);
        final ImageButton btnPassengerCount3 = (ImageButton) dlgPassengerCount.findViewById(R.id.btnPassengerCount3);
        final ImageButton btnPassengerCount4 = (ImageButton) dlgPassengerCount.findViewById(R.id.btnPassengerCount4);
        final Button btnCancel = (Button) dlgPassengerCount.findViewById(R.id.btnCancel);
        final Button btnContinue = (Button) dlgPassengerCount.findViewById(R.id.btnContinue);

        btnPassengerCount1.setTag(1);
        btnPassengerCount1.setTag(2);
        btnPassengerCount1.setTag(3);
        btnPassengerCount1.setTag(4);


        View.OnClickListener btnPassengerCountClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnPassengerCountSelected == v)
                    return;
                if (btnPassengerCountSelected != null){
                    btnPassengerCountSelected.setSelected(false);
                    btnPassengerCountSelected.animate().scaleX((float)0.9).scaleY((float)0.9);
                }
                btnPassengerCountSelected = (ImageButton)v;
                btnPassengerCountSelected.setSelected(true);
                btnPassengerCountSelected.animate().scaleX((float)1.1).scaleY((float)1.1);
            }
        };
        btnPassengerCount1.setOnClickListener(btnPassengerCountClickListener);
        btnPassengerCount2.setOnClickListener(btnPassengerCountClickListener);
        btnPassengerCount3.setOnClickListener(btnPassengerCountClickListener);
        btnPassengerCount4.setOnClickListener(btnPassengerCountClickListener);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlgPassengerCount.cancel();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlgPassengerCount.cancel();
                showPickupTimeDialog();
            }
        });

        dlgPassengerCount.show();

        new Handler().postDelayed(
            new Runnable(){
                @Override
                public void run() {
                    try {
                        LinearLayout panelBtnPassengerCount1 = (LinearLayout) dlgPassengerCount.findViewById(R.id.panelBtnPassengerCount1);
                        LinearLayout panelBtnPassengerCount2 = (LinearLayout) dlgPassengerCount.findViewById(R.id.panelBtnPassengerCount2);
                        LinearLayout panelBtnPassengerCount3 = (LinearLayout) dlgPassengerCount.findViewById(R.id.panelBtnPassengerCount3);
                        LinearLayout panelBtnPassengerCount4 = (LinearLayout) dlgPassengerCount.findViewById(R.id.panelBtnPassengerCount4);

                        ViewGroup.LayoutParams layoutParams1 = panelBtnPassengerCount1.getLayoutParams();
                        layoutParams1.height = (int) (((LinearLayout) panelBtnPassengerCount1.getParent()).getHeight() * 1.2);

                        panelBtnPassengerCount1.setLayoutParams(layoutParams1);
                        panelBtnPassengerCount2.setLayoutParams(layoutParams1);
                        panelBtnPassengerCount3.setLayoutParams(layoutParams1);
                        panelBtnPassengerCount4.setLayoutParams(layoutParams1);

                        new Handler().postDelayed(
                                new Runnable(){
                                    @Override
                                    public void run() {
                                        try {
                                            btnPassengerCountSelected = btnPassengerCount1;
                                            btnPassengerCountSelected.setSelected(true);
                                            btnPassengerCountSelected.animate().scaleX((float)1.1).scaleY((float)1.1);
                                        }catch (Exception ex){}
                                    }
                                }, 100);
                    }catch (Exception ex){}
                }
            }, 500);

    }

    private void showPickupTimeDialog(){
        dlgPickupTime = new Dialog(getActivity(),
                //R.style.DialogTranslucentNoTitleBar);
                android.R.style.Theme_Translucent_NoTitleBar);
        //android.R.style.Theme_DeviceDefault_Light);
        dlgPickupTime.setContentView(R.layout.dialog_set_pickup_time);
        dlgPickupTime.setTitle("");
        dlgPickupTime.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlgPickupTime.setCancelable(true);
        // dlgLogin.requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Button btnCancel = (Button) dlgPickupTime.findViewById(R.id.btnCancel);
        final Button btnContinue = (Button) dlgPickupTime.findViewById(R.id.btnContinue);
        final Spinner spinnerPickupTime = (Spinner) dlgPickupTime.findViewById(R.id.spinnerPickupTime);

        ArrayList<Long> spinnerArray = new ArrayList<Long>();
        spinnerArray.add((long)0);
        java.util.Date dt = new java.util.Date();
        for(int i=1; i<=30; i++)
            spinnerArray.add(dt.getTime() + i * 60000);
        PickupTimeSpinnerAdapter spinnerArrayAdapter = new PickupTimeSpinnerAdapter(context, spinnerArray);

        spinnerPickupTime.setAdapter(spinnerArrayAdapter);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlgPickupTime.cancel();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        dlgPickupTime.show();
    }

    public ArrayList<SpinnerModel> setListData()
    {
        ArrayList<SpinnerModel> CustomListViewValuesArr = new ArrayList<>();
        // Now i have taken static values by loop.
        // For further inhancement we can take data by webservice / json / xml;

        for (int i = 0; i < 11; i++) {

            final SpinnerModel sched = new SpinnerModel();

            /******* Firstly take data in model object ******/
            sched.setCompanyName("Company "+i);
            sched.setImage("image"+i);
            sched.setUrl("http:\\www."+i+".com");

            /******** Take Model Object in ArrayList **********/
            CustomListViewValuesArr.add(sched);
        }
        return CustomListViewValuesArr;

    }

    private void showDestSearchDialog(final View sender) {
        dlgDestSearch = new Dialog(getActivity(),
                //R.style.DialogTranslucentNoTitleBar);
                android.R.style.Theme_Translucent_NoTitleBar);
                //android.R.style.Theme_DeviceDefault_Light);
        dlgDestSearch.setContentView(R.layout.dialog_dest_search);
        dlgDestSearch.setTitle("جستجوی مقصد");
//        dlgDestSearch.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlgDestSearch.setCancelable(true);
        // dlgLogin.requestWindowFeature(Window.FEATURE_NO_TITLE);

        final EditText txtSearchPlaces = (EditText) dlgDestSearch.findViewById(R.id.txtSearchPlaces);
        final LinearLayout btnChooseOnTheMap = (LinearLayout)dlgDestSearch.findViewById(R.id.btnChooseOnTheMap);
        final ExpandableListView lstDestSearchResults = (ExpandableListView) dlgDestSearch.findViewById(R.id.lstDestSearchResults);
        lstDestSearchResults.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() { // Disable collapsing of the ListView
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true; // This way the expander cannot be collapsed
            }
        });

        if (sender == txtInlineSearchPlaces)
            btnChooseOnTheMap.setVisibility(View.GONE);
        btnChooseOnTheMap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (sender == btnEndingPoint){ // if the dialog was called by pressing the btnEndingPoint, choose the point as the dest
                    doSelectEndingPoint(null);
                }
                dlgDestSearch.cancel();
                return false;
            }
        });

        txtSearchPlaces.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String query = txtSearchPlaces.getText().toString().trim();
                if (query.equals(""))
                    lstDestSearchResults.setAdapter(initialDestSearchResults());
                else {
                    if (googleSearchRunnable != null)
                        googleSearchRunnable.Stop();
                    googleSearchRunnable = new StoppableRunnable(){
                        @Override
                        public void run() {
                            googleSearchRunnable = null;
                            if (!this.isStopped())
                                googleSearch(query, lstDestSearchResults);
                        }
                    };
                    new Handler().postDelayed(googleSearchRunnable, 500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        lstDestSearchResults.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final DestStructureBase dst = destListAdapter.getData().get(groupPosition).second.get(childPosition);
                if (dst.objectType == DestStructureBase.DestStructureType.LOCAL_LINES_DEST) {
                    DestStructureLine dest = (DestStructureLine) dst;

                    //MyToast.makeText(context, ((DestStructureLine) dst).dest, MyToast.LENGTH_LONG).show();
                    doSelectEndingPoint(dest);
                    dlgDestSearch.cancel();
                    MyToast.makeText(context, "لطفا در مسیر خط انتخاب شده نقطه سوار شدن خود را مشخص نمائید", MyToast.LENGTH_LONG).show();
                    selectedLine = dest.lineId;
                }else if (dst.objectType == DestStructureBase.DestStructureType.GOOLE_PLACES_MAP) {
                    MyToast.makeText(context, ((DestStructureGooglePlace) dst).description, MyToast.LENGTH_LONG).show();

                    Places.GeoDataApi.getPlaceById(mGoogleApiClient, ((DestStructureGooglePlace) dst).placeId.toString())
                            .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                @Override
                                public void onResult(PlaceBuffer places) {
                                    if (places.getStatus().isSuccess()) {
                                        final Place myPlace = places.get(0);
                                        LatLng queried_location = myPlace.getLatLng();
                                        //MyToast.makeText(context, queried_location.latitude+ " "+ queried_location.longitude, MyToast.LENGTH_LONG).show();
                                        dlgDestSearch.cancel();
                                        CameraPosition cameraPosition = CameraPosition.builder()
                                                .target(new LatLng(queried_location.latitude, queried_location.longitude))
                                                .zoom(13)
                                                .build();
                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                    }
                                    places.release();
                                }
                            });
                }


                return false;
            }
        });

        destListAdapter = initialDestSearchResults();
        lstDestSearchResults.setAdapter(destListAdapter);

        dlgDestSearch.show();
    }

    private DestSearchExpandableListAdapter initialDestSearchResults(){
        listDataHeader = new ArrayList<String>();
        listDataChild = new ArrayList<Pair<String, List<DestStructureBase>>>();

        List<DestStructureBase> nearbyLines = findNearbyLines();
        if (nearbyLines.size() > 0)
            listDataHeader.add("خطوط تاکسی مجاور");
        else
            listDataHeader.add("خط تاکسی در این حوالی جود ندارد");
        listDataChild.add(new Pair<>(listDataHeader.get(listDataHeader.size()-1), nearbyLines));

        return new DestSearchExpandableListAdapter(context, listDataHeader, listDataChild);
    }

    public List<DestStructureBase> findNearbyLines(){
        // Destinations near to current selected point
        LatLng latLng = mMap.getCameraPosition().target;
        List<LineInfo> linesInfo = MainActivity.getDB().findNearestLines(latLng.latitude, latLng.longitude, Constants.DISTANCE_THRESHOLD, 8, true);
        List<DestStructureBase> list = new ArrayList<DestStructureBase>();
        for (LineInfo lineInfo: linesInfo){
            if (lineInfo.points.size() > 0)
                list.add(new DestStructureLine(lineInfo.lineId,  lineInfo.src, lineInfo.dst,lineInfo.fare, lineInfo.points.get(0), lineInfo.points.get(lineInfo.points.size()-1)));
        }
        return list;
    }


    private ArrayList<DestStructureBase> getAutocomplete(CharSequence constraint) {
        String TAG = "dd";
        LatLngBounds mBounds = new LatLngBounds(new LatLng(24.949854, 43.817423), new LatLng(39.997026, 63.548866)); // (Left, Button), (Right, Top)
        AutocompleteFilter mPlaceFilter = null;

        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG, "Starting autocomplete query for: " + constraint);

            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<AutocompletePredictionBuffer> results = Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, constraint.toString(), mBounds, mPlaceFilter);

            // This method should have been called off the main UI thread. Block and wait for at most 60s
            // for a result from the API.
            AutocompletePredictionBuffer autocompletePredictions = results.await(60, TimeUnit.SECONDS);

            // Confirm that the query completed successfully, otherwise return null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Toast.makeText(context, "Error contacting API: " + status.toString(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting autocomplete prediction API call: " + status.toString());
                autocompletePredictions.release();
                return null;
            }

            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount() + " predictions.");

            // Copy the results into our own data structure, because we can't hold onto the buffer.
            // AutocompletePrediction objects encapsulate the API response (place ID and description).

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                // Get the details of this prediction and copy it into a new PlaceAutocomplete object.
                resultList.add(new DestStructureGooglePlace(prediction.getDescription(), prediction.getPlaceId()));
            }

            // Release the buffer now that all data has been copied.
            autocompletePredictions.release();

            return resultList;
        }
        Log.e("ss", "Google API client is not connected for autocomplete query.");
        return null;
    }
    public void googleSearch(final String query, final ExpandableListView expListView){
        MyAsyncTask.DoJob(context,
                new MyAsyncTask.MyAsyncTaskJob() {
                    @Override
                    public MyAsyncTask.MyAsyncTaskResultBase perform(MyAsyncTask.MyAsyncTaskParameterBase parameter) {
                        //GooglePlaceAPI googlePlaceAPI = new GooglePlaceAPI();
                        //ArrayList<String> list = googlePlaceAPI.autocomplete(query);
                        try {
                            ArrayList<DestStructureBase> list = getAutocomplete(query);
                            return new MyAsyncTask.MyAsyncTaskResultGooglePlaceAPI(list);
                        }catch (Exception ex){
                            return new MyAsyncTask.MyAsyncTaskResultBase(ex.getMessage());
                        }


                        /*ArrayList<String> list = new ArrayList<String>();
                        list.add("aaa");
                        list.add("aaa");
                        return new MyAsyncTaskResultGooglePlaceAPI(list);
                        return null;*/
                    }
                }, new MyAsyncTask.MyAsyncTaskPostJob() {
                    @Override
                    public void perform(MyAsyncTask.MyAsyncTaskResultBase result) {
                        if (result.errorMessage != null) {
                            //MyToast.makeText(context, result.errorMessage, MyToast.LENGTH_LONG).show();
                            return;
                        }
                        List<DestStructureBase> lst = ((MyAsyncTask.MyAsyncTaskResultGooglePlaceAPI) result).list;
                        /*List<DestStructureBase> lst = new ArrayList<DestStructureBase>();
                        for (String item : list)
                            lst.add(new DestStructureGooglePlace(item, new LatLng(0, 0)));*/

                        listDataHeader = new ArrayList<String>();
                        listDataChild = new ArrayList<Pair<String, List<DestStructureBase>>>();

                        listDataHeader.add("نتایج جستجو");
                        listDataChild.add(new Pair(listDataHeader.get(listDataHeader.size() - 1), lst));

                        destListAdapter = new DestSearchExpandableListAdapter(context, listDataHeader, listDataChild);
                        expListView.setAdapter(destListAdapter);
                    }
                },
                new MyAsyncTask.MyAsyncTaskParameterGooglePlaceAPI(query),
                null,
                null,
                new MyAsyncTask.MyAsyncTaskUIHandler() {
                    @Override
                    public void handle(MyAsyncTask.MyAsyncTaskResultBase result) {
                    }
                }, null, "GooglePlaceAPI"
        );
    }


}
