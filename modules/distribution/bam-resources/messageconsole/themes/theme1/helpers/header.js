var getNavigation = function(current){

    var links = [{
        link : "Messages",
        URL: "message-search.jag"

    }, {
        link : "Activities",
        URL: "activity-search.jag",
    }];

    for(var i in links){
        if(links[i].link === current){
            links[i].active = true;
            break;
        }
    }
    return {
        links:links
    }
}
