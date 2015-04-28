(function () {
    ues.dashboards.render($("#wrapper"), {
        "id": "123456",
        "pages": {
            "landing": {
                "title": "My Dashboard",
                "layout": {
                    "id": "grid_1",
                    "title": "Grid Layout",
                    "description": "This is a sample grid",
                    "thumbnail": "https://localhost:9443/dashboards/layouts/layout-1/index.jpg",
                    "url": "https://localhost:9443/designer/store/layout/layout-1/index.hbs",
                    "content": "<div class=\"row\"><div id=\"a\" class=\"col-md-3 ues-widget-box\"></div><div id=\"b\" class=\"col-md-6 ues-widget-box\"></div><div id=\"c\" class=\"col-md-3 ues-widget-box\"></div></div><div class=\"row\"><div id=\"d\" class=\"col-md-6 ues-widget-box\"></div><div id=\"e\" class=\"col-md-6 ues-widget-box\"></div></div><div class=\"row\"><div id=\"f\" class=\"col-md-4 ues-widget-box\"></div><div id=\"g\" class=\"col-md-4 ues-widget-box\"></div><div id=\"h\" class=\"col-md-4 ues-widget-box\"></div></div>"
                },
                "content": {
                    "a": [{
                        "id": "9zt9k74854",
                        "content": {
                            "id": "g1",
                            "title": "G1",
                            "type": "gadget",
                            "thumbnail": "https://localhost:9443/dashboards/widgets/usa-map/index.png",
                            "options": {
                                "username": {
                                    "type": "string",
                                    "description": "Username to be used for service invocation"
                                }
                            },
                            "data": {
                                "url": "https://localhost:9443/designer/store/gadget/g1/index.xml"
                            },
                            "description": "Allows to view and select US states",
                            "notify": {
                                "select": {
                                    "type": "address",
                                    "description": "This notifies selected state"
                                },
                                "cancel": {
                                    "type": "boolean",
                                    "description": "This notifies cancellation of state selection"
                                }
                            }
                        }
                    }],
                    "b": [{
                        "id": "rujw5jwm3i",
                        "content": {
                            "id": "g2",
                            "title": "G2",
                            "type": "gadget",
                            "thumbnail": "https://localhost:9443/dashboards/assets/gadgets/usa-business-revenue/index.png",
                            "options": {
                                "username": {
                                    "type": "string",
                                    "description": "Username to be used for service invocation"
                                }
                            },
                            "data": {
                                "url": "https://localhost:9443/designer/store/gadget/g2/index.xml"
                            },
                            "description": "Allows to view revenue by companies in US",
                            "listen": {
                                "state-selected": {
                                    "type": "address",
                                    "description": "Used to filter based on state",
                                    "on": [{
                                        "event": "client-country",
                                        "from": "pujw6jwm3t"
                                    }, {
                                        "event": "user-country",
                                        "from": "pujw6jwm3t"
                                    }, {
                                        "event": "select",
                                        "from": "9zt9k74854"
                                    }]
                                }
                            },
                            "notify": {
                                "select": {
                                    "type": "string",
                                    "description": "This notifies selected company"
                                },
                                "cancel": {
                                    "type": "boolean",
                                    "description": "This notifies cancellation of company selection"
                                }
                            }
                        }
                    }],
                    "c": [{
                        "id": "pujw6jwm3t",
                        "content": {
                            "id": "g3",
                            "title": "G2",
                            "type": "widget",
                            "thumbnail": "https://localhost:9443/dashboards/assets/gadgets/usa-business-revenue/index.png",
                            "options": {
                                "username": {
                                    "type": "string",
                                    "description": "Username to be used for service invocation"
                                }
                            },
                            "data": {
                                "url": "https://localhost:9443/designer/store/gadget/g2/index.xml"
                            },
                            "description": "Allows to view revenue by companies in US",
                            "listen": {
                                "state": {
                                    "type": "address",
                                    "description": "Used to filter based on state",
                                    "on": [{
                                        "event": "select",
                                        "from": "9zt9k74854"
                                    }]
                                }
                            },
                            "notify": {
                                "user-country": {
                                    "type": "country-code",
                                    "description": "This notifies selected country"
                                }
                            }
                        }
                    }]
                }
            }
        }
    }, 'landing', function () {
        console.log("page rendered");
    });
}());