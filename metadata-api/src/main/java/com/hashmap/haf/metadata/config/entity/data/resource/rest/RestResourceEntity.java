package com.hashmap.haf.metadata.config.entity.data.resource.rest;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.uuid.Generators;
import com.hashmap.haf.metadata.config.constants.ModelConstants;
import com.hashmap.haf.metadata.config.entity.data.resource.DataResourceEntity;
import com.hashmap.haf.metadata.config.model.data.resource.rest.RestResource;
import com.hashmap.haf.metadata.config.model.data.resource.rest.RestResourceId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Slf4j
@Table(name = ModelConstants.REST_TABLE_NAME)
public class RestResourceEntity extends DataResourceEntity<RestResource> {

    @Column(name = ModelConstants.REST_URL)
    private String url;

    public RestResourceEntity() {
        super();
    }

    public RestResourceEntity(RestResource restResource) {
        if (restResource.getId() != null) {
            this.setId(restResource.getId().getId());
        } else {
            this.setId(Generators.timeBasedGenerator().generate());
        }
        this.url = restResource.getUrl();
    }

    @Override
    public RestResource toData() {
        RestResource restResource = new RestResource(new RestResourceId(getId()));
        restResource.setCreatedTime(UUIDs.unixTimestamp(getId()));
        restResource.setUrl(url);
        return restResource;
    }
}