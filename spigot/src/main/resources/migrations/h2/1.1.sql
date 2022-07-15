-- apply alter tables
alter table treasure_chests add column item_spread boolean default true not null;
alter table treasure_chests add column title varchar(4096);
