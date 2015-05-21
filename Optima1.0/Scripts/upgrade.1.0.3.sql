use Optima;

alter table project add retained_percentage numeric(16,13);
alter table project add advanced_payment_percentage numeric(16,13);

CREATE TABLE portfolio_extraPayment
(
extraPayment_id int(11) AUTO_INCREMENT PRIMARY KEY,
portfolio_id int(11),
extraPayment_amount decimal(19,3),
extraPayment_date date
);

ALTER TABLE portfolio_extraPayment ADD 
FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id) 
ON UPDATE CASCADE ON DELETE CASCADE;