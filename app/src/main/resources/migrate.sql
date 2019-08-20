update communities set type='Job';
update favorites set favorites.name = (select username from userdb where id = favorites.user_id) where favorites.type="Default";
insert into communities_users select communities_id, users_id from users_communities;
