package com.example.navi_blind;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;

import android.os.Bundle;
import android.util.Log;

import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.navi_blind.bluetooth.BluetoothActivity;
import com.example.navi_blind.map.RouteActivity;
import com.example.navi_blind.voice.VoiceRecord;

public class MainActivity extends Activity implements LocationSource, AMapLocationListener, View.OnClickListener, AMap.OnPOIClickListener, AMap.OnMarkerClickListener, PoiSearch.OnPoiSearchListener, AMap.OnMapClickListener {

    //map
    private MapView mMapView;
    private AMap aMap;
    private UiSettings mUiSettings;
    //定位需要的声明
    private AMapLocationClient mLocationClient;//定位发起端
    private AMapLocationClientOption mLocationOption;//定位参数
    private LocationSource.OnLocationChangedListener mListener;//定位监听器
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    private TextView searchEditText;
    private LinearLayout bluetoothTab;
    private LinearLayout nearbyTab;
    private LinearLayout routeTab;
    private LinearLayout myTab;
    private LinearLayout mainBottomLayout;
    private LatLng locationLatLng;
    private Marker currentMarker;
    private String TAG ="POI";
    private PoiSearch poiSearch;
    //POI底部详细介绍
    private RelativeLayout mPoiDetailLayout,mSearchBarLayout;
    private TextView mPoiName, mPoiAddress,mTextSearchButton;
    private Button mPoiButton;
    private ImageView voiceButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_main);
        mMapView = (MapView)findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);//此方法必须重写
        init();
        setupPoiDetailLayout();
    }

    /**
     * 初始化AMap对象及其他
     */
    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            setUpMap();
        }
        //mTextSearchButton=(TextView)findViewById(R.id.btn_search);
        voiceButton=(ImageView)findViewById(R.id.search_voice_btn);
        mSearchBarLayout=(RelativeLayout)findViewById(R.id.search_bar_layout);
        mainBottomLayout=(LinearLayout)findViewById(R.id.main_bottom_layout);
        bluetoothTab = (LinearLayout)findViewById(R.id.id_tab_bluetooth);
        nearbyTab =(LinearLayout)findViewById(R.id.id_tab_nearby);
        routeTab =(LinearLayout)findViewById(R.id.id_tab_route);
        myTab = (LinearLayout)findViewById(R.id.id_tab_my);
        searchEditText = (TextView)findViewById(R.id.input_edittext);
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //当EditText失去焦点时，隐藏软键盘,隐藏搜索按钮
                if (!hasFocus) {
                    closeKeyBoard();
//                    mTextSearchButton.setVisibility(View.INVISIBLE);
//                    Log.d(TAG,"失去焦点");
//                    voiceButton.setVisibility(View.VISIBLE);
                }
//                else{
//                    showKeyBoard();
//                    voiceButton.setVisibility(View.INVISIBLE);
//                    Log.d(TAG,"获得焦点");
//                    mTextSearchButton.setVisibility(View.VISIBLE);
//                }
            }
        });
        searchEditText.setOnClickListener(this);
        //mTextSearchButton.setOnClickListener(this);
        voiceButton.setOnClickListener(this);
        bluetoothTab.setOnClickListener(this);
        nearbyTab.setOnClickListener(this);
        routeTab.setOnClickListener(this);
        myTab.setOnClickListener(this);
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        //aMap.setTrafficEnabled(true);//显示实时交通状况
        //地图模式可选类型：MAP_TYPE_NORMAL,MAP_TYPE_SATELLITE,MAP_TYPE_NIGHT
        //aMap.setMapType(AMap.MAP_TYPE_NORMAL);//卫星地图模式

        mUiSettings = aMap.getUiSettings();
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setScaleControlsEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        // 自定义定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        aMap.setOnPOIClickListener(MainActivity.this);
        aMap.setOnMarkerClickListener(MainActivity.this);
        aMap.setOnMapClickListener(MainActivity.this);
    }

    private void setupPoiDetailLayout() {
        mPoiDetailLayout = (RelativeLayout) findViewById(R.id.poi_detail);
        mPoiDetailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//				Intent intent = new Intent(PoiSearchActivity.this,
//						SearchDetailActivity.class);
//				intent.putExtra("poiitem", mPoi);
//				startActivity(intent);

            }
        });
        mPoiName = (TextView) findViewById(R.id.poi_name);
        mPoiAddress = (TextView) findViewById(R.id.poi_address);
        mPoiButton = (Button) findViewById(R.id.poi_route_button);

        poiSearch =new PoiSearch(this,null);//通过ID检索POI
        poiSearch.setOnPoiSearchListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
//            case R.id.btn_search:
//                Toast.makeText(MainActivity.this,"点击了搜索按钮",Toast.LENGTH_SHORT).show();
//                break;
            case R.id.search_voice_btn:
                //Intent intent0 = new Intent(MainActivity.this, VoiceRecord.class);
               // startActivity(intent0);
                Toast.makeText(MainActivity.this,"点击了录音按钮",Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_tab_bluetooth:
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
                break;
            case R.id.id_tab_nearby:
                Toast.makeText(MainActivity.this,"点击了附近按钮",Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_tab_route:
                Toast.makeText(MainActivity.this,"点击了路线按钮",Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_tab_my:
                Toast.makeText(MainActivity.this,"点击了我的按钮",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     *  底图poi点击回调
     */
    @Override
    public void onPOIClick(Poi poi) {
        Log.d(TAG,"onPOIClick");
        if(aMap!=null&currentMarker!=null){
            currentMarker.remove();
        }
        poiSearch.searchPOIIdAsyn(poi.getPoiId());//异步查询
        MarkerOptions markerOptiopns = new MarkerOptions();
        markerOptiopns.position(poi.getCoordinate());//封装位置信息
        markerOptiopns.title(poi.getName()).snippet(poi.getPoiId());//封装标题和备注
        TextView textView = new TextView(getApplicationContext());
        textView.setText(poi.getName());
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundResource(R.drawable.custom_info_bubble);
        markerOptiopns.icon(BitmapDescriptorFactory.fromView(textView));//标记图标
        currentMarker=aMap.addMarker(markerOptiopns);//添加标记

    }


    /**
     * Marker 点击回调
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.d(TAG,"onMarkerClick");
        if (aMap != null) {
            currentMarker = marker;
            currentMarker.remove();
        }
        whetherToShowDetailInfo(false);
        return true;
    }

    //是否在底部显示图标的细节
    private void whetherToShowDetailInfo(boolean isToShow) {
        if (isToShow) {
            mPoiDetailLayout.setVisibility(View.VISIBLE);
        } else {
            mPoiDetailLayout.setVisibility(View.GONE);
        }
    }

    private void setPoiItemDisplayContent(final PoiItem mCurrentPoi) {
        mPoiName.setText(mCurrentPoi.getTitle());
        mPoiAddress.setText(mCurrentPoi.getSnippet()+mCurrentPoi.getDistance());
        Log.d(TAG,"setPoiItemDisplayContent");
        mPoiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RouteActivity.class);
                intent.putExtra("markerLatLngPoint",mCurrentPoi.getLatLonPoint());
                intent.putExtra("locationLatLng", locationLatLng);
                startActivity(intent);
            }});
    }

    /**
     * 弹出软键盘
     */
    private void showKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }
    /**
     * 收起软键盘
     */
    public void closeKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();
            Log.d("homeFragment","激活定位");
        }
    }
    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }
    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null&&amapLocation != null) {
            if (amapLocation != null
                    &&amapLocation.getErrorCode() == 0) {
                locationLatLng = new LatLng(amapLocation.getLatitude(),amapLocation.getLongitude());
                //mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                if (isFirstLoc) {
                    //点击定位按钮 能够将地图的中心移动到定位点,并显示系统小蓝点
                    mListener.onLocationChanged(amapLocation);
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //将地图移动到定位点
                    //aMap.moveCamera(CameraUpdateFactory.changeLatLng(locationLatLng));
                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() + "" + amapLocation.getProvince() + "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
                    Toast.makeText(MainActivity.this, buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                }

            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("homeFragment",errText);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        Log.d("homeFragment","amap onResume");
    }
    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
        Log.d("homeFragment","amap onPause");
        //deactivate();
    }
    /**
     * 方法必须重写
     * map的生命周期方法
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if(null != mLocationClient){
            mLocationClient.onDestroy();
        }
        Log.d("homeFragment","amap onDestroy");
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
        setPoiItemDisplayContent(poiItem);
        whetherToShowDetailInfo(true);//显示底部细节栏
        Log.d(TAG,"setPoiItemDisplayContent");
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(aMap!=null&&currentMarker!=null){
            currentMarker.remove();
            currentMarker=null;
            mPoiDetailLayout.setVisibility(View.INVISIBLE);
            Log.d(TAG,"mPoiDetailLayout.INVISIBLE");
        }else if(mSearchBarLayout.getVisibility()==View.VISIBLE&&mainBottomLayout.getVisibility()==View.VISIBLE){
            mSearchBarLayout.setVisibility(View.INVISIBLE);
            mainBottomLayout.setVisibility(View.INVISIBLE);
            Log.d(TAG," mSearchBarLayout(View.INVISIBLE);");
        }else if(mSearchBarLayout.getVisibility()==View.INVISIBLE&&mainBottomLayout.getVisibility()==View.INVISIBLE){
            mSearchBarLayout.setVisibility(View.VISIBLE);
            mainBottomLayout.setVisibility(View.VISIBLE);
            Log.d(TAG," mSearchBarLayout(VISIBLE);");
        }
    }
}
