package gruppe3.convoy.functionality;

import android.app.Application;
import android.content.SharedPreferences;
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

import gruppe3.convoy.ProgressFragment;

/**
 * Created by Jon on 06/01/2016.
 */
public class SingleTon extends Application {

    private static SingleTon ourInstance = new SingleTon();
    public static ArrayList<Spot> spots, searchedSpots;
    public static MyLocation myLocation;
    public static int timer,minutter;
    public static final String searchTxt1 = "Finding Location", searchTxt2 = "Connecting to Database", searchTxt3 = "Connected. Fetching data";
    public static Boolean food, wc, bed, bath, fuel, adblue, roadTrain = false, dataLoadDone = false, dataLoading = false, nightMode, saveData, switchMode = false;
    public static boolean hasDest;
    public static LatLng destPos;
    public static String destAdress = "Your destination";
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
        } else {
            SingleTon.nightMode = false;
            SingleTon.food = false;
            SingleTon.wc = false;
            SingleTon.bath = false;
            SingleTon.bed = false;
            SingleTon.fuel = false;
            SingleTon.adblue = false;
            SingleTon.roadTrain = false;
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
                            LatLng pos = new LatLng(Double.valueOf(spotList.get(i).getString("posLat")), Double.valueOf(spotList.get(i).getString("posLng")));
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
                                    pos
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
    public void saveSpots(ArrayList spots, String filename) {
       try {
          Serialisering.gem(spots, filename);

       } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void loadSpots(String filename) {
        spotsFile = new File(getFilesDir(), "Spots");
        if (spotsFile.exists()) {
            try {
                Serialisering.hent(filename);
                ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Spots1");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

}
