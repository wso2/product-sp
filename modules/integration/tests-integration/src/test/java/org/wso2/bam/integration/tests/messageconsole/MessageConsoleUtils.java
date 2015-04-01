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

import org.wso2.carbon.analytics.messageconsole.stub.beans.ColumnBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.TableBean;

public class MessageConsoleUtils {

    public static final String TABLE_NO_1 = "TABLE_NO_1";
    public static final String TABLE_NO_2 = "TABLE_NO_2";
    public static final String TABLE_NO_3 = "TABLE_NO_3";
    public static final String TABLE_NO_4 = "TABLE_NO_4";
    public static final String TABLE_NO_5 = "TABLE_NO_5";
    public static final String STRING = "STRING";
    public static final String INTEGER = "INTEGER";
    public static final String LONG = "LONG";
    public static final String FLOAT = "FLOAT";
    public static final String DOUBLE = "DOUBLE";
    public static final String BOOLEAN = "BOOLEAN";

    public static ColumnBean getColumnBean(String name, String type, boolean isPrimary, boolean isIndexed) {
        ColumnBean firstColumn = new ColumnBean();
        firstColumn.setName(name);
        firstColumn.setType(type);
        firstColumn.setPrimary(isPrimary);
        firstColumn.setIndex(isIndexed);
        return firstColumn;
    }

    public static TableBean getFirstTable() {
        TableBean tableBean = new TableBean();
        tableBean.setName(TABLE_NO_1);
        ColumnBean column1 = getColumnBean("string_s1", STRING, true, true);
        ColumnBean column2 = getColumnBean("string_s2", STRING, false, false);
        ColumnBean column3 = getColumnBean("int_i1", INTEGER, true, true);
        ColumnBean column4 = getColumnBean("int_i2", INTEGER, false, false);
        ColumnBean column5 = getColumnBean("long_l1", LONG, true, false);
        ColumnBean column6 = getColumnBean("long_l2", LONG, false, true);
        ColumnBean column7 = getColumnBean("float_f1", FLOAT, false, true);
        ColumnBean column8 = getColumnBean("float_f2", FLOAT, false, false);
        ColumnBean column9 = getColumnBean("double_d1", DOUBLE, false, false);
        ColumnBean column10 = getColumnBean("double_d2", DOUBLE, false, true);
        ColumnBean column11 = getColumnBean("boolean_b1", BOOLEAN, false, true);
        ColumnBean column12 = getColumnBean("boolean_b2", BOOLEAN, false, false);
        ColumnBean[] columns = new ColumnBean[]{column1, column2, column3, column4, column5, column6, column7,
                                                column8, column9, column10, column11, column12};
        tableBean.setColumns(columns);
        return tableBean;
    }

    public static TableBean getSecondTable() {
        TableBean tableBean = new TableBean();
        tableBean.setName(TABLE_NO_2);
        ColumnBean column1 = getColumnBean("string_s1", STRING, true, true);
        ColumnBean column2 = getColumnBean("int_i1", INTEGER, false, false);
        ColumnBean column3 = getColumnBean("long_l1", LONG, true, true);
        ColumnBean column4 = getColumnBean("float_f1", FLOAT, false, false);
        ColumnBean column5 = getColumnBean("double_d1", DOUBLE, true, false);
        ColumnBean column6 = getColumnBean("boolean_b1", BOOLEAN, false, true);
        ColumnBean[] columns = new ColumnBean[]{column1, column2, column3, column4, column5, column6};
        tableBean.setColumns(columns);
        return tableBean;
    }

    public static TableBean getThirdTable() {
        TableBean tableBean = new TableBean();
        tableBean.setName(TABLE_NO_3);
        return tableBean;
    }

    public static TableBean getFourthTable() {
        TableBean tableBean = new TableBean();
        tableBean.setName(TABLE_NO_4);
        ColumnBean column1 = getColumnBean("string_s1", STRING, true, true);
        ColumnBean[] columns = new ColumnBean[]{column1};
        tableBean.setColumns(columns);
        return tableBean;
    }

    public static TableBean getFifthTable() {
        TableBean tableBean = new TableBean();
        tableBean.setName(TABLE_NO_5);
        ColumnBean column1 = getColumnBean("int_i1", INTEGER, false, false);
        ColumnBean[] columns = new ColumnBean[]{column1};
        tableBean.setColumns(columns);
        return tableBean;
    }
}
