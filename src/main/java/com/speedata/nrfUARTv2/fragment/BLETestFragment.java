package com.speedata.nrfUARTv2.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.speedata.nrfUARTv2.DeviceListActivity;
import com.speedata.nrfUARTv2.R;
import com.speedata.nrfUARTv2.UartService;
import com.speedata.nrfUARTv2.myinterface.OnFragmentInteractionListener;
import com.speedata.nrfUARTv2.utils.DataConversionUtils;
import com.speedata.nrfUARTv2.utils.MyLogger;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain mContext fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BLETestFragment#newInstance} factory method to
 * create an instance of mContext fragment.
 */
public class BLETestFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private MyLogger logger = MyLogger.jLog();

    /**
     * Use mContext factory method to create a new instance of
     * mContext fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BLETestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BLETestFragment newInstance(String param1, String param2) {
        BLETestFragment fragment = new BLETestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public BLETestFragment() {
        // Required empty public constructor
    }

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect, btnSend;
    private EditText edtMessage;
    private Spinner spCmd;
    private ArrayAdapter<String> adpater;// = new ArrayAdapter<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private Context mContext;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for mContext fragment
        mContext = getActivity();
        view = inflater.inflate(R.layout.fragment_bletest, container, false);
        initUI(view);
        return view;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    EditText editText;

    private boolean isBCD = false;

    private void initUI(View view) {
//        setContentView(R.layout.main);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(mContext, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            getActivity().finish();
            return;
        }
        editText = (EditText) view.findViewById(R.id.sendText);
        messageListView = (ListView) view.findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(mContext, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect = (Button) view.findViewById(R.id.btn_select);
        btnSend = (Button) view.findViewById(R.id.sendButton);
        edtMessage = (EditText) view.findViewById(R.id.sendText);
        spCmd = (Spinner) view.findViewById(R.id.sp_cmd);
        adpater = new ArrayAdapter<String>(getActivity(), android.R.layout
                .simple_dropdown_item_1line,
                getResources().getStringArray(R.array.choose_cmd));
        spCmd.setAdapter(adpater);
        edtMessage.setText("ATTESTR");
        service_init();
        spCmd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    // <item>AT+CLOCK</item>
//                    <item>AT+INFO</item>
//                    <item>AT+READ</item>
//                    <item>AT+POWER</item>
//                    <item>AT+FLY</item>
//                    <item>AT+SLEEP</item>
                    case 0:
                        isBCD = true;
                        edtMessage.setText("AT+CLOCK" + DataConversionUtils.getDefautCurrentTime());
                        break;
                    case 1:
                        isBCD = false;
                        edtMessage.setText("AT+INFO" + "");//加包裹号，绑定标签
                        break;
                    case 2:
                        isBCD = false;
                        edtMessage.setText("AT+READ");
                        break;
                    case 3:
                        isBCD = false;
                        edtMessage.setText("AT+POWER");
                        break;
                    case 4:
                        isBCD = false;
                        edtMessage.setText("AT+FLY");
                        break;
                    case 5:
                        isBCD = false;
                        edtMessage.setText("AT+SLEEP");
                        break;
                    case 6:
                        isBCD = false;
                        edtMessage.setText("ATTESTT");
                        break;
                    case 7:
                        isBCD = false;
                        edtMessage.setText("AT+START");
                        break;
                    case 8:
                        isBCD = false;
                        edtMessage.setText("AT+TIME");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Handler Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    if (btnConnectDisconnect.getText().equals("Connect")) {

                        // Connect button pressed, open DeviceListActivity
                        // class, with popup windows that scan for devices

                        Intent newIntent = new Intent(mContext,
                                DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        // Disconnect button pressed
                        if (mDevice != null) {
                            mService.disconnect();

                        }
                    }
                }
            }
        });

        // Handler Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] value = null;
                String message = editText.getText().toString();
                if (isBCD) {
                    byte[] temp = "AT+CLOCK".getBytes();
                    String time = DataConversionUtils.getCurrentTime("yyMMddHHmmss");
                    byte[] clock = DataConversionUtils.hexStringToByteArray(time);
                    logger.d("clock.size=" + clock.length);
                    value = new byte[temp.length + clock.length + 1];
                    System.arraycopy(temp, 0, value, 0, temp.length);
                    System.arraycopy(clock, 0, value, temp.length, 3);
                    value[temp.length + 3] = 0x00;//sun
                    System.arraycopy(clock, 3, value, temp.length + 3, 3);
                } else {
                    try {
                        // send data to service
                        value = message.getBytes("UTF-8");
//					edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                mService.writeRXCharacteristic(value);
                // Update the log with time stamp
                String currentDateTimeString = DateFormat.getTimeInstance()
                        .format(new Date());
                listAdapter.add("[" + currentDateTimeString + "] TX: "
                        + message);
                messageListView.smoothScrollToPosition(listAdapter
                        .getCount() - 1);

            }
        });
    }

    // UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                showMessage("Unable to initialize Bluetooth");
                getActivity().finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            // // mService.disconnect(mDevice);
            mService = null;
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String currentDateTimeString = DateFormat
                    .getTimeInstance().format(new Date());
            switch (msg.what) {

                case STATE_CONNECT:
                    Log.d(TAG, "UART_CONNECT_MSG");
                    btnConnectDisconnect.setText("Disconnect");
                    edtMessage.setEnabled(true);
                    btnSend.setEnabled(true);
                    ((TextView) view.findViewById(R.id.deviceName))
                            .setText(mDevice.getName() + " - ready");
                    listAdapter.add("[" + currentDateTimeString
                            + "] Connected to: " + mDevice.getName());
                    messageListView.smoothScrollToPosition(listAdapter
                            .getCount() - 1);
                    mState = UART_PROFILE_CONNECTED;
                    break;
                case STATE_DISCONNECT:

                    Log.d(TAG, "UART_DISCONNECT_MSG");
                    btnConnectDisconnect.setText("Connect");
                    edtMessage.setEnabled(false);
                    btnSend.setEnabled(false);
                    ((TextView) view.findViewById(R.id.deviceName))
                            .setText("Not Connected");
                    listAdapter.add("[" + currentDateTimeString
                            + "] Disconnected to: " + mDevice.getName());
                    mState = UART_PROFILE_DISCONNECTED;
                    mService.close();
                    break;
                case STATE_DATA_AVAILABLE:
                    try {
                        byte[] txValue = (byte[]) msg.obj;
                        String text = new String(txValue, "UTF-8");

                        listAdapter.add("[" + currentDateTimeString
                                + "] RX: " + text);
                        messageListView.smoothScrollToPosition(listAdapter
                                .getCount() - 1);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private final int STATE_CONNECT = 0;
    private final int STATE_DISCONNECT = 1;
    private final int STATE_DATA_AVAILABLE = 2;
    private int recePackageCount = 0;
    private boolean isCounter = false;
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            // *********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                Message msg = new Message();
                msg.what = STATE_CONNECT;
                handler.sendMessage(msg);
            }

            // *********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                Message msg = new Message();
                msg.what = STATE_DISCONNECT;
                handler.sendMessage(msg);
            }

            // *********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            // *********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent
                        .getByteArrayExtra(UartService.EXTRA_DATA);
                Message msg = new Message();
                msg.what = STATE_DATA_AVAILABLE;
                msg.obj = txValue;
                handler.sendMessage(msg);
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                       
//                    }
//                });
            }
            // *********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(mContext, UartService.class);
        mContext.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(
                UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                // When the DeviceListActivity return, with the selected device
                // address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data
                            .getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(
                            deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice
                            + "mserviceValue" + mService);
                    ((TextView) view.findViewById(R.id.deviceName)).setText(mDevice
                            .getName() + " - connecting");
                    mService.connect(deviceAddress);

                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(mContext, "Bluetooth has turned on ",
                            Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(mContext, "Problem in BT Turning ON ",
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private void showMessage(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
