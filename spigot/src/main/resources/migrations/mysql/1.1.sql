-- apply alter tables
alter table treasure_chests add column item_spread tinyint(1) default 1 not null;
alter table treasure_chests add column title varchar(4096);
