package com.wms.model;

import java.sql.Date;
import java.sql.Timestamp;

public class WasteRequest {
    private int requestId;
    private int userId;
    private String citizenName;   // joined from users table, display only
    private String wasteType;
    private String quantity;
    private String address;
    private Date pickupDate;
    private String status;        // PENDING, SCHEDULED, COLLECTED, CANCELLED
    private Integer assignedStaffId;
    private String assignedStaffName; // joined, display only
    private String remarks;
    private Timestamp createdAt;

    public WasteRequest() { }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }

    public String getWasteType() { return wasteType; }
    public void setWasteType(String wasteType) { this.wasteType = wasteType; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Date getPickupDate() { return pickupDate; }
    public void setPickupDate(Date pickupDate) { this.pickupDate = pickupDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getAssignedStaffId() { return assignedStaffId; }
    public void setAssignedStaffId(Integer assignedStaffId) { this.assignedStaffId = assignedStaffId; }

    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
