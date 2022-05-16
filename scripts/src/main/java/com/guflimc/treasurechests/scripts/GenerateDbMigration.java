package com.guflimc.treasurechests.scripts;

import com.gufli.dbeantools.api.migration.MigrationTool;
import com.guflimc.treasurechests.spigot.data.DatabaseContext;
import io.ebean.annotation.Platform;

import java.io.IOException;

public class GenerateDbMigration {

    public static void main(String[] args) throws IOException {
        DatabaseContext context = new DatabaseContext();
        MigrationTool tool = new MigrationTool(context, "app/src/main/resources",
                Platform.H2, Platform.MYSQL);
        tool.generate();
    }
}