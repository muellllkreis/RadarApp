package com.radarapp.mjr9r.radar.services;

import com.radarapp.mjr9r.radar.model.DropMessage;
import com.radarapp.mjr9r.radar.model.Filter;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Matias on 12.09.2018.
 */

public class DataService {
    private static final DataService instance = new DataService();

    public static DataService getInstance() {
        return instance;
    }

    private DataService() {
    }

    public ArrayList<DropMessage> getSampleMessages() {
        //pretending we are downloading

        ArrayList<DropMessage> list = new ArrayList<>();
        list.add(new DropMessage(52.530673f, 13.328216f, new Date(), "Das hier ist eine coole Tankstelle!", Filter.CUTE));
        list.add(new DropMessage(52.528086f, 13.324048f, new Date(), "Ei wie toll ein Gasturbinenwerk!", Filter.EVENT));
        list.add(new DropMessage(52.527386f, 13.326866f, new Date(), "Vietnamesisch essen nur hier!", Filter.YUMYUM));
        return list;
    }
}
