package com.example.twy.gaodetest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by twy on 2018/4/9.
 */

public class LocationSourceActivity extends AppCompatActivity implements LocationSource, AMapLocationListener {
    private AMap aMap;
    private MapView mapView;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private RadioGroup mGPSModeGroup;

    private TextView mLocationErrText;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private GeocodeSearch geocodeSearch;
    private String simpleAddress;
    private TextView tvAddress;
    private TextView tvSnippet;
    private View tvSure;
    private double latitude;
    private double longitude;
    private View ivLocation;
    /*private LatLng latLng;
    private LatLng latLng1;
    private LatLng latLng2;
    private LatLng latLng3;
    private LatLng latLng4;*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locationsource_activity);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1001 && requestCode == 1001 && data!=null){
            latitude = data.getDoubleExtra("latitude", 0);
            longitude = data.getDoubleExtra("longitude", 0);
            getAddressByLatlng(new LatLng(latitude,longitude));
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),25));
        }
    }

    private PoiIAddressItem pai = new PoiIAddressItem();

    /**
     * 初始化AMap对象
     */
    private void init() {
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
        mLocationErrText = (TextView)findViewById(R.id.location_errInfo_text);
        mLocationErrText.setVisibility(View.GONE);
        tvAddress = findViewById(R.id.tv);
        tvSnippet = findViewById(R.id.tv_snippet);
        tvSure = findViewById(R.id.tv_sure);
        ivLocation = findViewById(R.id.iv_location);

        //地理搜索类
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
                String formatAddress = regeocodeAddress.getFormatAddress();
                simpleAddress = formatAddress.substring(9);
                //Toast.makeText(LocationSourceActivity.this,"查询经纬度对应详细地址：\n" + simpleAddress,Toast.LENGTH_LONG).show();
                pai.province = regeocodeAddress.getProvince();
                pai.city = regeocodeAddress.getCity();
                pai.area = regeocodeAddress.getDistrict();
                //pai.address = regeocodeAddress.getTownship();
                pai.address = regeocodeAddress.getStreetNumber().getStreet();
                pai.adCode = regeocodeAddress.getAdCode();
                if(regeocodeAddress!=null && regeocodeAddress.getPois().size()>0){
                    Collections.sort(regeocodeAddress.getPois(), new SortByDistance());
                    String title = regeocodeAddress.getPois().get(0).getTitle();
                    String snippet = regeocodeAddress.getPois().get(0).getSnippet();
                    pai.latitude = regeocodeAddress.getPois().get(0).getLatLonPoint().getLatitude();
                    pai.longitude = regeocodeAddress.getPois().get(0).getLatLonPoint().getLongitude();
                    pai.title = title;
                    pai.snippet = snippet;
                    tvAddress.setText(title);
                    tvSnippet.setText(snippet);
                    //regeocodeAddress.getPois().get(0).get();
                }

                //tvAddress.setText(simpleAddress);
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });

        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("poilAddressItem",pai);
                setResult(1001,intent);
                LocationSourceActivity.this.finish();
            }
        });

        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),25));
            }
        });

        ivLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mlocationClient!=null) {
                    mlocationClient.startLocation();
                } else if(aMapLocation!=null){
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude()),25));
                }
            }
        });

        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(pai!=null){
                    Intent intent = new Intent(LocationSourceActivity.this,PoiListActivity.class);
                    intent.putExtra("latitude",pai.latitude);
                    intent.putExtra("longitude",pai.longitude);
                    startActivityForResult(intent,1001);
                }

            }
        });

    }

    private CountDownTimer countDownTimer;

    private void location(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                    "需要您提供定位权限",
                    101);
        } else {
            aMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                aMap.setMyLocationEnabled(true);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestPermission(final String permission, String rationale, final int requestCode){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(LocationSourceActivity.this, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.permission_dialog_cancel, null)
                    .create().show();
        } else if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(LocationSourceActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.permission_dialog_cancel, null)
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{permission,Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        location();
        //aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false


        setupLocationStyle();

        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                LatLng target = cameraPosition.target;
                System.out.println(target.latitude + "++++++++++++" + target.longitude);
                if(countDownTimer==null) {
                    countDownTimer = new CountDownTimer(3000, 200) {
                        @Override
                        public void onTick(long l) {
                            i++;
                            if(i==0){
                                tvAddress.setText("正在获取地理位置");
                            }else if(i==1){
                                tvAddress.setText("正在获取地理位置.");
                            }else if(i<=2){
                                tvAddress.setText("正在获取地理位置..");
                            }else{
                                tvAddress.setText("正在获取地理位置...");
                                i=-1;
                            }
                        }

                        @Override
                        public void onFinish() {
                            countDownTimer.start();
                        }
                    };
                    flag = true;
                    countDownTimer.start();
                }else if(!flag){
                    flag = true;
                    countDownTimer.start();
                }
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                if(countDownTimer!=null)
                    countDownTimer.cancel();
                flag = false;
                LatLng target = cameraPosition.target;
                System.out.println(target.latitude + "jinjin------" + target.longitude);
                getAddressByLatlng(target);
            }
        });
    }
    private int i = -1;

    private boolean flag = false;

    private void getAddressByLatlng(LatLng latLng) {
        //逆地理编码查询条件：逆地理编码查询的地理坐标点、查询范围、坐标类型。
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 500f, GeocodeSearch.AMAP);
        //异步查询
        geocodeSearch.getFromLocationAsyn(query);
    }

    private void setupLocationStyle(){
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.drawable.gps_point));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(STROKE_COLOR);
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(3);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }
    }

    private AMapLocation aMapLocation;
    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                this.aMapLocation = amapLocation;
                mLocationErrText.setVisibility(View.GONE);
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                //aMap.moveCamera(CameraUpdateFactory.zoomTo(25));
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(amapLocation.getLatitude(),amapLocation.getLongitude()),25));

                /*latLng = new LatLng(amapLocation.getLatitude(),amapLocation.getLongitude());

                aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                        .position(latLng).title("ss")
                        .snippet("sssss").draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point)));

                Point point = toScreenLocation(amapLocation.getLatitude(), amapLocation.getLongitude());

                //左上角
                int lefttopx = point.x-px2dip(5);
                int lefttopy = point.y-px2dip(5);
                latLng1 = toGeoLocation(lefttopx, lefttopy);
                Log.i("twy","左上角"+ latLng1.latitude+"***"+ latLng1.longitude);
                aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                        .position(latLng1).title("左上角")
                        .snippet("左上角").draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.green2d)));

                //右上角
                int righttopx = point.x+px2dip(5);
                int righttopy = point.y-px2dip(5);
                latLng2 = toGeoLocation(righttopx, righttopy);
                Log.i("twy","右上角"+ latLng2.latitude+"***"+ latLng2.longitude);
                aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                        .position(latLng2).title("右上角")
                        .snippet("右上角").draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.green2d)));

                //左下角
                int leftbottompx = point.x-px2dip(5);
                int leftbottomy = point.y+px2dip(15);
                latLng3 = toGeoLocation(leftbottompx, leftbottomy);
                Log.i("twy","左下角"+ latLng3.latitude+"***"+ latLng3.longitude);
                aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                        .position(latLng3).title("左下角")
                        .snippet("左下角").draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.green2d)));


                //右下角
                int rightbottompx = point.x+px2dip(5);
                int rightbottomy = point.y+px2dip(15);
                latLng4 = toGeoLocation(rightbottompx, rightbottomy);
                Log.i("twy","右下角"+ latLng4.latitude+"***"+ latLng4.longitude);
                aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                        .position(latLng4).title("右下角")
                        .snippet("右下角").draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.green2d)));

                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(latLng).include(latLng1).include(latLng2).include(latLng3).include(latLng4).build();
                aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));*/

                mlocationClient.stopLocation();
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
                mLocationErrText.setVisibility(View.VISIBLE);
                mLocationErrText.setText(errText);
            }
        }
    }

    private boolean isLocation = false;

    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            if (latitude == 0)
                mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    private LatLng toGeoLocation(int x,int y) {
        Point mPoint = new Point(x, y);
        LatLng mLatlng = aMap.getProjection().fromScreenLocation(mPoint);
        return mLatlng;
    }
    private Point toScreenLocation(double lat,double lng) {
        LatLng mLatlng = new LatLng(lat, lng);
        Point mPoint = aMap.getProjection().toScreenLocation(mLatlng);
        return mPoint;
    }

    public int dip2px(float dpValue) {
        float scale = this.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public int px2dip(float pxValue) {
        float scale = this.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
