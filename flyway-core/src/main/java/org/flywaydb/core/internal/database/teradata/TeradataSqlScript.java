package org.flywaydb.core.internal.database.teradata;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.ExecutableSqlScript;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.jdbc.ContextImpl;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

public class TeradataSqlScript extends ExecutableSqlScript<ContextImpl> {
    public TeradataSqlScript(Configuration configuration, LoadableResource sqlScriptResource, boolean mixed, PlaceholderReplacer placeholderReplacer) {
        super(configuration, sqlScriptResource, mixed, placeholderReplacer);
    }

    @Override
    protected SqlStatementBuilder createSqlStatementBuilder() {
        return new TeradataSqlStatementBuilder(Delimiter.SEMICOLON);
    }

}
