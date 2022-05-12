package com.guflimc.treasurechests.scripts;

import com.guflimc.treasurechests.spigot.data.DatabaseContext;
import io.ebean.annotation.Platform;
import org.minestombrick.ebean.migration.MigrationGenerator;

import java.io.IOException;
import java.nio.file.Path;

public class GenerateDbMigration {

    /**
     * Generate the next "DB schema DIFF" migration.
     */
    public static void main(String[] args) throws IOException {
        MigrationGenerator generator = new MigrationGenerator(
                DatabaseContext.DATASOURCE_NAME,
                Path.of("app/src/main/resources"),
                Platform.H2, Platform.MYSQL
        );
        DatabaseContext.classes().forEach(generator::addClass);
        generator.generate();
    }
}