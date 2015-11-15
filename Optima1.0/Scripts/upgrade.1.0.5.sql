use Optima;

ALTER TABLE project_task
ADD CONSTRAINT uc_project_task_name UNIQUE (project_id, task_name);

ALTER TABLE portfolio
ADD CONSTRAINT uc_portfolio_name UNIQUE (portfolio_name);
