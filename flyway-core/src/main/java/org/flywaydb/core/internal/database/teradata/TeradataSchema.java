/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.database.teradata;


import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Teradata implementation of Schema.
 */
public class TeradataSchema extends Schema<TeradataDatabase> {
    private static final Log LOG = LogFactory.getLog(TeradataSchema.class);

    public TeradataSchema(JdbcTemplate jdbcTemplate, TeradataDatabase dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        try {
            doAllTables();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM dbc.tables WHERE databasename=?", name) == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        LOG.info("Teradata does not support creating schemas. Schema not created: " + name);
    }

    @Override
    protected void doDrop() throws SQLException {
        LOG.info("Teradata does not support dropping schemas. Schema not dropped: " + name);
    }

    /*
     * Because Teradata does not support multiple DDL statements in a transaction we set autocommit =
     * true to avoid this SQLException : [Teradata Database] [TeraJDBC 15.10.00.05] [Error 3932]
     * [SQLState 25000] Only an ET or null statement is legal after a DDL Statement.
     *
     * @throws SQLException when the clean failed.
     */
    @Override
    protected void doClean() throws SQLException {
        if (!database.supportsDdlTransactions()) {
            jdbcTemplate.getConnection().setAutoCommit(true);
        }

        for (String statement : generateDropStatementsForForeignKeys()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForJoinIndexes()) {
            jdbcTemplate.execute(statement);
        }

        jdbcTemplate.execute("DELETE DATABASE " + database.quote(name) + " ALL");
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList("SELECT TRIM(TableName) FROM dbc.Tables WHERE DatabaseName = ? AND TableKind = 'T'", name);
        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new TeradataTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new TeradataTable(jdbcTemplate, database, this, tableName);
    }

    /**
     * Generates the statements for dropping the join indexes in this schema.
     * <p>
     * DELETE DATABASE statement cannot operate if there is JOIN INDEX left.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForJoinIndexes() throws SQLException {
        List<String> viewNames = jdbcTemplate.queryForStringList("SELECT TRIM(TableName) FROM dbc.Tables WHERE DatabaseName = ? AND TableKind = 'I'", name);
        List<String> statements = new ArrayList<>();
        for (String viewName : viewNames) {
            statements.add("DROP JOIN INDEX " + database.quote(name, viewName));
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the foreign keys constraints in this schema.
     * <p>
     * DELETE DATABASE statement cannot operate if there is cross database FOREIGN KEY left.
     * <p>
     * Warning : All the constraints must have a name.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForForeignKeys() throws SQLException {
        List<Map<String, String>> rows = jdbcTemplate.queryForList("SELECT DISTINCT ChildDB, ChildTable, IndexName FROM dbc.All_RI_ParentsV WHERE ParentDB = ? AND ChildDB <> ? AND IndexName IS NOT NULL", name, name);
        List<String> statements = new ArrayList<>();

        for (Map<String, String> row : rows) {
            statements.add("ALTER TABLE " + database.quote(row.get("ChildDB"), row.get("ChildTable")) + " DROP CONSTRAINT " + database.quote(row.get("IndexName")));
        }

        return statements;
    }
}
