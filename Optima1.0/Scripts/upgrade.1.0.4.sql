use Optima;

alter table project add advanced_payment_amount numeric(19,3);
alter table project add collect_payment_period INT;
alter table project add payment_request_period INT;
alter table project add delay_penalty_amount numeric(19,3);