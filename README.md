# A9LH-Guide-File-Downloader
A utility to download files associated with the A9LH guide

## What does it do

Choose the page of the guide you are up to, and your firmware, console type & region, and click Download. Files will be downloaded & extracted to their correct directories. If this isn't possible automatically there will be a message telling the user what to look out for that they need to change. Magnet links will open in the default torrent client.

Database is kept updated by a script which goes through the whole guide and updates any relevant links. For pages that the user would normally have to visit and choose a file to download, it will find those files (that are the same extension as the previous version) and update the direct file links. Hopefully this can be triggered whenever there is a commit to the guide, or if not it will be done hourly.

There is also a script to return all the information by a json string for the program to use.

To-do
* ~~add path info to database and make default be root when inserting new row in script~~
* ~~add firmware and region info to database manually~~ (mostly done, any others will have to be found later)
* ~~add name (text in link) to database in script~~
* ~~add message section to display any warnings (e.g. file info not known, path info not known etc)~~
* ~~find out how to get the script to run anytime there is an update to the guide (instead of every hour etc)~~
* implement search feature (low priority)
* ~~have pages not display & files not added to list if timestamp is older than the others~~ (not tested)
* make status window be able to change size to fit progress bar + name, or, get progress bar to be underneath name label
* ~~make files be extracted to the path specified~~
* find out how to send myself a text message if there is a page with 'unknown' (so I need to add a filename to it)
* make a web page where I can enter in details and submit without having to open up CPanel etc
* have a button for users to submit bugs/errors from the program
* ~~make script not look for an updated file if the domains do not match~~
* add individual downloads for ropbin instead of user needing to visit the page (unlikely, too complicated)
* ~~add region, type, and firmware selector for program~~
* make a page for people to view the database
