package com.humaralabs.fieldrun.datastructure;

public class Basket {


    public Long basket_server_id;
    public Long basket_trip_id;
    public String basket_trip_type;
    public String basket_seller_name;
    public String basket_seller_address;
    public String basket_seller_zipcode;
    public int basket_eqty;
    public int basket_status;


    public Basket(Long basket_server_id, Long basket_trip_id, String basket_trip_type,
                  String basket_seller_name, String basket_seller_address,String basket_seller_zipcode,
                  int basket_eqty, int basket_status)  {
        this.basket_server_id = basket_server_id;
        this.basket_trip_id = basket_trip_id;
        this.basket_trip_type = basket_trip_type;
        this.basket_seller_name = basket_seller_name;
        this.basket_seller_address = basket_seller_address;
        this.basket_seller_zipcode=basket_seller_zipcode;
        this.basket_eqty=basket_eqty;
        this.basket_status = basket_status;
    }
}
