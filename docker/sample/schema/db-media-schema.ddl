create gdb cdranalysis into 'mydb.dex'
  
create node 'Document' (
	'ID' string unique,
	'difTokens' long indexed
)

create node 'Entity' (
	'ID' string unique
)

create node 'Community' (
	'ID' string unique
)

create node 'Influence' (
	'ID' string unique
)

create edge 'PUBLISHES' from 'Entity' to 'Document' materialize neighbors
	
create edge 'COPIES' from 'Document' to 'Document' materialize neighbors

create edge 'REFERENCES' from 'Document' to 'Document' materialize neighbors

create edge 'PROPAGATES' from 'Document' to 'Document' (
	'WEIGHT' DOUBLE indexed
) materialize neighbors

create edge 'INFLUENCER' from 'Influence' to 'Entity' materialize neighbors

create edge 'INFLUENCED' from 'Influence' to 'Entity' materialize neighbors

create edge 'COMMUNITY-INFLUENCE' from 'Community' to 'Influence' materialize neighbors

create edge 'MEMBERS' from 'Community' to 'Entity' materialize neighbors

create node 'Term' (
	'name' string unique,
	'frequency' integer indexed default 0
)

create edge 'inferred-body' from 'Document' to 'Term' (
	'frequency' integer indexed
) materialize neighbors 
