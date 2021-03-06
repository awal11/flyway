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

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;

import java.sql.SQLException;
import java.sql.Types;

/**
 * Teradata connection.
 */
public class TeradataConnection extends Connection<TeradataDatabase> {
    private static final Log LOG = LogFactory.getLog(TeradataConnection.class);

    TeradataConnection(Configuration configuration, TeradataDatabase database, java.sql.Connection connection,
                       boolean originalAutoCommit) {
        super(configuration, database, connection,originalAutoCommit, Types.VARCHAR);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SELECT database");
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        LOG.debug("Changing schema to " + schema);
        jdbcTemplate.execute("DATABASE " + database.quote(schema));
        jdbcTemplate.execute("COMMIT");
    }

    @Override
    public Schema getSchema(String name) {
        return new TeradataSchema(jdbcTemplate, database, name);
    }
}