package com.humaralabs.fieldrun.datastructure;

public class Task implements Comparable<Task>  {
    public Long tripId;
    public Long taskId;
    public String ref;
    public String platformId;
    public long pickups;
    public String name;
    public String address;
    public String zipCode;
    public String phone;
    public String taskType;
    public int pickupQty;
    public String reason;
    public String delieveryDateTime;
    public String comments;
    public String status;
    public String consignee_number;
    public String consignee_name;
    public String payment_mode;
    public String pinno;
    public String amount;
    public String mandatoryPhotocount;
    public String optionPhotocount;
    public String codamount;
    public String itemCategory;
    public String itemDescription;
    public int retryCount;

    public Task(Long tripId,Long taskId,String ref,String platformId,long pickups,String name,
                String address,String zipCode,String phone,String taskType,
                int pickupQty,String reason,String delieveryDateTime,
                String comments,String status,String consignee_number,String consignee_name,
                String payment_mode,String pinno,int count,String amount,String codamount,
                String itemCategory,String itemDescription,String mandatoryPhotocount,String optionPhotocount) {
        this.codamount=codamount;
        this.amount=amount;
        this.tripId = tripId;
        this.taskId = taskId;
        this.ref = ref;
        this.platformId = platformId;
        this.itemCategory = itemCategory;
        this.itemDescription = itemDescription;
        this.pickups = pickups;
        this.name = name;
        this.address = address;
        this.zipCode = zipCode;
        this.phone = phone;
        this.taskType = taskType;
        this.pickupQty=pickupQty;
        this.reason = reason;
        this.delieveryDateTime=delieveryDateTime;
        this.comments=comments;
        this.status=status;
        this.payment_mode=payment_mode;
        this.consignee_number=consignee_number;
        this.consignee_name=consignee_name;
        this.pinno=pinno;
        this.retryCount=count;
        this.optionPhotocount=optionPhotocount;
        this.mandatoryPhotocount=mandatoryPhotocount;
    }

    @Override
    public int compareTo(Task o) {
        if ((delieveryDateTime == null || delieveryDateTime.equals("")) && (o.delieveryDateTime == null || o.delieveryDateTime.equals(""))) {
            return 0;
        }
        if (delieveryDateTime == null || delieveryDateTime.equals("")) {
            return 1;
        }
        if (o.delieveryDateTime == null  || o.delieveryDateTime.equals("")) {
            return -1;
        }
        int a=delieveryDateTime.compareTo(o.delieveryDateTime);
        return a;
    }
}
