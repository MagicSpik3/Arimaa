Arimaa
======

CS 229 Project

How to run the starter code:
- Open Eclipse and import project into workspace
- Run ArimaaEngine.java using these VM arguments: -Xmx500000000
- Profit???

Migrating Data:
- Get zip from Google Drive
- Unzip
- Copy the following lines of code with ConvertData as the pwd:

<pre><code>
mysqladmin create Arimaa           # create database; may need to use 'sudo'
cat FastSQLLoad/*.sql | mysql -u root Arimaa   # create tables in database
mysqlimport --local -u root Arimaa FastSQLLoad/*.txt   # load data into tables; may need to specify full path
</code></pre>

Other items: 
- Use MySQL5.5
