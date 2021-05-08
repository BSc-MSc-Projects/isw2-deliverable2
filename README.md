# isw2-deliverable1

Compute a set of usefull metrics on a project, which can be used to train a ML model and preditc future bugs in further releases

The requirement is that the repository is versioned using git and is cloned from GitHub. The output is a .csv file with this structure:

Project Name	ClassName		Release		Metric I	Metric II		....	Buggyness

NAME		a/b/.../Name.java	x.y.z		Size (LOC)	Average LOC added	...	Yes/No

where buggyness means if that class had a bug in release x.y.z. To check wheater this was true or false, two methods are used:

	- Affected Versions from Jira ticket, to which a specific git commit refers
	- Proportion method, computed using an incremental method



