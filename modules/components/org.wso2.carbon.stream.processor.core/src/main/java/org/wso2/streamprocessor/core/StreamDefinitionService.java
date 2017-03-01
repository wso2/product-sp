package org.wso2.streamprocessor.core;


import scala.util.parsing.combinator.testing.Str;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by ruwini on 2/15/17.
 */
public interface StreamDefinitionService {

   LinkedHashMap<String,StreamDefinitionRetriever.Type> streamDefinitionService(String streamName);
}
