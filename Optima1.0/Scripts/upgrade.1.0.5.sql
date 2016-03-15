use Optima;

ALTER TABLE project_task
ADD CONSTRAINT uc_project_task_name UNIQUE (project_id, task_name);

ALTER TABLE portfolio
ADD CONSTRAINT uc_portfolio_name UNIQUE (portfolio_name);

create table settings (
settings_id INT NOT null AUTO_INCREMENT primary key, 
name varchar(50) not null, 
value varchar(10) not null, 
CONSTRAINT unique_name UNIQUE (name)
);

create table capicatl_plan_projects (
plan_id INT NOT null AUTO_INCREMENT primary key, 
project_id INT unique, 
CONSTRAINT FOREIGN KEY (project_id) REFERENCES project(project_id) 
);

alter table capicatl_plan_projects drop foreign key  capicatl_plan_projects_ibfk_1;

alter table capicatl_plan_projects 
add constraint  capicatl_plan_projects_ibfk_1 
FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) on delete cascade ;


create table Payment (
Payment_id INT NOT null AUTO_INCREMENT primary key, 
project_id INT, 
payment_date DATE, 
payment_amount numeric(19,3),
UNIQUE KEY (project_id,payment_date),
CONSTRAINT FOREIGN KEY (project_id) REFERENCES project(project_id) on delete cascade
);
