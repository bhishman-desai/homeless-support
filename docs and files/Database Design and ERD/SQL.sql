create schema homeless_support;

use
    homeless_support;

/* DONOR TABLE */
CREATE TABLE donor
(
    donor_id  INT PRIMARY KEY AUTO_INCREMENT,
    name      VARCHAR(255),
    /* Rather than creating a separate table for location, we have decomposed the multivalued attribute into two distinct columns. The location coordinates will always consist of just two points, and this is not expected to change. */
    locationX INT,
    locationY INT,
    contact   VARCHAR(255)
);

/* FUNDING PROGRAM TABLE */
CREATE TABLE funding_program
(
    program_id INT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(255),
    donor_id   INT,
    FOREIGN KEY (donor_id) REFERENCES donor (donor_id) ON DELETE CASCADE ON UPDATE CASCADE,
    /* Each donor should be associated with a distinct funding program, ensuring uniqueness in their contributions. */
    UNIQUE (donor_id, name)
);

/* RECEIVE DONATION RECORD TABLE */
CREATE TABLE receive_donation_record
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    date       DATE,
    donation   INT,
    donor_id   INT,
    program_id INT,
    /* Upon the deletion or update of a donor or program, it is imperative to perform corresponding actions in our system by deleting or updating their donation records. */
    FOREIGN KEY (donor_id) REFERENCES donor (donor_id) ON DELETE CASCADE ON UPDATE CASCADE ,
    FOREIGN KEY (program_id) REFERENCES funding_program (program_id) ON DELETE CASCADE ON UPDATE CASCADE
);

/* SERVICE TABLE */
CREATE TABLE service
(
    service_id INT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(255),
    /* Double-checking that the frequency is greater than or equal to 0 ensures data consistency in our database. */
    frequency  INT CHECK (frequency >= 0)
);

/* SHELTER TABLE */
CREATE TABLE shelter
(
    shelter_id      INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255),
    locationX       INT,
    locationY       INT,
    /* Double-checking that the capacity is greater than 0 ensures data consistency in our database. */
    capacity        INT CHECK (capacity > 0),
    staff_in_charge VARCHAR(255)
);

/* SERVICE FOR SHELTER TABLE */
CREATE TABLE service_for_shelter
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    service_id INT,
    shelter_id INT,
    /* When a service or shelter undergoes deletion or modification in our database, we also aim to correspondingly delete or update their associations. */
    FOREIGN KEY (service_id) REFERENCES service (service_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (shelter_id) REFERENCES shelter (shelter_id) ON DELETE CASCADE ON UPDATE CASCADE,
    /* A shelter cannot be associated with the same service more than once. */
    UNIQUE (service_id, shelter_id)
);

/* STAFF TABLE */
CREATE TABLE staff
(
    staff_id     INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(255),
    is_volunteer BOOLEAN,
    manager_id   INT,
    /* When a manager is deleted or updated, it is imperative to perform the same action for associated staff members, as each staff member must have a manager. */
    FOREIGN KEY (manager_id) REFERENCES staff (staff_id) ON DELETE CASCADE ON UPDATE CASCADE
);

/* STAFF FOR SERVICE TABLE */
CREATE TABLE staff_for_service
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    staff_id   INT,
    service_id INT,
    /* When a staff or service undergoes deletion or modification in our database, we also aim to correspondingly delete or update their associations. */
    FOREIGN KEY (staff_id) REFERENCES staff (staff_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (service_id) REFERENCES service (service_id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (staff_id, service_id)
);

/* SHELTER OCCUPANCY RECORD TABLE */
CREATE TABLE shelter_occupancy_record
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    date       DATE,
    /* Double-checking if the occupancy is greater than or equals to 0 to ensure consistent data in database */
    occupancy  INT CHECK (occupancy >= 0),
    shelter_id INT,
    /* Upon the deletion or update of a shelter from our database, it is essential to ensure a corresponding operation for the occupancy record associated with that shelter. */
    FOREIGN KEY (shelter_id) REFERENCES shelter (shelter_id) ON DELETE CASCADE ON UPDATE CASCADE,
    /* Each shelter is required to have a singular occupancy record for a given date. */
    UNIQUE (shelter_id, date)
);

/* DISBURSE FUND RECORD TABLE */
CREATE TABLE disburse_fund_record
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    date       DATE,
    funds      INT,
    shelter_id INT,
    /* Upon the deletion or update of a shelter from our database, it is essential to ensure a corresponding operation for the disburse fund record associated with that shelter. */
    FOREIGN KEY (shelter_id) REFERENCES shelter (shelter_id) ON DELETE CASCADE ON UPDATE CASCADE
);

/* Inserting the manager to the system */
INSERT INTO staff (name, is_volunteer, manager_id)
VALUES ('Manager A', FALSE, NULL);

