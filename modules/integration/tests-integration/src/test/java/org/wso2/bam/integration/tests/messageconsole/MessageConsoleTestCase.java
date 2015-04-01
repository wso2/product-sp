/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.bam.integration.tests.messageconsole;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bam.integration.common.utils.BAMIntegrationTest;
import org.wso2.carbon.analytics.messageconsole.stub.beans.ColumnBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.RecordBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.RecordResultBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.TableBean;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.tests.CarbonTestServerManager;
import org.wso2.das.integration.common.clients.MessageConsoleClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageConsoleTestCase extends BAMIntegrationTest {

    private static final Log log = LogFactory.getLog(MessageConsoleTestCase.class);
    private MessageConsoleClient messageConsoleClient;
    public Map<String, String> startupParameterMap1 = new HashMap<String, String>();
    private CarbonTestServerManager server1;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init("superTenant", "bam002", "admin");
        startupParameterMap1.put("-DportOffset", "20");
        server1 = new CarbonTestServerManager(bamServer, System.getProperty("carbon.zip"), (HashMap<String, String>) startupParameterMap1);
        server1.startServer();
        String backendURL = super.backendURL.replace("9443", "9463");
        String session = new AuthenticatorClient(backendURL).
                login(bamServer.getContextTenant().getContextUser().getUserName(),
                      bamServer.getContextTenant().getContextUser().getPassword(),
                      bamServer.getInstance().getHosts().get("default"));
        messageConsoleClient = new MessageConsoleClient("https://localhost:9463/services/", session);
    }

    @AfterClass(alwaysRun = true)
    protected void cleanup() throws Exception {
        if (server1 != null) {
            server1.stopServer();
        }
    }

    @Test(groups = "wso2.bam", description = "Create new table")
    public void createTablesTest()
            throws Exception {
        log.info("Invoking createTablesTest ... ");
        TableBean tableBean = MessageConsoleUtils.getFirstTable();
        messageConsoleClient.createTable(tableBean);
        tableBean = MessageConsoleUtils.getSecondTable();
        messageConsoleClient.createTable(tableBean);
        tableBean = MessageConsoleUtils.getThirdTable();
        messageConsoleClient.createTable(tableBean);
        tableBean = MessageConsoleUtils.getFourthTable();
        messageConsoleClient.createTable(tableBean);
        tableBean = MessageConsoleUtils.getFifthTable();
        messageConsoleClient.createTable(tableBean);
    }

    @Test(groups = "wso2.bam", description = "List tables", dependsOnMethods = "createTablesTest")
    public void listTablesTest()
            throws Exception {
        log.info("Invoking listTablesTest ... ");
        String[] tableNames = messageConsoleClient.listTables();
        List<String> tableNameList = Arrays.asList(tableNames);
        Assert.assertEquals(tableNameList.size(), 5, "Table count not equal.");
        Assert.assertTrue(tableNameList.contains(MessageConsoleUtils.TABLE_NO_1), "Table name not contain in the " +
                                                                                  "response.");
        Assert.assertTrue(tableNameList.contains(MessageConsoleUtils.TABLE_NO_2), "Table name not contain in the " +
                                                                                  "response.");
        Assert.assertTrue(tableNameList.contains(MessageConsoleUtils.TABLE_NO_3), "Table name not contain in the " +
                                                                                  "response.");
    }

    @Test(groups = "wso2.bam", description = "Get table info", dependsOnMethods = "listTablesTest")
    public void getTableInfoTest()
            throws Exception {
        log.info("Invoking getTableInfoTest ... ");
        TableBean tableBean = messageConsoleClient.getTableInfo(MessageConsoleUtils.TABLE_NO_1);
        Assert.assertEquals(tableBean.getColumns().length, 12, "Invalid column count in the table.");
        List<ColumnBean> columns = Arrays.asList(tableBean.getColumns());
        for (ColumnBean columnBean : columns) {
            if (columnBean.getName().equalsIgnoreCase("long_l1")) {
                Assert.assertTrue(columnBean.getPrimary(), "Column return as non primary.");
                Assert.assertFalse(columnBean.getIndex(), "Column return as indexed column.");
                break;
            }
        }
    }

    @Test(groups = "wso2.bam", description = "Get table info with indices info", dependsOnMethods = "listTablesTest")
    public void getTableInfoWithIndicesInfoTest()
            throws Exception {
        // Waiting until index column details get save
        log.info("Invoking getTableInfoWithIndicesInfoTest ... ");
        TableBean tableBean = messageConsoleClient.getTableInfoWithIndicesInfo(MessageConsoleUtils.TABLE_NO_1);
        Assert.assertEquals(tableBean.getColumns().length, 12, "Invalid column count in the table.");
        List<ColumnBean> columns = Arrays.asList(tableBean.getColumns());

        for (ColumnBean columnBean : columns) {
            String columnName = columnBean.getName();
            switch (columnName) {
                case "string_s1": {
                    Assert.assertTrue(columnBean.getIndex(), "string_s1 column not getting as an index column");
                    break;
                }
                case "int_i1": {
                    Assert.assertTrue(columnBean.getIndex(), "int_i1 column not getting as an index column");
                    break;
                }
                case "long_l2": {
                    Assert.assertTrue(columnBean.getIndex(), "long_l2 column not getting as an index column");
                    break;
                }
                case "float_f1": {
                    Assert.assertTrue(columnBean.getIndex(), "float_f1 column not getting as an index column");
                    break;
                }
                case "double_d2": {
                    Assert.assertTrue(columnBean.getIndex(), "double_d2 column not getting as an index column");
                    break;
                }
                case "boolean_b1": {
                    Assert.assertTrue(columnBean.getIndex(), "boolean_b1 column not getting as an index column");
                    break;
                }
                case "boolean_b2": {
                    Assert.assertFalse(columnBean.getIndex(), "Invalid column getting as an index column");
                    break;
                }
            }
        }
    }

    @Test(groups = "wso2.bam", description = "Edit table info", dependsOnMethods = "getTableInfoTest")
    public void editTableInfoTest()
            throws Exception {
        log.info("Invoking editTableInfoTest ... ");
        TableBean tableBean = messageConsoleClient.getTableInfo(MessageConsoleUtils.TABLE_NO_4);
        Assert.assertTrue(tableBean.getColumns()[0].getName().equalsIgnoreCase("string_s1"));
        Assert.assertEquals(tableBean.getColumns().length, 1);
        ColumnBean[] columnArray = new ColumnBean[2];
        ColumnBean firstColumn = tableBean.getColumns()[0];
        firstColumn.setType(MessageConsoleUtils.INTEGER);
        columnArray[0] = firstColumn;
        ColumnBean secondColumn = MessageConsoleUtils.getColumnBean("int_i1", MessageConsoleUtils.INTEGER, false, false);
        columnArray[1] = secondColumn;
        tableBean.setColumns(columnArray);
        messageConsoleClient.editTable(tableBean);
        tableBean = messageConsoleClient.getTableInfo(MessageConsoleUtils.TABLE_NO_4);
        Assert.assertEquals(tableBean.getColumns().length, 2, "Invalid column count in the table.");
        List<ColumnBean> columns = Arrays.asList(tableBean.getColumns());
        boolean containsNewColumn = false;
        for (ColumnBean columnBean : columns) {
            if (columnBean.getName().equalsIgnoreCase("int_i1")) {
                containsNewColumn = true;
                break;
            }
        }
        Assert.assertTrue(containsNewColumn, "Newly added column not containing.");
        for (ColumnBean columnBean : columns) {
            if (columnBean.getName().equalsIgnoreCase("string_s1")) {
                Assert.assertEquals(columnBean.getType(), MessageConsoleUtils.INTEGER, "Type is not updated");
            }
        }
    }

    @Test(groups = "wso2.bam", description = "Add record", dependsOnMethods = "getTableInfoWithIndicesInfoTest")
    public void addRecordTest()
            throws Exception {
        log.info("Invoking addRecordTest ... ");
        log.info("Adding empty record ... ");
        String[] columnNames = new String[]{"string_s1", "int_i1", "long_l1", "float_f1", "double_d1", "boolean_b1"};
        RecordBean emptyRecord = messageConsoleClient.addRecord(MessageConsoleUtils.TABLE_NO_2, columnNames, new
                String[]{"", "", "", "", "", ""});
        Assert.assertNotNull(emptyRecord.getRecordId(), "Getting empty recordId");
        String[] values = new String[]{"string_val", "100", "200", "10.3", "12.5", "true"};
        RecordBean fixedValueRecord = messageConsoleClient.addRecord(MessageConsoleUtils.TABLE_NO_2, columnNames, values);
        RecordResultBean records = messageConsoleClient.getRecords(MessageConsoleUtils.TABLE_NO_2, 0, System.currentTimeMillis() + 1000, 0, -1,
                                                                   "");
        Assert.assertEquals(records.getTotalResultCount(), 2, "Record count is incorrect");
    }
}
