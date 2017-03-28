/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var app = {}; // create namespace for our app

app.Stream = Backbone.Model.extend({
    defaults: {
        id: '',
        define: '',
        type: '',
        attributes: [
            {
                attribute:'',
                type: ''
            }
        ]
    }
});

app.FilterQuery = Backbone.Model.extend({
    defaults: {
        id: '',
        name: '',
        inStream: '',
        outStream: '',
        filter: '',
        attributes: []
    }
});

app.PassThroughQuery = Backbone.Model.extend({
    defaults: {
        id: '',
        name: '',
        inStream: '',
        outStream: '',
        attributes: []
    }
});

app.WindowQuery = Backbone.Model.extend({
    defaults: {
        id: '',
        name: '',
        inStream: '',
        outStream: '',
        filter1: '',
        filter2: '',
        window: '',
        attributes: []
    }
});
//model for all 3 simple queries ( passthrough, window and filter)
app.Query = Backbone.Model.extend({
    defaults: {
        "id": '',
        "name": '',
        "from": '',
        "insert-into": '',
        "filter": '',
        "post-window-query": '',
        "window": '',
        "output-type": '',
        "projection": []
    }
});

app.Pattern = Backbone.Model.extend({
    defaults: {
        "id": '',
        "name": '',
        "states": [],
        "logic": '',
        "projection": [],
        "filter": '',
        "post-window-filter": '',
        "window": '',
        "having": '',
        "group-by": '',
        "output-type": '',
        "insert-into": '',
        //additional attribute for form generation
        "from": []
    }
});

app.JoinQuery = Backbone.Model.extend({

    "join":{
        "type":'',
        "left-stream":{
            "from":'',
            "filter":'',
            "window":'',
            "post-window-query":'',
            "as":''
        },
        "right-stream":{
            "from":'',
            "filter":'',
            "window":'',
            "post-window-query":'',
            "as":''
        },
        "on":''
    },
    "projection":[],
    "output-type": '',
    "insert-into":'',
    //additional attribute for form generation
    "from" : []
});

app.Partition = Backbone.Model.extend({
    defaults:{
        "partition": {
            "with" :[] // this will contain json objects { stream : '', property :''}
        },
        "queries" :[]
    }

});

//--------------
// Collections
//--------------
app.StreamList = Backbone.Collection.extend({
    model: app.Stream
});

app.FilterList = Backbone.Collection.extend({
    model: app.FilterQuery
});
app.PassThroughList = Backbone.Collection.extend({
    model: app.PassThroughQuery
});
app.WindowQueryList = Backbone.Collection.extend({
    model: app.WindowQuery
});
app.QueryList = Backbone.Collection.extend({
    model: app.Query
});
app.PatternList = Backbone.Collection.extend({
    model: app.Pattern
});
app.PatternList = Backbone.Collection.extend({
    model: app.Pattern
});
app.JoinQueryList = Backbone.Collection.extend({
    model: app.JoinQuery
});
app.PartitionList = Backbone.Collection.extend({
    model: app.Partition
});

//initiates the collections
streamList = new app.StreamList();
filterList = new app.FilterList();
passThroughList = new app.PassThroughList();
windowQueryList = new app.WindowQueryList();
queryList = new app.QueryList();
patternList = new app.PatternList();
joinQueryList = new app.JoinQueryList();
partitionList = new app.PartitionList();