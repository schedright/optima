drop schema optima;
create schema optima;
use optima;

create table portfolio (
portfolio_id INT NOT null AUTO_INCREMENT primary key, 
portfolio_name varchar(32),
portfolio_descreption varchar(1024)
);
create index portofolio_name_idx on portfolio(portfolio_name);

ALTER TABLE portfolio
ADD CONSTRAINT uc_portfolio_name UNIQUE (portfolio_name);

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
portfolio_id INT, 
project_name varchar(128) not null, 
project_code varchar(32) not null, 
project_description varchar(1024),
propused_start_date DATE, 
proposed_finish_date DATE, 
Weekend_days_id INT, 
overhead_per_day numeric(9,3),
retained_percentage numeric(16,13),
advanced_payment_percentage numeric(16,13),
collect_payment_period INT,              
payment_request_period INT,              
delay_penalty_amount numeric(19,3)      
);

create index project_name_idx on project(project_name);
create index project_code_idx on project(project_code);

ALTER TABLE project ADD  UNIQUE (project_code);


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
payment_interim_number varchar(64),
payment_initial_amount numeric(19,3)
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

create table settings (
settings_id INT NOT null AUTO_INCREMENT primary key, 
name varchar(50) not null, 
value varchar(10) not null, 
CONSTRAINT unique_name UNIQUE (name)
);

create table capicatl_plan_projects (
plan_id INT NOT null AUTO_INCREMENT primary key, 
project_id INT unique, 
CONSTRAINT FOREIGN KEY (project_id) REFERENCES project(project_id) ON DELETE CASCADE 
);

create table Payment (
Payment_id INT NOT null AUTO_INCREMENT primary key, 
project_id INT, 
payment_date DATE, 
payment_amount numeric(19,3),
UNIQUE KEY (project_id,payment_date),
CONSTRAINT FOREIGN KEY (project_id) REFERENCES project(project_id) on delete cascade
);

create table user (
 user_id INT NOT null AUTO_INCREMENT primary key, 
  user_name         varchar(50) not null unique,
  user_pass         varchar(50) not null
);

create table user_role (
  role_id INT NOT null AUTO_INCREMENT primary key, 
  user_name         varchar(50) not null,
  role_name         varchar(50) not null,
  FOREIGN KEY (user_name) REFERENCES user(user_name)
  ON UPDATE CASCADE ON DELETE CASCADE;

);

alter table portfolio add column solve_date timestamp;
alter table project add column solve_date timestamp;

alter table Project add column last_updated timestamp default now() on update now() ;
alter table project_task add column last_updated timestamp default now() on update now() ;
alter table portfolio_finance add column last_updated timestamp default now() on update now() ;
alter table days_off add column last_updated timestamp default now() on update now() ;


alter table payment add column last_updated timestamp default now() on update now() ;
alter table capicatl_plan_projects add column last_updated timestamp default now() on update now() ;

alter table portfolio_finance add column interest_rate numeric(16,13);
alter table portfolio_finance add column project_id INT;

ALTER TABLE portfolio_finance ADD 
FOREIGN KEY (project_id) REFERENCES project(project_id) 
ON UPDATE CASCADE ON DELETE CASCADE;

alter table project add column guid varchar(20);
alter table project_task add column guid varchar(20);

create table primavira_file (
file_id INT NOT null AUTO_INCREMENT primary key, 
file_name varchar(32),
file_contnet LONGTEXT
);

create table primavira_project {
primavira_project_id INT NOT null AUTO_INCREMENT primary key, 
file_name varchar(32),
primavira_projectfile_id INT,
project_quid guid varchar(20),

FOREIGN KEY (primavira_projectfile_id) REFERENCES primavira_file(file_id) 
}

commit;

