package org.redpin.android.wifi;

import android.net.wifi.ScanResult;
import android.util.Log;

import org.redpin.android.ui.MapViewActivity;

import java.util.List;

public class WifiInformation {

   List<ScanResult> currentScanList;
   private static long numOfScans = 0;
   String[] wifiString;

   private static double ID_POS_CONTRIBUTION = 1;
   private static double SIGNAL_CONTRIBUTION = 1;
   private static double FACTOR = 10000;

   private FingerprintDatabase fingerprintDB;

   public WifiInformation()
   {
      currentScanList = null;
      wifiString=new String[1];
      wifiString[0]="Starting";

      initializeFingerprintDb(fingerprintDB);
   }

   // Function to calculate signal strength accuracy per wifi reading
   private double signalContribution(double baseRssi, double measuredRssi) {
      //Possible RSSI range 0 to 100
      double base = baseRssi;
      //Possible diff range 0 to 100
      double diff = Math.abs(baseRssi - measuredRssi);
      Log.i("#####FoongFoong#####",
              "signalContribution(): base: " + base
                      + ", diff: " + diff);

      //Normalize to range of -1 to 1
      double normalize = (2*(diff/100))-1;
      Log.i("#####FoongFoong#####",
              "signalContribution(): normalize: " + normalize);

      //Inverse
      double finalNormalize = -normalize;
      Log.i("#####FoongFoong#####",
              "signalContribution(): finalNormalize: " + finalNormalize);

      return finalNormalize;
   }

   // Function to calculate accuracy level per location
   public int measurementAccuracyLevel(MeasurementPerLocation baseMeasurementPerLocation,
                                         MeasurementPerLocation currentMeasurementPerLocation) {

      double totalCredit = 0;
      double account = 0;

      for (int i = 0; i < baseMeasurementPerLocation.numOfWifiPoints; i++) {
         WifiInfoRow baseWifiRow = baseMeasurementPerLocation.myWifiInfoRowArray[i];
         for (int j = 0; j < currentMeasurementPerLocation.numOfWifiPoints; j++) {
            WifiInfoRow currentWifiRow = currentMeasurementPerLocation.myWifiInfoRowArray[j];

            Log.i("#####FoongFoong#####",
                    "measurementAccuracyLevel(): baseWifiRow.BSSID: " + baseWifiRow.BSSID
                            + ", baseWifiRow.SSID: " + baseWifiRow.SSID
                            + ", baseWifiRow.level: " + baseWifiRow.level);
            if (currentWifiRow != null) {
               Log.i("#####FoongFoong#####",
                       "measurementAccuracyLevel(): currentWifiRow.BSSID: " + currentWifiRow.BSSID
                               + ", currentWifiRow.SSID: " + currentWifiRow.SSID
                               + ", currentWifiRow.level: " + currentWifiRow.level);
            } else {
               Log.i("#####FoongFoong#####",
                       "measurementAccuracyLevel(): currentWifiRow == NULL ");
            }

            //bssid match: add ID contribution and signal strength
            if (baseWifiRow != null && baseWifiRow.BSSID != null && currentWifiRow != null && currentWifiRow.BSSID != null
                    && baseWifiRow.BSSID.equals(currentWifiRow.BSSID)) {
               Log.i("#####FoongFoong#####",
                       "measurementAccuracyLevel(): MATCH baseWifiRow.BSSID: " + baseWifiRow.BSSID
                               + ", baseWifiRow.SSID: " + baseWifiRow.SSID
                               + ", baseWifiRow.level: " + baseWifiRow.level);
               Log.i("#####FoongFoong#####",
                       "measurementAccuracyLevel(): MATCH currentWifiRow.BSSID: " + currentWifiRow.BSSID
                               + ", currentWifiRow.SSID: " + currentWifiRow.SSID
                               + ", currentWifiRow.level: " + currentWifiRow.level);
               account += ID_POS_CONTRIBUTION;
               account += signalContribution(baseWifiRow.level, currentWifiRow.level);
            }
         }
      }

      totalCredit += baseMeasurementPerLocation.numOfWifiPoints * ID_POS_CONTRIBUTION;
      totalCredit += baseMeasurementPerLocation.numOfWifiPoints * SIGNAL_CONTRIBUTION;

      int accuracy = 0;
      if (account > 0) {
         //Compute percentage of account from totalCredit -> [0,1];
         //stretch by accuracy span -> [0,FACTOR];
         double a = (account / totalCredit) * FACTOR;
         // same as Math.round
         accuracy = (int) Math.floor(a + 0.5d);
      }

      Log.i("#####FoongFoong#####",
              "measurementAccuracyLevel(): Account: " + account
                      + ", Total Credit possible: " + totalCredit
                      + ", Accuracy: " + accuracy);

      return accuracy;
   }

   public void updateInformation(List<ScanResult> wifiScanList) {

      //----------------------------------------------------------------------------------------------------
      //   TODO: move this code back to initializeFingerprintDb() later
      //----------------------------------------------------------------------------------------------------
      fingerprintDB = new FingerprintDatabase(3);

      fingerprintDB.fillUpEachMeasurementPerLocation(0,"A1",3);
      fingerprintDB.fillUpEachMeasurementPerLocation(1,"B1",3);
      fingerprintDB.fillUpEachMeasurementPerLocation(2,"C1",3);


      Log.i("wj", "initializeFingerprintDb part 1 complete ");

      fingerprintDB.myMeasurementPerLocationArray[0].fillUpEachWifiInfoRow(0,"84:24:8d:40:84:00","M-Wireless", 45);
      fingerprintDB.myMeasurementPerLocationArray[0].fillUpEachWifiInfoRow(1,"84:24:8d:40:4d:81","M-Guest", 63);
      fingerprintDB.myMeasurementPerLocationArray[0].fillUpEachWifiInfoRow(2,"84:24:8d:40:68:91","M-Guest", 65);

      Log.i("wj", "initializeFingerprintDb part 2 complete ");

      fingerprintDB.myMeasurementPerLocationArray[1].fillUpEachWifiInfoRow(0,"84:24:8d:40:2f:11","M-Guest", 61);
      fingerprintDB.myMeasurementPerLocationArray[1].fillUpEachWifiInfoRow(1,"84:24:8d:3f:be:b1","M-Guest", 59);
      fingerprintDB.myMeasurementPerLocationArray[1].fillUpEachWifiInfoRow(2,"84:24:8d:3f:e2:51","M-Guest", 43);

      Log.i("wj", "initializeFingerprintDb part 3 complete ");

      fingerprintDB.myMeasurementPerLocationArray[2].fillUpEachWifiInfoRow(0,"84:24:8d:3f:d1:e0","M-Wireless", 74);
      fingerprintDB.myMeasurementPerLocationArray[2].fillUpEachWifiInfoRow(1,"84:24:8d:40:f8:90","M-Wireless", 74);
      fingerprintDB.myMeasurementPerLocationArray[2].fillUpEachWifiInfoRow(2,"84:24:8d:40:fe:c1","M-Guest", 60);
      //----------------------------------------------------------------------------------------------------
      //   END_OF_TODO: move this code back to initializeFingerprintDb() later
      //----------------------------------------------------------------------------------------------------


      // for debugging , u can use --> db.printFingerprintDb();

      currentScanList = wifiScanList;
      int currentScanListSize = currentScanList.size();
      wifiString=new String[wifiScanList.size()];
      int i;
      int highestAccuracyLevel = 0;
      MeasurementPerLocation highAccuracyMeasurementPerLocation = null;


      numOfScans++;


      //----------------------------------------------------------------------------------------------------
      //   Getting current wifi point and data into MeasurementPerLocation
      //----------------------------------------------------------------------------------------------------
      int count = 0;
      for (i = 0; i < currentScanList.size(); i++) {
         if (currentScanList.get(i).SSID.equals("M-Wireless")  || currentScanList.get(i).SSID.equals("M-Guest")) {
            count++;
         }
      }
      MeasurementPerLocation currentMeasurementPerLocation = new MeasurementPerLocation("current",count);
      count = 0;
      for (i = 0; i < currentScanList.size(); i++) {

         if (currentScanList.get(i).SSID.equals("M-Wireless")  || currentScanList.get(i).SSID.equals("M-Guest")) {
            currentMeasurementPerLocation.fillUpEachWifiInfoRow(count, currentScanList.get(i).BSSID, currentScanList.get(i).SSID, Math.abs(currentScanList.get(i).level));
            count++;
            Log.i("#####FoongFoong#####",
                    "updateInformation(): MATCH currentScanList.get(i).BSSID: " + currentScanList.get(i).BSSID
                            + " currentScanList.get(i).SSID: " + currentScanList.get(i).SSID
                            + " currentScanList.get(i).level: " + currentScanList.get(i).level);
         }
      }
      Log.i("#####FoongFoong#####",
              "updateInformation(): currentMeasurementPerLocation.numOfWifiPoints: " + currentMeasurementPerLocation.numOfWifiPoints
               + " count: " + count);

      // for logging , u can use --> currentMeasurementPerLocation.printWifiInfoRow();

      //-----------------------------------------------------------------------------------------------------

      //---------------------------------------------------------------
      // Log scan list
      //---------------------------------------------------------------
      for (i = 0; i < currentScanListSize; i++) {
         Log.i("wj", "numOfScans: " + numOfScans
                 + ", BSSID: " + currentScanList.get(i).BSSID
                 + ", SSID: " + currentScanList.get(i).SSID
                 + ", level: " + currentScanList.get(i).level);

         wifiString[i] = (currentScanList.get(i).BSSID
                 +","+currentScanList.get(i).SSID
                 +","+currentScanList.get(i).level);

      }

      //---------------------------------------------------------------
      // Location Calculation
      //---------------------------------------------------------------
      //Compare measured data with each location's data in database
      for (int j = 0; fingerprintDB != null && j < fingerprintDB.numOfLocations; j++) {
         MeasurementPerLocation baseMeasurementPerLocation = fingerprintDB.myMeasurementPerLocationArray[j];
         Log.i("#####FoongFoong#####",
                 "updateInformation(): FOR LOOP baseMeasurementPerLocation.numOfWifiPoints: " + baseMeasurementPerLocation.numOfWifiPoints);
         int accuracyLevelPerLocation = 0;
         if (baseMeasurementPerLocation != null && currentMeasurementPerLocation != null) {
            accuracyLevelPerLocation = measurementAccuracyLevel(baseMeasurementPerLocation, currentMeasurementPerLocation);
         }

         //Store the highest accuracy level and its MeasurementPerLocation class pointer
         if (accuracyLevelPerLocation > highestAccuracyLevel) {
            highestAccuracyLevel = accuracyLevelPerLocation;
            highAccuracyMeasurementPerLocation = baseMeasurementPerLocation;
         }
      }

      Log.i("#####FoongFoong#####",
              "updateInformation(): Highest Accuracy Level: " + highestAccuracyLevel);

      if (highAccuracyMeasurementPerLocation != null) {
         Log.i("#####FoongFoong#####",
                 "updateInformation(): Highest Accuracy Level Location Name: " + highAccuracyMeasurementPerLocation.name);

         //---------------------------------------------------------------
         // TODO: Show Location, temp, change later.
         //---------------------------------------------------------------
         String locationString = highAccuracyMeasurementPerLocation.name;
         if (locationString.equals("A1")) {
            MapViewActivity.setMarkerLocation(205*2, 8*2);
            Log.i("#####FoongFoong#####", "updateInformation(): At A1: ");
         } else if (locationString.equals("B1")) {
            MapViewActivity.setMarkerLocation(297*2, 8*2);
            Log.i("#####FoongFoong#####", "updateInformation(): At B1: ");
         } else if (locationString.equals("C1")) {
            MapViewActivity.setMarkerLocation(599*2, 50*2);
            Log.i("#####FoongFoong#####", "updateInformation(): At C1: ");
         } else {
            //to prove that it not work, point to E11
            MapViewActivity.setMarkerLocation(167*2, 412*2);
            Log.i("#####FoongFoong#####", "updateInformation(): NOT MATCH... At E11: ");
         }
         //---------------------------------------------------------------
         // END_OF_TODO: Show Location, temp, change later.
         //---------------------------------------------------------------
      }
   }

   public void initializeFingerprintDb(FingerprintDatabase db)
   {
      //---------------------------------------------------------------------------------------------
      // Initialize fingerprint database , store all hard-coded database information during this function
      //------------------------------------------------------------------------------------------------


   }

   public String[] getStringArray()
   {
      return wifiString;
   }
}