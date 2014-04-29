var monitorServerList;

function getBackendServerUrl() {

    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getBackendServerUrl" +
                "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }

    return false;
}

function getAdminConsoleUrl() {

    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getAdminConsoleUrl" +
                "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }

    return false;
}

function createXmlHttpRequest() {
    var request;

    // Lets try using ActiveX to instantiate the XMLHttpRequest
    // object
    try {
        request = new ActiveXObject("Microsoft.XMLHTTP");
    } catch(ex1) {
        try {
            request = new ActiveXObject("Msxml2.XMLHTTP");
        } catch(ex2) {
            request = null;
        }
    }

    // If the previous didn't work, lets check if the browser natively support XMLHttpRequest
    if (!request && typeof XMLHttpRequest != "undefined") {
        //The browser does, so lets instantiate the object
        request = new XMLHttpRequest();
    }

    return request;
}

function removeCarriageReturns(string) {
    return string.replace(/\n/g, "");
}

function loadServerListWithServices() {
    getServerList(function(serverListArray) {
        serverListArray = serverListArray.split("|");
        // Cleaning up the existing select box
        var selectBoxEl = document.getElementById("server_select_box");
        selectBoxEl.innerHTML = "";

        var newServerSelectHTML = '<select id="serverIDs" onchange="loadServices();"><option value="">--Server--</option>';
        for (var x = 0; x < serverListArray.length; x++) {
            var _tokens = serverListArray[x].split(",");
            newServerSelectHTML +=
                    '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
        }
        newServerSelectHTML += '</select>';

        // Adding the new select to div
        selectBoxEl.innerHTML = newServerSelectHTML;

        if (!isServerExists(serverListArray, serverID)) {
            serverID = "";
            prefs.set("serverID", serverID);
        }

        tabs.setSelectedTab(0);
        drawDiagram();
    });
}

function loadServerList() {
    getServerList(function(serverListArray) {

        serverListArray = serverListArray.split("|");
        // Cleaning up the existing select box
        var selectBoxEl = document.getElementById("server_select_box");
        selectBoxEl.innerHTML = "";

        var newServerSelectHTML = '<select id="serverIDs" onchange="refreshDataWithServerID();"><option value="">--Server--</option>';
        for (var x = 0; x < serverListArray.length; x++) {
            var _tokens = serverListArray[x].split(",");
            newServerSelectHTML +=
                    '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
        }
        newServerSelectHTML += '</select>';

        // Adding the new select to div
        selectBoxEl.innerHTML = newServerSelectHTML;

        if (!isServerExists(serverListArray, serverID)) {
            serverID = "";
            prefs.set("serverID", serverID);
        }
        tabs.setSelectedTab(0);
        drawDiagram();
    });
}

function loadServerListWithCategory() {
    getServerListWithCategoryName(function (servers) {

        monitorServerList = servers;

        var newServerSelectHTML = "";

        newServerSelectHTML += '<option value="">--Server--</option>';

        for (var serverUrlkey in servers) {
            newServerSelectHTML +=
                    '<option value="' + serverUrlkey + '">' + serverUrlkey + '</option>';
        }

        $("#serverUrls").html(newServerSelectHTML);

        // hiding
        document.getElementById('server_type_div').style.display = 'none';
        document.getElementById('category_type_div').style.display = 'none';

        tabs.setSelectedTab(0);
        drawDiagram();
    });
}


function getServerListWithCategoryName(callback) {
    var xmlHttpReq = createXmlHttpRequest();
    var serverList = [];
    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq) {
        // This is a asynchronous POST
        xmlHttpReq.onreadystatechange = function() {
            if (xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(processJson(xmlHttpReq.responseText));
            }
        };
        xmlHttpReq.open("GET", "carbon/gauges/jsp/json_ajaxprocessor.jsp?service=BAMListAdminService&action=urn:getServerListWithCategoryName", true);
        xmlHttpReq.send(null);
    }
}

function processJson(servers) {
    servers = new wso2.xml.axiom.OMElement(
            wso2.xml.utils.xml2bf(wso2.xml.utils.xml2DOM(servers))).getChildren();
    var serverIndices = {};
    for (var i = 0; i < servers.length; i++) {
        var value = servers[i];
        var s = {
            url : value.getChildrenWithLocalName("serverURL")[0].getText(),
            type : value.getChildrenWithLocalName("serverType")[0].getText(),
            category : value.getChildrenWithLocalName("categoryName")[0].getText(),
            sid : value.getChildrenWithLocalName("id")[0].getText()
        };
        var server = serverIndices[s.url];
        server = serverIndices[s.url] = server ? server : {};
        var type = server[s.type];
        type = server[s.type] = type ? type : {};
        type[s.category] = s.sid;
    }
    return serverIndices;
}

/**
 * This fired when serverUrl drop down list changed
 */
function refreshDataOnServerUrlChange() {
    var serverUrlInConfig =
            document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;

    var serverTypeSelectElement = "";
    var serverTypes = monitorServerList[serverUrlInConfig];

    for (var serverType in serverTypes) {
        serverTypeSelectElement += '<option value="' + serverType + '">' + serverType + '</option>';
    }
    document.getElementById('selectServerType').innerHTML = serverTypeSelectElement;

    document.getElementById('server_type_div').style.display = '';

    refreshDataOnServerTypesChange();
}

/**
 * This fired when server type drop down list changed
 */
function refreshDataOnServerTypesChange() {
    var serverUrlInConfig =
            document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;
    var serverTypeVal = document.getElementById('selectServerType')[document.getElementById('selectServerType').selectedIndex].value;

    var serverTypes = monitorServerList[serverUrlInConfig];
    var serverCategoryName = serverTypes[serverTypeVal];

    var serverCategoryTypeSelectElement = "";

    for (var category in serverCategoryName) {
        serverCategoryTypeSelectElement += '<option value="' + serverCategoryName[category] + '">' + category + '</option>';
    }

    document.getElementById('selectServerCategory').innerHTML = serverCategoryTypeSelectElement;

    document.getElementById('category_type_div').style.display = '';

    refreshDataOnServerCategoryChange();
}

/**
 * This fired when category type drop down list changed
 */
function refreshDataOnServerCategoryChange() {
    var serverIDVal = document.getElementById('selectServerCategory')[document.getElementById('selectServerCategory').selectedIndex].value;
    var serverUrlVal = document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;
    if (serverIDVal != "") {
        prefs.set("serverID", serverIDVal);
        prefs.set("serverURL", serverUrlVal);
    }
    tabs.setSelectedTab(0);
    drawDiagram();
}


function refreshEndpointDataOnServerUrlChange() {
    var serverUrlInConfig =
            document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;

    var serverTypeSelectElement = "";
    var serverTypes = monitorServerList[serverUrlInConfig];

    for (var serverType in serverTypes) {
        serverTypeSelectElement += '<option value="' + serverType + '">' + serverType + '</option>';
    }
    document.getElementById('selectServerType').innerHTML = serverTypeSelectElement;

    document.getElementById('server_type_div').style.display = '';

    refreshEndpointDataOnServerTypesChange();
}

function refreshEndpointDataOnServerTypesChange() {
    var serverUrlInConfig =
            document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;
    var serverTypeVal = document.getElementById('selectServerType')[document.getElementById('selectServerType').selectedIndex].value;

    var serverTypes = monitorServerList[serverUrlInConfig];
    var serverCategoryName = serverTypes[serverTypeVal];

    var serverCategoryTypeSelectElement = "";

    for (var category in serverCategoryName) {
        serverCategoryTypeSelectElement += '<option value="' + serverCategoryName[category] + '">' + category + '</option>';
    }

    document.getElementById('selectServerCategory').innerHTML = serverCategoryTypeSelectElement;

    document.getElementById('category_type_div').style.display = '';

    refreshEndpointDataOnServerCategoryChange();
}

function refreshEndpointDataOnServerCategoryChange() {
    var serverIDVal = document.getElementById('selectServerCategory')[document.getElementById('selectServerCategory').selectedIndex].value;
    var serverUrlVal = document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;
    if (serverIDVal != "") {
        prefs.set("serverID", serverIDVal);
        prefs.set("serverURL", serverUrlVal);
    }
    document.getElementById('endpointTr').style.display = '';
    loadEndpoints();
}


function loadServerListWithCategoryAndSequences(){
    getServerListWithCategoryName(function (servers){

        monitorServerList = servers;
        var newServerSelectHTML = "";

        newServerSelectHTML += '<option value="">--Server--</option>';

        for (var serverUrlkey in servers) {
            newServerSelectHTML +=
            '<option value="' + serverUrlkey + '">' + serverUrlkey + '</option>';
        }

        $("#serverUrls").html(newServerSelectHTML);

        // hiding
        document.getElementById('server_type_div').style.display = 'none';
        document.getElementById('category_type_div').style.display = 'none';
        document.getElementById('sequenceTr').style.display='none';
    });
}


function refreshSequenceDataOnServerUrlChange() {
    var serverUrlInConfig =
            document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;

    var serverTypeSelectElement = "";
    var serverTypes = monitorServerList[serverUrlInConfig];

    for (var serverType in serverTypes) {
        serverTypeSelectElement += '<option value="' + serverType + '">' + serverType + '</option>';
    }
    document.getElementById('selectServerType').innerHTML = serverTypeSelectElement;

    document.getElementById('server_type_div').style.display = '';

    refreshSequenceDataOnServerTypesChange();
}

function refreshSequenceDataOnServerTypesChange() {
    var serverUrlInConfig =
            document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;
    var serverTypeVal = document.getElementById('selectServerType')[document.getElementById('selectServerType').selectedIndex].value;

    var serverTypes = monitorServerList[serverUrlInConfig];
    var serverCategoryName = serverTypes[serverTypeVal];

    var serverCategoryTypeSelectElement = "";

    for (var category in serverCategoryName) {
        serverCategoryTypeSelectElement += '<option value="' + serverCategoryName[category] + '">' + category + '</option>';
    }

    document.getElementById('selectServerCategory').innerHTML = serverCategoryTypeSelectElement;

    document.getElementById('category_type_div').style.display = '';

    refreshSequenceDataOnServerCategoryChange();
}

function refreshSequenceDataOnServerCategoryChange() {
    var serverIDVal = document.getElementById('selectServerCategory')[document.getElementById('selectServerCategory').selectedIndex].value;
    var serverUrlVal = document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;
    if (serverIDVal != "") {
        prefs.set("serverID", serverIDVal);
        prefs.set("serverURL", serverUrlVal);
    }
    document.getElementById('sequenceTr').style.display = '';
    loadSequences();
}

function loadSequencesOnChange(){
    var sequenceName = document.getElementById('sequence')[document.getElementById('sequence').selectedIndex].value;

    if(sequenceName!=""){
       prefs.set("sequenceName", sequenceName);
    }

    tabs.setSelectedTab(0);
    drawDiagram();
}

function loadServerListForActivity() {
	getServerList(function(serverListArray) {
        serverListArray = serverListArray.split("|");
        // Cleaning up the existing select box
        var selectBoxEl = document.getElementById("server_select_box");
        selectBoxEl.innerHTML = "";

        var newServerSelectHTML = '';
        newServerSelectHTML += '<div><table>';

        for (var x = 0; x < serverListArray.length; x++) {
            var _tokens = serverListArray[x].split(",");
        newServerSelectHTML += '<tr><td>';
        newServerSelectHTML +='<a href="#" onchange="refreshDataWithServerID();" onClick="selectService(' + _tokens[0] + ')">'+
                '<img src="registry/resource/_system/config/repository/dashboards/gadgets/images/server.png" /></a>';
        newServerSelectHTML += '</td>';
        newServerSelectHTML += '<td>';
        newServerSelectHTML += '<font size="2">' + _tokens[1] + '</font>';
        newServerSelectHTML += '</td></tr>';
        newServerSelectHTML += '<tr></tr>';
        }

        newServerSelectHTML += '</table></div>';


        // Adding the new select to div
        selectBoxEl.innerHTML = newServerSelectHTML;

        if (!isServerExists(serverListArray, serverID)) {
            serverID = "";
            prefs.set("serverID", serverID);
        }

        //tabs.setSelectedTab(1);
        selectServer();
    });
}


function getServiceList() {
    var selectedServerID = document.getElementById('serverIDs').value;

    if (!(selectedServerID == "No Servers Configured")) {
        var xmlHttpReq = createXmlHttpRequest();

        // Make sure the XMLHttpRequest object was instantiated
        if (xmlHttpReq)
        {
            // This is a synchronous POST, hence UI blocking.
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getServicesList&serverID=" +
                    selectedServerID + "&ms=" +
                    new Date().getTime(), false);
            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }
    return false;
}

function getServerList(callback) {
    var xmlHttpReq = createXmlHttpRequest();
    var serverList = [];
    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.onreadystatechange = function() {
            if(xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getServerList" +
                "&ms=" + new Date().getTime(), true);
        xmlHttpReq.send(null);
    }
}

function getlastminuterequestcount(serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=lastminuterequestcount&serviceID=" + serviceID
                + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getminmaxaverageresptimessystem(serverID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getminmaxaverageresptimessystem&serverID=" +
                serverID + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getMinMaxAverageRespTimesService(serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getminmaxaverageresptimesservice&serviceID=" +
                serviceID + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function lastminuterequestcountsystem() {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=lastminuterequestcountsystem&serverUrl=" +
                encodeHex(serverUrl) + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function loadServices() {
    var serviceListArray = getServiceList().split("|");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("service_select_box");
    selectBoxEl.innerHTML = "";

    var newServerSelectHTML = '<select id="services" onchange="refreshData();"><option value="">--Service--</option>';
    for (var x = 0; x < serviceListArray.length; x++) {
        var _tokens = serviceListArray[x].split(",");
        newServerSelectHTML +=
                '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
    }
    newServerSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newServerSelectHTML;
}

function refreshData() {
    serverID =
            document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].value;
    serverURL =
            document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].text;
    serviceID =
            document.getElementById('services')[document.getElementById('services').selectedIndex].value;
    serviceName =
            document.getElementById('services')[document.getElementById('services').selectedIndex].text;

    if ((serverID != "") && (serverID != "No Servers Configured") &&
            (serviceID != "")) {
        prefs.set("serverID", serverID);
        prefs.set("serverURL", serverURL);
        prefs.set("serviceID", serviceID);
        prefs.set("serviceName", serviceName);
    }
    tabs.setSelectedTab(0);
    drawDiagram();
}

function refreshDataWithServerID() {
    serverID =
            document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].value;
    serverURL =
            document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].text;

    if ((serverID != "") && (serverID != "No Servers Configured")) {
        prefs.set("serverID", serverID);
        prefs.set("serverURL", serverURL);
    }

   // drawDiagram();
   // tabs.setSelectedTab(0);
    /* it is assumed main_disp is having 0 index */
  //  document.getElementById('disp_config').style.display = "none";
  //  document.getElementById('main_disp').style.display = "block";
}

function isServerExists(serverListArray, monitoredServer) {
    for (var x = 0; x < serverListArray.length; x++) {

        var _tokens = serverListArray[x].split(",");

        if (_tokens[0] == monitoredServer) {
            return true
        }
    }

    return false;
}

function getLatestRequestCountForServer(serverID, callback) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.onreadystatechange = function() {
            if(xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestRequestCountForServer&serverID=" + serverID, true);
        xmlHttpReq.send(null);
    }
}

function getLatestResponseCountForServer(serverID, callback) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpResponse object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.onreadystatechange = function() {
            if(xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestResponseCountForServer&serverID=" + serverID, true);
        xmlHttpReq.send(null);
    }
}

function getLatestFaultCountForServer(serverID, callback) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpFault object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.onreadystatechange = function() {
            if(xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestFaultCountForServer&serverID=" + serverID, true);
        xmlHttpReq.send(null);
    }
}

function getLatestRequestCountForService(serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestRequestCountForService&serviceID=" + serviceID
                + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getLatestResponseCountForService(serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestResponseCountForService&serviceID=" + serviceID
                + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getLatestFaultCountForService(serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestFaultCountForService&serviceID=" + serviceID
                + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

// Endpoint related functions

function loadServerListWithEndpoints() {
    getServerList(function(serverListArray) {
        serverListArray = serverListArray.split("|");
        // Cleaning up the existing select box
        var selectBoxEl = document.getElementById("server_select_box");
        selectBoxEl.innerHTML = "";

        var newServerSelectHTML = '<select id="serverIDs" onchange="loadEndpoints();"><option value="">--Server--</option>';
        for (var x = 0; x < serverListArray.length; x++) {
            var _tokens = serverListArray[x].split(",");
            newServerSelectHTML +=
                    '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
        }
        newServerSelectHTML += '</select>';

        // Adding the new select to div
        selectBoxEl.innerHTML = newServerSelectHTML;

        if (!isServerExists(serverListArray, serverID)) {
            serverID = "";
            prefs.set("serverID", serverID);
        }
        tabs.setSelectedTab(0);
        drawDiagram();
    });
}

function loadEndpoints() {
    var epListArray = getEndpointList().split("&");

    var newEndpointSelectHTML = "";
    newEndpointSelectHTML += '<option value="">--Endpoints--</option>';

    for (var x = 0; x < epListArray.length; x++) {
        newEndpointSelectHTML +=
        '<option value="' + epListArray[x] + '">' + epListArray[x] + '</option>';
    }

    $("#endpoints").html(newEndpointSelectHTML);
}

function loadEndpointsOnChange(){
    var endpointName = document.getElementById('endpoints')[document.getElementById('endpoints').selectedIndex].value;

    if(endpointName!=""){
       prefs.set("endpointName", endpointName);
    }

    tabs.setSelectedTab(0);
    drawDiagram();
}


function loadServerListWithCategoryAndEndpoints(){
    getServerListWithCategoryName(function (servers){

        monitorServerList = servers;
        var newServerSelectHTML = "";

        newServerSelectHTML += '<option value="">--Server--</option>';

        for (var serverUrlkey in servers) {
            newServerSelectHTML +=
            '<option value="' + serverUrlkey + '">' + serverUrlkey + '</option>';
        }

        $("#serverUrls").html(newServerSelectHTML);

        // hiding
        document.getElementById('server_type_div').style.display = 'none';
        document.getElementById('category_type_div').style.display = 'none';
        document.getElementById('endpointTr').style.display='none';
    });
}


function getEndpointList() {
    var selectedServerID = document.getElementById('selectServerCategory').value;

    if (!(selectedServerID == "No Servers Configured")) {
        var xmlHttpReq = createXmlHttpRequest();

        // Make sure the XMLHttpRequest object was instantiated
        if (xmlHttpReq)
        {
            // This is a synchronous POST, hence UI blocking.
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getEndpoints&serverID=" +
                    selectedServerID + "&ms=" +
                    new Date().getTime(), false);
            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }
    return false;
}

function refreshEndpointData() {
    serverID =
            document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;
    serverURL =
            document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].text;
    endpointID =
            document.getElementById('endpoints')[document.getElementById('endpoints').selectedIndex].value;
    endpointName =
            document.getElementById('endpoints')[document.getElementById('endpoints').selectedIndex].text;

    if ((serverID != "") && (serverID != "No Servers Configured") &&
            (endpointID != "")) {
        prefs.set("serverID", serverID);
        prefs.set("serverURL", serverURL);
        prefs.set("endpointID", endpointID);
        prefs.set("endpointName", endpointName);
    }
    tabs.setSelectedTab(0);
    drawDiagram();
}

function loadServerListWithCategoryAndProxies(){
    getServerListWithCategoryName(function (servers){

        monitorServerList = servers;
        var newServerSelectHTML = "";

        newServerSelectHTML += '<option value="">--Server--</option>';

        for (var serverUrlkey in servers) {
            newServerSelectHTML +=
            '<option value="' + serverUrlkey + '">' + serverUrlkey + '</option>';
        }

        $("#serverUrls").html(newServerSelectHTML);

        // hiding
        document.getElementById('server_type_div').style.display = 'none';
        document.getElementById('category_type_div').style.display = 'none';
        document.getElementById('proxyTr').style.display='none';
    });
}

function refreshProxyDataOnServerUrlChange() {
    var serverUrlInConfig =
            document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;

    var serverTypeSelectElement = "";
    var serverTypes = monitorServerList[serverUrlInConfig];

    for (var serverType in serverTypes) {
        serverTypeSelectElement += '<option value="' + serverType + '">' + serverType + '</option>';
    }
    document.getElementById('selectServerType').innerHTML = serverTypeSelectElement;

    document.getElementById('server_type_div').style.display = '';

    refreshProxyDataOnServerTypesChange();
}

function refreshProxyDataOnServerTypesChange() {
    var serverUrlInConfig =
            document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;
    var serverTypeVal = document.getElementById('selectServerType')[document.getElementById('selectServerType').selectedIndex].value;

    var serverTypes = monitorServerList[serverUrlInConfig];
    var serverCategoryName = serverTypes[serverTypeVal];

    var serverCategoryTypeSelectElement = "";

    for (var category in serverCategoryName) {
        serverCategoryTypeSelectElement += '<option value="' + serverCategoryName[category] + '">' + category + '</option>';
    }

    document.getElementById('selectServerCategory').innerHTML = serverCategoryTypeSelectElement;

    document.getElementById('category_type_div').style.display = '';

    refreshProxyDataOnServerCategoryChange();
}

function refreshProxyDataOnServerCategoryChange() {
    var serverIDVal = document.getElementById('selectServerCategory')[document.getElementById('selectServerCategory').selectedIndex].value;
    var serverUrlVal = document.getElementById('serverUrls')[document.getElementById('serverUrls').selectedIndex].value;
    if (serverIDVal != "") {
        prefs.set("serverID", serverIDVal);
        prefs.set("serverURL", serverUrlVal);
    }
    document.getElementById('proxyTr').style.display = '';
    loadProxies();
}

function loadProxies() {
    var proxyListArray = getProxyList().split("&");

    var newEndpointSelectHTML = "";
    newEndpointSelectHTML += '<option value="">--Proxy--</option>';

    for (var x = 0; x < proxyListArray.length; x++) {
        newEndpointSelectHTML +=
        '<option value="' + proxyListArray[x] + '">' + proxyListArray[x] + '</option>';
    }

    $("#proxy").html(newEndpointSelectHTML);
}

function getProxyList() {
    var selectedServerID = document.getElementById('selectServerCategory').value;

    if (!(selectedServerID == "No Servers Configured")) {
        var xmlHttpReq = createXmlHttpRequest();

        // Make sure the XMLHttpRequest object was instantiated
        if (xmlHttpReq)
        {
            // This is a synchronous POST, hence UI blocking.
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getProxyServices&serverID=" +
                    selectedServerID + "&ms=" +
                    new Date().getTime(), false);
            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }
    return false;
}

function loadProxiesOnChange(){
    var proxyName = document.getElementById('proxy')[document.getElementById('proxy').selectedIndex].value;

    if(proxyName!=""){
       prefs.set("proxyName", proxyName);
    }

    tabs.setSelectedTab(0);
    drawDiagram();
}

function getLatestInCumulativeCountForEndpoint(serverID, endpointName, callback) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is an asynchronous POST
        xmlHttpReq.onreadystatechange = function() {
            if (xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };
        // This is an asynchronous POST
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInCumulativeCountForEndpoint&serverID=" +
                serverID + "&endpointName=" + endpointName + "&ms=" + new Date().getTime(), true);
        xmlHttpReq.send(null);
    }
}

function getLatestInFaultCountForEndpoint(serverID, endpointName, callback) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is an asynchronous POST
        xmlHttpReq.onreadystatechange = function() {
            if (xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };

        // This is an asynchronous POST.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInFaultCountForEndpoint&serverID=" +
                serverID + "&endpointName=" + endpointName + "&ms=" + new Date().getTime(), true);
        xmlHttpReq.send(null);
    }
}

// Sequence related functions

function loadServerListWithSequences() {
    getServerList(function(serverListArray) {
        serverListArray = serverListArray.split("|");
        // Cleaning up the existing select box
        var selectBoxEl = document.getElementById("server_select_box");
        selectBoxEl.innerHTML = "";

        var newServerSelectHTML = '<select id="serverIDs" onchange="loadSequences();"><option value="">--Server--</option>';
        for (var x = 0; x < serverListArray.length; x++) {
            var _tokens = serverListArray[x].split(",");
            newServerSelectHTML +=
                    '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
        }
        newServerSelectHTML += '</select>';

        // Adding the new select to div
        selectBoxEl.innerHTML = newServerSelectHTML;

        if (!isServerExists(serverListArray, serverID)) {
            serverID = "";
            prefs.set("serverID", serverID);
        }
        tabs.setSelectedTab(0);
        drawDiagram();
    });
}

function loadSequences() {
    var seqListArray = getSequenceList().split("&");

    var newEndpointSelectHTML = "";
    newEndpointSelectHTML += '<option value="">--Sequence--</option>';

    for (var x = 0; x < seqListArray.length; x++) {
        newEndpointSelectHTML +=
        '<option value="' + seqListArray[x] + '">' + seqListArray[x] + '</option>';
    }

    $("#sequence").html(newEndpointSelectHTML);
}

function getSequenceList() {
    var selectedServerID = document.getElementById('selectServerCategory').value;

    if (!(selectedServerID == "No Servers Configured")) {
        var xmlHttpReq = createXmlHttpRequest();

        // Make sure the XMLHttpRequest object was instantiated
        if (xmlHttpReq)
        {
            // This is a synchronous POST, hence UI blocking.
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getSequences&serverID=" +
                    selectedServerID + "&ms=" +
                    new Date().getTime(), false);
            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }
    return false;
}

function refreshSequenceData() {
    serverID =
            document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].value;
    serverURL =
            document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].text;
    sequenceID =
            document.getElementById('sequences')[document.getElementById('sequences').selectedIndex].value;
    sequenceName =
            document.getElementById('sequences')[document.getElementById('sequences').selectedIndex].text;

    if ((serverID != "") && (serverID != "No Servers Configured") &&
            (sequenceID != "")) {
        prefs.set("serverID", serverID);
        prefs.set("serverURL", serverURL);
        prefs.set("sequenceID", sequenceID);
        prefs.set("sequenceName", sequenceName);
    }
    tabs.setSelectedTab(0);
    drawDiagram();
}

function getLatestInCumulativeCountForSequence(serverID, sequenceName, callback) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is an asynchronous POST
        xmlHttpReq.onreadystatechange = function() {
            if (xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };

        // This is an asynchronous POST
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInCumulativeCountForSequence&serverID=" +
                serverID + "&sequenceName=" + sequenceName + "&ms=" + new Date().getTime(), true);
        xmlHttpReq.send(null);
    }
}

function getLatestInFaultCountForSequence(serverID, sequenceName, callback) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is an asynchronous POST
        xmlHttpReq.onreadystatechange = function() {
            if (xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };
        // This is an asynchronous POST
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInFaultCountForSequence&serverID=" +
                serverID + "&sequenceName=" + sequenceName + "&ms=" + new Date().getTime(), true);
        xmlHttpReq.send(null);
    }
}

function refreshProxyData() {
    serverID =
            document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].value;
    serverURL =
            document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].text;
    proxyID =
            document.getElementById('proxys')[document.getElementById('proxys').selectedIndex].value;
    proxyName =
            document.getElementById('proxys')[document.getElementById('proxys').selectedIndex].text;

    if ((serverID != "") && (serverID != "No Servers Configured") &&
            (proxyID != "")) {
        prefs.set("serverID", serverID);
        prefs.set("serverURL", serverURL);
        prefs.set("proxyID", proxyID);
        prefs.set("proxyName", proxyName);
    }
    tabs.setSelectedTab(0);
    drawDiagram();
}

function getLatestInCumulativeCountForProxy(serverID, proxyName, callback) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is an asynchronous POST
        xmlHttpReq.onreadystatechange = function() {
            if(xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };

        // This is an asynchronous POST.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInCumulativeCountForProxy&serverID=" +
                               serverID + "&proxyName=" + proxyName + "&ms=" + new Date().getTime(), true);
        xmlHttpReq.send(null);
    }
}

function getLatestInFaultCountForProxy(serverID, proxyName,callback) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {

        // This is an asynchronous POST
        xmlHttpReq.onreadystatechange = function() {
            if(xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };

        // This is an asynchronous POST
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInFaultCountForProxy&serverID=" +
                               serverID + "&proxyName=" + proxyName + "&ms=" + new Date().getTime(), true);
        xmlHttpReq.send(null);

    }
}

//////////////////////////////////////////// added  activity

function getLatestMaximumOperationsForAnActivityID(activityID) {
    var xmlHttpReq = createXmlHttpRequest();
    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestMaximumOperationsForAnActivityID&activityID=" + activityID, false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}


function loadActivities() {
    var activityListArray = getActivityList().split("|");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("activity_select_box");
    selectBoxEl.innerHTML = "";

    var newServerSelectHTML = '<select id="activities" onchange="refreshActivityData();"><option value="">--Activity--</option>';
    for (var x = 0; x < activityListArray.length; x++) {
        var _tokens = activityListArray[x].split(",");
        if (_tokens[1] != "DefaultActivity" && _tokens[0] != "") {
               newServerSelectHTML +=
                '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
            }        


    }
    newServerSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newServerSelectHTML;

    tabs.setSelectedTab(0);
    drawDiagram();
}


function getActivityList() {
    var xmlHttpReq = createXmlHttpRequest();
    var activityList = [];
    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getActivityList" +
                "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function isActivityExists(activityListArray, activityID) {
    for (var x = 0; x < activityListArray.length; x++) {
        if (activityListArray[x] == activityID) {
            return true
        }
    }

    return false;
}


function refreshActivityData() {

    activityID =
            document.getElementById('activities')[document.getElementById('activities').selectedIndex].value;
    activityName =
            document.getElementById('activities')[document.getElementById('activities').selectedIndex].text;

    if (activityID != "" && (activityID != "No Activities Configured")) {
        prefs.set("activityID", activityID);
        prefs.set("activityName", activityName);
    }

    tabs.setSelectedTab(0);
    drawDiagram();
}


// Operations for a given Service

function getOperationsOfService(serverID, serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {

        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getOperationsOfService&serverID=" +
                serverID + "&serviceID=" + serviceID + "&ms=" + new Date().getTime(), false);

        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getServerWithData(functionName, callback) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a async POST, hence does NOT block UI.
        xmlHttpReq.onreadystatechange = function() {
            if(xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?" +
                "funcName=getServerWithData&function=" + functionName, true);
        xmlHttpReq.send(null);
    }
}

// Add for the activity data retrieve

function getActivityServers() {
    var selectedStartTime = document.getElementById("startTime");
    var selectedEndTime = document.getElementById("endTime");

    if (selectedStartTime.value != null && selectedEndTime.value != null) {

        var xmlHttpReq = createXmlHttpRequest();

        if (xmlHttpReq) {
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getAllIpAddressForTimeRange&startTime=" +
                    selectedStartTime.value + "&endTime=" + selectedEndTime.value, false);

            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }

}

function loadActivityServers() {
    var testServer = getActivityServers();
    var serviceListArray = testServer.split("|");
    if (serviceListArray != null && serviceListArray.length > 0) {
        document.getElementById("server_select_box").style.display = "";
    }

    var selectBoxEl = document.getElementById("server_select_box");
    selectBoxEl.innerHTML = "";

    var newServerSelectHTML = '<select id="activity_server" onchange="loadAllActivitiesForServer();"><option value="">--Server--</option>';
    for (var x = 0; x < serviceListArray.length; x++) {
        newServerSelectHTML +=
                '<option value="' + serviceListArray[x] + '">' + serviceListArray[x] + '</option>';
    }
    newServerSelectHTML += '</select>';
    selectBoxEl.innerHTML = newServerSelectHTML;

}

function getAllActivitiesForServer() {

    var selectedStartTime = document.getElementById("startTime");
    var selectedEndTime = document.getElementById("endTime");
    var selectedServer = document.getElementById("activity_server");

    if (selectedStartTime.value != null && selectedEndTime.value != null && selectedServer.value != null) {
        var xmlHttpReq = createXmlHttpRequest();

        if (xmlHttpReq) {
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getAllActivityForTimeRange&startTime=" +
                    selectedStartTime.value + "&endTime=" + selectedEndTime.value + "&ipAddress=" + selectedServer.value, false);

            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }


}

function loadAllActivitiesForServer() {
    var activityDetailsArray = getAllActivitiesForServer().split("|");

    if (activityDetailsArray != null && activityDetailsArray.length > 0) {
        document.getElementById("activity_select_box").style.display = "";
    }
    var selectBoxEl = document.getElementById("activity_select_box");
    selectBoxEl.innerHTML = "";

    var newActivitySelectHTML = '<select id="activity_id_selected" onchange="loadAllActivityOperation();"><option value="">--Activity--</option>';
    for (var x = 0; x < activityDetailsArray.length; x++) {
        var _activityId = activityDetailsArray[x].split(",");
        newActivitySelectHTML +=
                '<option value="' + _activityId[0] + '">' + _activityId[1] + '</option>';
    }
    newActivitySelectHTML += '</select>';

    selectBoxEl.innerHTML = newActivitySelectHTML;

}

function getAllOperationForActivity() {

    var selectedStartTime = document.getElementById("startTime");
    var selectedEndTime = document.getElementById("endTime");
    var selectedServer = document.getElementById("activity_server");
    var selected_activity = document.getElementById("activity_id_selected");
    var selected_activity_Id = selected_activity[selected_activity.selectedIndex].value;
    // var selectName = activity_id_selected[activity_id_selected.selectedIndex].innerHTML;

    if (selectedStartTime.value != null && selectedEndTime.value != null && selectedServer.value != null && selected_activity_Id != null) {
        var xmlHttpReq = createXmlHttpRequest();

        if (xmlHttpReq) {
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getAllOperationsForTimeRange&startTime=" +
                    selectedStartTime.value + "&endTime=" + selectedEndTime.value + "&ipAddress=" + selectedServer.value + "&activityId=" + selected_activity_Id, false);

            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }

}

function loadAllActivityOperation() {
    var selectedOpArray = getAllOperationForActivity().split("|");
    if (selectedOpArray != null && selectedOpArray.length > 0) {
        document.getElementById("operation_select_box").style.display = "";
    }

    var selectBoxEl = document.getElementById("operation_select_box");
    selectBoxEl.innerHTML = "";

    var newOperationSelectHTML = '<select id="operation_id_selected" onchange="loadAllActivityMessages();"><option value="">--Operation--</option>';
    for (var x = 0; x < selectedOpArray.length; x++) {
        var _operationId = selectedOpArray[x].split(",");
        newOperationSelectHTML +=
                '<option value="' + _operationId[0] + '">' + _operationId[1] + '</option>';
    }
    newOperationSelectHTML += '</select>';

    selectBoxEl.innerHTML = newOperationSelectHTML;
}

function getAllActivityMessages() {
    var selectedStartTime = document.getElementById("startTime");
    var selectedEndTime = document.getElementById("endTime");
    var selectedServer = document.getElementById("activity_server");
    var selected_activity = document.getElementById("activity_id_selected");
    var selected_activity_Id = selected_activity[selected_activity.selectedIndex].value;

    var selected_operation = document.getElementById("operation_id_selected");
    var selected_operation_Id = selected_operation[selected_operation.selectedIndex].value;

    if (selectedStartTime.value != null && selectedEndTime.value != null && selectedServer.value != null && selected_activity_Id != null && selected_operation_Id != null) {
        var xmlHttpReq = createXmlHttpRequest();

        if (xmlHttpReq) {
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getAllMessagesForTimeRange&startTime=" +
                    selectedStartTime.value + "&endTime=" + selectedEndTime.value + "&ipAddress=" + selectedServer.value + "&activityId=" +
                    selected_activity_Id + "&opId=" + selected_operation_Id, false);

            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }
}

function loadAllActivityMessages() {
    var selectedMsgArray = getAllActivityMessages().split("|");

    if (selectedMsgArray != null && selectedMsgArray.length > 0) {
        document.getElementById("activity_msg_table_div").style.display = "";
    }
    var selectMsgTableEl = document.getElementById("activity_msg_table_div");
    selectMsgTableEl.innerHTML = "";

    var newMsgTableHTML = '<table id="activity_msg_table">';
    for (var x = 0; x < selectedMsgArray.length; x++) {
        newMsgTableHTML += '<tr>' + '<td>' + selectedMsgArray[x] + '</td>' + '</tr>';
    }
    newMsgTableHTML += '</table>';


    selectMsgTableEl.innerHTML = newMsgTableHTML;


}

function loadActivityMonitoringServer() {
    getServerList(function(monitorServerArray) {
        monitorServerArray = monitorServerArray.split("|");
        // Cleaning up the existing select box
        var selectBoxEl = document.getElementById("activity_server_select_box");
        selectBoxEl.innerHTML = "";

        var newActivityServerSelectHTML = '<select id="activity_server_Id" onchange="loadActivityServices();"><option value="">--Server--</option>';
        for (var x = 0; x < monitorServerArray.length; x++) {
            var _tokens = monitorServerArray[x].split(",");
            newActivityServerSelectHTML +=
                    '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
        }
        newActivityServerSelectHTML += '</select>';

        // Adding the new select to div
        selectBoxEl.innerHTML = newActivityServerSelectHTML;
    });

}

function getActivityServiceList() {
    var selectedServerID = document.getElementById('activity_server_Id')[document.getElementById('activity_server_Id').selectedIndex].value;

    if (!(selectedServerID == "No Servers Configured")) {
        var xmlHttpReq = createXmlHttpRequest();

        // Make sure the XMLHttpRequest object was instantiated
        if (xmlHttpReq)
        {
            // This is a synchronous POST, hence UI blocking.
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getServicesList&serverID=" +
                    selectedServerID, false);
            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }
    return false;
}

function loadActivityServices() {
    var serviceListArray = getActivityServiceList().split("|");

    if(serviceListArray !=null && serviceListArray.length >0){
        document.getElementById("activity_service_select_box").style.display = "";

    }

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("activity_service_select_box");
    selectBoxEl.innerHTML = "";

    var newServiceSelectHTML = '<select id="activity_service_id" onchange="loadAllActivityOperations();"><option value="">--Service--</option>';
    for (var x = 0; x < serviceListArray.length; x++) {
        var _tokens = serviceListArray[x].split(",");
        newServiceSelectHTML +=
                '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
    }
    newServiceSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newServiceSelectHTML;
}


function loadAllActivityOperations() {

    if(document.getElementById('activity_service_id')!=null){


    var selectedServiceID = document.getElementById('activity_service_id')[document.getElementById('activity_service_id').selectedIndex].value;

    var operationDataArray = getActivityOperationsOfService(selectedServiceID).split("|");
    if(operationDataArray !=null && operationDataArray.length >0){
       document.getElementById("activity_operation_select_box").style.display = "";
    }

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("activity_operation_select_box");
    selectBoxEl.innerHTML = "";

    var newOperationSelectHTML = '<select id="activity_operation_id" onchange="loadTimeRangeSelector();"><option value="">--Operations--</option>';
    for (var x = 0; x < operationDataArray.length; x++) {
        var _tokens = operationDataArray[x].split(",");
        newOperationSelectHTML +=
                '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
    }
    newOperationSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newOperationSelectHTML;
    }
}

function getActivityOperationsOfService(serverID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {

        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getActivityOperationsOfService&serverID=" +
                serverID, false);

        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;

}

function getAllActivityDetails(){
    var selectedStartTime = document.getElementById("startTime");
    var selectedEndTime = document.getElementById("endTime");
    var selectedOpId = document.getElementById("activity_operation_id")[document.getElementById("activity_operation_id").selectedIndex].value;

    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {

        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getAllActivityDataForTimeRangeAndOperation&startTime=" +
                    selectedStartTime.value+" 00:00:00.0" + "&endTime=" + selectedEndTime.value+" 00:00:00.0" + "&opId=" + selectedOpId, false);

        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function loadAllActivityDetails(){
  var activityDataArray =   getAllActivityDetails().split("|");
    if(activityDataArray !=null && activityDataArray.length >0){
      document.getElementById("activity_select_box").style.display = "";
    }

    var selectBoxEl = document.getElementById("activity_select_box");
    selectBoxEl.innerHTML = "";

    var newActivitySelectHTML = '<select id="activity_selected_id" onchange="loadFinalActivityMessages();"><option value="">--Service--</option>';
    for (var x = 0; x < activityDataArray.length; x++) {
        var _tokens = activityDataArray[x].split(",");
        newActivitySelectHTML +=
                '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
    }
    newActivitySelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newActivitySelectHTML;
}

function getFinalActivityMessages(){
    var selectedStartTime = document.getElementById("startTime");
    var selectedEndTime = document.getElementById("endTime");
    var selectedActivityId= document.getElementById("activity_selected_id")[document.getElementById("activity_selected_id").selectedIndex].value;

    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {

        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getAllActivityMessagesForTimeRange&startTime=" +
                    selectedStartTime.value+" 00:00:00.0" + "&endTime=" + selectedEndTime.value+" 00:00:00.0" + "&activityId=" + selectedActivityId, false);

        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;

}

function loadFinalActivityMessages(){
  var msgArray = getFinalActivityMessages().split("|")

   
      if (msgArray != null && msgArray.length > 0) {
        document.getElementById("activity_msg_table_div").style.display = "";
    }
    var selectMsgTableEl = document.getElementById("activity_msg_table_div");
    selectMsgTableEl.innerHTML = "";

    var newMsgTableHTML = '<table id="activity_msg_table">';
    for (var x = 0; x < msgArray.length; x++) {
        newMsgTableHTML += '<tr>' + '<td   id="' + x + '">' + msgArray[x] + '</td>' + '</tr>';
    }
    newMsgTableHTML += '</table>';


    selectMsgTableEl.innerHTML = newMsgTableHTML;
}

function loadTimeRangeSelector() {
    var selectedActivityId = document.getElementById("activity_operation_id")[document.getElementById("activity_operation_id").selectedIndex].value;
    if (selectedActivityId > 0) {
        document.getElementById("activity_time_range_selector").style.display = "";
    }
}

function getAllActivityForTimstampGadget() {
    var selectedStartTime = document.getElementById("startTime");
    var selectedEndTime = document.getElementById("endTime");

    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {

        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getAllActivityDataForTimeRange&startTime=" +
                selectedStartTime.value + " 00:00:00.0" + "&endTime=" + selectedEndTime.value +" 00:00:00.0", false);

        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function loadAllActivityForTimeStampGadget() {

    var selectBoxEl = document.getElementById("activity_select_box");
    selectBoxEl.innerHTML = "";
    var activityDataArray = getAllActivityForTimstampGadget().split("|");
    
    if (activityDataArray != null && activityDataArray.length > 0 && activityDataArray[0] != "") {
        document.getElementById("activity_select_box").style.display = "";

        var newActivitySelectHTML = '<select id="activity_selected_id" onchange="changeTabTest();"><option value="">--Activity--</option>';
        for (var x = 0; x < activityDataArray.length; x++) {
            var _tokens = activityDataArray[x].split(",");
            if (_tokens[1] != "DefaultActivity" && _tokens[0] != "") {
                newActivitySelectHTML +=
                        '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
            }
        }
        newActivitySelectHTML += '</select>';
        selectBoxEl.innerHTML = newActivitySelectHTML;
    } else {
        document.getElementById("activity_select_box").style.display = "";
        selectBoxEl.innerHTML = '<select id="activity_selected_id"><option value="0">--No Activity For Time Range--</option></select>';
        var msgBoxEl = document.getElementById("activity_msg_table_div");
        msgBoxEl.innerHTML = "";
        msgBoxEl.style.display = "";
    }

}

function getAllMessagesForTimeStampGadget(callback){
    var selectedStartTime = document.getElementById("startTime");
    var selectedEndTime = document.getElementById("endTime");
     var selectedActivityId= document.getElementById("activity_selected_id")[document.getElementById("activity_selected_id").selectedIndex].value;

    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq) {
        xmlHttpReq.onreadystatechange = function() {
            if (xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
                callback(removeCarriageReturns(xmlHttpReq.responseText));
            }
        };
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getAllMessagesForTimeRangeAndActivity&startTime=" +
                selectedStartTime.value + " 00:00:00.0" + "&endTime=" + selectedEndTime.value + " 00:00:00.0" + "&activityId=" + selectedActivityId, true);
        xmlHttpReq.send(null);
    }
}

function loadAllMessagesForTimeStampGadget(callback) {
    getAllMessagesForTimeStampGadget(function(msgArray) {
        msgArray = msgArray.split("|");
        if (msgArray != null && msgArray.length > 0) {
            document.getElementById("activity_msg_table_div").style.display = "";
        }
        var selectMsgTableEl = document.getElementById("activity_msg_table_div");
        selectMsgTableEl.innerHTML = "";

        var newMsgTableHTML = '<table id="activity_msg_table">';
        for (var x = 0; x < msgArray.length; x++) {
            if (msgArray[x] != "") {
                newMsgTableHTML += '<tr>' + '<td   id="' + x + '">' + msgArray[x] + '</td>' + '</tr>';
            }
        }
        newMsgTableHTML += '</table>';
        selectMsgTableEl.innerHTML = newMsgTableHTML;
        callback();
    });
}

function changeTabTest(){
     tabs.setSelectedTab(0);
}



