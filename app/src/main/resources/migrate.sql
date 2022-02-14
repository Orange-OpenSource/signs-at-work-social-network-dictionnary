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

--
update articles set description_text='<h2>Article 11 - Disposition Diverses </h2><p>Dans l\'éventualité où l\'une quelconque des stipulations de ces CGU serait déclarée nulle ou sans effet, les stipulations restantes seront considérées comme applicables de plein droit.</p><p>Les stipulations déclarées nulles et non valides seront alors remplacées par des stipulations qui se rapprocheront le plus quant à leur contenu des stipulations initialement arrêtées.</p><p>Les parties ne seront pas tenues pour responsables, ou considérées comme ayant failli aux présentes CGU, pour tout retard ou inexécution, lorsque la cause du retard ou de l\'inexécution est liée à un cas de force majeure telle que définie par la jurisprudence des tribunaux français.</p><h1><font color=\'blue\'>II: CGU de Dailymotion</h1></font><p>L\'utilisation du service est soumise aux <a href=\"https://www.dailymotion.com/legal\">Conditions Générales d\'Utilisation de Dailymotion</a> consultables en suivant le lien hypertexte</p>' where id=11;
update articles set description_text='<h2>5 - Droits des Utilisateurs et/ou Contributeurs</h2><p>L\'Utilisateur et/ou Contributeur dispose d’un droit d’accès, de rectification ou de suppression des Données Personnelles le concernant, ainsi que d’un droit d’opposition pour motif légitime sous réserve de justifier de son identité (indiquer son nom, prénom, adresse, numéro de téléphone et joindre un justificatif d\'identité).</p><p>Il dispose également d\'un droit à la limitation des traitements, ainsi que du droit à la portabilité de ses données.</p><p>L\'Utilisateur et/ou Contributeur peut émettre des directives sur la conservation, la suppression ou la communication de ses Données Personnelles après son décès L\'Utilisateurs et/ou Contributeurs peut exercer ses droits en écrivant à l\'adresse suivante : <a href="mailto:signsatwork.support@orange.com">signsatwork.support@orange.com</a></p><h1><font color=\'blue\'>II: Politique de Dailymotion</font></h1><p>L\'utilisation du service est soumise à la <a href=\"https://www.dailymotion.com/legal/privacy\">Politique des Données Personnelles de Dailymotion</a> consultable en suivant le lien hypertexte</p>' where id=17;
update articles set description_text='<h2> Article 11 - Miscellaneous Provisions </h2> <p> In the event that any of the stipulations of these T & Cs are declared null or void, the remaining stipulations will be considered as applicable as of right. </ p > <p> The stipulations declared null and void will then be replaced by stipulations which will be the closest in their content to the stipulations initially adopted. </p> <p> The parties will not be held responsible, or considered as having failed in these T & Cs, for any delay or non-performance, when the cause of the delay or non-performance is related to a case of force majeure as defined by the jurisprudence of French courts.</p><h1><font color=\'blue\'>II: CGU of Dailymotion</font></h1><p>The use of the service is subject to the <a href=\"https://www.dailymotion.com/legal\"> General Conditions of Use of Dailymotion </a> which can be viewed by following the hypertext link </p>' where id=28;
update articles set description_text='<h2> 5 - Rights of Users and / or Contributors </h2> <p> The User and / or Contributor has the right to access, rectify or delete Personal Data concerning him, as well as a right of opposition for legitimate reason subject to proving his identity (indicate his name, first name, address, telephone number and attach proof of identity). </p> <p> He also has a right to the limitation of processing, as well as the right to portability of their data. </p> <p> The User and / or Contributor may issue directives on the storage, deletion or communication of their Personal Data after their death Users and / or Contributors can exercise their rights by writing to the following address: <a href="mailto:signsatwork.support@orange.com"> signsatwork.support@orange.com </a> </p><h1><font color=\'blue\'>II: Dailymotion Policy</font></h1> <p> Use of the service is subject to the <a href=\"https://www.dailymotion.com/legal/privacy\"> Data Policy Dailymotion staff </a> available for consultation by following the hypertext link </p>' where id=34;

--
alter table comments CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
alter table comments MODIFY text TEXT CHARSET utf8mb4;