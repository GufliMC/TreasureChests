package com.guflimc.treasurechests.spigot.data.beans;

import com.gufli.dbeantools.api.BaseModel;
import com.guflimc.treasurechests.spigot.data.DatabaseContext;
import io.ebean.Model;
import io.ebean.annotation.DbName;

import javax.persistence.MappedSuperclass;

@DbName(DatabaseContext.DATASOURCE_NAME)
@MappedSuperclass
public class BModel extends Model implements BaseModel {

    public BModel() {
        super(DatabaseContext.DATASOURCE_NAME);
    }

}