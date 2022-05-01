package com.gufli.treasurechests.app.data.beans;

import com.gufli.treasurechests.app.data.DatabaseContext;
import org.minestombrick.ebean.BaseModel;
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