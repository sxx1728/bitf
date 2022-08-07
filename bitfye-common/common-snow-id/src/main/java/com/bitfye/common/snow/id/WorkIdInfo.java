package com.bitfye.common.snow.id;

import java.io.Serializable;

public class WorkIdInfo implements Serializable {
    private static final long serialVersionUID = 296716915294070972L;
    private int workId = -1;
    private String registerBy;
    private String businessContextName;
    private Long registerTime;

    public WorkIdInfo() {
    }

    public int getWorkId() {
        return this.workId;
    }

    public String getRegisterBy() {
        return this.registerBy;
    }

    public String getBusinessContextName() {
        return this.businessContextName;
    }

    public Long getRegisterTime() {
        return this.registerTime;
    }

    public void setWorkId(int workId) {
        this.workId = workId;
    }

    public void setRegisterBy(String registerBy) {
        this.registerBy = registerBy;
    }

    public void setBusinessContextName(String businessContextName) {
        this.businessContextName = businessContextName;
    }

    public void setRegisterTime(Long registerTime) {
        this.registerTime = registerTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof WorkIdInfo)) {
            return false;
        } else {
            WorkIdInfo other = (WorkIdInfo)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.getWorkId() != other.getWorkId()) {
                return false;
            } else {
                label49: {
                    Object this$registerBy = this.getRegisterBy();
                    Object other$registerBy = other.getRegisterBy();
                    if (this$registerBy == null) {
                        if (other$registerBy == null) {
                            break label49;
                        }
                    } else if (this$registerBy.equals(other$registerBy)) {
                        break label49;
                    }

                    return false;
                }

                Object this$businessContextName = this.getBusinessContextName();
                Object other$businessContextName = other.getBusinessContextName();
                if (this$businessContextName == null) {
                    if (other$businessContextName != null) {
                        return false;
                    }
                } else if (!this$businessContextName.equals(other$businessContextName)) {
                    return false;
                }

                Object this$registerTime = this.getRegisterTime();
                Object other$registerTime = other.getRegisterTime();
                if (this$registerTime == null) {
                    if (other$registerTime != null) {
                        return false;
                    }
                } else if (!this$registerTime.equals(other$registerTime)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof WorkIdInfo;
    }

    @Override
    public int hashCode() {
        boolean PRIME = true;
        int result = 1;
        result = result * 59 + this.getWorkId();
        Object $registerBy = this.getRegisterBy();
        result = result * 59 + ($registerBy == null ? 43 : $registerBy.hashCode());
        Object $businessContextName = this.getBusinessContextName();
        result = result * 59 + ($businessContextName == null ? 43 : $businessContextName.hashCode());
        Object $registerTime = this.getRegisterTime();
        result = result * 59 + ($registerTime == null ? 43 : $registerTime.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "WorkIdInfo(workId=" + this.getWorkId() + ", registerBy=" + this.getRegisterBy() + ", businessContextName=" + this.getBusinessContextName() + ", registerTime=" + this.getRegisterTime() + ")";
    }
}
