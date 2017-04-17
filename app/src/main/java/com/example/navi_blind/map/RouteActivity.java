package com.example.navi_blind.map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.example.navi_blind.R;
import com.example.navi_blind.utils.AMapUtil;
import com.example.navi_blind.utils.ToastUtil;

/**
 * Route路径规划: 公交规划、步行规划
 */
public class RouteActivity extends Activity implements OnMapClickListener,
        OnMarkerClickListener, OnInfoWindowClickListener, InfoWindowAdapter, OnRouteSearchListener {
    private AMap aMap;
    private MapView mapView;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private BusRouteResult mBusRouteResult;
    private WalkRouteResult mWalkRouteResult;
    private LatLng mStartLatlng;//起点
    private LatLonPoint mStartPoint;
    private LatLng mEndLatlng;//终点
    private LatLonPoint mEndPoint;
    private String mCurrentCityName = "北京";
    private final int ROUTE_TYPE_WALK = 1;
    private final int ROUTE_TYPE_BUS = 2;

    private LinearLayout mBusResultLayout;
    private RelativeLayout mBottomLayout;
    private TextView mRotueTimeDes, mRouteDetailDes;
    private ImageView mBus;
    private ImageView mWalk;
    private ListView mBusResultList;
    private ProgressDialog progDialog = null;// 搜索时进度条
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_route);

        mContext = this.getApplicationContext();
        mapView = (MapView) findViewById(R.id.route_map);
        mapView.onCreate(bundle);// 此方法必须重写
        init();
		getIntentData();
        setfromandtoMarker();
        setDefaultRouteSearch();
    }
    //获取mainActivity传递过来的起始终点位置
    private void getIntentData(){
        Intent intent=getIntent();
        mStartLatlng=intent.getParcelableExtra("locationLatLng");
        mStartPoint=AMapUtil.convertToLatLonPoint(mStartLatlng);

        mEndPoint=intent.getParcelableExtra("markerLatLngPoint");
        mEndLatlng=AMapUtil.convertToLatLng(mEndPoint);
    }

    //设置起点和终点图标
    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(mStartLatlng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions()
                .position(mEndLatlng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        registerListener();
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mBusResultLayout = (LinearLayout) findViewById(R.id.bus_result);
        mRotueTimeDes = (TextView) findViewById(R.id.firstline);
        mRouteDetailDes = (TextView) findViewById(R.id.secondline);
        mBus = (ImageView)findViewById(R.id.route_bus);
        mWalk = (ImageView)findViewById(R.id.route_walk);
        mBusResultList = (ListView) findViewById(R.id.bus_result_list);
    }

    /**
     * 注册监听
     */
    private void registerListener() {
        aMap.setOnMapClickListener(RouteActivity.this);
        aMap.setOnMarkerClickListener(RouteActivity.this);
        aMap.setOnInfoWindowClickListener(RouteActivity.this);
        aMap.setInfoWindowAdapter(RouteActivity.this);
    }

    @Override
    public View getInfoContents(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onMarkerClick(Marker arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub

    }

    /**
     * 公交路线搜索
     */
    public void onBusClick(View view) {
        searchRouteResult(ROUTE_TYPE_BUS, RouteSearch.BusDefault);
        mBus.setImageResource(R.drawable.route_bus_select);
        mWalk.setImageResource(R.drawable.route_walk_normal);
        mapView.setVisibility(View.GONE);
        mBusResultLayout.setVisibility(View.VISIBLE);
    }


    /**
     * 步行路线搜索
     */
    public void onWalkClick(View view) {
        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_select);
        mapView.setVisibility(View.VISIBLE);
        mBusResultLayout.setVisibility(View.GONE);
    }

    //设置默认的路径规划方案
    private void setDefaultRouteSearch(){
        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
        mBus.setImageResource(R.drawable.route_bus_normal);
        mWalk.setImageResource(R.drawable.route_walk_select);
        mapView.setVisibility(View.VISIBLE);
        mBusResultLayout.setVisibility(View.GONE);
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            ToastUtil.show(mContext, "起点未设置");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint,mEndPoint);
        if (routeType == ROUTE_TYPE_BUS) {// 公交路径规划
            BusRouteQuery query = new BusRouteQuery(fromAndTo, mode,
                    mCurrentCityName, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        } else if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            WalkRouteQuery query = new WalkRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }

    /**
     * 公交路线搜索结果方法回调
     */
    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        dissmissProgressDialog();
        mBottomLayout.setVisibility(View.GONE);
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mBusRouteResult = result;
                    BusResultListAdapter mBusResultListAdapter = new BusResultListAdapter(mContext, mBusRouteResult);
                    mBusResultList.setAdapter(mBusResultListAdapter);
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }
            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }


    /**
     * 步行路线搜索结果方法回调
     */
    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
                    mBottomLayout.setVisibility(View.VISIBLE);
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur)+"("+AMapUtil.getFriendlyLength(dis)+")";
                    mRotueTimeDes.setText(des);
                    mRouteDetailDes.setVisibility(View.GONE);
                    mBottomLayout.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext,
                                    WalkRouteDetailActivity.class);
                            intent.putExtra("walk_path", walkPath);
                            intent.putExtra("walk_result",
                                    mWalkRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }

            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }


    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
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
    }

    /**
     * 骑行路线搜索结果方法回调
     */
    @Override
    public void onRideRouteSearched(RideRouteResult arg0, int arg1) {
        // TODO Auto-generated method stub

    }

}

