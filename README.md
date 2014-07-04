## Cautions

  **WARNING**: The docker container in this project contains a fantastic backdoor that allows unauthenticated root access to the container's filesystem via [rsyncd_backdoor_setup.bash](https://bitbucket.org/mdye/docker-container_setup/src/88fd82643996d0b711c524988f2a04a4c9273b48/centos/rsyncd_backdoor_setup.bash?at=master). This backdoor is used in this guide to customize SSH keys in-container. By default, the rsync daemon that creates this vulnerability is running but in order for it to be used the port `873` must be mapped out of the container with something like the Docker command `run`'s `-p` argument. If you don't map the port to the host system the backdoor can't be used outside of the system. If this mode still leaves you uncomfortable, remove the file `/etc/rsyncd.conf` and edit `/etc/supervisord.conf` in-container, commit it, and use your modified version.

### Docker installation

See http://docs.docker.io/en/latest/installation/.

### Container setup

Build container:

    docker build --rm -t mdye/runtime_comp:vanilla .

### Dev environment
#### Using the stock environment

Start the container (note this assumes the current working directory is the project's dir):

    docker run -d --name runtime_comp -p 3328:22 -v `pwd`:/work mdye/runtime_comp:vanilla

SSH into container (password is 'devo'):

    ssh -oPort=3328 root@localhost -oPubKeyAuthentication=no

#### Using a customized environment

If you've customized the container as described in the directions below, use the tag `mykey` to start the container:

    docker run -d --name runtime_comp -p 3328:22 -v `pwd`:/work mdye/runtime_comp:mykey

SSH into the container without a password:

    ssh -oPort=3328 root@localhost

#### Container customization

(optional) Given server ssh keys in ~/.ssh/servers/testo/, follow these steps to customize the container and re-tag it `mdye/runtime_comp:vanilla`.

0.  Set environment var for container:

        export CONTAINER="mdye/runtime_comp"

1.  If running, stop and remove running container:

        docker stop $( docker ps -a | grep $CONTAINER:vanilla | cut -d' ' -f1 ) && docker rm $_

2.  Start container with **huge**, gaping rsync security backdoor:

        docker run -d -p 4873:873 $CONTAINER:vanilla

3.  Push server SSH keys into container:

        rsync -vr ~/.ssh/servers/testo/* rsync://root@localhost:4873/root/etc/ssh/

4.  Push SSH pub key to root account:

        rsync -vL ~/.ssh/id_rsa.pub rsync://root@localhost:4873/root/root/.ssh/authorized_keys

5.   Commit customized container with tag `mykey`, stop and remove container with rsync backdoor's port exposed:

        CID=$( docker ps -a | grep $CONTAINER:vanilla | cut -d' ' -f1 ); docker commit $CID $CONTAINER:mykey; docker stop $CID; docker rm $CID

6.   (optional) Remove vanilla runtime_comp container:

        [ $CONTAINER ] && docker rmi $( docker images | tr -s ' ' | grep -e "$CONTAINER vanilla" | cut -d' ' -f3 )

7.   Unset envvar:

        unset CONTAINER

You can now start your customized container with the tag `mykey` using the command provided above.

### Project use

#### New container setup

Once inside container container, and in directory `/work`, execute:

    npm install

#### Common in-container project commands

(TODO: expand)

### Misc. docker commands

See all containers (running and not):

    docker ps -a

Stop and remove a container by ID:

    docker stop <running_container_id> && docker rm $_

See all images:

    docker images

Stop and remove an image:

    docker rmi <image_id>

For more information about docker commands, see: http://docs.docker.io/en/latest/reference/commandline/

### Notes
* I use supervisord to start the SSH service in the runtime_comp container when used as a dev environment. This can easily be added-to with other persistent services.
