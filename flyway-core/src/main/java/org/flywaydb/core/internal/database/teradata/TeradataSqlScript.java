package org.flywaydb.core.internal.database.teradata;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.ExecutableSqlScript;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.jdbc.ContextImpl;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

public class TeradataSqlScript extends ExecutableSqlScript<ContextImpl> {
    public TeradataSqlScript(LoadableResource sqlScriptResource, PlaceholderReplacer placeholderReplacer, boolean mixed) {
        super(sqlScriptResource, placeholderReplacer, mixed);
    }

    @Override
    protected SqlStatementBuilder createSqlStatementBuilder() {
        return new TeradataSqlStatementBuilder(Delimiter.SEMICOLON);
    }

}
