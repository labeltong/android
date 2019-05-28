package com.team4.caucapstone.labeltong;

public class UserData {
    private String ID;
    private String CreatedAt;
    private String UpdatedAt;
    private String DeletedAt;
    private String Token;
    private String Name;
    private String PhoneNum;
    private String Points;
    private String IsBanned;
    private String BanPoint;
    private String IsAdmin;
    private String LastLoginDate;
    private String AnserList;
    private String PointUsageList;
    private String Email;

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }

    public String getUpdatedAt() {
        return UpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        UpdatedAt = updatedAt;
    }

    public String getDeletedAt() {
        return DeletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        DeletedAt = deletedAt;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhoneNum() {
        return PhoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        PhoneNum = phoneNum;
    }

    public String getPoints() {
        return Points;
    }

    public void setPoints(String points) {
        Points = points;
    }

    public String getIsBanned() {
        return IsBanned;
    }

    public void setIsBanned(String isBanned) {
        IsBanned = isBanned;
    }

    public String getBanPoint() {
        return BanPoint;
    }

    public void setBanPoint(String banPoint) {
        BanPoint = banPoint;
    }

    public String getIsAdmin() {
        return IsAdmin;
    }

    public void setIsAdmin(String isAdmin) {
        IsAdmin = isAdmin;
    }

    public String getLastLoginDate() {
        return LastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        LastLoginDate = lastLoginDate;
    }

    public String getAnserList() {
        return AnserList;
    }

    public void setAnserList(String anserList) {
        AnserList = anserList;
    }

    public String getPointUsageList() {
        return PointUsageList;
    }

    public void setPointUsageList(String pointUsageList) {
        PointUsageList = pointUsageList;
    }
}
