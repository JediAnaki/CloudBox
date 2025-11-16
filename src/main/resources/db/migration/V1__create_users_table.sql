CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(64) UNIQUE NOT NULL ,
    email VARCHAR(128) NOT NULL ,
    enabled BOOLEAN default true NOT NULL ,
    account_non_expired BOOLEAN default true NOT NULL ,
    account_non_locked BOOLEAN default true NOT NULL ,
    credentials_non_expired BOOLEAN default true NOT NULL ,
    password VARCHAR(255) NOT NULL ,
    created_at TIMESTAMP default CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY ,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL ,
    role_id BIGINT NOT NULL ,
    PRIMARY KEY (user_id, role_id) ,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- доделать индексы для быстрого поиска.