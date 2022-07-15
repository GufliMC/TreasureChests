-- apply alter tables
alter table treasure_chests add column split_stacks tinyint(1) default 1 not null;
