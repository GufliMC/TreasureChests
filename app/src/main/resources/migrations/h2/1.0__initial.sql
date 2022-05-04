-- apply changes
create table treasure_chests (
  id                            uuid not null,
  location                      varchar(255) not null,
  respawn_time                  integer default 3600 not null,
  mode                          varchar(12) default 'PLAYER_BOUND' not null,
  constraint ck_treasure_chests_mode check ( mode in ('SERVER_BOUND','PLAYER_BOUND')),
  constraint pk_treasure_chests primary key (id)
);

create table treasure_chest_inventories (
  id                            uuid not null,
  player_id                     uuid not null,
  chest_id                      uuid,
  inventory                     TEXT not null,
  created_at                    timestamp not null,
  updated_at                    timestamp not null,
  constraint pk_treasure_chest_inventories primary key (id)
);

create table treasure_loot (
  id                            uuid not null,
  chest_id                      uuid,
  item                          TEXT not null,
  chance                        integer default 100 not null,
  constraint pk_treasure_loot primary key (id)
);

create index ix_treasure_chest_inventories_chest_id on treasure_chest_inventories (chest_id);
alter table treasure_chest_inventories add constraint fk_treasure_chest_inventories_chest_id foreign key (chest_id) references treasure_chests (id) on delete cascade on update restrict;

create index ix_treasure_loot_chest_id on treasure_loot (chest_id);
alter table treasure_loot add constraint fk_treasure_loot_chest_id foreign key (chest_id) references treasure_chests (id) on delete cascade on update restrict;

