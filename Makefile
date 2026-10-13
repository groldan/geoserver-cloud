.PHONY: all
all: deps install test build-image ## Full build: GeoServer dependencies, artifacts, tests and local images

.PHONY: help
help: ## Show this help
	@awk 'BEGIN {FS = ":.*## "; printf "Targets:\n"} /^[a-zA-Z0-9_-]+:.*## / {printf "  \033[36m%-32s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# VERSION is the release version, e.g. 3.1.0-M1. It becomes the Maven revision
# and the image tag; pom.xml keeps its snapshot. The workflow sets it for
# every build: the tag name for a release, the pom version for a snapshot.
# Empty locally for a snapshot build, where TAG comes from the pom.
VERSION ?=
ifneq ($(VERSION),)
# override: make ignores a plain += when MVN_FLAGS comes from the command line.
override MVN_FLAGS += -Drevision=$(VERSION)
# override: an exported TAG must not tag the images differently from the jars.
override TAG = $(VERSION)
else
TAG ?= $(shell ./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
endif
MVNW = ./mvnw $(MVN_FLAGS)

# Optional. Empty: the release check finds the geoserver/geoserver-cloud remote
# by URL. A name given here must point at that repository.
GIT_REMOTE ?=

.PHONY: show-version
show-version: ## Print the project version, or VERSION when set
	@echo $(TAG)

COSIGN_PASSWORD := $(COSIGN_PASSWORD)


REPACKAGE ?= true

.PHONY: clean
clean: ## Remove build output (mvnw clean)
	$(MVNW) clean

# Build the customized GeoServer from the geoserver_submodule/geoserver
# submodule and install its artifacts into the local Maven repository.
# `make deps` builds the submodule as checked out, `make deps-sync` moves it to
# the head of the GeoServer branch this branch tracks.
# The deps-* targets call ./mvnw directly because the fork does not use the
# revision property passed by MVNW.
GEOSERVER_SUBMODULE = geoserver_submodule/geoserver
GEOSERVER_SUBMODULE_BRANCH = $(shell git config -f .gitmodules submodule.geoserver.branch)
# JGit for the git-commit-id plugin. Native mode runs git in the parent of the git
# directory, inside .git/modules for a submodule, and fails there. The gitNative
# profile recommended by the GeoServer pom turns native mode on.
GEOSERVER_BUILD_FLAGS = --batch-mode -DskipTests -ntp -fae -Dsort.skip=true -Dspotless.apply.skip=true -T1C -Dgit.commit.useNative=false
GEOSERVER_COMMUNITY_PROFILES = pending,cog,acl,flatgeobuf,geoparquet,graticule,gwc-azure-blob,gwc-gcs-blob,pmtiles-store,oseo,features-templating

.PHONY: deps
deps: deps-core deps-extensions deps-community ## Build the customized GeoServer from the submodule into the local repo

# Moves the submodule to the current head of the GeoServer branch declared in
# .gitmodules, which differs per release line, and leaves the resulting commit
# for the superproject to record. That branch is force-pushed upstream, hence
# fetching it by name at depth 1: the new head needs no ancestry with the old
# one, and no remote-tracking ref has to exist for it. Build the result with
# `make deps` and commit the submodule to pin it.
.PHONY: deps-sync
deps-sync: ## Move the submodule to the head of the GeoServer branch it tracks
	@test -e $(GEOSERVER_SUBMODULE)/.git || git submodule update --init $(GEOSERVER_SUBMODULE)
	git -C $(GEOSERVER_SUBMODULE) fetch --depth 1 origin $(GEOSERVER_SUBMODULE_BRANCH)
	git -C $(GEOSERVER_SUBMODULE) checkout --detach FETCH_HEAD
	@git status --short $(GEOSERVER_SUBMODULE)

.PHONY: deps-core
deps-core:
	./mvnw install -f $(GEOSERVER_SUBMODULE)/src/pom.xml $(GEOSERVER_BUILD_FLAGS)

.PHONY: deps-extensions
deps-extensions:
	./mvnw install -f $(GEOSERVER_SUBMODULE)/src/extension/pom.xml -Prelease $(GEOSERVER_BUILD_FLAGS)

.PHONY: deps-community
deps-community:
	./mvnw install -f $(GEOSERVER_SUBMODULE)/src/community/pom.xml -P$(GEOSERVER_COMMUNITY_PROFILES) $(GEOSERVER_BUILD_FLAGS)

.PHONY: build-tools
build-tools:
	$(MVNW) clean install -pl src/build-tools/build-config/ -ntp

.PHONY: lint
lint: build-tools ## Run static analysis (checkstyle, spotless, sortpom)
	$(MVNW) validate -Dqa -fae -ntp -T1C

.PHONY: lint-pom
lint-pom:
	$(MVNW) validate -Dqa -fae -Dspotless.skip=true -Dcheckstyle.skip=true -ntp -T1C

.PHONY: lint-java
lint-java: build-tools
	$(MVNW) validate -Dqa -fae -Dsortpom.skip=true -ntp -T1C

.PHONY: format
format: format-pom format-java ## Apply Java and POM formatting

.PHONY: format-pom
format-pom:
	$(MVNW) sortpom:sort -ntp -T1C

.PHONY: format-java
format-java:
	$(MVNW) spotless:apply -ntp -T1C

.PHONY: install
install: build-tools ## Compile and install all modules, skipping tests
	$(MVNW) clean install -DskipTests -ntp -U -T1C

.PHONY: package
package: ## Package all modules, skipping tests
	$(MVNW) clean package -Dfmt.skip -DskipTests -ntp -U -T1C

.PHONY: test
test: ## Run unit and integration tests
	$(MVNW) verify -Dfmt.skip -ntp -T1C

.PHONY: build-image
build-image: build-base-images build-image-infrastructure build-image-geoserver ## Build the Docker images for this platform

.PHONY: build-base-images
build-base-images: package-base-images
	TAG=$(TAG) docker compose -f docker-build/base-images.yml build jre
	TAG=$(TAG) docker compose -f docker-build/base-images.yml build spring-boot
	TAG=$(TAG) docker compose -f docker-build/base-images.yml build geoserver-common

.PHONY: build-image-infrastructure
build-image-infrastructure: package-infrastructure-images
	TAG=$(TAG) docker compose -f docker-build/infrastructure.yml build  $(filter-out $@ build-image build-image-multiplatform,$(MAKECMDGOALS))

# This uses $(MAKECMDGOALS) (all targets specified) and filters out the target itself ($@), passing the rest as arguments. The %: rule tells make to ignore any unrecognized "targets" (which are actually your service names).
# Then you can call:
#  make build-image-geoserver wcs wfs
.PHONY: build-image-geoserver
build-image-geoserver: package-geoserver-images
	TAG=$(TAG) docker compose -f docker-build/geoserver.yml build $(filter-out $@ build-image,$(MAKECMDGOALS))


.PHONY: package-base-images
package-base-images:
ifeq ($(REPACKAGE), true)
	$(MVNW) clean package -Dfmt.skip -DskipTests -T1C -ntp -am -pl src/apps/base-images/jre,src/apps/base-images/spring-boot,src/apps/base-images/geoserver
else
	@echo "Not re-packaging base images, assuming the target/*-bin.jar files exist"
endif

.PHONY: package-infrastructure-images
package-infrastructure-images:
ifeq ($(REPACKAGE), true)
	$(MVNW) clean package -Dfmt.skip -DskipTests -T1C -ntp -am -pl src/apps/infrastructure/config,src/apps/infrastructure/gateway-webmvc
else
	@echo "Not re-packaging infra images, assuming the target/*-bin.jar files exist"
endif

.PHONY: package-geoserver-images
package-geoserver-images:
ifeq ($(REPACKAGE), true)
	$(MVNW) clean package -Dfmt.skip -DskipTests -T1C -ntp -am -pl src/apps/geoserver/gwc,src/apps/geoserver/restconfig,src/apps/geoserver/wcs,src/apps/geoserver/webui,src/apps/geoserver/wfs,src/apps/geoserver/wms,src/apps/geoserver/wcs,src/apps/geoserver/wps
else
	@echo "Not re-packaging geoserver images, assuming the target/*-bin.jar files exist"
endif

# One service: APP=wms, wfs, wcs, wps, gwc, restconfig or webui, the folders
# under src/apps/geoserver.
.PHONY: package-geoserver-app
package-geoserver-app: ## Package one GeoServer service, APP=<service>
	@test -n "$(APP)" || { echo "APP is required, e.g. make package-geoserver-app APP=wms"; exit 2; }
	$(MVNW) clean package -Dfmt.skip -DskipTests -T1C -ntp -am -pl src/apps/geoserver/$(APP)

# The version is shell input, not recipe text: shell syntax in it must reach
# the script unchanged. Make exports command-line and environment variables.
.PHONY: release-check
release-check: ## Check that HEAD can be released as VERSION
	@test -n "$$VERSION" || { echo "VERSION is required, e.g. make release-check VERSION=3.1.0"; exit 2; }
	GIT_REMOTE="$$GIT_REMOTE" ci/release-check.sh "$$VERSION"

# Release build dry run on this machine: nothing is pushed or tagged. No tests:
# the pin pull request ran them. Sub-makes, because the image targets pass
# command line goals to docker compose as service names.
.PHONY: release-dry-run
release-dry-run: release-check ## Build the release images for VERSION locally; pushes nothing
	$(MAKE) deps
	$(MAKE) build-image
	@echo "Dry run of release $(VERSION) done: images tagged $(TAG) built, nothing pushed"

# The geoserver/geoserver-cloud remote, found by URL; GIT_REMOTE overrides it.
# Recursive: the script runs only when a recipe uses the variable.
RELEASE_REMOTE = $(shell GIT_REMOTE=$(GIT_REMOTE) ci/release-check.sh --remote)

# Put a signed tag on HEAD and push it. The push starts the release workflow.
# DRY_RUN=true: dry-run push, local tag removed again. Other values are refused,
# DRY_RUN=1 would push for real.
# The refusal is a recipe line: an exported DRY_RUN must not stop other targets.
# A failed real push keeps the tag: retry with git push <remote> <version>.
.PHONY: release-tag
release-tag: release-check ## Put a signed tag VERSION on HEAD and push it; DRY_RUN=true pushes nothing
ifneq ($(DRY_RUN),)
ifneq ($(DRY_RUN),true)
	$(error DRY_RUN must be true or unset, got '$(DRY_RUN)')
endif
endif
	git tag -s $(VERSION) -m "GeoServer Cloud $(VERSION)"
ifeq ($(DRY_RUN),true)
	git push --dry-run $(RELEASE_REMOTE) $(VERSION) || { git tag -d $(VERSION); exit 1; }
	git tag -d $(VERSION)
else
	git push $(RELEASE_REMOTE) $(VERSION)
endif

.PHONY: pull-images
pull-images: ## Pull the published images for the current version
	TAG=$(TAG) docker compose \
     -f docker-build/geoserver.yml \
     -f docker-build/infrastructure.yml \
     pull --quiet

.PHONY: sign-image
sign-image: ## Sign the release images with cosign
	@bash -c '\
	TAG=$(TAG); \
	images=$$(TAG=$$TAG docker compose -f docker-build/geoserver.yml -f docker-build/infrastructure.yml config --images); \
	for image in $$images; do \
	  echo "Signing $$image"; \
	  output=$$(cosign sign --yes --key env://COSIGN_KEY --recursive $$image 2>&1); \
	  if [ $$? -ne 0 ]; then \
	    echo "Error occurred: $$output"; \
	    exit 1; \
	  else \
	    echo "Signing successful: $$output"; \
	  fi; \
	done'

.PHONY: verify-image
verify-image: ## Verify the release image signatures with cosign
	@bash -c '\
	TAG=$(TAG); \
	images=$$(TAG=$$TAG docker compose -f docker-build/geoserver.yml -f docker-build/infrastructure.yml config --images); \
	for image in $$images; do \
	  echo "Verifying $$image"; \
	  output=$$(cosign verify --key env://COSIGN_PUB_KEY $$image 2>&1); \
	  if [ $$? -ne 0 ]; then \
	    echo "Error occurred: $$output"; \
	    exit 1; \
	  else \
	    echo "Verification successful: $$output"; \
	  fi; \
	done'

.PHONY: build-acceptance
build-acceptance:
	docker build --tag=geoservercloud/acceptance:latest acceptance_tests

.PHONY: acceptance-tests-datadir
acceptance-tests-datadir: build-acceptance start-acceptance-tests-datadir run-acceptance-tests-datadir ## Run the acceptance tests on the data directory backend

.PHONY: start-acceptance-tests-datadir
start-acceptance-tests-datadir:
	(cd compose/ && TAG=$(TAG) ./acceptance_datadir up -d)

.PHONY: run-acceptance-tests-datadir
run-acceptance-tests-datadir:
	(cd compose/ && TAG=$(TAG) ./acceptance_datadir run --rm -T acceptance bash -c 'until [ -f /tmp/healthcheck ]; do echo "Waiting for /tmp/healthcheck to be available..."; sleep 5; done && COLUMNS=120 pytest -v --color=yes --pyargs geoserver_acceptance_tests.tests  -k "not test_i18n_layers_default_locale"')

.PHONY: clean-acceptance-tests-datadir
clean-acceptance-tests-datadir: ## Tear down the data directory acceptance composition
	(cd compose/ && TAG=$(TAG) ./acceptance_datadir down -v --remove-orphans)

.PHONY: acceptance-tests-pgconfig
acceptance-tests-pgconfig: build-acceptance start-acceptance-tests-pgconfig run-acceptance-tests-pgconfig ## Run the acceptance tests on the pgconfig backend

.PHONY: start-acceptance-tests-pgconfig
start-acceptance-tests-pgconfig:
	(cd compose/ && TAG=$(TAG) ./acceptance_pgconfig up -d)

.PHONY: run-acceptance-tests-pgconfig
run-acceptance-tests-pgconfig:
	(cd compose/ && TAG=$(TAG) ./acceptance_pgconfig run --rm -T acceptance bash -c 'until [ -f /tmp/healthcheck ]; do echo "Waiting for /tmp/healthcheck to be available..."; sleep 5; done && COLUMNS=120 pytest -v --color=yes --pyargs geoserver_acceptance_tests.tests  -k "not test_i18n_layers_default_locale"')

.PHONY: clean-acceptance-tests-pgconfig
clean-acceptance-tests-pgconfig: ## Tear down the pgconfig acceptance composition
	(cd compose/ && TAG=$(TAG) ./acceptance_pgconfig down -v --remove-orphans)

# Prevent make from treating service names as targets when using $(MAKECMDGOALS) in build-image-geoserver
%:
	@:
