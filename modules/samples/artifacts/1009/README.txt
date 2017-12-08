1. Copy {WSO2SPHome}/samples/artifacts/1009/TensorFlowTestApp.siddhi file to {WSO2_SP_Home}/wso2/editor/deployment/workspace

2. Open the copied file TensorFlowTestApp.siddhi in WSO2_SP_Home}/wso2/editor/deployment/workspace and observe the query. You can find the first parameter as '/home/niruhan/siddhi-execution-tensorflow/component/src/test/resources/TensorFlowModels/Regression' which gives the absolute path to the regression TensorFlow model. Change it to the absolute path of the folder {WSO2SPHome}/samples/artifacts/1009/Regression on your machine

3. Start the editor using ./{WSO2SPHome}/bin/editor.sh

4. Go to the event simulator in the editor (Stream Processor Studio) and find the TensorFlowTestApp under Siddhi App Name drop down

5. Select 'InputStream' in the Stream Name drop down.

6. Leave the Timestamp empty and paste "double:[1,-2]" in the text box under x (String)

7. Now start the Siddhi App by clicking start and send the event. You can observe the output in the console.
