drop schema optima;
create schema optima;
use optima;

create  table location_info (
location_id INT NOT null AUTO_INCREMENT primary key, 
location_name varchar(50) not null, 
location_type varchar(10), 
parent_id INT, 
CONSTRAINT location_paret_id FOREIGN KEY (parent_id) REFERENCES location_info (location_id) ON DELETE SET NULL ON UPDATE CASCADE
);

Create Index location_name_idx on location_info(location_name);

create table client (
client_id INT NOT null AUTO_INCREMENT primary key, 
client_name varchar(100) not null, 
client_address_street varchar(100), 
client_address_city INT, 
client_address_province INT, 
client_address_country INT, 
client_address_postal_code varchar(50)
); 

create index client_name_idx on client(client_name);

ALTER TABLE client ADD 
FOREIGN KEY (client_address_city) REFERENCES location_info(location_id) 
ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE client ADD 
FOREIGN KEY (client_address_province) REFERENCES location_info(location_id) 
ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE client ADD 
FOREIGN KEY (client_address_country) REFERENCES location_info(location_id) 
ON UPDATE CASCADE ON DELETE SET NULL;

create table portfolio (
portfolio_id INT NOT null AUTO_INCREMENT primary key, 
portfolio_name varchar(32),
portfolio_descreption varchar(1024)
);
create index portofolio_name_idx on portfolio(portfolio_name);

insert into portfolio (portfolio_name , portfolio_descreption) values ('Default Portfolio' , 'Default Portfolio. All projects in the same portfolio are financed together.');

create table weekend_days (
weekend_days_id INT NOT null AUTO_INCREMENT primary key, 
weekend_days varchar(32)
);

insert into weekend_days (weekend_days) values ('SAT-SUN');
insert into weekend_days (weekend_days) values ('FRI-SAT');
insert into weekend_days (weekend_days) values ('THU-FRI');

commit;


create index weekend_days_idx on weekend_days(weekend_days);

create table project (
project_id INT NOT null AUTO_INCREMENT primary key, 
client_id INT, 
portfolio_id INT, 
project_name varchar(128) not null, 
project_code varchar(32) not null, 
project_description varchar(1024),
project_address_street varchar(100), 
project_address_city INT, 
project_address_province INT,
project_address_country INT, 
project_address_postal_code varchar(50), 
propused_start_date DATE, 
proposed_finish_date DATE, 
Weekend_days_id INT, 
daily_interest_rate numeric(9,3), 
overhead_per_day numeric(9,3)
);

create index project_name_idx on project(project_name);
create index project_code_idx on project(project_code);

ALTER TABLE project ADD  UNIQUE (project_code);

ALTER TABLE project ADD 
FOREIGN KEY (project_address_city) REFERENCES location_info(location_id) 
ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE project ADD 
FOREIGN KEY (project_address_province) REFERENCES location_info(location_id) 
ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE project ADD 
FOREIGN KEY (project_address_country) REFERENCES location_info(location_id) 
ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE project ADD 
FOREIGN KEY (client_id) REFERENCES client(client_id) 
ON UPDATE CASCADE ON DELETE SET NULL;


ALTER TABLE project ADD 
FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id) 
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE project ADD 
FOREIGN KEY (Weekend_days_id) REFERENCES weekend_days(weekend_days_id) 
ON UPDATE CASCADE ON DELETE SET NULL;


create table Days_off(
dayoff_id INT NOT null AUTO_INCREMENT primary key, 
project_id INT, 
day_off DATE, 
dayoff_type varchar(50)
);

ALTER TABLE Days_off ADD UNIQUE (project_id,day_off);

ALTER TABLE Days_off ADD 
FOREIGN KEY (project_id) REFERENCES project(project_id) 
ON UPDATE CASCADE ON DELETE CASCADE;






create table project_task (
task_id INT NOT null AUTO_INCREMENT primary key, 
project_id INT, 
task_name varchar(256),
task_description varchar(1024), 
duration int, 
calender_duration int,
uniform_daily_cost numeric(19,3),
uniform_daily_income numeric(19,3),
tentative_start_date DATE, 
calendar_start_date DATE,
scheduled_start_date DATE, 
actual_start_date DATE
);
create index tentative_start_date_idx on project_task(tentative_start_date);
create index scheduled_start_date_idx on project_task(scheduled_start_date);
create index actual_start_date_idx on project_task(actual_start_date);


ALTER TABLE project_task ADD 
FOREIGN KEY (project_id) REFERENCES project(project_id) 
ON UPDATE CASCADE ON DELETE CASCADE;


create table task_dependency (
dependency_id INT NOT null AUTO_INCREMENT primary key, 
dependant_task_id INT, 
dependency_task_id INT
);

ALTER TABLE task_dependency ADD 
FOREIGN KEY (dependant_task_id) REFERENCES project_task(task_id) 
ON UPDATE CASCADE ON DELETE CASCADE;


ALTER TABLE task_dependency ADD 
FOREIGN KEY (dependency_task_id) REFERENCES project_task(task_id) 
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE task_dependency ADD  UNIQUE (dependant_task_id,dependency_task_id);



create table payment_type(
payment_type_id INT NOT null AUTO_INCREMENT primary key, 
payment_type varchar(32)
);

insert into payment_type (payment_type) values ('Advance');
insert into payment_type (payment_type) values ('Intrim');

commit;

ALTER TABLE payment_type ADD  UNIQUE (payment_type);



create table project_payment(
payment_id INT NOT null AUTO_INCREMENT primary key, 
project_id INT, 
payment_type_id INT, 
payment_amount numeric(19,3),
payment_date date, 
payment_interim_number varchar(64)
);

create index project_payment_date_idx on project_payment(payment_date);

ALTER TABLE project_payment ADD 
FOREIGN KEY (project_id) REFERENCES project(project_id) 
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE project_payment ADD 
FOREIGN KEY (payment_type_id) REFERENCES payment_type(payment_type_id) 
ON UPDATE CASCADE ON DELETE SET NULL;

-- ALTER TABLE project_payment ADD  UNIQUE (project_id, payment_interim_number);



create table portfolio_finance(
finance_id INT NOT null AUTO_INCREMENT primary key, 
portfolio_id INT, 
finance_amount numeric(19,3), 
finance_untill_date date
);

create index finance_untill_date_idx on portfolio_finance(finance_untill_date);

ALTER TABLE portfolio_finance ADD 
FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id) 
ON UPDATE CASCADE ON DELETE CASCADE;


commit;

