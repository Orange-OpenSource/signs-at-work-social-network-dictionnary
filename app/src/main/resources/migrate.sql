update communities set type='Job';
update favorites set favorites.name = "Ma liste", favorites.type="Individual" where favorites.type="Default";
insert into communities_users select communities_id, users_id from users_communities;
drop table users_communities;
update favorites set id_for_name=0;
delete from ratings where rating='Neutral';
alter table userdb change column job_text_description job_description_text varchar(1000);
alter table userdb change column job_video_description job_description_video varchar(255);

delete from communities_users where communities_id in (select id from communities where type='Job');
update userdb set job=null;
delete from favorites_communities where communities_id in (select id from communities where type='Job');
delete from communities where type='Job';
insert into communities (name, type) values ('Architecte', 'Job');
insert into communities (name, type) values ('Chef de Projet', 'Job');
insert into communities (name, type) values ('Chercheur', 'Job');
insert into communities (name, type) values ('Concepteur', 'Job');
insert into communities (name, type) values ('Développeur', 'Job');
insert into communities (name, type) values ('Ergonome', 'Job');
insert into communities (name, type) values ('Ingénieur', 'Job');
insert into communities (name, type) values ('Interprète LSF', 'Job');
insert into communities (name, type) values ('UX Designer', 'Job');
insert into communities (name, type) values ('Valideur', 'Job');
insert into communities (name, type) values ('Vendeur', 'Job');
insert into communities (name, type) values ('Stagiaire', 'Job');



---
alter table signs modify name varchar(255) character set utf8 collate utf8_bin;
alter table requests modify name varchar(255) character set utf8 collate utf8_bin;

---
alter table communities modify name varchar(255) character set utf8 collate utf8_bin;

---
alter table favorites modify name varchar(255) character set utf8 collate utf8_bin;

--
update communities set user_id=1 where type='Job';