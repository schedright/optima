use Optima;

alter table project change daily_interest_rate interest_rate numeric(9,3);


alter table project MODIFY  interest_rate numeric(16,13);