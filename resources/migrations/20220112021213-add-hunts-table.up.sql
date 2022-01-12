CREATE TABLE hunts
(hunter_id UUID NOT NULL,
 prey_id INT NOT NULL,
 CONSTRAINT fk_hunter
  FOREIGN KEY (hunter_id)
    REFERENCES hunters(id)
    ON DELETE CASCADE,
 CONSTRAINT fk_prey
  FOREIGN KEY (prey_id)
    REFERENCES prey(id)
    ON DELETE CASCADE,
 PRIMARY KEY(hunter_id, prey_id)
);
