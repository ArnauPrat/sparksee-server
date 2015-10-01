create gdb cdranalysis into 'mydb.dex'
  
create node CUSTOMER (
    ID long unique,
    NAME string indexed
)

create edge 'CALLS' from 'CUSTOMER' to 'CUSTOMER'(TOTAL_CALLS_MADE int, TOTAL_MINUTES int, OFF_NET int) materialize neighbors

LOAD NODES '/home/root/sparksee/docker/sample/data/customers.csv'
locale ".utf8"
COLUMNS 'ID', 'NAME'
INTO CUSTOMER
FIELDS TERMINATED '|'
mode rows

load edges "/home/root/sparksee/docker/sample/data/calls.csv"
    columns headId, tailId, TOTAL_CALLS_MADE, TOTAL_MINUTES, OFF_NET
    into CALLS
    ignore headId, tailId
    where
        tail tailId=CUSTOMER.ID
        head headId=CUSTOMER.ID
    fields terminated '|' 
    mode rows

