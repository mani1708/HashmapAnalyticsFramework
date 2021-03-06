package com.hashmap.haf.metadata.config.model.query;

import com.hashmap.haf.metadata.config.model.SearchTextBased;
import com.hashmap.haf.metadata.config.model.config.MetadataConfigId;
import com.hashmap.haf.metadata.config.trigger.TriggerType;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class MetadataQuery extends SearchTextBased<MetadataQueryId> {

    private static final long serialVersionUID = 3828979402280029033L;

    private MetadataConfigId metadataConfigId;
    private String queryStmt;
    private TriggerType triggerType;
    private String triggerSchedule;

    public MetadataQuery() {
    }

    public MetadataQuery(MetadataQueryId id) {
        super(id);
    }

    public MetadataQuery(MetadataQuery metadataQuery) {
        super(metadataQuery);
        this.metadataConfigId = metadataQuery.metadataConfigId;
        this.queryStmt = metadataQuery.queryStmt;
        this.triggerType = metadataQuery.triggerType;
        this.triggerSchedule = metadataQuery.triggerSchedule;
    }

    public MetadataConfigId getMetadataConfigId() {
        return metadataConfigId;
    }

    @Override
    public String getSearchText() {
        return getQueryStmt();
    }

    public void setMetadataConfigId(MetadataConfigId metadataConfigId) {
        this.metadataConfigId = metadataConfigId;
    }

    public String getQueryStmt() {
        return queryStmt;
    }

    public void setQueryStmt(String queryStmt) {
        this.queryStmt = queryStmt;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerSchedule() {
        return triggerSchedule;
    }

    public void setTriggerSchedule(String triggerSchedule) {
        this.triggerSchedule = triggerSchedule;
    }

    @Override
    public String toString() {
        return "MetadataQuery{" +
                "metadataConfigId = " + metadataConfigId +
                ", queryStmt=" + queryStmt +
                ", triggerType=" + triggerType +
                ", triggerSchedule=" + triggerSchedule +
                '}';
    }

    public void update(MetadataQuery metadataQuery) {
        if(metadataQuery.getQueryStmt() != null) {
            this.setQueryStmt(metadataQuery.getQueryStmt());
        }
        this.setTriggerType(metadataQuery.getTriggerType());
        this.setTriggerSchedule(metadataQuery.getTriggerSchedule());
    }
}
