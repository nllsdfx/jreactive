create extension if not exists citext;

create domain email AS citext
  CHECK (length(value) <= 320 and VALUE ~
                                  '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$');

create table account (
  id              serial                                             not null primary key,
  username        varchar(32) unique                                 not null,
  email           email unique                                       not null,
  sha_pass_hash   varchar(40)                                        not null,
  session_key     varchar(80)                                        not null,
  v               varchar(64)                                        not null,
  s               varchar(64)                                        not null,
  token_key       varchar(100) default ''                            not null,
  join_date       timestamp default now()                            not null,
  last_ip         varchar(15) default '127.0.0.1'                    not null,
  last_attempt_ip varchar(15) default '127.0.0.1'                    not null,
  failed_logins   bigint default '0'                                 not null,
  locked          boolean default false                              not null,
  last_login      timestamp default 'epoch'                          not null,
  online          boolean default false                              not null,
  os              varchar(3) default ''                              not null
);

create index on account (username, email);

comment on table account
is 'User account';

create table account_banned (
  id         serial                                                                                     not null primary key,
  account_id serial references account (id)                                                             not null,
  ban_date   timestamp default now()                                                                    not null,
  unbad_date timestamp default 'epoch'                                                                  not null,
  banned_by  serial references account (id)                                                             not null,
  ban_reason text                                                                                       not null,
  banned     boolean default true                                                                       not null
);

create index on account_banned (account_id);

create table account_muted (
  id          serial                         not null primary key,
  account_id  serial references account (id) not null,
  mute_date   timestamp default now()        not null,
  mute_time   bigint                         not null,
  muted_by    serial references account (id) not null,
  mute_reason text                           not null
);

create index on account_muted (account_id);

comment on column account_muted.mute_time
is 'Mute time in millis';

create table ip_banned (
  id        serial                           not null primary key,
  ip        varchar(15) default '127.0.0.1'  not null,
  bandate   bigint                           not null,
  unbandate bigint                           not null,
  bannedby  varchar(50) default '[Console]'  not null,
  banreason varchar(255) default 'no reason' not null
);

create index on ip_banned (ip);

comment on table ip_banned
is 'Banned IPs';

