/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oracle.adbaoverjdbc.test;

import static com.oracle.adbaoverjdbc.JdbcConnectionProperties.JDBC_CONNECTION_PROPERTIES;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;
import jdk.incubator.sql2.Result.RowColumn;
import jdk.incubator.sql2.Session;

/**
 * This is a quick and dirty test to check if anything at all is working.
 * 
 * Depends on the scott/tiger schema availble here:
 * https://github.com/oracle/dotnet-db-samples/blob/master/schemas/scott.sql
 */
public class MysqlTest {

    private static final String URL = "jdbc:mysql://localhost:3306/datasets?useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "root123";

    private static final String FACTORY_NAME = "com.oracle.adbaoverjdbc.DataSourceFactory";

    /**
     * Execute a few trivial queries.
     */
    @Test
    public void rowOperation() {
        Properties props = new Properties();
        props.setProperty("oracle.jdbc.implicitStatementCacheSize", "10");
        DataSourceFactory factory = DataSourceFactory.newFactory(FACTORY_NAME);
        try (DataSource ds = factory.builder().url(URL).username(USER).password(PASSWORD).sessionProperty(JDBC_CONNECTION_PROPERTIES, props).build();
                Session session = ds.getSession(t -> System.out.println("ERROR: " + t.getMessage()))) {

            session.<List<Integer>> rowOperation("select * from bank_details").collect(() -> {
                return new ArrayList<Integer>();
            }, (List<Integer> a, RowColumn r) -> {
                int id = r.at("id").get(Integer.class);
                System.out.println(id);
            }).submit().getCompletionStage().whenComplete((List<Integer> ids, Throwable t) -> {
            });

        }

        ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES);

    }

}
