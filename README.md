# VSDS TestBed Shacl Validator

> ⚠️ **Warning!**
>
> **This page has been archived. All documentation, source code, Docker images, and release information for the LDES software have been moved to the open-source project at [openldes.org](https://openldes.org).**
>
> **This page was originally created for the completed VSDS project, funded under the European Recovery and Resilience Facility (RRF) as part of NextGenerationEU and aligned with EU digital transformation strategies.**
>
> You can find the new source code at: https://github.com/orgs/OpenLDES/repositories 

<!-- TOC -->
* [VSDS TestBed Shacl Validator](#vsds-testbed-shacl-validator)
  * [Introduction](#introduction)
  * [ReplicationProcesssingService](#replicationprocesssingservice)
      * [startReplicating](#startreplicating)
      * [haltWhenReplicated](#haltwhenreplicated)
      * [destroyPipeline](#destroypipeline)
  * [ShaclValidationService](#shaclvalidationservice)
  * [How to run in Docker](#how-to-run-in-docker)
    * [Prerequisites](#prerequisites)
    * [Set up a TestBed instance yourself](#set-up-a-testbed-instance-yourself)
    * [Steps to use the TestBed Shacl Validator](#steps-to-use-the-testbed-shacl-validator)
      * [interact](#interact)
      * [call](#call)
  * [Building and running the project](#building-and-running-the-project)
    * [Live reload for development](#live-reload-for-development)
    * [Packaging using Docker](#packaging-using-docker)
<!-- TOC -->

## Introduction

This application implements the [GITB test service APIs](https://www.itb.ec.europa.eu/docs/services/latest/) in a  
[Spring Boot](https://spring.io/projects/spring-boot) web application that is meant to support
[GITB TDL test cases](https://www.itb.ec.europa.eu/docs/tdl/latest/) running in the Interoperability Test Bed.

## ReplicationProcesssingService

This service is responsible for creating an LDIO pipeline, checking if the LDES Client is still REPLACTING and
destroying the pipeline afterwards. It is available through the endpoint's
WSDL: http://localhost:8080/services/process?wsdl. This processing service has three operations:

#### startReplicating

This operation is responsible for creating the pipeline, which immediately starts the replication process.

Input parameters:

|   Name   | Description                     | Required |
|:--------:|---------------------------------|----------|
| ldes-url | the url of the LDES to validate | true     |

#### haltWhenReplicated

This operation is responsible for polling and returning the status of the LDES Client

#### destroyPipeline

This operation is resonsible for deleting the LDIO pipeline, as well the GraphDB repository

More information about processing services can be
found [here](https://www.itb.ec.europa.eu/docs/services/latest/processing/)

## ShaclValidationService

This service is responsible for executing the SHACL validation and is accessible through the endpoint's
WSDL: http://localhost:8080/services/validation?wsdl

Input parameters:

|    Name     | Description                                                         | Required |
|:-----------:|---------------------------------------------------------------------|----------|
| shacl-shape | the shacl shape that will be used to validate the server against to | true     |

This validation services requires in the validation call two parameters:

1. **ldes-url**: the url of the LDES to validate
2. **shacl-shape**: the shacl shape that will be used to validate the server against to

More information about validation services can be
found [here](https://www.itb.ec.europa.eu/docs/services/latest/validation/)

## How to run in Docker

### Prerequisites

* Docker
* A complete up and running TestBed instance
* An [LDI Orchestrator](https://informatievlaanderen.github.io/VSDS-Linked-Data-Interactions/)
* An [Ontotext GraphDB](https://www.ontotext.com/products/graphdb/) instance

All these services are set up in the [`./docker/docker-compose.yaml`](./docker/docker-compose.yaml) config file, but
here are dummy environment variables used that must be changed in a production environment

### Set up a TestBed instance yourself

If you need to set up a TestBed instance by yourself, there are some several steps you must do.
Go to [http://localhost:9000](http://localhost:9000) and log in. 

When logging in for the first time, the credentials can be found in the logs of the UI container through: 
``` bash
$ docker compose logs --since 24h | grep -C 4 "admin@itb"| tail -7
```
(be sure to narrow or extend the "24h" indicator to include the first moment of starting up the docker-compose stack)

When logged in, perform the following steps:

1. Create a _community_. (If you want to use the REST API, this is required, otherwise, this can be omitted)
2. Create a _domain_, which **can** be linked to the created community
3. Create in the domain a _specification_
4. The _test suite_ that will be created later on in this tutorial can be uploaded in this specification
5. Create an _organisation_
6. Create a _system_ in the created organisation
7. Create _statements_ in the system based on the testsuite uploaded into the specification
8. Now the _test session_ based on the statements can be run

How to exactly configure each part, can be
found [here](https://joinup.ec.europa.eu/collection/interoperability-test-bed-repository/solution/interoperability-test-bed/documentation) <br />
How TestBed work in general, can be
found [here](https://joinup.ec.europa.eu/collection/interoperability-test-bed-repository/solution/interoperability-test-bed/detailed-information)

### Steps to use the TestBed Shacl Validator

1. Add the TestBed Shacl Validator to `docker-compose.yaml` file

```yaml
  testbed-shacl-validator:
    image: ghcr.io/informatievlaanderen/testbed-shacl-validator:latest
    depends_on:
      - <GRAPH_DB_SERVICE_NAME>
      - <LDIO_WORKBENCH_SERVICE_NAME>
    environment:
      - LDIO_SPARQLHOST=http://<LDIO_WORKBENCH_SERVICE_NAME>:<INTERNAL_LDIO_CONTAINER_PORT>
      - LDIO_SPARQLHOST=http://<GRAPH_DB_SERVICE_NAME>:<INTERNAL_GRAPHDB_CONTAINER_PORT>
      - SERVER_PORT=8080
```

In this specific tutorial, this would result in the following config:

```yaml
  testbed-shacl-validator:
    image: ghcr.io/informatievlaanderen/testbed-shacl-validator:latest
    depends_on:
      - graphdb
      - ldio-workbench
    environment:
      - LDIO_HOST=http://ldio-workbench:8080
      - LDIO_SPARQLHOST=http://graphdb:7200
      - SERVER_PORT=8080
```

This service has already been added to the provided `docker-compose.yaml` file.

2. Start up all the services

```shell
docker compose up -d --wait
```

3. Add a test suite to TestBed that uses this service

In this step, a test suite will be written and can be written in many different ways. In this specific case, a test
suite will be written that contains only one test case. This test case will prompt for a Linked Data Event Stream url,
which will be validated against the provided SHACL shape.

First of all, make a folder that will contain all required files for the test suite:

```shell
mkdir validate_ldes_to_shacl_shape
cd validate_ldes_to_shacl_shape
```

Secondly, a scriptlet will be added to the test suite, which contains all the required logic to create the necessary
LDIO pipelines, check the LDES Client status and so on.

```shell
mkdir scriptlets
```

In this directory, place the [`validate-ldes.xml`](./docker/scriplets/validate-ldes.xml) file.

Now, the test cases can be written and added to the test suite. First, a new folder must be created.

```shell
mkdir test_cases
```

Now, `test_case.xml` file can be added to the newly created folder

```xml

<testcase id="ts1_tc1" xmlns="http://www.gitb.com/tdl/v1/" xmlns:gitb="http://www.gitb.com/core/v1/">
    <metadata>
        <gitb:name>[TC1] Validate LDES</gitb:name>
        <gitb:version>1.0</gitb:version>
        <gitb:description>Validate an LDES against a specific SHACL shape</gitb:description>
    </metadata>
    <actors>
        <gitb:actor id="LDESServer" role="SUT"/>
        <gitb:actor id="SHACLValidator"/>
    </actors>

    <steps stopOnError="true">
        <interact id="validationData" desc="Setup parameters to validate the LDES">
            <request desc="URL of the LDES to validate" name="ldesUrl"/>
            <request desc="Shacl shape that must be used for validating" name="shaclShape" inputType="UPLOAD"/>
            <request desc="Amount of seconds between each polling attempt" name="pollingInterval" inputType="TEXT"/>
        </interact>
        <assign to="delayDuration">concat($validationData{pollingInterval}, '000')</assign>
        <assign to="addresses{processing}">"http://testbed-shacl-validator:8080/services/process?wsdl"</assign>
        <assign to="addresses{validation}">"http://testbed-shacl-validator:8080/services/validation?wsdl"</assign>
        <call id="validateLdes" path="scriptlets/validate-ldes.xml">
            <input name="ldesUrl">$validationData{ldesUrl}</input>
            <input name="shaclShape">$validationData{shaclShape}</input>
            <input name="delayDuration">$delayDuration</input>
            <input name="addresses">$addresses</input>
        </call>
    </steps>

    <output>
        <success>
            <default>"Test session completed successfully."</default>
        </success>
        <failure>
            <default>
                "Test session failed. Please check the failed step report and the test session log for details."
            </default>
        </failure>
    </output>
</testcase>
```

Some interesting things we see in this test case, are the `interact` block, as well as the `verify` block.

#### interact

This block is responsible for prompting the user/tester to enter some data that will be used for the test. In this
specific case, it will be the URL of the LDES that must be validated, a SHACL shape file and a polling interval on which
the LDES Client status must be checked.

| Name of the parameter | Description                                                                                                                                                                                                                                                            |
|:---------------------:|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|        ldesUrl        | URL of the LDES to be validated                                                                                                                                                                                                                                        |
|      SHACL shape      | SHACL shape that will be used to validate the LDES                                                                                                                                                                                                                     |
|    pollingInterval    | Interval in which the LDES Client status will be checked in seconds. Keep in mind if the LDES to validate contains 1M members, it can take a very long time to replicate. Therefore, it can be better to configure a larger interval (e.g. an hour or even half a day) |

#### call

This block is responsible for executing the scriptlet, which is responsible for creating an LDIO pipeline, checking when
the LDES Client has finished with REPLICATING, performing the SHACL validation and destroying the pipeline afterward.

Another thing that can be noticed here, are the input parameters. These three parameters are required to let the test
run.

1. `ldesUrl`: the url of the LDES that must be validated. The value here is fetched from the prompted input
2. `shaclShape`: the SHACL shape that will be used to validate all the members against to. Here is the value also
   fetched from the prompted input
3. `delayDuration`: the amount of seconds on which the LDES Client must be checked

To finish up this step, a test suite itself must be added as well. This can be done by adding `test_suite.xml` to the
`validate_ldes_to_shacl_shape` folder.
This is how the file should look like:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite id="ts1" xmlns="http://www.gitb.com/tdl/v1/"
           xmlns:gitb="http://www.gitb.com/core/v1/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <!--
        This is the test suite's entry point file as it points to the test cases which in turn point to other scriptlets and resources. In general there is
        no requirement on the structure of a test suite but it is a good practice to keep things organised with a consistent structure that makes sense to you
        (e.g. a "test_cases" folder for the test cases, a "scriptlets" folder for scriptlets, "resources" for files to import etc.).
    -->
    <metadata>
        <gitb:name>LDES MUST be valid against the provided SHACL shape</gitb:name>
        <gitb:version>1.0</gitb:version>
        <gitb:description>Test suite to validate the LDES against a certain SHACL shape</gitb:description>
    </metadata>
    <actors>
        <!--
            The IDs of the actors need to match the ones you see on the UI. Note that when you deploy a test suite it will automatically create actors based on
            the below definitions if missing.
        -->
        <gitb:actor id="LDESServer">
            <gitb:name>LDES Server</gitb:name>
            <gitb:desc>The system acting as an LDES server that contains an event stream full of members</gitb:desc>
        </gitb:actor>
        <gitb:actor id="SHACLValidator">
            <gitb:name>Shacl Validator</gitb:name>
            <gitb:desc>The independent shacl validator service that will validate an LDES againt a SHACL shape
            </gitb:desc>
        </gitb:actor>
    </actors>

    <!-- 
        The test case IDs refer to the IDs of the relevant test cases (see their root element). You can place test cases anywhere you want in the test suite archive
        and they will be picked up automatically when parsing the test suite.
    -->
    <testcase id="ts1_tc1"/>
</testsuite>
```

Most important to notice here, is that de test case declared in the `test_cases` folder, must be referred from this file
by adding the `test case` tag with as attribute the id that has been assigned to the test case in its file.

4. Add the Test Suite to the TestBed instance

Create a ZIP file contains all the items that are in `validate_ldes_to_shacl_shape` folder. This can be uploaded to
TestBed now. This can be done either by the UI, or via the REST API. If you choose to do it via the REST API, the

> [!IMPORTANT]
> When creating the ZIP file, make sure the ZIP contains **the contents** of the `validate_ldes_to_shacl_shape` folder
> and **not the folder itself**.

```shell
curl -F updateSpecification=true -F specification=<SPECIFICATION_API_KEY> -F testSuite=@test_shacl_validator.zip --header "ITB_API_KEY: <COMMUNITY_API_KEY>" -X POST http://localhost:9000/api/rest/testsuite/deploy;
```

5. Run the test via the TestBed UI

## Building and running the project

1. Build using `mvn clean package`.
2. Once built you can run the application using `mvn spring-boot:run`.

### Live reload for development

This project uses Spring Boot's live reloading capabilities. When running the application from your IDE or through
Maven, any change in classpath resources is automatically detected to restart the application.

### Packaging using Docker

Running this application as a [Docker](https://www.docker.com/) container is very simple as described in Spring Boot's
[Docker documentation](https://spring.io/guides/gs/spring-boot-docker/). The first step is to
[Install Docker](https://docs.docker.com/install/) and ensure it is up and running. You can now build the Docker image
through Maven:

1. Build the JAR file with `mvn package`.
2. Build the Docker image with `docker build -f ./.github/Dockerfile .`.

