CREATE TABLE IF NOT EXISTS jenkins (
    region VARCHAR(30) NOT NULL,
    order_num INT NOT NULL,
    title VARCHAR(50) NOT NULL,
    branch VARCHAR(30) NOT NULL,
    job_url VARCHAR(200) NULL,
    job_token VARCHAR(50) NOT NULL,

    PRIMARY KEY (region)
)
;

SET mode MySQL;

INSERT INTO jenkins
(region, order_num, title, branch, job_url, job_token)
VALUES
('{{KEY INFO}}', 1, '개발', '', '{{JOBNAME}}', 'DKSSUDGKTPDY')
ON DUPLICATE KEY UPDATE order_num = VALUES(order_num)
;