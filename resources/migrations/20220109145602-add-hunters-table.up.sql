CREATE TABLE hunters
(id UUID NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
 login VARCHAR(100) UNIQUE NOT NULL,
 name VARCHAR(100) NOT NULL,
 password VARCHAR(300) NOT NULL,
 last_hunted TIMESTAMP);
