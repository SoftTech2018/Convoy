package gruppe3.convoy.functionality;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jon on 06/01/2016.
 */
public class SingleTon extends Application {

    private static SingleTon ourInstance = new SingleTon();
    public static ArrayList<Spot> spots, searchedSpots;
    public static MyLocation myLocation;
    public static int timer,minutter;
    public static final String searchTxt1 = "Finding Location", searchTxt2 = "Connecting to Database", searchTxt3 = "Connected. Fetching data";
    public static Boolean food, wc, bed, bath, fuel, adblue, roadTrain = false, dataLoadDone = false, dataLoading = false, nightMode, saveData, switchMode = false, powerSaving = false, session = false;
    public static boolean hasDest;
    public static LatLng destPos;
    public static String destAdress = "Your destination";
    public static Sensor accelerometer;
    public static SensorManager sensorManager;
    BoundService.LocalBinder mBinder;
    public static BoundService mService;
    public static ServiceConnection mConnection;
    public static boolean mBound = false;
    static ArrayList<Spot> spotOut;
    public static double speedSetting;
    private File spotsFile;


    public static SingleTon getInstance() {
        return ourInstance;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("Data", "SingleTon OnCreate");
        Parse.initialize(this);
        Log.d("Data", "Parse initialiseret");

        startBinding();
        Intent intent = new Intent(this, BoundService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("Debug", "Preference Manager er startet");
        SingleTon.saveData = prefs.getBoolean("saveData", true);
        if(SingleTon.saveData){
            SingleTon.nightMode = prefs.getBoolean("nightMode", false);
            SingleTon.food = prefs.getBoolean("food", false);
            SingleTon.wc = prefs.getBoolean("wc", false);
            SingleTon.bed = prefs.getBoolean("bed", false);
            SingleTon.bath = prefs.getBoolean("bath", false);
            SingleTon.fuel = prefs.getBoolean("fuel", false);
            SingleTon.adblue = prefs.getBoolean("adblue", false);
            SingleTon.roadTrain = prefs.getBoolean("roadTrain", false);
            SingleTon.powerSaving = prefs.getBoolean("powerSaving", false);
            SingleTon.speedSetting = Double.valueOf(prefs.getString("speedSetting", "1.3"));
        } else {
            SingleTon.nightMode = false;
            SingleTon.food = false;
            SingleTon.wc = false;
            SingleTon.bath = false;
            SingleTon.bed = false;
            SingleTon.fuel = false;
            SingleTon.adblue = false;
            SingleTon.roadTrain = false;
            SingleTon.powerSaving = false;
            SingleTon.speedSetting = 1.3;
        }
        if (SingleTon.myLocation != null){
            SingleTon.myLocation.onResume(); // Start opdatering fra GPS
        }
    }

    public static void fetchData(){
        dataLoading=true;

        if(spots==null){
            Log.d("Data", "Spots er null");
            // Asynkront kald til DB
            ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Spots1");
            query2.setLimit(1000);
            query2.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> spotList, ParseException e) {
                    Log.d("Data", "e = "+e);
                    if (e == null) {
                        spots = new ArrayList<Spot>();
                        for (int i = 0; spotList.size() > i; i++) {
                            //LatLng pos = new LatLng(Double.valueOf(spotList.get(i).getString("posLat")), Double.valueOf(spotList.get(i).getString("posLng")));
                            spots.add(new Spot(
                                    spotList.get(i).getString("desc"),
                                    spotList.get(i).getBoolean("adblue"),
                                    spotList.get(i).getBoolean("food"),
                                    spotList.get(i).getBoolean("bath"),
                                    spotList.get(i).getBoolean("bed"),
                                    spotList.get(i).getBoolean("wc"),
                                    spotList.get(i).getBoolean("fuel"),
                                    spotList.get(i).getBoolean("roadtrain"),
                                    spotList.get(i).getString("createdAt"),
                                    spotList.get(i).getString("posLat"),
                                    spotList.get(i).getString("posLng")
                            ));

                        }
                        Log.d("Data", "Done with spots!");
                        Log.d("Data", "Size of Spots = " + spots.size());

                        SingleTon.dataLoadDone = true;
                    } else {
                        Log.d("score", "Error: " + e.getMessage());
                    }
                }
            });
        }
    }

//    public void saveSpots(ArrayList<Spot> spots, String filename) {
//       try {
//          Serialisering.gem(spots, filename);
//
//       } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void loadSpots(String filename) {
//        spotsFile = new File(getFilesDir(), "Spots");
//        if (spotsFile.exists()) {
//            try {
//                ArrayList<Spot> spots = (ArrayList<Spot>) Serialisering.hent(filename);
//                System.out.println(spots.get(spots.size()-1).getCreatedAt());
//
//                ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Spots1");
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//
//    }



    public void startBinding(){
        /** Defines callbacks for service binding, passed to bindService() */
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                mBinder = (BoundService.LocalBinder) service;

                mService = mBinder.getService();
                System.out.println("mService"+mService);
                mBound = true;
                System.out.println("mBound"+mBound);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }
        };
    }

    public void dropBinding(){
        unbindService(mConnection);
    }

    public static ArrayList hentSpotsLocal(final String name){

        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SingleTon.mBound) {
                    System.out.println("if sætning");
                    SingleTon.spotOut = (ArrayList<Spot>) mService.hent(name);

                } else {
                    h.postDelayed(this, 100);
                }
            }
        }, 100);
        return spotOut;
    }

    public static void gemSpotsLocal(final String name){
        final ArrayList<Spot> spots = new ArrayList();
        spots.add(new Spot("boundTest", true, true, true, true, true, true, true, "createdAt", "posLat", "posLng"));

        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SingleTon.mBound) {
                    System.out.println("if sætning");
                    mService.gem(spots, name);
                } else {
                    h.postDelayed(this, 100);
                }
            }
        }, 100);
    }


    public void hentSpotsDb(){

    }

    public void gemSpotsDb(){

    }

}
