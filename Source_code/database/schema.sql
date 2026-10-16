-- Campus ICT Fault Reporting and Support System database schema.

CREATE DATABASE IF NOT EXISTS campus_ict_db;
USE campus_ict_db;

-- Students and staff who report faults.
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role ENUM('Student', 'Staff') NOT NULL DEFAULT 'Student'
);

-- ICT technicians who handle faults.
CREATE TABLE IF NOT EXISTS technicians (
    tech_id INT AUTO_INCREMENT PRIMARY KEY,
    tech_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL DEFAULT 'ICT Support',
    email VARCHAR(100) NOT NULL UNIQUE,
    status ENUM('Available', 'Busy', 'On Leave') NOT NULL DEFAULT 'Available'
);

-- Fault reports and their progress.
CREATE TABLE IF NOT EXISTS tickets (
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    reporter_id INT NOT NULL,
    category ENUM(
        'Wi-Fi / Network',
        'Hardware',
        'Software',
        'Lab Equipment',
        'Other'
    ) NOT NULL,
    location VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    urgency ENUM('Low', 'Medium', 'High', 'Critical') NOT NULL DEFAULT 'Medium',
    status ENUM('Pending', 'In Progress', 'Resolved') NOT NULL DEFAULT 'Pending',
    assigned_tech_id INT NULL,
    resolution_notes TEXT,
    resolved_at DATETIME NULL,
    date_reported TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT fk_ticket_reporter
        FOREIGN KEY (reporter_id)
        REFERENCES users(user_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_ticket_technician
        FOREIGN KEY (assigned_tech_id)
        REFERENCES technicians(tech_id)
        ON DELETE SET NULL,

    -- Indexes for common dashboard filters
    INDEX idx_tickets_status (status),
    INDEX idx_tickets_reporter (reporter_id),
    INDEX idx_tickets_tech (assigned_tech_id)
);

SHOW TABLES;