1) Build the docker image: 

docker build -t sparksee-server .

2) Start the server:

docker run -e "MVN_USER=****" -e "MVN_PWD=*****" -e "SPARKSEE_LICENSE=******" -p 8182:8182 -h myhostname -i -t sparksee-server


The MVN_USER and MVN_PWD are your credentials for the Maven private repository for the CPaaS project. 

The SPARKSEE_LICENSE is the license that Sparsity gives for your database.

"myhostname" represents the name of your server machine that you will use to stablish connection. It can't be localhost.


To connect to the server with a shell there is an available command called sqlline to query data using the Sparksee algebra.

docker exec -i -t CONTAINER_ID sqlline

To load a sample of data execute:

docker exec -i -t CONTAINER_ID sparksee-script.sh /home/root/sparksee/docker/schema/db-cdr-schema.ddl