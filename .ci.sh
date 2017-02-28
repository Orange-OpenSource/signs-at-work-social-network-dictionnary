#!/bin/sh

TAG=`git describe`

(cd app && ./mvnw clean install) && \
cp app/target/sign*.jar signs-at-work-$TAG.jar && \
echo && \
echo OK && exit 0

echo [FAILED] && exit 1
