<!DOCTYPE html>
<html lang="en">
<head>
<title>Business Activity Monitor Dashboard</title>
<link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap-responsive.min.css" />
<link rel="stylesheet" type="text/css" href="css/bam-dashboard-common-styles.css" />
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


<div class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container-fluid">
      <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </a>

      <a class="brand" href="#"><img src="images/bamlogo.png" alt=""><span style="color: #B4D8FF;margin: 0 10px;">Dashboard</span></a>
      <!--<div class="btn-group pull-right">
        <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
          <i class="icon-user"></i> Username
          <span class="caret"></span>
        </a>
        <ul class="dropdown-menu">
          <li><a href="#">Profile</a></li>
          <li class="divider"></li>
          <li><a href="#">Sign Out</a></li>
        </ul>
      </div>
      <div class="nav-collapse">
      </div>--><!--/.nav-collapse -->
    </div>
  </div>
</div>

<div class="container-fluid">
  <div class="row-fluid">
    <div class="span2">
      <div class="well sidebar-nav">
        <ul id="leftnav" class="nav nav-list">
          <li class="nav-header">Tool Boxes</li>
          
          <!--<li><a href="#">ESB Activity Statistics</a></li>
          <li class="nav-header">Custom Dashboards</li>
          <li><a href="#" style="color: #cccccc;cursor:default">AS -Overview</a></li>
          <li><a href="#" style="color: #cccccc;cursor:default">ESB - Overview</a></li>-->
        </ul>
      </div><!--/.well -->
    </div><!--/span-->
    <div class="span10">
      <div class="well topFilteringPanel"><span class="span3"><nobr>Select Server :
          <select id="server-dd" name="basic-combo">
                <option value="__default__"></option>
		</select></nobr></span>
          &nbsp;&nbsp;&nbsp;<button id="clearSelectionBtn" class="btn btn-primary btn-small filter-btn">Clear</button>
      </div>
          <!--<ul class="breadcrumb">
            <li>
            <a href="#">Home</a> <span class="divider">/</span>
            </li>
            <li>
            <a href="#">Library</a> <span class="divider">/</span>
            </li>
            <li class="active">Data</li>
          </ul>-->
      <div class="navbar timelySwitch" style="overflow: hidden;">
          <div id="timely-dd" class="btn-group timely-dd-btns">
            <button class="btn">All</button>
            <button class="btn">Month</button>
            <button class="btn">Day</button>
		<button class="btn btn-primary">Now</button>
          </div>
      </div>
      <div class="clearfix"></div>
       <!-- gadget iframes -->
      <div class="hero-unit">
        <iframe id="dashboardWidget-1" class="single-column-gadget" src="gadgets/mult-line-dashboard-chart.jag?t=Now&rtype=Overview"></iframe>
      </div>
      <div class="row-fluid">
        <div class="hero-unit">
          <iframe id="dashboardWidget-2" src="gadgets/simple.bar.chart.jag?t=Now&rtype=Overview" class="single-column-gadget"></iframe>
        </div>
      </div>
      <div class="row-fluid">
        <div class="span4">
          <iframe id="dashboardWidget-3" src="gadgets/gauge_InDirection.jag?t=Now&rtype=Overview" class="gadget-small"></iframe>
        </div><!--/span-->
        <div class="span4">
          <iframe id="dashboardWidget-4" src="gadgets/gauge_OutDirection.jag?t=Now&rtype=Overview" class="gadget-small gadget-larger-width"></iframe>
        </div><!--/span-->

      </div><!--/row-->
	<div class="row-fluid">
        
          <iframe id="dashboardWidget-5" src="gadgets/simple.pie.chart.jag?t=Now&rtype=Overview" class="bottom-row-single-column-gadget"></iframe>
        </div>
      
    </div><!--/span-->
  </div><!--/row-->
  <hr>

  <footer>
    <p class="wso2copyRight_footer">&copy; <a href="http://wso2.org">WSO2</a> Inc</p>
    <span class="wso2Logo_footer">
        <a class="wso2" target="_blank" href="http://www.wso2.com"></a>
    </span>
  </footer>

</div><!--/.fluid-container-->
<input type="hidden" id="resource_type" value="Overview" />
<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script type="text/javascript" src="scripts/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="scripts/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="bootstrap/js/bootstrap.js"></script>
<script type="text/javascript" src="../navigation.populator.js"></script>
</body>
</html>
