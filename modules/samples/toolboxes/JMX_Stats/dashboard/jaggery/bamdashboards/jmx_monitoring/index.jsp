<!DOCTYPE html>
<html lang="en">
<head>
    <title>Business Activity Monitor Dashboard</title>
    <link href='http://fonts.googleapis.com/css?family=Open+Sans' rel='stylesheet' type='text/css'>
    <link href="../resources/css/bootstrap.css" rel="stylesheet">
    <link href="../resources/css/bootstrap-theme.css" rel="stylesheet">
    <link href="../resources/css/bootstrap-missing.css" rel="stylesheet">
    <style>
        body {
            padding-top: 50px;
            padding-bottom: 20px;
        }
    </style>
    <link rel="stylesheet" href="../resources/css/main.css">
    <link rel="stylesheet" href="../resources/font-awesome/css/font-awesome.min.css">


    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <style type="text/css">
        body {padding-top: 60px;
            padding-bottom: 40px;
        }
        .sidebar-nav {
            padding: 9px 0;
        }
    </style>
</head>
<body>


<!--[if lt IE 7]>
<p class="chromeframe">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade
    your browser</a> or <a href="http://www.google.com/chromeframe/?redirect=true">activate Google Chrome Frame</a> to
    improve your experience.</p>
<![endif]-->


<!-- Part 1: Wrap all page content here -->
<div id="wrap">
<div class="navbar navbar-inverse navbar-fixed-top main-menu">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="../../../carbon"><img src="../resources/img/bam-logo.png"/>
            </a>
        </div>
        <div class="navbar-collapse collapse main-menu-inside">
            <ul class="nav navbar-nav pull-left menu1" id="leftnav"></ul>
        </div>
        <!--/.navbar-collapse -->
    </div>
    <div class="breadcrumb-strip">
        <div class="container">
            <!-- Example row of columns -->
            <div class="row">
                <div class="col-lg-12">

                    <ul class="breadcrumb pull-left">
                        <li><a href="../../../carbon">Carbon Console</a> <span class="divider"></span></li>
                        <li class="active">JMX Statistics</li>
                    </ul>
                    <!--form class="form-search pull-right margin-remover header-form">
                        <input type="text" class="input-medium search-query" placeholder="Activity ID">
                    </form -->

                </div>
            </div>
        </div>
    </div>
</div>

<div class="container content-starter">
    <div class="row">
        <div class="col-lg-12">

        </div>
    </div>
</div>
<div class="container">
    <div class="row">
        <div class="col-lg-12">
            <h1>JMX Statistics</h1>

            <div class="container content-section">
                <div class="row">
                    <div class="col-lg-12">
                        <div class="well topFilteringPanel">
                            <span class="span3">Select Server :
                                <select id="server-dd" name="basic-combo">
                                    <option value="__default__"></option>
                                </select>
                            </span>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-lg-12">
                        <div class="navbar timelySwitch" style="overflow: hidden;">
                            <div id="timely-dd" class="btn-group timely-dd-btns">
                                <button class="btn">Month</button>
                                <button class="btn">Day</button>
                                <button class="btn">Hour</button>
                                <button class="btn btn-primary">Now</button>
                            </div>
                        </div>
                    </div>
                </div>


                <!-- gadget iframes -->
                <div class="row">
                    <div class="col-lg-12">
                    <iframe id="dashboardWidget-1" class="single-column-gadget" src="gadgets/heapMem.jag?t=Now"></iframe>
                </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <iframe id="dashboardWidget-2" src="gadgets/nonHeapMem.jag?t=Now" class="single-column-gadget"></iframe>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <iframe id="dashboardWidget-3" src="gadgets/cpuUsage.jag?t=Now" class="single-column-gadget"></iframe>
                    </div><!--/span-->
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <iframe id="dashboardWidget-4" src="gadgets/threads.jag?t=Now" class="single-column-gadget"></iframe>
                    </div><!--/span-->
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <iframe id="dashboardWidget-5" src="gadgets/classes.jag?t=Now" class="single-column-gadget"></iframe>
                    </div><!--/span-->
                </div><!--/row-->




            </div>
        </div>

    </div>
    <!-- /container -->
    <div id="push"></div>
</div>

<footer id="footer">
    <div class="container">
        <p class="muted credit">&copy; WSO2 2013</p>
    </div>
</footer>

<input type="hidden" id="resource_type" value="Endpoint"/>





<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="../resources/js/vendor/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="scripts/bam.dashboard.main.js"></script>
    <script type="text/javascript">
        var currentTab = "JMX Statistics";
    </script>
    <script type="text/javascript" src="../navigation.populator.js"></script>
</body>
</html>

