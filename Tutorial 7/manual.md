# Packaging
-----

## Virtual Machines
Virtual Machines allow applications to run in a contained environment, that is separate from the host operating system. Thus this is useful for applications that do not require libraries from the host operating system, this option also allows for running a separate operating system inside the guest machine.

There are two types of virtual machines:

- Hypervisor: this is a middle layer between the host operating system and guest operating system it manages the virtual machine threads in the operating system and is responsible for mapping guest machine operations into the kernel of the host system. ***Not all virtual machines support all x86_64 instruction set, thus programs requiring special instructions such as SSE and AVX may not run inside the guest***

- Bare Metals: this mode does not require an operating system to function, the virtual machine manager comes with a dedicated kernel that interact with the hardware directly thus reducing the overhead of the Hypervisor.


### Vagrant
Vagrant is a tool to manage virtual machines for easier development environments that can be shareable. It requires that a software for virtualization to be present on the host OS, for instance:

- VirtualBox
- VMWare
- Parallels

It can additionally work using virtualization libraries, for instance on Linux and some Unix machines there are libraries such as `libvirt` and `lxc` (`lxc` is the containerization library on Linux described below). Windows offers `Hyper-V` as a virtualization library. Vagrant offers a central repository for downloading virtual images [here](https://app.vagrantup.com/boxes/search). The official docs are [here](https://www.vagrantup.com/docs/index.html).

#### Using Vagrant

Internally there needs to a file named `Vagrantfile` which is responsible for creating the virtual environment. A new vagrant file can be created using the following command:

> `vagrant init`

It may optionally take as a parameter the name of the base operating system that is to be emulated. For example:

> `vagrant init geerlingguy/ubuntu1604`

This will define a `Vagrantfile` with a minimal Ubuntu 16.04 image for development. The `Vagrantfile` contains additional commented options to extend the functionality of the virtual machine. For example to allow for shared files between the host OS and virtual machine, the following line needs to be uncommented:

> `config.vm.synced_folder "FOLDER ON HOST", "PATH ON VIRTUAL"`



To access the virtual machine from the terminal, you can SSH into it:

> `vagrant ssh`

This will log into the machine to allow for development.



##### Exporting

To export a virtual environment:

> `vagrant package`

This will export a *currently* running virtual machine, to specify the machine to run based on its name, the `--name` flag may be used.



##### Importing

To import a virtual environment:

> `vagrant add NAME PATH`

This will add the virtual machine using the `.box` file.

## Containers

Containers are operating system level virtualization, that depend on the operating system kernel.

### Docker

Docker is a software technology providing operating-system-level virtualization as containers. Docker provides an additional layer of abstraction and automation of operating-system-level virtualization on Windows and Linux. Docker uses the resource isolation features of the Linux kernel such as cgroups and kernel namespaces, and a union-capable file system such as OverlayFS and others to allow independent "containers" to run within a single Linux instance, avoiding the overhead of starting and maintaining virtual machines.

The Linux kernel's support for namespaces mostly isolates an application's view of the operating environment, including process trees, network, user IDs and mounted file systems, while the kernel's cgroups provide resource limiting, including the CPU, memory, block I/O, and network. Since version 0.9, Docker includes the libcontainer library as its own way to directly use virtualization facilities provided by the Linux kernel, in addition to using abstracted virtualization interfaces via libvirt, LXC (Linux Containers) and systemd-nspawn.

#### How Docker Works
A Docker image is built up from a series of layers. Each layer represents an instruction in the image’s Dockerfile. Each layer except the very last one is read-only. Consider the image built using the following commands:

```dockerfile
FROM ubuntu:16.04
COPY . /app
RUN make /app
CMD python /app/app.py
```

This `Dockerfile` contains four commands, each of which creates a layer. The `FROM` statement starts out by creating a layer from the ubuntu:16.04 image. The `COPY` command adds some files from your Docker client’s current directory. The `RUN` command builds your application using the make command. Finally, the last layer specifies what command to run within the container.

Each layer is only a set of differences from the layer before it. The layers are stacked on top of each other. When a new container is created, you add a new writable layer on top of the underlying layers. This layer is often called the **Container Layer**. All changes made to the running container, such as writing new files, modifying existing files, and deleting files, are written to this thin writable container layer. The diagram below shows a container based on the Ubuntu 15.04 image.

![Layers](layers.jpg)

The major difference between a container and an image is the top writable layer. All writes to the container that add new or modify existing data are stored in this writable layer. When the container is deleted, the writable layer is also deleted. The underlying image remains unchanged.

Because each container has its own writable container layer, and all changes are stored in this container layer, multiple containers can share access to the same underlying image and yet have their own data state. The diagram below shows multiple containers sharing the same Ubuntu 15.04 image.

![Sharing](sharing.jpg)

If multiple images need to have shared access to the exact same data, store this data in a Docker volume and mount it into the containers.

When a container is deleted, any data written to the container that is not stored in a data volume is deleted along with the container.

A data volume is a directory or file in the Docker host’s filesystem that is mounted directly into a container. Data volumes are not controlled by the storage driver. Reads and writes to data volumes bypass the storage driver and operate at native host speeds. You can mount any number of data volumes into a container. Multiple containers can also share one or more data volumes.

The diagram below shows a single Docker host running two containers. Each container exists inside of its own address space within the Docker host’s local storage area (/var/lib/docker/...). There is also a single shared data volume located at /data on the Docker host. This is mounted directly into both containers.

![Volumes](volumes.jpg)

##### Storage Driver
Ideally, very little data is written to a container’s writable layer, and you use Docker volumes to write data. However, some workloads require you to be able to write to the container’s writable layer. This is where storage drivers come in.

Docker supports several different storage drivers, using a pluggable architecture. The storage driver controls how images and containers are stored and managed on your Docker host. If multiple storage drivers are supported in your kernel, Docker has a prioritized list of which storage driver to use if no storage driver is explicitly configured, assuming that the prerequisites for that storage driver are met:
- If possible, the storage driver with the least amount of configuration is used, such as *BTRFS* or *ZFS*. Each of these relies on the backing filesystem being configured correctly.

- Otherwise, try to use the storage driver with the best overall performance and stability in the most usual scenarios:
    - *overlay2* is preferred, followed by *overlay.* Neither of these requires extra configuration, *overlay2* is the default choice for Docker CE

    - *devicemapper* is next, but requires *direct-lvm* for production environments, because *loopback-lvm* with zero-configuration, has very poor performance

By default docker uses *Copy On Write* strategy to manage its files. CoW creates a new copy of the file each time it is modified, filesystems BTRFS and ZFS are designed to use CoW by default.

##### Configuration
To configure Docker, you can follow this [link](https://wiki.archlinux.org/index.php/Docker#Configuration)

##### Building A Docker Image
To place your application in an image, a `Dockerfile` must be created, which describes how to build the image. A sample file is provided in the *alpine-docker* directory, to build the image:

> `docker build -t alpine-docker .`

This will build the image with the name *alpine-docker*. Alpine is minimal Linux distribution, other distributions exist and base images exist in the [library](https://hub.docker.com/u/library/). More information can be found [here](https://deis.com/blog/2015/creating-sharing-first-docker-image/) and [here](https://docs.docker.com/get-started/part2/).

------

# Deployment

## Packer

Packer is a tool that create virtual environments for production use, it can spawn multiple backends ranging from virtual machines and containers to Amazon AWS. The configuration file for packer is written in JSON format, in which there are three main entries:
| Entry     | Description     |
| :------------- | :------------- |
| Builders       | Builders are responsible for creating machines and generating images from them for various platforms |
| Provisioners | Provisioners use builtin and third-party software to install and configure the machine image after booting. Provisioners prepare the system for use, so common use cases for provisioners include: installing packages, creating users, etc.|
| Post-Processors  |Post-processors run after the image is built by the builder and provisioned by the provisioner(s). Post-processors are optional, and they can be used to upload artifacts, re-package, or more|

Two examples have been provided in the Packer folder for building virtualbox machines once using an ISO (`Ubuntu1704.json`) and another using an exported virtual machine (`Vbox-ovf.json`). The template file for the ISO version requires additional files which are found [here](https://github.com/kaorimatz/packer-templates), in addition to other prebuilt template files, the [docs](https://www.packer.io/docs/builders/index.html) contain base templates for other backends as well.

To build the image:

> `packer build <template.json>`


## Consul

[Consul](https://www.consul.io/intro/getting-started/install.html) is a tool for service discovery of applications. Applications register themselves into what is known as a `Datacenter` where inside each datacenter, the applications can discover other applications using HTTP or DNS interfaces. Additionally datacenters can discover other datacenters for added scalability, within each datacenter there must be at least one designated Consul server node and multiple client nodes. Consul also provides failure detection on services as well as a key-value storage for application storing configurations.


## Nomad

[Nomad](https://www.nomadproject.io/intro/getting-started/install.html) is a tool for managing a cluster of machines and spawning applications on them, it depends on Consul for operation. By specifying which datacenters each Nomad node operates under, it can spawn container images, runtimes such as JVM and virtual machines.

An intro to how to use it can be found [here](https://www.katacoda.com/hashicorp/scenarios/nomad-introduction).
