#!/bin/bash

# Added || true - The true command's sole purpose is to return a successful exit status.

# Stopping the Docker container SpringbootApp if it running
docker stop SpringbootApp || true	

# Removing all the Docker images 
docker rmi -f $(docker images | grep 'lartex') || true 

# Removing the Docker container SpringbootApp
docker rm -f SpringbootApp || true  
