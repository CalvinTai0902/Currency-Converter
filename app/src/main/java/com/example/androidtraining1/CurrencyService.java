package com.example.androidtraining1;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CurrencyService extends LifecycleService implements Runnable {
    private static final String TAG = CurrencyService.class.getSimpleName();
    private static final int SYNC_INTERVAL = 10000;
    private Thread mTask = new Thread(this);
    private Map<String, Double> mCurrenciesRateMap = new HashMap<>();

    public class MyBinder extends Binder {
        public CurrencyService getService(){
            return CurrencyService.this;
        }
    }

    public CurrencyService() {
        super();
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            Log.d(TAG, "onStateChanged: " + event.name());
            if(event.equals(Lifecycle.Event.ON_START)){
                mTask.start();
            } else if(event.equals(Lifecycle.Event.ON_STOP)){
                mTask.interrupt();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return new MyBinder();
    }

    @Override
    public void run() {
        Log.d(TAG, "+++ runnable start +++");
        while(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)){
            try {
                updateCurrenciesRate();
                mTask.sleep(SYNC_INTERVAL);
            } catch (InterruptedException e) {
                Log.d(TAG, "runnable interrupted!");
            }
        }
        Log.d(TAG, "--- runnable stop ---");
    }

    // ===== To achieve the capability of exchange rate sync & multi language support ===== //
    private void updateCurrenciesRate(){
//        Log.d(TAG, "updateCurrenciesRate!");
        mCurrenciesRateMap.put(getResources().getString(R.string.currency_usd), realtimeCurRateUpdate("USD"));
        mCurrenciesRateMap.put(getResources().getString(R.string.currency_jpy), realtimeCurRateUpdate("JPY"));
        mCurrenciesRateMap.put(getResources().getString(R.string.currency_twd), realtimeCurRateUpdate("TWD"));
        mCurrenciesRateMap.put(getResources().getString(R.string.currency_cny), realtimeCurRateUpdate("CNY"));
        mCurrenciesRateMap.put(getResources().getString(R.string.currency_eur), realtimeCurRateUpdate("EUR"));
//        Log.d(TAG, "MAP" +mCurrenciesRateMap.toString());
    }

    // ===== Create a function to Get realtime exchange rate relative to USD through API ===== //
    private Double realtimeCurRateUpdate(String exchangeTarget){
        Double[] rate = {(double) 0};

        // * Create new thread to run API request //
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    String GET_URL = "https://tw.rter.info/capi.php";
                    URL url = new URL(GET_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");

                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){ //successful
                        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while((inputLine = in.readLine()) != null){
                            response.append(inputLine);
                        }in.close();

                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONObject fromCurrencyJson = jsonObject.getJSONObject("USD" +exchangeTarget);
//                        Log.d(TAG, fromCurrencyJson.toString());
                        rate[0] = fromCurrencyJson.getDouble("Exrate");      // ??? Question why have to be defined as Array
//                        Log.d(TAG, "rate: " + rate[0]);

                    }
                }
                catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            // * Wait for the task to complete //
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
//        Log.d(TAG, "FinalRate: " + rate[0]);
        return rate[0];
    }

//     ======== Original Function Code ========
//    random rate with +-10%
//    private double fakeRateRandomFixed(double originRate){
//        return originRate*(1.0+(Math.random()-0.5)/10);
//    }
//     ========================================

    // * Revise to support multi language //
    public List<String> getCurrencyList(){
//        return Arrays.asList(new String[]{"USD", "TWD", "JPY", "EUR", "CNY"});
        return Arrays.asList(getResources().getStringArray(R.array.currency_list));
    }

    // * Use BigDecimal to handle large numbers //
    public BigDecimal exchange(BigDecimal amount, String from, String to){
        return amount.multiply(BigDecimal.valueOf(getExchangeRate(from, to)));
    }

    private double getExchangeRate(String from, String to){
        double fromRate = getExchangeRateFromUSD(from);
        double toRate = getExchangeRateFromUSD(to);

        // * Avoid divide by zero //
        if(fromRate == 0.0 || toRate == 0.0){
            return 0.0;
        }
        return toRate / fromRate;
    }

    private double getExchangeRateFromUSD(String to){
        return mCurrenciesRateMap.getOrDefault(to, 0.0);
    }
}