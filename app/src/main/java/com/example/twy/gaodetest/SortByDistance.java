package com.example.twy.gaodetest;

import com.amap.api.services.core.PoiItem;

import java.util.Comparator;

/**
 * Created by twy on 2018/4/10.
 */

public class SortByDistance implements Comparator<PoiItem> {
    public int compare(PoiItem o1, PoiItem o2) {
        if(o1.getDistance()>(o2.getDistance())){
            return 1;
        }else{
            return -1;
        }
    }
}
