create gdb bibliographic into 'bibliographic.dex'

create node 'Person' (
	'name' string unique
)

create edge 'Friendships' from 'Person' to 'Person' materialize neighbors

LOAD NODES '/home/root/sparksee/docker/sample/data/persons.csv'
locale ".utf8"
COLUMNS 'name'
INTO Person
FIELDS TERMINATED '|'
mode rows

LOAD EDGES '/home/root/sparksee/docker/sample/data/friendships.csv'
COLUMNS src, target
INTO Friendships
ignore src, target
WHERE TAIL src = Person.name HEAD target = Person.name
FIELDS TERMINATED '|'
mode rows

