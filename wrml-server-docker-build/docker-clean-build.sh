docker rm `docker ps -aq`
docker rmi -f wrml/wrml-server
./docker-build.sh