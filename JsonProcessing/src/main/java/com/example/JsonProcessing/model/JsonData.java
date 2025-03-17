package com.example.JsonProcessing.model;

import java.util.List;

public class JsonData {

    private NotificationInfo notificationInfo;
    private TrackingInfo trackingInfo;
    private JobInfo jobInfo;
    private List<TransactionInfo> transactionInfo;

    public void setNotificationInfo(NotificationInfo notificationInfo) {
        this.notificationInfo = notificationInfo;
    }
    public TrackingInfo getTrackingInfo() {
        return trackingInfo;
    }
    public void setTrackingInfo(TrackingInfo trackingInfo) {
        this.trackingInfo = trackingInfo;
    }
    public JobInfo getJobInfo() {
        return jobInfo;
    }
    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }
    public List<TransactionInfo> getTransactionInfo() {
        return transactionInfo;
    }
    public void setTransactionInfo(List<TransactionInfo> transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    public static class NotificationInfo {
        private String stage;
        private String source;
        public String getStage() { return stage; }
        public void setStage(String stage) { this.stage = stage; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }

    public static class TrackingInfo {
        private String rayIdentifier;
        private String sourceTypeIdentifier;
        private String state;
        public String getRayIdentifier() { return rayIdentifier; }
        public void setRayIdentifier(String rayIdentifier) { this.rayIdentifier = rayIdentifier; }
        public String getSourceTypeIdentifier() { return sourceTypeIdentifier; }
        public void setSourceTypeIdentifier(String sourceTypeIdentifier) { this.sourceTypeIdentifier = sourceTypeIdentifier; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
    }

    public static class JobInfo {
        private String identifier;
        private String referenceIdentifier;
        private String definition;
        private int transactionCount;
        private String state;
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public String getReferenceIdentifier() { return referenceIdentifier; }
        public void setReferenceIdentifier(String referenceIdentifier) { this.referenceIdentifier = referenceIdentifier; }
        public String getDefinition() { return definition; }
        public void setDefinition(String definition) { this.definition = definition; }
        public int getTransactionCount() { return transactionCount; }
        public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
    }

    public static class TransactionInfo {
        private String recordIdentifier;
        private GenericExtract genericExtract;
        private List<ProcessStageInfo> processStageInfo;
        public String getRecordIdentifier() { return recordIdentifier; }
        public void setRecordIdentifier(String recordIdentifier) { this.recordIdentifier = recordIdentifier; }
        public GenericExtract getGenericExtract() { return genericExtract; }
        public void setGenericExtract(GenericExtract genericExtract) { this.genericExtract = genericExtract; }
        public List<ProcessStageInfo> getProcessStageInfo() { return processStageInfo; }
        public void setProcessStageInfo(List<ProcessStageInfo> processStageInfo) { this.processStageInfo = processStageInfo; }
    }

    public static class GenericExtract {
        private int pageNo;
        private String pageDataId;
        private int lineCount;
        private int totalPage;
        private String extractFileName;
        public int getPageNo() { return pageNo; }
        public void setPageNo(int pageNo) { this.pageNo = pageNo; }
        public String getPageDataId() { return pageDataId; }
        public void setPageDataId(String pageDataId) { this.pageDataId = pageDataId; }
        public int getLineCount() { return lineCount; }
        public void setLineCount(int lineCount) { this.lineCount = lineCount; }
        public int getTotalPage() { return totalPage; }
        public void setTotalPage(int totalPage) { this.totalPage = totalPage; }
        public String getExtractFileName() { return extractFileName; }
        public void setExtractFileName(String extractFileName) { this.extractFileName = extractFileName; }
    }

    public static class ProcessStageInfo {
        private String processStageName;
        private String processStatus;
        private String auditTime;
        public String getProcessStageName() { return processStageName; }
        public void setProcessStageName(String processStageName) { this.processStageName = processStageName; }
        public String getProcessStatus() { return processStatus; }
        public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
        public String getAuditTime() { return auditTime; }
        public void setAuditTime(String auditTime) { this.auditTime = auditTime; }
    }
}
