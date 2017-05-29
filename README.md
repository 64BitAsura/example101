[![circleci](https://circleci.com/gh/sambathl/example101/tree/master.svg?style=svg)](https://circleci.com/gh/sambathl/example101/tree/master)

# example101

checkout api as microservice in standalone spring boot using jersey, swagger and
docker

to run test

$ mvn clean test

to package as docker image, docker must available in machine

$ mvn clean package docker:build

checkout api microservice depend upon discount api microservice, but for testing
purpose it is mocked
