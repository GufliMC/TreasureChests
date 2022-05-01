-- apply changes
create table treasure_chests (
  id                            uuid not null,
  location                      varchar(255),
  constraint pk_treasure_chests primary key (id)
);

create table treasure_chest_inventories (
  id                            uuid not null,
  player_id                     uuid,
  chest_id                      uuid,
  time                          timestamp,
  inventory                     varchar(255),
  constraint pk_treasure_chest_inventories primary key (id)
);

create table treasure_loot (
  id                            uuid not null,
  chest_id                      uuid,
  item                          varchar(255),
  chance                        double not null,
  constraint pk_treasure_loot primary key (id)
);

create index ix_treasure_chest_inventories_chest_id on treasure_chest_inventories (chest_id);
alter table treasure_chest_inventories add constraint fk_treasure_chest_inventories_chest_id foreign key (chest_id) references treasure_chests (id) on delete cascade on update restrict;

create index ix_treasure_loot_chest_id on treasure_loot (chest_id);
alter table treasure_loot add constraint fk_treasure_loot_chest_id foreign key (chest_id) references treasure_chests (id) on delete cascade on update restrict;

