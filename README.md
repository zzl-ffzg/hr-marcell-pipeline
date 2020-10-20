# hr-marcell-pipeline

## Creating docker images

To create docker images on a host, build.sh script needs to be copied
and SSH access to the repository needs to be configured.

```
scp <user>@<server> build.sh
ssh <user>@<server>
cd
bash build.sh
```

## Running docker-compose

To run a container in a detached mode, run the following

```
ssh <user>@<server>
cd /root/hr-marcell-pipeline
docker-compose up -d all-in-one
```

This will keep the container running even after SSH connection is closed.