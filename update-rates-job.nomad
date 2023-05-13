job "rates-load-job" {
  datacenters = ["home"]
  type        = "batch"

  periodic {
    cron      = "10 19 * * * *"
    prohibit_overlap = true
  }

  group "rates-load-job-group" {
    count = 1
    task "rates-load-job-task" {
      driver = "docker"
      template {
        data = <<EOH
OBJECT_STORAGE_ENDPOINT="{{ key "expenses/object/storage/fs.s3a.endpoint" }}"
OBJECT_STORAGE_KEY="{{ key "expenses/object/storage/fs.s3a.access.key" }}"
OBJECT_STORAGE_SECRET="{{ key "expenses/object/storage/fs.s3a.secret.key" }}"
POSTGRES_JDBC_URL="{{ key "postgres.jdbc.url" }}"
POSTGRES_JDBC_DRIVER="{{ key "postgres.jdbc.driver" }}"
POSTGRES_JDBC_USER="{{ key "postgres.jdbc.user" }}"
POSTGRES_JDBC_PASSWORD="{{ key "postgres.jdbc.password" }}"
EOH
        destination = "secrets.env"
        env = true
      }

      config {
        network_mode = "host"
        privileged = true
        image = "10.8.0.5:5000/docker/rates-update-job:0.0.13"
        command = "bash"
        args = [
          "/app/run.sh",
        ]
      }

      resources {
        cpu    = 1000
        memory = 1000
      }
    }
  }
}
