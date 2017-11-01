1. Copy {WSO2SPHome}/samples/artifacts/0053/resources folder to {WSO2_SP_Home}/wso2/dashboard directory.
2. You need to start both worker and dashboard runtimes in order to deploy business rules. To do that, first set an offset to the worker. Follow the steps given below.

 - Open {WSO2_SP_Home}/wso2/worker/conf/transports/netty-transports.yml and update the existing ports 9090 and 9443, to 9091 and 9444 respectively.
 - Then open {WSO2_SP_Home}/conf/worker/deployment.yaml and under wso2.carbon, set offset to 1.

3. Add following deployment configurations for ruleTemplates to {WSO2_SP_Home}/conf/dashboard/deployment.yaml.

  nodes:
  - 0.0.0.0:9091:
    - stock-data-analysis
    - stock-exchange-input
    - stock-exchange-output

4. Start both worker and dashboard runtimes by running {WSO2SPHome}/bin/worker.sh and {WSO2SPHome}/bin/dashboard.sh respectively.

5. Open Business Rules Manager in the web browser through the URL, http://localhost:9090/business-rules

6. Existing business rules (if available) and a create button will be listed. Click the Create button

7. Choose the option to create a Business Rule either from template or from scratch.

8. Available TemplateGroups will be listed.

9. Select a template group

10. Select Rule Template(s) Create a business rule from through the UI.

11. Try to save and deploy them. When deploying, respective siddhi apps will be deployed on worker node and status will be logged to the console.
