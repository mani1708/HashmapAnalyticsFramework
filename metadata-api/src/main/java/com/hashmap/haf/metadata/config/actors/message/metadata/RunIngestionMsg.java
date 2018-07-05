package com.hashmap.haf.metadata.config.actors.message.metadata;

import com.hashmap.haf.metadata.config.actors.message.AbstractMessage;
import com.hashmap.haf.metadata.config.actors.message.metadata.MetadataMessage;
import com.hashmap.haf.metadata.config.model.MetadataConfig;

public final class RunIngestionMsg extends AbstractMessage {

    public RunIngestionMsg(MetadataConfig metadataConfig) {
        super(metadataConfig);
    }
}
