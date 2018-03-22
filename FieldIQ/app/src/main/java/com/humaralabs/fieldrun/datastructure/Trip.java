package com.humaralabs.fieldrun.datastructure;

public class Trip{
    public Long tripId;
    public String tripDate;
    public int numTasks;
    public String zipCode;
    public String origin;
    public String trip_facility;
    public String trip_type;
    public int status;
    public String tripExpiryDateTime;

    public Trip(Long tripId,String tripDate,int numTasks,String zipCode,String origin,int status,String tripExpiryDateTime,String trip_facility,String trip_type)  {
        this.tripId = tripId;
        this.tripDate = tripDate;
        this.numTasks = numTasks;
        this.zipCode = zipCode;
        this.origin = origin;
        this.trip_facility=trip_facility;
        this.trip_type=trip_type;
        this.status = status;
        this.tripExpiryDateTime = tripExpiryDateTime;
    }
}
