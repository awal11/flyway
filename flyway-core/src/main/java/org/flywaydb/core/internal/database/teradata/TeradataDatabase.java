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
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;

import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.sql.Connection;

/**
 * Teradata-specific support.
 */
public class TeradataDatabase extends Database<TeradataConnection> {

    public TeradataDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit) {
        super(configuration, connection, originalAutoCommit);
    }

    @Override
    protected TeradataConnection getConnection(Connection connection) {
        return new TeradataConnection(configuration, this, connection, originalAutoCommit);
    }

    @Override
    protected void ensureSupported() {
        if (majorVersion < 15) {
            throw new FlywayDbUpgradeRequiredException("Teradata", "" + majorVersion, "15");
        }
    }

    @Override
    protected SqlScript doCreateSqlScript(LoadableResource resource, PlaceholderReplacer placeholderReplacer, boolean mixed) {
        return new TeradataSqlScript(configuration, resource, mixed, placeholderReplacer);
    }

    @Override
    public String getDbName() {
        return "teradata";
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    protected boolean supportsChangingCurrentSchema() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }


}
