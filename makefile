# Author: Reece Sharp

# Import config for recipes to make use of.
# Can be changed via: `make cnf="FILE_PATH" build`
cnf ?= .env
include $(cnf)
$(eval export $(shell sed -ne 's/ *#.*$$//; /./ s/=.*$$// p' $(cnf)))

# Get the absolute path of the env file and save it as a variable
# Reference: https://stackoverflow.com/questions/23843106/how-to-set-child-process-environment-variable-in-makefile
export ENV_FILE = $(shell pwd)/.env
export CHUNK_CONFIG_FILE = $(shell pwd)/chunk_servers.txt


# HELP
# This will output the help for each task
# Reference:
# https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
# https://gist.github.com/mpneuried/0594963ad38e68917ef189b4e6a269db
.PHONY: help
help: ## This help.
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help



# Docker Tasks
# Because make depends on the file modification time, and docker has its own internal system, we have to use a phony targets

# Because Docker doesn't support --env-file for 'docker build', we have to do this. Thanks Docker

.PHONY: image
image: ## Build the docker image, this same image is used for all different types of nodes
	docker build -f Dockerfile --tag $(DOCKER_IMAGE) $(for i in `cat $(ENV_FILE)`; do out+="--build-arg $i " ; done; echo $out;out="") .

.PHONY: network
network:
	docker network create --driver=bridge $(DOCKER_NETWORK_NAME) --subnet=$(DOCKER_NETWORK_SUBNET) --gateway=10.0.1.1

# There's some additional logic needed to run these containers, so it's in a separate script for those recipes
.PHONY: controller
controller: image network ## Run the controller, there will only ever be one of these
	./scripts/run.sh $(ENV_FILE) controller

.PHONY: chunk_holder
chunk_holder: image network ## Run the chunk_holder, there can be n number of these, must be ran after controller if done explicitly
	./scripts/run.sh $(ENV_FILE) chunk_holder

.PHONY: infra
infra: image network ## Run the controller and chunk_holder(s) as defined in chunk_servers.txt
	./scripts/run.sh $(ENV_FILE) infra

.PHONY: client
client: image network ## Run the client, there will can be n number of these
	./scripts/run.sh $(ENV_FILE) client

.PHONY: clean
clean: ## Remove all containers, images, and the network created by this project
	-docker container rm -f $(shell docker ps -a --filter name=shridoop- | grep -oE "shridoop-.*")
	-docker network rm -f $(DOCKER_NETWORK_NAME)
	-docker rmi -f shridoop