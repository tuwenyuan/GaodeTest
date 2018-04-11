package com.example.twy.gaodetest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by twy on 2018/4/10.
 */

public class PoiListActivity extends AppCompatActivity {

    private GeocodeSearch geocodeSearch;
    List<PoiIAddressItem> list = new ArrayList<>();
    private ListView lv;
    private MyAdapter adapter;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_list);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        //地理搜索类
        geocodeSearch = new GeocodeSearch(this);
        lv = findViewById(R.id.lv);

    }

    private void initData() {
        adapter = new MyAdapter();
        lv.setAdapter(adapter);
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        LatLng latLng = new LatLng(latitude,longitude);
        getAddressByLatlng(latLng);
    }

    private void getAddressByLatlng(LatLng latLng) {
        //逆地理编码查询条件：逆地理编码查询的地理坐标点、查询范围、坐标类型。
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 500f, GeocodeSearch.AMAP);
        //异步查询
        geocodeSearch.getFromLocationAsyn(query);
    }

    private void initListener() {
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
                //String formatAddress = regeocodeAddress.getFormatAddress();
                PoiIAddressItem item;
                Collections.sort(regeocodeAddress.getPois(), new SortByDistance());
                for(PoiItem pi : regeocodeAddress.getPois()){
                    item = new PoiIAddressItem();
                    item.province = regeocodeAddress.getProvince();
                    item.city = regeocodeAddress.getCity();
                    item.area = regeocodeAddress.getDistrict();
                    //pai.address = regeocodeAddress.getTownship();
                    item.address = regeocodeAddress.getStreetNumber().getStreet();
                    item.adCode = regeocodeAddress.getAdCode();
                    item.latitude = pi.getLatLonPoint().getLatitude();
                    item.longitude = pi.getLatLonPoint().getLongitude();
                    item.title = pi.getTitle();
                    item.snippet = pi.getSnippet();
                    item.distance = pi.getDistance();
                    list.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PoiIAddressItem item = list.get(i);
                Intent intent = new Intent();
                intent.putExtra("latitude",item.latitude);
                intent.putExtra("longitude",item.longitude);
                setResult(1001,intent);
                PoiListActivity.this.finish();
            }
        });
    }

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder vh;
            if(view!=null){
                vh = (ViewHolder) view.getTag();
            }else{
                view = View.inflate(PoiListActivity.this,R.layout.item_poi_layout,null);
                vh = new ViewHolder();
                vh.tvTitle = view.findViewById(R.id.tv_title);
                vh.tvAddress = view.findViewById(R.id.tv_address);
                vh.tvDistance = view.findViewById(R.id.tv_distance);
                view.setTag(vh);
            }
            PoiIAddressItem item = list.get(i);
            vh.tvTitle.setText(item.title);
            vh.tvAddress.setText(item.province+item.city+item.area+item.address+item.snippet+item.title);
            vh.tvDistance.setText(item.distance+"m");
            return view;
        }
    }

    private class ViewHolder{
        public TextView tvTitle;
        public TextView tvAddress;
        public TextView tvDistance;
    }

}
