#!/usr/bin/env bash

git pull

git checkout d9dbdfe -- Dockerfile
git checkout d9dbdfe -- docker-compose.yml
git checkout d9dbdfe -- docker-entry.sh
sed '12s/$/ libglib2.0-0/' Dockerfile > Dockerfile
sed -i -e 's/202/212/g' Dockerfile