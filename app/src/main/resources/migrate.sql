update communities set type='Job';
update favorites set favorites.name = "Ma liste", favorites.type="Individual" where favorites.type="Default";
insert into communities_users select communities_id, users_id from users_communities;
drop table users_communities;
update favorites set id_for_name=0;
delete from ratings where rating='Neutral';