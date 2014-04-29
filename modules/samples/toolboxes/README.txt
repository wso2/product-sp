NOTE: This directory contains toolboxes required for various samples and use cases. 
Please take a look at other directories for instructions on using these toolboxes 
for a specific sample.

=================  BAM Toolboxes ================
A toolbox is an installable archive, with a .tbox extension. A toolbox will contains necessary artifacts that models a complete use case. A toolbox can contain the below artifacts

    -Stream definitions
    -Analytics
    -Dashboard components


Installing Toolbox
-------------------
1) Installing Default Toolbox

    - Start WSO2 BAM server
    - Go to management console and login
    - Go to Main -> BAM ToolBox -> Add
    - Select an option which are listed under 'Basic Toolbox'. Eg: Phone Retail Store Toolbox, HTTPD Logs Analysis Toolbox
    - Click on Install Button
	(Wait for approximately 1mintue and refresh the page, then you can notice the status of the toolbox  status has changed to 'Installed' state.)
    - You can notice the toolbox status to be installed.

2) Installing Custom Toolbox

    - Start WSO2 BAM server
    - Go to management console and login
    - Go to Main -> BAM Toolbox -> Add
    - Select 'Toolbox From File System' if you have the toolbox in your system OR Select 'ToolBox From URL ' if you have the toolbox in hosted in a location.
    - And provide the necessary toolbox file path OR URL path depending on your selection in step-4.
    - Click on Install Button.
    - This will redirect to 'List' page of BAM toolboxes. There you can notice that the selected toolbox will be in the status of 'installing'.
    - Wait for approximately 1mintue and refresh the page, then you can notice the status of the toolbox  status has changed to 'Installed' state.


Uninstalling Toolbox
---------------------
    - Start WSO2 BAM server
    - Go to management console and login
    - Go to Main -> BAM Toolbox - > List
    - Select the toolboxes which you are going to uninstall by selecting the relevant check boxes of toolbox
    - Click on 'Uninstall' icon
    - Click 'yes' on the confirmation dialog box which asks for uninstalling the toolbox
    - There you can notice that the selected toolbox(s) will be in the status of 'uninstalling'
    - Wait for approximately 1mintue and refresh the page, then you can notice the toolbox(s) have been removed from list of toolboxes.

For more information on Toolboxes visit to our documentation on http://docs.wso2.org/wiki/display/BAM240/Toolboxes

