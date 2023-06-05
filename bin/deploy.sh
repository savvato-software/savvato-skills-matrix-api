#!/bin/bash

rm target -rf
mvn clean package

aws s3 cp ./target/dmpj-backend-app-0.0.1-SNAPSHOT.jar s3://savvato-builds-bucket/dmpj-backend-api.jar

echo "\nServer has been built and copied to the S3 deploy bucket.\n"

