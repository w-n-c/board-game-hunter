-- :name create-hunter! :! :n
-- :doc creates a new hunter (app user) record using the given login, name, and hashed password
INSERT INTO hunters
(login, name, password)
VALUES (:login, :name, :password)

-- :name get-hunter-for-auth* :? :1
-- :doc retrieves a hunter record given the login
SELECT * FROM hunters
WHERE login = :login

-- :name get-hunter* :? :1
-- :doc retrieves a hunter's available information and their hunts
SELECT hunters.name as hunter_name, prey.*
FROM hunters
INNER JOIN hunts
ON hunters.id = hunts.hunter_id
INNER JOIN prey
ON hunts.prey_id = prey.id
WHERE hunters.id = :id

-- :name delete-hunter! :! :n
-- :doc deletes a hunter record given the id
DELETE FROM hunters
WHERE id = :id

-- :name create-prey! :! :n
-- :doc creates a new prey (board game) record using the given id (bgg id), name, url, bgg-url and time of last track
INSERT INTO prey
(id, name, bgg_url, last_tracked)
VALUES(:id, :name, :bgg-url :last-tracked)

-- :name get-prey* :? :1
-- :doc retrieves a prey's bgg information
SELECT * FROM prey
WHERE id = :id

-- :name hunt-prey! :! :n
-- :doc creates a new hunt record using the given hunter and prey
INSERT INTO hunts
(hunter_id, prey_id)
VALUES(:hunter-id, :prey-id)
