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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bam.integration.common.utils.BAMIntegrationTest;
import org.wso2.carbon.analytics.messageconsole.stub.beans.ColumnBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.TableBean;
import org.wso2.das.integration.common.clients.MessageConsoleClient;

import java.util.Arrays;
import java.util.List;

public class MessageConsoleTestCase extends BAMIntegrationTest {

    private static final Log log = LogFactory.getLog(MessageConsoleTestCase.class);

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.bam", description = "Create new table")
    public void createTablesTest()
            throws Exception {
        MessageConsoleClient messageConsoleClient = new MessageConsoleClient(backendURL, getSessionCookie());
        TableBean tableBean = new TableBean();
        tableBean.setName("MC_inte_test1");
        ColumnBean[] columns = new ColumnBean[1];

        ColumnBean firstColumn = new ColumnBean();
        firstColumn.setName("first_column");
        firstColumn.setType("STRING");
        firstColumn.setPrimary(true);
        firstColumn.setIndex(true);
        columns[0] = firstColumn;

        tableBean.setColumns(columns);

        messageConsoleClient.createTable(tableBean);
    }


    @Test(groups = "wso2.bam", description = "List tables", dependsOnMethods = "createTablesTest")
    public void listTablesTest()
            throws Exception {
        MessageConsoleClient messageConsoleClient = new MessageConsoleClient(backendURL, getSessionCookie());
        String[] tableNames = messageConsoleClient.listTables();
        List<String> tableNameList = Arrays.asList(tableNames);
        Assert.assertTrue(tableNameList.contains("MC_inte_test1".toUpperCase()));
    }

    @Test(groups = "wso2.bam", description = "Get table info", dependsOnMethods = "createTablesTest")
    public void getTableInfoTest()
            throws Exception {
        MessageConsoleClient messageConsoleClient = new MessageConsoleClient(backendURL, getSessionCookie());
        TableBean tableBean = messageConsoleClient.getTableInfo("MC_inte_test1");
        Assert.assertTrue(tableBean.getColumns()[0].getName().equalsIgnoreCase("first_column"));
        Assert.assertEquals(tableBean.getColumns().length, 1);
    }

    @Test(groups = "wso2.bam", description = "Edit table info", dependsOnMethods = "getTableInfoTest")
    public void editTableInfoTest()
            throws Exception {
        MessageConsoleClient messageConsoleClient = new MessageConsoleClient(backendURL, getSessionCookie());
        TableBean tableBean = messageConsoleClient.getTableInfo("MC_inte_test1");
        Assert.assertTrue(tableBean.getColumns()[0].getName().equalsIgnoreCase("first_column"));
        Assert.assertEquals(tableBean.getColumns().length, 1);

        ColumnBean[] columns = new ColumnBean[2];
        columns[0] = tableBean.getColumns()[0];

        ColumnBean secondColumn = new ColumnBean();
        secondColumn.setName("second_column");
        secondColumn.setType("INTEGER");
        secondColumn.setPrimary(false);
        secondColumn.setIndex(true);
        columns[1] = secondColumn;

        tableBean.setColumns(columns);
        messageConsoleClient.editTable(tableBean);
        tableBean = messageConsoleClient.getTableInfo("MC_inte_test1");
        Assert.assertTrue(tableBean.getColumns()[1].getName().equalsIgnoreCase("second_column"));
        Assert.assertEquals(tableBean.getColumns().length, 2);
    }
}
