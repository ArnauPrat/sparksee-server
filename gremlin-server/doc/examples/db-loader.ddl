create gdb bibliographic into 'bibliographic.dex'

create node 'Person' (
	'name' string unique
)

create edge 'Friendships' from 'Person' to 'Person' materialize neighbors

LOAD NODES './rawdata/persons.csv'
locale ".utf8"
COLUMNS 'name'
INTO Person
FIELDS TERMINATED '|'
mode rows

LOAD EDGES './rawdata/friendships.csv'
COLUMNS src, target
INTO Friendships
ignore src, target
WHERE TAIL src = Person.name HEAD target = Person.name
FIELDS TERMINATED '|'
mode rows

