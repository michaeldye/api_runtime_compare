# api_runtime_compare

## Introduction

This project is intended to collect API runtime samples for comparison. It's aimed at investigating runtimes and libs that support HTTP-based APIs. The common API implemented in each runtime is a simple sequence generator (cf. <raml URL>). Each runtime should return a 400-level HTTP response code if it fails to implement a path in the spec. Each runtime binds to port `9009` and all tests are to be executed in-container. Only test results with no reported socket errors are allowed.

## Test Results

### Platforms

**heidegger**: 3.15.3-1-ARCH x86_64 GNU/Linux; Intel(R) Core(TM) i7-4770K CPU @ 3.50GHz; 16GB RAM; java version "1.8.0_05"

### Runtime Performance Results

##### netty on heidegger, 2014-07-09 (4GB heap)

As usual, I ran the tests on the same system as the running service for maximum convenience.

        mdye@heidegger:tests[10066]# ./test_count.bash
        Testing http://localhost:9009/api/count for correctness...
        Testing complete, starting benchmark
        Running 5m test @ http://localhost:9009/api/count/35
          3 threads and 500 connections
          Thread Stats   Avg      Stdev     Max   +/- Stdev
            Latency     6.25ms   31.05ms 242.23ms   97.84%
            Req/Sec   120.67k    54.68k  232.89k    65.46%
          98720311 requests in 5.00m, 16.09GB read
        Requests/sec: 329066.91
        Transfer/sec:     54.92MB

        mdye@heidegger:tests[10118]# ./test_fib.bash
        Testing http://localhost:9009/api/fib for correctness...
        Testing complete, starting benchmark
        Running 5m test @ http://localhost:9009/api/fib/
          2 threads and 400 connections
          Thread Stats   Avg      Stdev     Max   +/- Stdev
            Latency     2.54ms    5.40ms 141.97ms   91.57%
            Req/Sec    81.18k    11.24k  139.44k    73.79%
          45832291 requests in 5.00m, 21.32GB read
        Requests/sec: 152774.34
        Transfer/sec:     72.77MB

##### scala_spraycan on heidegger, 2014-07-09 (4GB heap)

        mdye@heidegger:tests[10088]# ./test_count.bash
        Testing http://localhost:9009/api/count for correctness...
        Testing complete, starting benchmark
        Running 5m test @ http://localhost:9009/api/count/35
          3 threads and 500 connections
          Thread Stats   Avg      Stdev     Max   +/- Stdev
            Latency    80.57ms  170.44ms 475.10ms   83.25%
            Req/Sec    33.88k    16.62k   56.09k    80.21%
          30189344 requests in 5.00m, 6.66GB read
        Requests/sec: 100631.06
        Transfer/sec:     22.74MB

        -bash-4.1# /work/tests/test_fib.bash
        Testing http://localhost:9009/api/fib for correctness...
        Testing complete, starting benchmark
        Running 5m test @ http://localhost:9009/api/fib/
          2 threads and 400 connections
          Thread Stats   Avg      Stdev     Max   +/- Stdev
            Latency    29.96ms   61.00ms 263.46ms   92.83%
            Req/Sec    14.93k     6.05k   26.92k    69.62%
          8869760 requests in 5.00m, 4.64GB read
        Requests/sec:  29565.78
        Transfer/sec:     15.83MB

##### clojure_httpkit on heidegger, 2014-07-10 (4GB heap)

        -bash-4.1# /work/tests/test_count.bash
        Testing http://localhost:9009/api/count for correctness...
        Testing complete, starting benchmark
        Running 5m test @ http://localhost:9009/api/count/35
          3 threads and 500 connections
          Thread Stats   Avg      Stdev     Max   +/- Stdev
            Latency     6.31ms    4.83ms 206.08ms   98.24%
            Req/Sec    28.43k     5.32k   83.56k    73.22%
          24172528 requests in 5.00m, 4.84GB read
        Requests/sec:  80574.80
        Transfer/sec:     16.52MB

        -bash-4.1# /work/tests/test_fib.bash
        Testing http://localhost:9009/api/fib for correctness...
        Testing complete, starting benchmark
        Running 5m test @ http://localhost:9009/api/fib/
          2 threads and 400 connections
          Thread Stats   Avg      Stdev     Max   +/- Stdev
            Latency     6.71ms    1.76ms  51.79ms   86.84%
            Req/Sec    30.93k     4.83k   61.00k    76.14%
          17814419 requests in 5.00m, 8.95GB read
        Requests/sec:  59381.34
        Transfer/sec:     30.55MB

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

#### One-time host system setup

Raise hard and soft file limits. In stock Arch linux, this'll do:

    sudo echo -e "\n*   hard  nofile    30000\n*   soft  nofile    30000" >> /etc/security/limits.conf

... and then reboot. You can check the limits on your box with `ulimit -Hn` and `ulimit -n`.

#### New container setup

Once inside container, start a runtime:

    /work/jvm/netty/start.bash

... and then start a test:

    /work/tests/test_count.bash

Note that JVM runtimes perform best after runtime optimization. To get the best results, run a test multiple times against the same runtime before recording them.

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
