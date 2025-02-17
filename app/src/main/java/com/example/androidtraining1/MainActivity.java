package com.example.androidtraining1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Context mContext = this;
    private Spinner currency_type;
    private EditText currency_value;
    private Button btn_exchange;
    private RecyclerView list_exchange;
    private ExchangeAdapter list_exchange_adapter;

    private CurrencyService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = ((CurrencyService.MyBinder)iBinder).getService();
            initExchangeList();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    public MainActivity() {
        super();
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            Log.d(TAG, "onStateChanged: " + event.name());
            if(event.equals(Lifecycle.Event.ON_CREATE)){
                setContentView(R.layout.activity_main);
                initView();
                initListener();
                bindCurrencyService();
            } else if(event.equals(Lifecycle.Event.ON_DESTROY)){
                unbindService(mConnection);
            }
        });
    }

    private void initView(){
        //get the view component after Activity created
        currency_type = findViewById(R.id.currency_type);
        currency_value = findViewById(R.id.currency_value);
        list_exchange = findViewById(R.id.list_exchange);
//        btn_exchange = findViewById(R.id.btn_exchange);
    }

    private void setAdapter(){
        ArrayAdapter <String> adapter=
                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mService.getCurrencyList());
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        currency_type.setAdapter(adapter);
//        spinner.setAdapter(adapter);
    }

    private void initListener(){
        //update exchange result when exchange button be clicked
        currency_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    // * Revise to support multi language //
                    String currency = getString(R.string.currency_usd);
                    switch (position) {
                        case 0:
                            currency = getString(R.string.currency_usd);
                            break;
                        case 1:
                            currency = getString(R.string.currency_twd);
                            break;
                        case 2:
                            currency = getString(R.string.currency_jpy);
                            break;
                        case 3:
                            currency = getString(R.string.currency_eur);
                            break;
                        case 4:
                            currency = getString(R.string.currency_cny);
                            break;
                    }

                    // * Use BigDecimal to handle large numbers //
                    BigDecimal amount = BigDecimal.ZERO;
                    try {
                        amount = new BigDecimal(currency_value.getText().toString());
                    } catch (Exception e) {
                        currency_value.setText("");
                    }
                    list_exchange_adapter.updateBaseCurrencyAndAmount(currency, amount);
                }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // ===== Automatic update exchange result when currency value be changed ===== //
        currency_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                currency_value.removeTextChangedListener(this);
                BigDecimal amount = BigDecimal.ZERO;
                try {
                    amount = new BigDecimal(currency_value.getText().toString());
                } catch (Exception e) {
                    currency_value.setText("");
                }

                String currency = getString(R.string.currency_usd);
                switch (currency_type.getSelectedItemPosition()) {
                    case 0:
                        currency = getString(R.string.currency_usd);
                        break;
                    case 1:
                        currency = getString(R.string.currency_twd);
                        break;
                    case 2:
                        currency = getString(R.string.currency_jpy);
                        break;
                    case 3:
                        currency = getString(R.string.currency_eur);
                        break;
                    case 4:
                        currency = getString(R.string.currency_cny);
                        break;
                }
                list_exchange_adapter.updateBaseCurrencyAndAmount(currency, amount);
                currency_value.addTextChangedListener(this);
            }
        });

//        btn_exchange.setOnClickListener(v->{
//            String currency = "USD";
//            switch (currency_type.getSelectedItemPosition()){
//                case 0:
//                    currency = "USD";
//                    break;
//                case 1:
//                    currency = "TWD";
//                    break;
//                case 2:
//                    currency = "JPY";
//                    break;
//            }
//            double amount = 0.0;
//            try {
//                amount = Double.valueOf(currency_value.getText().toString());
//            } catch (Exception e) {
//                currency_value.setText("");
//            }
//            list_exchange_adapter.updateBaseCurrencyAndAmount(currency, amount);
//        });
    }

    private void bindCurrencyService(){
        Intent intent = new Intent(MainActivity.this, CurrencyService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void initExchangeList(){
        //create ListView Adapter to control list items
        list_exchange_adapter = new ExchangeAdapter(LayoutInflater.from(mContext), mService);
        list_exchange_adapter.updateBaseCurrencyAndAmount(getString(R.string.currency_usd), BigDecimal.valueOf(1.0));
        list_exchange.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        list_exchange.setAdapter(list_exchange_adapter);
    }
}