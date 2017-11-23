1. If the sample is not running for the first time, do as following first.
    - Move all the files in {WSO2SPHome}/samples/artifacts/0038/files/consumed directory to
      {WSO2SPHome}/samples/artifacts/0038/files/new.
    - Delete all the files in consumed and sink directories.

After moving those file,

2. Edit '{WSO2SPHome}/samples/artifacts/0038/siddhi-io-file-sample.siddhi' file by replacing {WSO2SPHome} with the
      absolute path of your WSO2SP home directory.

3. Copy {WSO2SPHome}/samples/artifacts/0038/siddhi-io-file-sample.siddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

4. Start the worker using ./{WSO2SPHome}/bin/worker.sh

5. siddhi-io-file-sample.siddhi will be automatically deployed.

6. Check the console for events which were published to files also.

7. Check the directories {WSO2SPHome}/samples/artifacts/0038/files/consumed, new and sink.

8. All the files which were in the directory 'new' should have been moved to 'consumed' directory.
   Also the retrieved events should be published to the directories in 'sink' directory.

