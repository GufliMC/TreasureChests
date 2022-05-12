-- apply changes
create table treasure_chests (
  id                            varchar(40) not null,
  location                      varchar(255) not null,
  respawn_time                  integer default 3600 not null,
  mode                          varchar(12) default 'PLAYER_BOUND' not null,
  constraint pk_treasure_chests primary key (id)
);

create table treasure_chest_inventories (
  id                            varchar(40) not null,
  player_id                     varchar(40) not null,
  chest_id                      varchar(40),
  inventory                     TEXT not null,
  created_at                    datetime(6) not null,
  updated_at                    datetime(6) not null,
  constraint pk_treasure_chest_inventories primary key (id)
);

create table treasure_loot (
  id                            varchar(40) not null,
  chest_id                      varchar(40),
  item                          TEXT not null,
  chance                        integer default 100 not null,
  constraint pk_treasure_loot primary key (id)
);

create index ix_treasure_chest_inventories_chest_id on treasure_chest_inventories (chest_id);
alter table treasure_chest_inventories add constraint fk_treasure_chest_inventories_chest_id foreign key (chest_id) references treasure_chests (id) on delete cascade on update restrict;

create index ix_treasure_loot_chest_id on treasure_loot (chest_id);
alter table treasure_loot add constraint fk_treasure_loot_chest_id foreign key (chest_id) references treasure_chests (id) on delete cascade on update restrict;

