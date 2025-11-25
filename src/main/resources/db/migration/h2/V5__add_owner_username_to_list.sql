alter table accounts drop column email;
alter table accounts drop column password;
alter table accounts add column email varchar(50) null;
alter table accounts add column login_id bigint not null default 0;
alter table accounts add column login_provider enum ('GITHUB', 'ROOT')
    NOT NULL DEFAULT 'ROOT'; -- Only default accounts will have this combo
-- Give ourselves the default user
insert into accounts (user_id, username, email, login_id, login_provider)
  values (0, 'Guest', 'guest@mut-ink.io', 0, 'ROOT');
alter table accounts add constraint uk_provider_accounts unique (login_provider, login_id);
alter table list add column user_id bigint not null default 0;
alter table list add constraint fk_list
    foreign key (user_id) references accounts(user_id);
