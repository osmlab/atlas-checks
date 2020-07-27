# Development - Docker

Use the [Dockerfile](/Dockerfile) to build a simple Docker container for development. The base image is derived from
[docker-gradle](https://github.com/keeganwitt/docker-gradle), a simple image that contains 
both gradle (v6.4) and [adoptopenjdk11](https://github.com/AdoptOpenJDK/openjdk-docker).

## Installation and setup
Create docker image (in atlas-checks root).
```bash
docker build --rm --tag checks .
```

Create a docker container from the "checks" image. By default, the container exposes the gradle
wrapper (`gradlew`) script that invokes project's latest version of gradle. With this, you can
run the following commands: `run, build, buildCheck`
```bash
docker run [image name] [gradle command]
```


## Examples
Build the project (runs unit and integration tests).
```
docker run checks clean build
```

Run the default gradle run command. This will download a example pbf from geofabrik, execute the
atlas checks specified in the default configuration, and save the results to 
`/app/atlas-checks/build/example/flag`

```
docker run checks run
```

Run the gradle run command using your local directory as the source. This is useful for local
development -- as changes to to the source code will be reflected when executing the command. Also,
outputs will be stored on your local machine.
```bash
docker run --mount type=bind,src=/path/to/local/atlas-checks,dst=/app/atlas-checks checks run \
-Pchecks.local.countries=AIA \
-Pchecks.local.checkFilter=SpikyBuildingCheck
```
