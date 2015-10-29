package com.speedata.nrfUARTv2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.speedata.nrfUARTv2.R;
import com.speedata.nrfUARTv2.utils.MyLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * TODO: document your custom view class.
 */
public class AdapterListView433 extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<HashMap<String, String>> listdata;
    private MyLogger logger = MyLogger.jLog();

    //    public ListViewAdapter(List<CampaignRecordEntity> bean, Context mContext) {
//        mInflater = (LayoutInflater) mContext
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        this.mContext = mContext;
//        this.bean = bean;
//    }
    public AdapterListView433(ArrayList<HashMap<String, String>> listdata, Context mContext) {
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = mContext;
        this.listdata = listdata;
    }

    public void refresh(ArrayList<HashMap<String, String>> listdata) {
        this.listdata = listdata;
        notifyDataSetChanged();
    }

    public ArrayList<HashMap<String, String>> getAlldata() {
        return listdata;
    }

    @Override
    public int getCount() {
        return listdata.size();
    }

    @Override
    public Object getItem(int position) {
        return listdata.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getId(int position) {
        return "";
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.adapter_listview,
                    null);
            holder.tvHumidity = (TextView) convertView.findViewById(R.id.tv_humidity);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tvPckNum = (TextView) convertView.findViewById(R.id.tv_pck_num);
            holder.tvTemp = (TextView) convertView.findViewById(R.id.tv_temp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String humidity = listdata.get(position).get("humidity") + "";
        holder.tvHumidity.setText(humidity);
        holder.tvTime.setText(listdata.get(position).get("time"));
        String temp = listdata.get(position).get("temp");
        holder.tvTemp.setText(temp);
        String pck_num = listdata.get(position).get("pck_num");
        holder.tvPckNum.setText(pck_num);
        logger.d("===humidity" + humidity);
        logger.d("===temp=" + temp);
        logger.d("===pck_num" + pck_num);
        return convertView;
    }

    static final class ViewHolder {
        private TextView tvHumidity, tvTime, tvTemp, tvPckNum;
//        private CustomDigitalClock clock;
    }
}