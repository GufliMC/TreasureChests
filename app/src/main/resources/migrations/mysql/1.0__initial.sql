-- apply changes
create table treasure_chests (
  id                            varchar(40) not null,
  location                      varchar(255),
  constraint pk_treasure_chests primary key (id)
);

create table treasure_chest_inventories (
  id                            varchar(40) not null,
  player_id                     varchar(40),
  chest_id                      varchar(40),
  time                          datetime(6),
  inventory                     varchar(255),
  constraint pk_treasure_chest_inventories primary key (id)
);

create table treasure_loot (
  id                            varchar(40) not null,
  chest_id                      varchar(40),
  item                          varchar(255),
  chance                        double not null,
  constraint pk_treasure_loot primary key (id)
);

create index ix_treasure_chest_inventories_chest_id on treasure_chest_inventories (chest_id);
alter table treasure_chest_inventories add constraint fk_treasure_chest_inventories_chest_id foreign key (chest_id) references treasure_chests (id) on delete cascade on update restrict;

create index ix_treasure_loot_chest_id on treasure_loot (chest_id);
alter table treasure_loot add constraint fk_treasure_loot_chest_id foreign key (chest_id) references treasure_chests (id) on delete cascade on update restrict;

