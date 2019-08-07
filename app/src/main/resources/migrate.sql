update communities set type='Job';
update favorites set favorites.name = (select username from userdb where id = favorites.user_id) where favorites.type="Default";
