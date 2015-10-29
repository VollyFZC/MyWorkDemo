package com.speedata.nrfUARTv2.fragment;

import android.CRC.CRC;
import android.app.Activity;
import android.content.Context;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.serialport.SerialPort;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.speedata.nrfUARTv2.R;
import com.speedata.nrfUARTv2.adapter.AdapterListView433;
import com.speedata.nrfUARTv2.myinterface.OnFragmentInteractionListener;
import com.speedata.nrfUARTv2.utils.DataConversionUtils;
import com.speedata.nrfUARTv2.utils.DeviceControl;
import com.speedata.nrfUARTv2.utils.MyLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link My433TestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class My433TestFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment My433TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static My433TestFragment newInstance(String param1, String param2) {
        My433TestFragment fragment = new My433TestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public My433TestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private ListView listview;
    private AdapterListView433 adapter;
    private ArrayList<HashMap<String, String>> listdata = new ArrayList<HashMap<String, String>>();
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my433_test, container, false);
        mContext = getActivity();
        initUI(view);
        initdevice();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        closeDevice();
    }

    private void closeDevice() {
        if (mDeviceControl != null) {
            mDeviceControl.MTGpioOff();
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (soundPool != null)
            soundPool.release();
        logger.d("close--device");
    }

    private Button btnClear;

    private void initUI(View view) {
        btnClear = (Button) view.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(this);
        listview = (ListView) view.findViewById(R.id.list_433);
        adapter = new AdapterListView433(listdata, mContext);
        listview.setAdapter(adapter);
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


    private DeviceControl mDeviceControl;
    private SerialPort mSerialPort;
    private int fd;
    private MyLogger logger = MyLogger.jLog();
    //    private ReadThread mreadThread;
    private SoundPool soundPool;
    private int soundId;
    private Timer timer;

    private void initdevice() {
        mSerialPort = new SerialPort();
        try {
            mSerialPort.OpenSerial("/dev/ttyMT2", 9600);
            fd = mSerialPort.getFd();
            logger.d("--onCreate--open-serial=" + fd);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            Toast.makeText(mContext, "无串口权限,强制退出！", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(mContext, "未找到串口,强制退出！", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            System.exit(0);
        }

        try {
            mDeviceControl = new DeviceControl("sys/class/misc/mtgpio/pin", mContext);
        } catch (IOException e) {
            e.printStackTrace();
            mDeviceControl = null;
            return;
        }
        mDeviceControl.MTGpioOn();
        timer = new Timer();
        timer.schedule(new ReadThread(), 1000, 10 * 1000);
    }

    @Override
    public void onClick(View v) {
        if (v == btnClear) {
            listdata.clear();
            adapter.notifyDataSetChanged();
        }
    }

    private byte[] getTestData() {
        byte[] result = new byte[10];
        result[0] = 0x0a;
        result[1] = 0x09;
        result[2] = 0x31;
        result[3] = 0x32;
        result[4] = 0x32;
        result[5] = 0x32;
        result[6] = 0x32;
        result[6] = 0x32;
        result[7] = 0x32;
        result[8] = 0x32;
        result[9] = CRC.crc(result);
        return result;
    }

    private class ReadThread extends TimerTask {
        @Override
        public void run() {
            try {
//                parseData(listdata, getTestData()); //new byte[]{0x0a, 0x08, 0x02, 0x01, 0x02,
                // 0x01, 0x00, 0x00,
//                        0x0e});
                byte[] temp1 = mSerialPort.ReadSerial(fd, 1024);
//                    logger.d("----read--run");
                logger.d("----read");
                if (temp1 != null) {
                    logger.d("----read--ok---" + DataConversionUtils.byteArrayToStringLog(temp1,
                            temp1.length));
                    Message msg = new Message();
                    msg.what = SERIAL_DATA_AVAILIABLE;
                    msg.obj = temp1;
                    handler.sendMessage(msg);
                    SystemClock.sleep(1000);
                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private final int SERIAL_DATA_AVAILIABLE = 1;
    private final int REFRASH_LIST = 2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SERIAL_DATA_AVAILIABLE:
                    logger.d("====SERIAL_DATA_AVAILIABLE====");
                    byte[] data = (byte[]) msg.obj;
                    parseData(listdata,data);
                    break;
                case REFRASH_LIST:
                    adapter.notifyDataSetChanged();
            }
        }
    };


    private void parseData(ArrayList<HashMap<String, String>> listdataContainer, byte[]
            originalData) {
        List<byte[]> listbyte = new ArrayList<byte[]>();
        for (int j = 0; j < originalData.length; j++) {
            if (originalData[j] == 0x0a) {
                int len = originalData[j + 1];
                byte[] temp = new byte[len];
                byte crc = 0;
                for (int i = 0; i < len; i++) {
                    temp[i] = originalData[i + j + 1];
                    if (i < len - 1)
                        crc += originalData[i + j + 1];
                }
                if (crc != originalData[j + len]) {
                    logger.d("===crc is wrong!===   crc=" + crc + "  rece crc=" + originalData[j
                            + len]);
                } else {
                    logger.d("====crc is ok====");
                    listbyte.add(temp);
                }
                j += len;
                for (byte[] bytes : listbyte) {
                    HashMap<String, String> map = new HashMap<>();
                    // holder.tvHumidity.setText(listdata.get(position).get("humidity") + "");
                    map.put("time", DataConversionUtils.getCurrentTime("HH:mm:ss"));
                    String pick_num = "";
                    byte[] temp_pck_num = new byte[len - 5];
                    for (int m = 0; m < len - 5; m++) {
                        temp_pck_num[m] = bytes[m + 5];
                    }
                    map.put("pck_num", new String(temp_pck_num));
                    double temp_temp = Double.parseDouble(new String(new byte[]{bytes[1],
                            bytes[2]})) / 100;
                    map.put("temp", temp_temp + "");
                    map.put("humidity", new String(new byte[]{bytes[3], bytes[4]}));
                    listdataContainer.add(map);
                    Message msg = new Message();
                    msg.what = REFRASH_LIST;
                    handler.sendMessage(msg);
//                    adapter.notifyDataSetChanged();
                    logger.d("====adapter.notifyDataSetChanged()===");
                }
            }
        }

    }
}
