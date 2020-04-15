CREATE TABLE note
(
    id serial NOT NULL,
    owner varchar(200) NOT NULL,
    title varchar(100) NOT NULL,
    message text NOT NULL,
    created timestamp not null default now(),
    constraint notes_pkey primary key (id),
    constraint notes_title_key unique (title)
);
insert into note(owner,title,message) values ('daroge','docker','docker is awesome');
insert into note(owner,title,message) values ('daroge','quarkus','The new Java framework from Red Hat');