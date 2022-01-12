# board-game-hunter

generated using Luminus version "4.30"

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

You will need some version of PostgreSQL, w-n-c is currently testing against v13.4.

[1]: https://www.postgresql.org/

### Please enable the UUID extension if necessary:

1. Login to PostgreSQL interactive terminal as a [user with CREATE privilege on the current database](https://www.postgresql.org/docs/14/uuid-ossp.html).

`psql`*
**step may vary depending on how you access your admin account*

2. Switch to your board game hunter database

`\c board_game_hunter_dev`

3. Check to see if "uuid-ossp" is installed on your db server

`select * from pg_extension;`

4. If you do not see uuid-ossp in the 'extname' column of the table (or are too lazy to check):

`CREATE EXTENSION IF NOT EXISTS "uuid-ossp";`

5. You can confirm it is working by running one of the extension's procedures

`select * from uuid_generate_v4();`

### Create databases/users per environment and add credentials to [env]-config.edn
The `dev-config.edn` file should look something like this:

```
{:dev true
 ; port on which the application server will host the site
 :port 3000
 ; when :nrepl-port is set the application starts the nREPL server on load
 :nrepl-port 7000
 ; set your dev database connection URL here
 :database-url "postgresql://localhost/board_game_hunter_dev?user=numberfoursir&password=livelongandpostgre"}
```

Please change the username and password from the one commited to this public repo.

## Running

To start a web server for the application, run:

    lein run 

## License

Copyright © 2022 William Newell (w-n-c)
