package com.humaralabs.fieldrun.datastructure;

import java.util.ArrayList;

public class HyperBasket {


    public String hyper_basket_ref_no;
    public Long hyper_basket_trip_id;
    public ArrayList<HyperBasketChild> hychild;


    public HyperBasket(String hyper_basket_ref_no, Long hyper_basket_trip_id,ArrayList<HyperBasketChild> hychild)  {
        this.hyper_basket_ref_no = hyper_basket_ref_no;
        this.hyper_basket_trip_id = hyper_basket_trip_id;
        this.hychild=hychild;
    }
}
