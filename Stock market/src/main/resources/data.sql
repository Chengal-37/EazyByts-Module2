-- IMPORTANT:
-- 1. Ensure your 'application.properties' (or 'application.yml') has:
--    spring.jpa.hibernate.ddl-auto=create
--    spring.sql.init.mode=always
-- 2. This will re-create your schema and insert this data EVERY TIME the application starts.
--    This is great for development/testing but NOT for production where you need data persistence.

-- Optional: DELETE/TRUNCATE existing data if running on a database that doesn't drop tables automatically (e.g., 'update' ddl-auto)
-- For MySQL/MariaDB:
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE portfolio_holdings;
-- TRUNCATE TABLE transactions;
-- TRUNCATE TABLE stocks;
-- TRUNCATE TABLE users;
-- SET FOREIGN_KEY_CHECKS = 1;

-- For PostgreSQL:
-- DELETE FROM portfolio_holdings;
-- DELETE FROM transactions;
-- DELETE FROM stocks;
-- DELETE FROM users;
-- Or TRUNCATE TABLE portfolio_holdings, transactions, stocks, users CASCADE; (Use with caution!)

-- 1. Insert Test Users
-- In a real application, passwords MUST be securely hashed (e.g., using BCrypt).
-- The 'password' value below is plain text (for 'password123') for demonstration.
-- You'd typically use a tool or code to generate a BCrypt hash, e.g., for "password123", it might be like:
-- $2a$10$w099e.bB/Zk0G1s1f0a.N.e.F.P.I.Q.E.R.S.T.U.V.W.X.Y.Z. (replace with an actual hash)
INSERT INTO users (id, username, password, email, account_balance, created_at) VALUES
(1, 'alice', '$2a$10$MtQnoZF9jIz1553MkXPGSusc.8aUnefjnveLcFRCn7yja5KkPT1RC', 'alice@example.com', 50000.00, NOW()),
(2, 'bob', '$2a$10$3iVmqwjHtDD4diwhOI0fMuNBwg6OHXjgJ0A6yr9IrEr8/SOMcW9BG', 'bob@example.com', 25000.00, NOW());

-- 2. Insert Test Stocks
-- Ensure column names match your database schema (e.g., symbol -> symbol, companyName -> company_name)
-- Prices are scaled to 4 decimal places as per our Stock entity update.
-- NOW() is a SQL function to get the current timestamp.
INSERT INTO stocks (id, symbol, company_name, current_price, previous_close, day_high, day_low, volume, last_updated) VALUES
(101, 'AAPL', 'Apple Inc.', 170.5000, 169.8000, 172.0000, 168.0000, 75000000.00, NOW()),
(102, 'MSFT', 'Microsoft Corp.', 430.2500, 428.5000, 432.0000, 425.0000, 60000000.00, NOW()),
(103, 'GOOGL', 'Alphabet Inc.', 180.7500, 180.0000, 181.5000, 179.0000, 25000000.00, NOW()),
(104, 'AMZN', 'Amazon.com Inc.', 190.1000, 189.5000, 191.0000, 188.0000, 50000000.00, NOW()),
(105, 'TSLA', 'Tesla Inc.', 600.0000, 595.0000, 610.0000, 590.0000, 40000000.00, NOW()),
(106, 'NVDA', 'NVIDIA Corp.', 900.0000, 890.0000, 910.0000, 885.0000, 30000000.00, NOW());

-- 3. Insert Test Portfolio Holdings (Optional: If you want pre-existing holdings for users)
-- Make sure 'user_id' and 'stock_id' correspond to IDs inserted above.
INSERT INTO portfolio_holdings (id, user_id, stock_id, shares, average_buy_price) VALUES
(1, 1, 101, 10, 165.0000), -- Alice owns 10 AAPL shares, bought at $165
(2, 2, 102, 5, 420.0000),  -- Bob owns 5 MSFT shares, bought at $420
(3, 1, 103, 20, 175.0000); -- Alice owns 20 GOOGL shares, bought at $175

-- 4. Insert Test Transactions (Optional: If you want pre-existing transaction history)
-- Make sure 'user_id' and 'stock_id' correspond to IDs inserted above.
-- 'type' should be 'BUY' or 'SELL' (matching your TransactionType enum string values)
-- 'timestamp' is handled by @PrePersist in the entity, but setting it explicitly here ensures consistent history
-- src/main/resources/data.sql
-- This script runs on application startup to insert initial data.
-- 'INSERT IGNORE' will prevent errors if data already exists (useful with ddl-auto=update)

-- Example: Insert a test stock if it doesn't exist
INSERT IGNORE INTO stocks (id, symbol, company_name, current_price, price_change, change_percent, volume, day_high, day_low, last_updated) VALUES
(1, 'IBM', 'International Business Machines Corp.', 180.50, 0.50, 0.28, 1000000, 182.00, 178.00, NOW());
-- Add other stocks like TSLA, AMZN if you want them available by default