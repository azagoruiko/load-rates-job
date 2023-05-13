FROM 10.8.0.5:5000/spark-s3:0.0.3

COPY bin/run.sh  /app/run.sh

ENV PATH="${PATH}:/opt/spark/bin:/opt/apache-hive-1.2.2-bin/bin"

WORKDIR /app
COPY target/rates-jar-with-dependencies.jar /app/rates.jar
