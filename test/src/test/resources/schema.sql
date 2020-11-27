create table if not exists users
(
    user_name varchar(100),
    name      varchar(100),
    surname   varchar(100)
);
create table if not exists files
(
    file_id  varchar(100),
    category varchar(100)
);
create table if not exists file_access
(
    user_name varchar(100),
    file_id   varchar(100)
);