package org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.util;

import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.metadata.MetaData;

/**
 * Used for holding the meta data of the processors available in the siddhi engine
 */
public class MetaDataHolder {
    private static MetaData inBuiltProcessorMetaData;

    private MetaDataHolder() {

    }

    /**
     * Returns the in built processor meta data
     *
     * @return In-built processor meta data
     */
    public static MetaData getInBuiltProcessorMetaData() {
        if (inBuiltProcessorMetaData == null) {
            inBuiltProcessorMetaData = SourceEditorUtils.getInBuiltProcessorMetaData();
        }
        return inBuiltProcessorMetaData;
    }
}
